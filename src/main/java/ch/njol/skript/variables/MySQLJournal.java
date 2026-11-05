package ch.njol.skript.variables;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

/**
	- Stores a copy of changes that still need to be saved to MySQL.

	- Before sending changes to MySQL, {@link PooledMySQLStorage} saves them here.
	The copy is removed only after the database transaction succeeds, so the changes
	can be tried again if the connection fails or something goes wrong after the commit.

	- The snapshot contains the changes, which database they belong to, and a checksum
	to make sure the data is not corrupted. The database identity also stops us from
	replaying the changes into the wrong database.

	- This is only for recovering changes that haven't been fully saved yet. It is not
	a database backup, migration tool, or another way of serializing values.

	- On startup, the snapshot is loaded and applied on top of the data read from MySQL.
	After that, the backend worker handles the journal. If MySQL is still unavailable
	when the server shuts down normally, the pending changes are kept for later.

	- The journal is not thread-safe. Changes that only existed in memory when the
	server suddenly crashed cannot be recovered.
*/

final class MySQLJournal {

	private static final int MAGIC = 0x534B4D31;
	private final Path path;
	private final String target;

	MySQLJournal(Path path, String target) {
		this.path = path;
		this.target = target;
	}

	Map<String, SerializedVariable> read() throws IOException {
		Map<String, SerializedVariable> pending = new LinkedHashMap<>();
		if (!Files.exists(path))
			return pending;
		CRC32 checksum = new CRC32();
		try (DataInputStream in = new DataInputStream(
				new CheckedInputStream(new BufferedInputStream(Files.newInputStream(path)), checksum))) {
			if (in.readInt() != MAGIC)
				throw new IOException("Unknown MySQL recovery format");
			String savedTarget = readString(in);
			int count = in.readInt();
			if (count < 0)
				throw new IOException("Invalid MySQL recovery record count");
			if (count > 0 && !target.equals(savedTarget))
				throw new IOException("MySQL recovery file belongs to a different database");
			for (int i = 0; i < count; i++) {
				String name = readString(in);
				SerializedVariable.Value value = null;
				if (in.readBoolean())
					value = new SerializedVariable.Value(readString(in), readBytes(in));
				pending.put(name, new SerializedVariable(name, value));
			}
			long actualChecksum = checksum.getValue();
			if (in.readLong() != actualChecksum)
				throw new IOException("MySQL recovery checksum mismatch");
			if (in.read() != -1)
				throw new IOException("Trailing data in MySQL recovery file");
		}
		return pending;
	}

	void write(Map<String, SerializedVariable> pending) throws IOException {
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		CRC32 checksum = new CRC32();
		try (FileOutputStream file = new FileOutputStream(temporary.toFile());
				DataOutputStream out = new DataOutputStream(
						new CheckedOutputStream(new BufferedOutputStream(file), checksum))) {
			out.writeInt(MAGIC);
			writeString(out, target);
			out.writeInt(pending.size());
			for (SerializedVariable variable : pending.values()) {
				writeString(out, variable.name);
				out.writeBoolean(variable.value != null);
				if (variable.value != null) {
					writeString(out, variable.value.type);
					out.writeInt(variable.value.data.length);
					out.write(variable.value.data);
				}
			}
			out.writeLong(checksum.getValue());
			out.flush();
			file.getFD().sync();
		}
		// Do not replace the last valid snapshot on filesystems without atomic rename.
		Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		try (FileChannel directory = FileChannel.open(path.toAbsolutePath().getParent(), StandardOpenOption.READ)) {
			directory.force(true);
		} catch (IOException | UnsupportedOperationException ignored) {
			// Directory fsync is not supported on all platforms.
		}
	}

	private static void writeString(DataOutputStream out, String value) throws IOException {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	private static String readString(DataInputStream in) throws IOException {
		return new String(readBytes(in), StandardCharsets.UTF_8);
	}

	private static byte[] readBytes(DataInputStream in) throws IOException {
		int length = in.readInt();
		if (length < 0)
			throw new IOException("Invalid MySQL recovery value length");
		// Stream instead of allocating an untrusted length before checking for truncation.
		byte[] bytes = in.readNBytes(length);
		if (bytes.length != length)
			throw new EOFException("Truncated MySQL recovery value");
		return bytes;
	}
}
