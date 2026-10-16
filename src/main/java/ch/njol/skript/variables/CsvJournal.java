package ch.njol.skript.variables;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.CRC32;

final class CsvJournal {
	static final String CHECKPOINT = "# journal sequence: ";
	private final Path path;

	record Entry(long sequence, SerializedVariable variable) {}

	CsvJournal(Path path) {
		this.path = path;
	}

	private static String encode(Entry entry) {
		StringWriter text = new StringWriter();
		try (PrintWriter writer = new PrintWriter(text)) {
			var value = entry.variable().value();
			FlatFileStorage.writeCSV(writer, entry.variable().name(),
				value == null ? "null" : value.type(), value == null ? "" : FlatFileStorage.encode(value.data()));
		}
		String payload = entry.sequence() + "\t" + Base64.getEncoder().encodeToString(
			text.toString().getBytes(StandardCharsets.UTF_8));
		CRC32 crc = new CRC32();
		crc.update(payload.getBytes(StandardCharsets.UTF_8));
		return payload + "\t" + Long.toHexString(crc.getValue()) + "\n";
	}

	void append(Entry entry) throws IOException {
		// Opening and closing each append flushes Java buffers; this is not a disk fsync.
		Files.writeString(path, encode(entry), StandardCharsets.UTF_8,
			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	List<Entry> read() throws IOException {
		List<Entry> entries = new ArrayList<>();
		if (!Files.exists(path))
			return entries;
		String text = Files.readString(path, StandardCharsets.UTF_8);
		int end = text.lastIndexOf('\n') + 1;
		long previous = 0;
		for (String line : text.substring(0, end).split("\n")) {
			if (line.isEmpty())
				continue;
			try {
				String[] parts = line.split("\t", -1);
				if (parts.length != 3)
					throw new IllegalArgumentException("Invalid journal record");
				CRC32 crc = new CRC32();
				crc.update((parts[0] + "\t" + parts[1]).getBytes(StandardCharsets.UTF_8));
				if (crc.getValue() != Long.parseUnsignedLong(parts[2], 16))
					throw new IllegalArgumentException("Journal checksum mismatch");
				long sequence = Long.parseLong(parts[0]);
				if (sequence <= previous)
					throw new IllegalArgumentException("Journal sequence out of order");
				String[] row = FlatFileStorage.splitCSV(new String(Base64.getDecoder().decode(parts[1]),
					StandardCharsets.UTF_8).stripTrailing());
				if (row == null || row.length != 3)
					throw new IllegalArgumentException("Invalid journal CSV");
				entries.add(new Entry(sequence, row[1].equals("null")
					? new SerializedVariable(row[0], null)
					: new SerializedVariable(row[0], row[1], FlatFileStorage.decode(row[2]))));
				previous = sequence;
			} catch (IllegalArgumentException exception) {
				throw new IOException("Invalid variable journal " + path, exception);
			}
		}
		if (end != text.length()) {
			// Preserve the incomplete crash for inspection before removing it.
			Files.copy(path, path.resolveSibling(path.getFileName() + ".incomplete"), StandardCopyOption.REPLACE_EXISTING);
			replace(entries);
		}
		return entries;
	}

	void reset() throws IOException {
		replace(List.of());
	}

	void compact(long checkpoint) throws IOException {
		replace(read().stream().filter(entry -> entry.sequence() > checkpoint).toList());
	}

	private void replace(List<Entry> entries) throws IOException {
		Path temporary = path.resolveSibling(path.getFileName() + ".temp");
		try (var writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
			for (Entry entry : entries)
				writer.write(encode(entry));
		}
		Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
	}
}
