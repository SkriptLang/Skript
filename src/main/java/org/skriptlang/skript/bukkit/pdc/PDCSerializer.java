package org.skriptlang.skript.bukkit.pdc;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.Yggdrasil;
import com.google.common.primitives.Primitives;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.*;
import java.util.*;

/**
 * A serializer that can serialize and deserialize Yggsdrasil serializable objects to and from PersistentDataContainers.
 */
public class PDCSerializer {

	private static final NamespacedKey BLOB_KEY = new NamespacedKey("skript", "blob");
	private static final NamespacedKey TYPE_KEY = new NamespacedKey("skript", "pdc_type");
	private static final NamespacedKey VALUE_KEY = new NamespacedKey("skript", "pdc_value");

	/**
	 * Types that are directly serializable to PDC, and therefore do not need to be handled through Fields.
	 * Never add a custom type that uses {@link PersistentDataContainer} as the primitive. That will cause
	 * the {@link SkriptDataType} to not be used.
	 */
	private static final Map<Class<?>, PersistentDataType<?, ?>> REPRESENTABLE_TYPES = new LinkedHashMap<>();

	private static final Yggdrasil YGGDRASIL = Variables.yggdrasil;

	static {
		// ensure boolean is first to avoid them being interpreted as bytes
		REPRESENTABLE_TYPES.put(Boolean.class, PersistentDataType.BOOLEAN);
		REPRESENTABLE_TYPES.put(Byte.class, PersistentDataType.BYTE);
		REPRESENTABLE_TYPES.put(Short.class, PersistentDataType.SHORT);
		REPRESENTABLE_TYPES.put(Integer.class, PersistentDataType.INTEGER);
		REPRESENTABLE_TYPES.put(Long.class, PersistentDataType.LONG);
		REPRESENTABLE_TYPES.put(Double.class, PersistentDataType.DOUBLE);
		REPRESENTABLE_TYPES.put(Float.class, PersistentDataType.FLOAT);
		REPRESENTABLE_TYPES.put(String.class, PersistentDataType.STRING);
	}

	public static @Unmodifiable Collection<PersistentDataType<?, ?>> getRepresentablePDCTypes() {
		return Collections.unmodifiableCollection(REPRESENTABLE_TYPES.values());
	}

	public static PersistentDataType<?, ?> getPDCType(ClassInfo<?> classInfo) {
		if (REPRESENTABLE_TYPES.containsKey(classInfo.getC())) {
			return REPRESENTABLE_TYPES.get(classInfo.getC());
		} else {
			return SkriptDataType.get();
		}
	}

	public static @NotNull PersistentDataContainer serialize(
		@NotNull Object unserializedData,
		@NotNull PersistentDataAdapterContext context
	) {
		return serialize(unserializedData, context, false);
	}

	@SuppressWarnings("unchecked")
	private static @NotNull PersistentDataContainer serialize(
		@NotNull Object unserializedData,
		@NotNull PersistentDataAdapterContext context,
		boolean nested
	) {
		assert Bukkit.isPrimaryThread();

		ClassInfo<?> classInfo = Classes.getSuperClassInfo(unserializedData.getClass());
		if (classInfo.getSerializeAs() != null) {
			classInfo = Classes.getExactClassInfo(classInfo.getSerializeAs());
			if (classInfo == null) {
				assert false : unserializedData.getClass();
				return null;
			}
			unserializedData = Converters.convert(unserializedData, classInfo.getC());
			if (unserializedData == null) {
				assert false : classInfo.getCodeName();
				return null;
			}
		}

		var serializer = (Serializer<Object>) classInfo.getSerializer();
		if (serializer == null) { // no serializer, fall back to Yggdrasil byte array as base64 string if this is a subvalue
			if (nested)
				return serializeToBase64(unserializedData, context);
			// unnested unserializable type is invalid
			throw new RuntimeException("The value " + unserializedData + " is not serializable!");
		}

		assert !serializer.mustSyncDeserialization() || Bukkit.isPrimaryThread();
		var container = context.newPersistentDataContainer();

		// shortcut for primitives
		if (REPRESENTABLE_TYPES.containsKey(classInfo.getC())) {
			container.set(TYPE_KEY, PersistentDataType.STRING, classInfo.getCodeName());
			var pdcType = (PersistentDataType<Object, Object>) REPRESENTABLE_TYPES.get(classInfo.getC());
			container.set(VALUE_KEY, pdcType, unserializedData);
			return container;
		}

		// If not a primitive, serialize normally and use Fields to store data
		try {
			Fields fields = serializer.serialize(unserializedData);
			container.set(TYPE_KEY, PersistentDataType.STRING, classInfo.getCodeName());
			for (var field : fields) {
				var tag = new NamespacedKey("skript", field.getID());
				var data = field.isPrimitive() ? field.getPrimitive() : field.getObject();
				if (data == null) {
					container.set(tag, PersistentDataType.TAG_CONTAINER, context.newPersistentDataContainer());
					continue;
				}
				if (field.isPrimitive() || data instanceof String) {
					var type = REPRESENTABLE_TYPES.get(data.getClass());
					if (type == null) {
						throw new NotSerializableException("Unsupported primitive type: " + data.getClass());
					}
					container.set(tag, (PersistentDataType<Object, Object>) type, data);
				} else {
					// write a nested PDC
					data = PDCSerializer.serialize(data, context, true);
					container.set(tag, PersistentDataType.TAG_CONTAINER, (PersistentDataContainer) data);
				}
			}
		} catch (NotSerializableException | StreamCorruptedException e) {
			throw new RuntimeException(e);
		}
		return container;
	}

	public static @NotNull Object deserialize(
		@NotNull PersistentDataContainer serializedData,
		@NotNull PersistentDataAdapterContext context
	) {
		// check for base64 blob fallback
		if (serializedData.has(BLOB_KEY, PersistentDataType.STRING))
			return deserializeFromBase64(serializedData);

		String typeName = serializedData.get(TYPE_KEY, PersistentDataType.STRING);
		if (typeName == null) {
			throw new IllegalArgumentException("Cannot deserialize PDC because it has no type");
		}
		ClassInfo<?> classInfo = Classes.getClassInfo(typeName);
		//noinspection unchecked
		Serializer<Object> serializer = (Serializer<Object>) classInfo.getSerializer();
		if (serializer == null) {
			throw new IllegalArgumentException("Cannot deserialize " + classInfo.getCodeName() + " because it has no serializer");
		}

		// shortcut for primitives
		if (REPRESENTABLE_TYPES.containsKey(classInfo.getC())) {
			//noinspection unchecked
			var pdcType = (PersistentDataType<Object, Object>) REPRESENTABLE_TYPES.get(classInfo.getC());
			Object value = serializedData.get(VALUE_KEY, pdcType);
			if (value == null) {
				throw new IllegalArgumentException("Cannot deserialize " + classInfo.getCodeName() + " because its value is missing");
			}
			return value;
		}

		// If not a primitive, deserialize normally using Fields
		try {
			Fields fields = new Fields(YGGDRASIL);
			for (var key : serializedData.getKeys()) {
				if (key.equals(TYPE_KEY))
					continue;
				Object data = null;
				boolean primitive = true;
				for (var entry : REPRESENTABLE_TYPES.entrySet()) {
					var type = entry.getValue();
					if (serializedData.has(key, type)) {
						data = serializedData.get(key, type);
						primitive = entry.getKey().isPrimitive() || isPrimitiveWrapper(entry.getKey());
						break;
					}
				}
				if (data == null) {
					if (serializedData.has(key, PersistentDataType.TAG_CONTAINER)) {
						PersistentDataContainer nestedContainer = serializedData.get(key, PersistentDataType.TAG_CONTAINER);
						assert nestedContainer != null;
						if (nestedContainer.isEmpty()) {
							// empty container is a null sentinel
							fields.putObject(key.getKey(), null);
							continue;
						}
						data = PDCSerializer.deserialize(nestedContainer, context);
						primitive = false;
					} else {
						throw new NotSerializableException("Unsupported data type for key: " + key);
					}
				}
				if (primitive) {
					fields.putPrimitive(key.getKey(), data);
				} else {
					fields.putObject(key.getKey(), data);
				}
			}
			assert !serializer.mustSyncDeserialization() || Bukkit.isPrimaryThread();
			if (serializer.canBeInstantiated(classInfo.getC())) {
				Object obj = serializer.newInstance(classInfo.getC());
				serializer.deserialize(obj, fields);
				if (obj == null)
					throw new NotSerializableException("Could not deserialize object of type " + classInfo.getC());
				return obj;
			}
			return serializer.deserialize(classInfo.getC(), fields);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Serializes an object to a base64-encoded Yggdrasil byte string, stored in a PDC.
	 * Used as a fallback for objects whose types have no Skript serializer (e.g. nested fields
	 * like {@link ArrayList} that are part of a larger serializable object).
	 */
	private static PersistentDataContainer serializeToBase64(
		@NotNull Object data,
		@NotNull PersistentDataAdapterContext context
	) {
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			var yggOut = YGGDRASIL.newOutputStream(byteOut);
			yggOut.writeObject(data);
			yggOut.flush();
			yggOut.close();
			var container = context.newPersistentDataContainer();
			container.set(BLOB_KEY, PersistentDataType.STRING, Base64.getEncoder().encodeToString(byteOut.toByteArray()));
			return container;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Deserializes an object from a base64-encoded Yggdrasil byte string stored in a PDC.
	 */
	private static Object deserializeFromBase64(@NotNull PersistentDataContainer container) {
		String blob = container.get(BLOB_KEY, PersistentDataType.STRING);
		if (blob == null)
			throw new IllegalArgumentException("Cannot deserialize PDC because blob value is missing");
		try {
			var yggIn = YGGDRASIL.newInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(blob)));
			return yggIn.readObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isPrimitiveWrapper(Class<?> key) {
		return Primitives.isWrapperType(key);
	}

}
