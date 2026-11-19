package org.skriptlang.skript.variables.storage.flatfile;

import ch.njol.skript.Skript;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Version;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.storage.CsvFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

/**
 * The file a {@link FlatFileStorage} saves to, laid out as a header, the compacted rows ending with
 * a {@link CsvFormat#COMPACTED_END} line, and then the rows appended since.
 * <p>
 * This handles everything about the file itself: reading its header, reading parts of it, appending to it,
 * and replacing it with a rewritten one. What the rows mean is up to {@link Changes}.
 */
final class VariableFile {

	/**
	 * How many characters to build up before writing them out, when writing a whole file.
	 */
	private static final int WRITE_CHUNK = 1 << 16;

	private final File file;
	private final Path path;
	// the file, opened for appending. null while it's being replaced, and before it's first opened
	private @Nullable FileChannel appender;
	// what the system identifies the file opened for appending by, so we notice if it's replaced.
	// null if it isn't open, or the system doesn't identify files this way, like Windows
	private @Nullable Object appenderKey;

	/**
	 * @param file The file
	 */
	VariableFile(File file) {
		this.file = file;
		this.path = file.toPath();
	}

	/**
	 * @return The file's name, for messages
	 */
	String name() {
		return file.getName();
	}

	// ---------------------------- READING ----------------------------

	/**
	 * What loading needs to know about the file before reading its rows.
	 *
	 * @param compactedEnd Where the compacted rows end, or -1 if the file has no valid compacted rows
	 * @param size         The size of the file
	 * @param tornTail     Whether the last row was cut off by a crash
	 */
	record Layout(long compactedEnd, long size, boolean tornTail) {

		/**
		 * @return Whether the file has valid compacted rows
		 */
		boolean compacted() {
			return compactedEnd >= 0;
		}

		/**
		 * @return Where the changes start: the end of the compacted rows, or 0 if every row is a change
		 */
		long changesFrom() {
			return compacted() ? compactedEnd : 0;
		}

	}

	/**
	 * Reads the header and checks the file's version, then checks where the compacted rows end
	 * and whether the last row is complete.
	 *
	 * @return The file's layout
	 * @throws IOException If the file can't be read, or is from before Skript 2.1
	 */
	Layout readLayout() throws IOException {
		try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
			long size = channel.size();
			long compactedEnd = readHeader(channel);
			if (compactedEnd >= 0 && !isCompactedEnd(channel, compactedEnd, size))
				compactedEnd = -1;
			return new Layout(compactedEnd, size, !endsWithNewline(channel, size));
		}
	}

	/**
	 * Reads the comment lines at the start of the file, checks its version, and finds where its compacted rows end.
	 *
	 * @param channel The file, open for reading at its start. It's left open
	 * @return The offset from the {@link CsvFormat#COMPACTED_HEADER} line, or -1 if there's no valid one
	 * @throws IOException If the file can't be read, or is from before Skript 2.1
	 */
	private long readHeader(FileChannel channel) throws IOException {
		long compactedEnd = -1;
		boolean versionChecked = false;
		// not closed, since that would close the channel
		BufferedReader reader = new BufferedReader(new InputStreamReader(Channels.newInputStream(channel), CsvFormat.CHARSET));
		String line;
		int lineNumber = 0;
		while ((line = reader.readLine()) != null) {
			lineNumber++;
			line = line.trim();
			if (line.startsWith(CsvFormat.VERSION_HEADER) && !versionChecked) {
				versionChecked = true;
				checkVersion(line, lineNumber);
			} else if (line.startsWith(CsvFormat.COMPACTED_HEADER)) {
				compactedEnd = parseOffset(line.substring(CsvFormat.COMPACTED_HEADER.length()).trim());
			} else if (line.equals(CsvFormat.COMPACTED_END) || Row.isRow(line)) {
				break; // the header is over
			}
		}
		return compactedEnd;
	}

	/**
	 * Checks the version header, refusing files from before Skript 2.1, which stored values as text.
	 * An invalid version is only warned about.
	 *
	 * @param header     The version header line, trimmed
	 * @param lineNumber The header's line number, for messages
	 * @throws IOException If the file is from before Skript 2.1
	 */
	private void checkVersion(String header, int lineNumber) throws IOException {
		Version version;
		try {
			version = new Version(header.substring(CsvFormat.VERSION_HEADER.length()).trim());
		} catch (IllegalArgumentException e) {
			Skript.warning("Ignoring the invalid version header in line " + lineNumber + " of " + name());
			return;
		}
		if (version.isSmallerThan(new Version(2, 1)))
			throw new IOException(name() + " is from Skript " + version + ", and files from before 2.1 can't be read anymore");
	}

	/**
	 * Parses the offset from a {@link CsvFormat#COMPACTED_HEADER} line.
	 *
	 * @param digits What follows the header, trimmed
	 * @return The offset, or -1 if it isn't exactly {@link CsvFormat#OFFSET_DIGITS} digits
	 */
	private static long parseOffset(String digits) {
		if (digits.length() != CsvFormat.OFFSET_DIGITS)
			return -1;
		for (int i = 0; i < digits.length(); i++) {
			char c = digits.charAt(i);
			if (c < '0' || c > '9')
				return -1;
		}
		return Long.parseLong(digits);
	}

	/**
	 * Checks that the {@link CsvFormat#COMPACTED_END} line, on a line of its own, ends exactly at the offset.
	 * If it does, nothing before it changed since we wrote the file, so we can trust the compacted rows.
	 *
	 * @param channel The file, open for reading
	 * @param offset  The offset the header gives
	 * @param size    The size of the file
	 * @return Whether the compacted rows end at the offset
	 * @throws IOException If the file can't be read
	 */
	private static boolean isCompactedEnd(FileChannel channel, long offset, long size) throws IOException {
		byte[] expected = ("\n" + CsvFormat.COMPACTED_END + "\n").getBytes(CsvFormat.CHARSET);
		if (offset < expected.length || offset > size)
			return false;
		return Arrays.equals(readAt(channel, offset - expected.length, expected.length), expected);
	}

	/**
	 * Checks whether the file's last row is complete. Every row is written with its line break,
	 * so a file that doesn't end with one was cut off mid-write.
	 *
	 * @param channel The file, open for reading
	 * @param size    The size of the file
	 * @return Whether the file is empty or ends with a line break
	 * @throws IOException If the file can't be read
	 */
	private static boolean endsWithNewline(FileChannel channel, long size) throws IOException {
		if (size == 0)
			return true;
		byte[] last = readAt(channel, size - 1, 1);
		return last != null && last[0] == '\n';
	}

	/**
	 * Reads some bytes of the file, without moving the channel's position.
	 *
	 * @param channel The file, open for reading
	 * @param offset  Where the bytes start
	 * @param length  How many to read
	 * @return The bytes, or null if the file ends before them
	 * @throws IOException If the file can't be read
	 */
	private static byte @Nullable [] readAt(FileChannel channel, long offset, int length) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		while (buffer.hasRemaining()) {
			if (channel.read(buffer, offset + buffer.position()) < 0)
				return null;
		}
		return buffer.array();
	}

	/**
	 * Opens part of the file for reading as text. Malformed bytes are replaced rather than failing the whole read,
	 * since a cut-off last row can end mid-character.
	 *
	 * @param from The first byte
	 * @param to   The byte after the last one
	 * @return A reader for that part, which closes the file when it's closed
	 * @throws IOException If the file can't be opened
	 */
	BufferedReader read(long from, long to) throws IOException {
		FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
		try {
			channel.position(from);
			InputStream in = new LimitedInputStream(Channels.newInputStream(channel), to - from);
			return new BufferedReader(new InputStreamReader(in, CsvFormat.CHARSET), WRITE_CHUNK);
		} catch (IOException | RuntimeException e) {
			channel.close();
			throw e;
		}
	}

	/**
	 * Backs up the file before it's rewritten without the rows that couldn't be read.
	 *
	 * @throws IOException If it couldn't be backed up, in which case it must not be rewritten
	 */
	void backUp() throws IOException {
		try {
			File backup = FileUtils.backup(file);
			Skript.info("Created a backup of " + name() + " as " + backup.getName());
		} catch (IOException e) {
			throw new IOException("Could not back up " + name() + " before rewriting it: " + e.getMessage(), e);
		}
	}

	/**
	 * @return Whether the file exists
	 */
	boolean exists() {
		return Files.exists(path);
	}

	/**
	 * Checks whether the file is still as we left it: the same file, the same size, and its compacted rows still end
	 * where they did. That catches nearly anything else changing it, like an edit, a backup restored over it,
	 * or it being replaced or deleted, while only reading the end of its compacted rows.
	 *
	 * @param compactedEnd Where we left its compacted rows ending
	 * @param size         The size we left it at
	 * @param mayBeLonger  Whether a failed append may have left more at its end
	 * @return Whether it looks like nothing else changed it
	 * @throws IOException If the file can't be read
	 */
	boolean isAsWritten(long compactedEnd, long size, boolean mayBeLonger) throws IOException {
		BasicFileAttributes attributes;
		try {
			attributes = Files.readAttributes(path, BasicFileAttributes.class);
		} catch (NoSuchFileException e) {
			return false;
		}
		Object key = appenderKey;
		if (key != null && !key.equals(attributes.fileKey()))
			return false; // replaced, while we still have the old one open
		long actual = attributes.size();
		if (mayBeLonger ? actual < size : actual != size)
			return false;
		try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
			return isCompactedEnd(channel, compactedEnd, actual);
		}
	}

	// ---------------------------- APPENDING ----------------------------

	/**
	 * Opens the file for appending.
	 *
	 * @throws IOException If it can't be opened
	 */
	void openAppend() throws IOException {
		appender = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
		appenderKey = Files.readAttributes(path, BasicFileAttributes.class).fileKey();
	}

	/**
	 * @return Whether the file is open for appending
	 */
	boolean isOpen() {
		return appender != null;
	}

	/**
	 * Appends rows with one write, synced to disk.
	 *
	 * @param rows Rows in the file's format, each ending with a line break
	 * @return The size of the file after appending them
	 * @throws IOException If they couldn't be written. Some of them may have been
	 * @throws IllegalStateException If the file isn't open for appending
	 */
	long append(CharSequence rows) throws IOException {
		FileChannel appender = this.appender;
		if (appender == null)
			throw new IllegalStateException(name() + " isn't open");
		write(appender, rows);
		appender.force(false);
		return appender.size();
	}

	/**
	 * Syncs and closes the file, if it's open. It's unset first, so it's never closed twice.
	 *
	 * @throws IOException If it can't be synced or closed
	 */
	void closeAppend() throws IOException {
		try (FileChannel appender = takeAppender()) {
			if (appender != null)
				appender.force(false);
		}
	}

	/**
	 * Unsets the file opened for appending, so it's never closed twice.
	 *
	 * @return The file opened for appending, or null if it isn't open
	 */
	private @Nullable FileChannel takeAppender() {
		FileChannel appender = this.appender;
		this.appender = null;
		appenderKey = null;
		return appender;
	}

	// ---------------------------- REWRITING ----------------------------

	/**
	 * What a rewrite wrote.
	 *
	 * @param rows How many rows the new file holds
	 * @param size Its size, which is also where its compacted rows end
	 */
	record Written(long rows, long size) {
	}

	/**
	 * Replaces the file with one holding exactly the given rows, as compacted rows.
	 * <p>
	 * They're written to a temporary file first, which is synced and then moved over the file, so the old file
	 * stays complete if anything fails. The folder is synced after the move too, so the move survives a power cut,
	 * not just a crash. The file is closed for appending once it's replaced, and left closed.
	 *
	 * @param rows The rows, in the order of their names. They may read from the file, which is only replaced after
	 * @return What was written
	 * @throws IOException If reading or writing fails. The old file is only replaced once the new one is complete
	 */
	Written rewrite(Row.Source rows) throws IOException {
		Path temp = path.resolveSibling(path.getFileName() + ".temp");
		Writer writer;
		// emptied, in case a failed rewrite left it behind
		try (FileChannel out = FileChannel.open(temp, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			writer = new Writer(out, true);
			rows.forEach(writer::row);
			writer.finish();
			fillInCompactedEnd(out, writer.bytes);
			out.force(true);
		}
		discardAppend();
		try {
			Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
		}
		syncFolder();
		return new Written(writer.rows, writer.bytes);
	}

	/**
	 * Closes the file for appending before it's replaced, without syncing it and ignoring any failure:
	 * every append was synced already, and the new file holds everything the old one does.
	 * That way a broken channel, like one an append failed on, can't stop the rewrite that recovers from it.
	 */
	private void discardAppend() {
		FileChannel appender = takeAppender();
		if (appender == null)
			return;
		try {
			appender.close();
		} catch (IOException ignored) {
			// it's being replaced anyway
		}
	}

	/**
	 * Syncs the folder the file is in, so that the move which replaced the file is on disk too. Otherwise, after
	 * a power cut, some systems can bring back the old file, and everything appended since the move is lost.
	 * Some systems, like Windows, can't open a folder this way, in which case this does nothing.
	 */
	private void syncFolder() {
		Path folder = path.toAbsolutePath().getParent();
		if (folder == null)
			return;
		try (FileChannel channel = FileChannel.open(folder, StandardOpenOption.READ)) {
			channel.force(true);
		} catch (IOException ignored) {
			// not possible here. the file was still replaced
		}
	}

	/**
	 * Writes a backup in the same format as the file.
	 * A backup has no end of compacted rows, since we can't fill in its header afterwards, so it's read whole.
	 *
	 * @param out  Where to write the backup. It isn't closed
	 * @param rows The rows, in the order of their names
	 * @throws IOException If reading or writing fails
	 */
	static void writeBackup(OutputStream out, Row.Source rows) throws IOException {
		Writer writer = new Writer(Channels.newChannel(out), false); // closing that channel would close out
		rows.forEach(writer::row);
		writer.finish();
	}

	/**
	 * Fills in where the compacted rows end in the header, which was written with zeros for it.
	 *
	 * @param out The new file
	 * @param end Where its compacted rows end
	 * @throws IOException If it can't be written
	 */
	private static void fillInCompactedEnd(FileChannel out, long end) throws IOException {
		StringBuilder header = new StringBuilder();
		CsvFormat.appendHeader(header, true);
		int digitsAt = header.indexOf(CsvFormat.COMPACTED_HEADER) + CsvFormat.COMPACTED_HEADER.length() + 1;
		long position = header.substring(0, digitsAt).getBytes(CsvFormat.CHARSET).length;
		String digits = String.format("%0" + CsvFormat.OFFSET_DIGITS + "d", end);
		ByteBuffer buffer = ByteBuffer.wrap(digits.getBytes(CsvFormat.CHARSET));
		while (buffer.hasRemaining())
			out.write(buffer, position + buffer.position());
	}

	/**
	 * Writes a whole file in chunks, counting its rows and bytes as it goes.
	 */
	private static final class Writer {

		private final WritableByteChannel out;
		private final boolean compacted;
		private final StringBuilder chunk = new StringBuilder(WRITE_CHUNK + 256);
		// how many rows were written
		long rows;
		// how many bytes were written, so where the next byte goes
		long bytes;

		/**
		 * Creates a writer, starting the file with its header. Nothing is written until the first chunk fills up.
		 *
		 * @param out       Where to write
		 * @param compacted Whether the rows are followed by a {@link CsvFormat#COMPACTED_END} line,
		 *                  and the header says where that ends
		 */
		Writer(WritableByteChannel out, boolean compacted) {
			this.out = out;
			this.compacted = compacted;
			CsvFormat.appendHeader(chunk, compacted);
		}

		/**
		 * Writes a row.
		 *
		 * @param row The row
		 * @throws IOException If a full chunk can't be written
		 */
		void row(Row row) throws IOException {
			row.appendTo(chunk);
			rows++;
			if (chunk.length() >= WRITE_CHUNK) {
				bytes += write(out, chunk); // only whole rows, so no character is split
				chunk.setLength(0);
			}
		}

		/**
		 * Writes what's left, and the {@link CsvFormat#COMPACTED_END} line if there is one.
		 *
		 * @throws IOException If it can't be written
		 */
		void finish() throws IOException {
			if (compacted)
				CsvFormat.appendCompactedEnd(chunk);
			bytes += write(out, chunk);
			chunk.setLength(0);
		}

	}

	/**
	 * Writes text to a channel in {@link CsvFormat#CHARSET}, as a single write where the channel allows it.
	 *
	 * @param out  Where to write
	 * @param text The text
	 * @return How many bytes were written
	 * @throws IOException If it can't be written
	 */
	private static long write(WritableByteChannel out, CharSequence text) throws IOException {
		ByteBuffer bytes = CsvFormat.CHARSET.encode(CharBuffer.wrap(text));
		long written = bytes.remaining();
		while (bytes.hasRemaining())
			out.write(bytes);
		return written;
	}

	/**
	 * An input stream that stops after a number of bytes.
	 */
	private static final class LimitedInputStream extends FilterInputStream {

		// how many bytes are left to read
		private long left;

		/**
		 * @param in    The stream to read from
		 * @param limit How many bytes to read at most
		 */
		LimitedInputStream(InputStream in, long limit) {
			super(in);
			left = limit;
		}

		@Override
		public int read() throws IOException {
			if (left <= 0)
				return -1;
			int read = super.read();
			if (read >= 0)
				left--;
			return read;
		}

		@Override
		public int read(byte[] bytes, int offset, int length) throws IOException {
			if (left <= 0)
				return -1;
			int read = super.read(bytes, offset, (int) Math.min(length, left));
			if (read > 0)
				left -= read;
			return read;
		}

		@Override
		public long skip(long n) throws IOException {
			long skipped = super.skip(Math.min(n, left));
			left -= skipped;
			return skipped;
		}

		@Override
		public int available() throws IOException {
			return (int) Math.min(super.available(), left);
		}

	}

}
