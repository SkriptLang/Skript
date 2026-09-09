package ch.njol.skript.variables;

import ch.njol.skript.registrations.Classes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * Read-only compatibility for the two previously shipped MySQL stream envelopes.
 * New writes use {@link Classes#serialize(Object)} unchanged. This reader knows only
 * the old framing and checksum; the shared Yggdrasil registry resolves every value.
 * Removing it would invalidate existing database rows and recovery journals.
 */
final class LegacyMySQLValueReader {

	private static final byte[] ENVELOPE = "SKMYSQL2".getBytes(StandardCharsets.US_ASCII);

	private LegacyMySQLValueReader() {}

	/** Called on the server thread, since registered deserializers may need Bukkit state. */
	static Object decode(SerializedVariable.Value value) throws IOException {
		byte[] data = value.data;
		boolean envelope = data.length >= ENVELOPE.length
				&& Arrays.equals(ENVELOPE, 0, ENVELOPE.length, data, 0, ENVELOPE.length);
		int offset = envelope ? ENVELOPE.length : 0;
		if (!envelope && !"mysql:yggdrasil:1".equals(value.type)) {
			var info = Classes.getClassInfoNoError(value.type);
			if (info == null || info.getSerializer() == null)
				throw new StreamCorruptedException("Unknown persisted type: " + value.type);
			Object decoded = Classes.deserialize(info, data);
			if (decoded == null)
				throw new StreamCorruptedException("Unreadable value: " + value.type);
			return decoded;
		}
		if (data.length - offset < 8)
			throw new StreamCorruptedException("Truncated MySQL value");
		int size = data.length - offset - 8;
		CRC32 checksum = new CRC32();
		checksum.update(data, offset, size);
		if (checksum.getValue() != ByteBuffer.wrap(data, offset + size, 8).getLong())
			throw new StreamCorruptedException("MySQL value checksum mismatch");
		ByteArrayInputStream bytes = new ByteArrayInputStream(data, offset, size);
		try (var in = Variables.yggdrasil.newInputStream(bytes)) {
			Object decoded = in.readObject();
			if (decoded == null || bytes.available() != 0)
				throw new StreamCorruptedException("Null or trailing persisted data");
			return decoded;
		}
	}
}
