package org.skriptlang.skript.bukkit.pdc;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A serializer that can serialize and deserialize Yggsdrasil serializable objects to and from PersistentDataContainers.
 */
public class PDCSerializer {

	/**
	 * Types that are directly serializable to PDC, and therefore do not need to be handled through Fields.
	 * Never add a custom type that uses {@link PersistentDataContainer} as the primitive. That will cause
	 * the {@link SkriptDataType} to not be used.
	 */
	private static final Map<Class<?>, PersistentDataType<?, ?>> REPRESENTABLE_TYPES = new HashMap<>();

	static {
		REPRESENTABLE_TYPES.put(Byte.class, PersistentDataType.BYTE);
		REPRESENTABLE_TYPES.put(Short.class, PersistentDataType.SHORT);
		REPRESENTABLE_TYPES.put(Integer.class, PersistentDataType.INTEGER);
		REPRESENTABLE_TYPES.put(Long.class, PersistentDataType.LONG);
		REPRESENTABLE_TYPES.put(Double.class, PersistentDataType.DOUBLE);
		REPRESENTABLE_TYPES.put(Float.class, PersistentDataType.FLOAT);
		REPRESENTABLE_TYPES.put(Boolean.class, PersistentDataType.BOOLEAN);
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

	@SuppressWarnings("unchecked")
	public static @NotNull PersistentDataContainer serialize(
		@NotNull Object unserializedData,
		@NotNull PersistentDataAdapterContext context
	) {
		// temporary
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
		if (serializer == null) // value cannot be saved
			throw new IllegalArgumentException("Cannot serialize " + classInfo.getCodeName() + " because it has no serializer");

		assert !serializer.mustSyncDeserialization() || Bukkit.isPrimaryThread();
		var container = context.newPersistentDataContainer();

		// shortcut for primitives
		if (REPRESENTABLE_TYPES.containsKey(classInfo.getC())) {
			container.set(new NamespacedKey("skript", "type"), PersistentDataType.STRING, classInfo.getCodeName());
			var tag = new NamespacedKey("skript", "value");
			var pdcType = (PersistentDataType<Object, Object>) REPRESENTABLE_TYPES.get(classInfo.getC());
			container.set(tag, pdcType, unserializedData);
			return container;
		}

		// If not a primitive, serialize normally and use Fields to store data
		try {
			Fields fields = serializer.serialize(unserializedData);
			container.set(new NamespacedKey("skript", "type"), PersistentDataType.STRING, classInfo.getCodeName());
			for (var field : fields) {
				var tag = new NamespacedKey("skript", field.getID());
				var data = field.isPrimitive() ? field.getPrimitive() : field.getObject();
				if (data == null) {
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
					data = PDCSerializer.serialize(data, context);
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
		String typeName = serializedData.get(new NamespacedKey("skript", "type"), PersistentDataType.STRING);
		if (typeName == null) {
			throw new IllegalArgumentException("Cannot deserialize PDC because it has no type");
		}
		ClassInfo<?> classInfo = Classes.getClassInfo(typeName);
		//noinspection unchecked
		var serializer = (Serializer<Object>) classInfo.getSerializer();
		if (serializer == null) {
			throw new IllegalArgumentException("Cannot deserialize " + classInfo.getCodeName() + " because it has no serializer");
		}

		// shortcut for primitives
		if (REPRESENTABLE_TYPES.containsKey(classInfo.getC())) {
			var tag = new NamespacedKey("skript", "value");
			//noinspection unchecked
			var pdcType = (PersistentDataType<Object, Object>) REPRESENTABLE_TYPES.get(classInfo.getC());
			Object value = serializedData.get(tag, pdcType);
			if (value == null) {
				throw new IllegalArgumentException("Cannot deserialize " + classInfo.getCodeName() + " because its value is missing");
			}
			return value;
		}

		// If not a primitive, deserialize normally using Fields
		try {
			Fields fields = new Fields();
			for (var key : serializedData.getKeys()) {
				if (key.getNamespace().equals("skript") && key.getKey().equals("type")) {
					continue;
				}
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
			return serializer.deserialize(classInfo.getC(), fields);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isPrimitiveWrapper(Class<?> key) {
		return key == Byte.class || key == Short.class || key == Integer.class ||
			key == Long.class || key == Double.class || key == Float.class ||
			key == Boolean.class || key == Character.class;
	}
}
