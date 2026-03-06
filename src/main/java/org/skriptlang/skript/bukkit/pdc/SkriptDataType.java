package org.skriptlang.skript.bukkit.pdc;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * A PersistentDataType that can store any Yggsdrasil-serializable object using PDCSerializer.
 */
public class SkriptDataType implements PersistentDataType<PersistentDataContainer, Object> {

	private static SkriptDataType instance = null;

	public static SkriptDataType get() {
		if (instance == null)
			instance = new SkriptDataType();
		return instance;
	}

	@Override
	public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
		return PersistentDataContainer.class;
	}

	@Override
	public @NotNull Class<Object> getComplexType() {
		return Object.class;
	}

	@Override
	public @NotNull PersistentDataContainer toPrimitive(@NotNull Object complex, @NotNull PersistentDataAdapterContext context) {
		return PDCSerializer.serialize(complex, context);
	}

	@Override
	public @NotNull Object fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
		return PDCSerializer.deserialize(primitive, context);
	}

}
