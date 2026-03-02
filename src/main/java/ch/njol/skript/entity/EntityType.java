package ch.njol.skript.entity;

import ch.njol.yggdrasil.YggdrasilSerializable;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link org.skriptlang.skript.bukkit.entity.EntityType} instead.
 */
@Deprecated(forRemoval = true, since = "INSERT VERSION")
public abstract class EntityType implements Cloneable, YggdrasilSerializable {

	public static @Nullable EntityType parse(String string) {
		return org.skriptlang.skript.bukkit.entity.EntityType.parse(string);
	}

	public int amount;
	public EntityData<?> data;
	public org.skriptlang.skript.bukkit.entity.EntityType newEntityType;

	/**
	 * Only used for deserialisation
	 */
	@SuppressWarnings({"unused", "null"})
	protected EntityType() {
		data = null;
	}

	public EntityType(EntityData<?> data, int amount) {
		assert data != null;
		this.data = data;
		this.amount = amount;
		if (!(this instanceof org.skriptlang.skript.bukkit.entity.EntityType))
			this.newEntityType = new org.skriptlang.skript.bukkit.entity.EntityType((org.skriptlang.skript.bukkit.entity.EntityData<?>) data, amount);
	}

	public EntityType(Class<? extends Entity> entityClass, int amount) {
		assert entityClass != null;
		data = EntityData.fromClass(entityClass);
		this.amount = amount;
		if (!(this instanceof org.skriptlang.skript.bukkit.entity.EntityType))
			this.newEntityType = new org.skriptlang.skript.bukkit.entity.EntityType(entityClass, amount);
	}

	public EntityType(Entity entity) {
		data = EntityData.fromEntity(entity);
		if (!(this instanceof org.skriptlang.skript.bukkit.entity.EntityType))
			this.newEntityType = new org.skriptlang.skript.bukkit.entity.EntityType(entity);
	}

	public EntityType(EntityType other) {
		amount = other.amount;
		data = other.data;
		if (!(this instanceof org.skriptlang.skript.bukkit.entity.EntityType))
			this.newEntityType = new org.skriptlang.skript.bukkit.entity.EntityType(other.newEntityType);
	}

	public boolean isInstance(Entity entity) {
		return newEntityType.isInstance(entity);
	};

	@Override
	public String toString() {
		return newEntityType.toString();
	};

	public String toString(int flags) {
		return newEntityType.toString(flags);
	}

	public int getAmount() {
		return newEntityType.getAmount();
	};

	public boolean sameType(EntityType other) {
		return newEntityType.sameType(other.newEntityType);
	};

	@Override
	public EntityType clone() {
		return newEntityType.clone();
	};

	@Override
	public int hashCode() {
		return newEntityType.hashCode();
	};

	@Override
	public boolean equals(@Nullable Object obj) {
		return newEntityType.equals(obj);
	}

}
