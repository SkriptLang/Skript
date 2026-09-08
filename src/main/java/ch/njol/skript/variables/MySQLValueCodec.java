package ch.njol.skript.variables;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.Yggdrasil;
import ch.njol.yggdrasil.YggdrasilSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** MySQL-only extensions to the registered Skript serializers. Never uses Java serialization. */
final class MySQLValueCodec {

	static final String FORMAT = "mysql:yggdrasil:1"; // Existing on-disk/journal format.
	private static final byte[] ENVELOPE = "SKMYSQL2".getBytes(StandardCharsets.US_ASCII);
	private final Yggdrasil yggdrasil;

	MySQLValueCodec(Yggdrasil registered) {
		yggdrasil = registered.fork();
		yggdrasil.registerClassResolver(new MySQLInventorySerializer());
		yggdrasil.registerSingleClass(SkriptColor.class, "SkriptColor");
		yggdrasil.registerClassResolver(new YggdrasilSerializer<ColorRGB>() {
			@Override
			public Class<ColorRGB> getClass(String id) {
				return "mysql:rgb-color:1".equals(id) ? ColorRGB.class : null;
			}

			@Override
			public String getID(Class<?> type) {
				return type == ColorRGB.class ? "mysql:rgb-color:1" : null;
			}

			@Override
			public Fields serialize(ColorRGB color) {
				Fields fields = new Fields();
				fields.putPrimitive("argb", color.asARGB());
				return fields;
			}

			@Override
			public boolean canBeInstantiated(Class<? extends ColorRGB> type) {
				return false;
			}

			@Override
			public <E extends ColorRGB> E newInstance(Class<E> type) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void deserialize(ColorRGB object, Fields fields) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <E extends ColorRGB> E deserialize(Class<E> type, Fields fields) throws StreamCorruptedException {
				int argb = fields.getPrimitive("argb", int.class);
				return type.cast(ColorRGB.fromBukkitColor(org.bukkit.Color.fromARGB(argb)));
			}
		});
	}

	SerializedVariable.Value encode(Object value) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (var out = yggdrasil.newOutputStream(bytes)) {
			out.writeObject(normalize(value, new IdentityHashMap<>(), 0));
		}
		byte[] payload = bytes.toByteArray();
		CRC32 checksum = new CRC32();
		checksum.update(payload);
		try (DataOutputStream footer = new DataOutputStream(bytes)) {
			footer.writeLong(checksum.getValue());
		}
		byte[] encoded = bytes.toByteArray();
		ByteArrayOutputStream envelope = new ByteArrayOutputStream();
		envelope.write(ENVELOPE);
		envelope.write(encoded);
		return new SerializedVariable.Value(typeName(value), envelope.toByteArray());
	}

	String typeName(Object value) throws IOException {
		if (value instanceof ch.njol.skript.util.Color || value instanceof org.bukkit.Color)
			return "color";
		if (value instanceof org.bukkit.inventory.Inventory)
			return "inventory";
		if (value instanceof List<?>)
			return "list";
		if (value instanceof Map<?, ?>)
			return "map";
		if (value.getClass().isArray())
			return "array";
		// The actual Skript registry is authoritative once registration has completed.
		if (Classes.getClassInfoNoError("object") != null) {
			ClassInfo<?> info = Classes.getSuperClassInfo(value.getClass());
			if (info.getC() != Object.class)
				return info.getCodeName();
		}
		if (value instanceof String)
			return "string";
		if (value instanceof Number || value instanceof Boolean)
			return value.getClass().getSimpleName().toLowerCase(Locale.ROOT);
		return yggdrasil.getID(value.getClass()).replace("ConfigurationSerializable_", "").toLowerCase(Locale.ROOT);
	}

	Object decode(SerializedVariable.Value value) throws IOException {
		if (value.data.length >= ENVELOPE.length
				&& Arrays.equals(ENVELOPE, Arrays.copyOf(value.data, ENVELOPE.length))) {
			Object decoded = decodePayload(Arrays.copyOfRange(value.data, ENVELOPE.length, value.data.length));
			if (!typeName(decoded).equals(value.type))
				throw new StreamCorruptedException("MySQL type label does not match its payload");
			return decoded;
		}
		if (!FORMAT.equals(value.type)) {
			if (value.type.startsWith("mysql:"))
				throw new StreamCorruptedException("Unknown MySQL serializer version");
			ClassInfo<?> info = Classes.getClassInfoNoError(value.type);
			if (info == null || info.getSerializer() == null)
				throw new StreamCorruptedException("Unknown persisted type or format: " + value.type);
			Object decoded = Classes.deserialize(info, value.data);
			if (decoded == null)
				throw new StreamCorruptedException("Unreadable legacy value: " + value.type);
			return decoded;
		}
		return decodePayload(value.data);
	}

	private Object decodePayload(byte[] data) throws IOException {
		if (data.length < 8)
			throw new StreamCorruptedException("Truncated MySQL value");
		int size = data.length - 8;
		CRC32 checksum = new CRC32();
		checksum.update(data, 0, size);
		if (checksum.getValue() != ByteBuffer.wrap(data, size, 8).getLong())
			throw new StreamCorruptedException("MySQL value checksum mismatch");
		ByteArrayInputStream bytes = new ByteArrayInputStream(data, 0, size);
		try (var in = yggdrasil.newInputStream(bytes)) {
			Object decoded = in.readObject();
			if (decoded == null || bytes.available() != 0)
				throw new StreamCorruptedException("Null or trailing persisted data");
			return decoded;
		}
	}

	// Skript list variables are stored as individual leaves. Java containers supplied by
	// addons are represented by Yggdrasil's existing list/map serializers, recursively.
	private Object normalize(Object value, IdentityHashMap<Object, Boolean> visiting, int depth) throws IOException {
		if (value != null && value.getClass() == Object.class)
			throw new NotSerializableException("Object has no persistent semantics");
		if (depth > 128)
			throw new NotSerializableException("Nested value exceeds 128 levels");
		if (!(value instanceof List<?> || value instanceof Map<?, ?> || value instanceof Object[]))
			return value;
		if (visiting.put(value, true) != null)
			throw new NotSerializableException("Cyclic container");
		try {
			if (value instanceof Map<?, ?> map) {
				Map<Object, Object> result = new HashMap<>();
				for (var entry : map.entrySet())
					result.put(normalize(entry.getKey(), visiting, depth + 1), normalize(entry.getValue(), visiting, depth + 1));
				return result;
			}
			if (value instanceof List<?> list) {
				List<Object> result = new ArrayList<>(list.size());
				for (Object item : list)
					result.add(normalize(item, visiting, depth + 1));
				return result;
			}
			Object[] array = (Object[]) value;
			Object[] result = array.clone();
			for (int i = 0; i < result.length; i++)
				result[i] = normalize(array[i], visiting, depth + 1);
			return result;
		} finally {
			visiting.remove(value);
		}
	}
}
