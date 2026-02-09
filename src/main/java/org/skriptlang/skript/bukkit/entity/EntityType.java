package org.skriptlang.skript.bukkit.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.yggdrasil.YggdrasilSerializable;

public class EntityType
	extends ch.njol.skript.entity.EntityType
	implements Cloneable, YggdrasilSerializable {

	static void register() {
		Classes.registerClass(new ClassInfo<>(EntityType.class, "entitytype")
				.name("Entity Type with Amount")
				.description("An <a href='#entitydata'>entity type</a> with an amount, e.g. '2 zombies'. I might remove this type in the future and make a more general 'type' type, i.e. a type that has a number and a type.")
				.usage("&lt;<a href='#number'>number</a>&gt; &lt;entity type&gt;")
				.examples("spawn 5 creepers behind the player")
				.since("1.3")
				.defaultExpression(new SimpleLiteral<>(new EntityType(Entity.class, 1), true))
				.parser(new Parser<>() {
					@Override
					public @Nullable EntityType parse(String string, ParseContext context) {
						return EntityType.parse(string);
					}
					
					@Override
					public String toString(EntityType entityType, int flags) {
						return entityType.toString(flags);
					}
					
					@Override
					public String toVariableNameString(EntityType entityType) {
						return "entitytype:" + entityType.toString();
					}
                })
				.serializer(new YggdrasilSerializer<>()));
	}

	public int amount;

	public EntityData<?> data;
	
	/**
	 * Only used for deserialisation
	 */
	@SuppressWarnings({"unused", "null"})
	private EntityType() {
		super();
		data = null;
		amount = 1;
	}
	
	public EntityType(EntityData<?> data, int amount) {
		super();
		assert data != null;
		this.data = data;
		this.amount = amount;
	}
	
	public EntityType(Class<? extends Entity> entityClass, int amount) {
		super();
		assert entityClass != null;
		data = EntityData.fromClass(entityClass);
		this.amount = amount;
	}
	
	public EntityType(Entity entity) {
		data = EntityData.fromEntity(entity);
		amount = 1;
	}
	
	public EntityType(EntityType other) {
		amount = other.amount;
		data = other.data;
	}
	
	public boolean isInstance(Entity entity) {
		return data.isInstance(entity);
	}
	
	@Override
	public String toString() {
		if (amount == 1)
			return data.toString(0);
		return amount + " " + data.toString(Language.F_PLURAL);
	}
	
	public String toString(int flags) {
		if (amount == 1)
			return data.toString(flags);
		return amount + " " + data.toString(flags | Language.F_PLURAL);
	}
	
	public int getAmount() {
		return amount;
	}

	public EntityData<?> getData() {
		return data;
	}

	public boolean sameType(EntityType other) {
		return data.equals(other.data);
	}

	public static @Nullable EntityType parse(String string) {
		assert string != null && !string.isEmpty();
		int amount = -1;
		if (string.matches("\\d+ .+")) {
			amount = Utils.parseInt(string.split(" ", 2)[0]);
			string = string.split(" ", 2)[1];
		} else if (string.matches("(?i)an? .+")) {
			string = string.split(" ", 2)[1];
		}
		EntityData<?> data = EntityData.parseWithoutIndefiniteArticle(string);
		if (data == null)
			return null;
		return new EntityType(data, amount);
	}
	
	@Override
	public EntityType clone() {
		return new EntityType(this);
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + data.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EntityType other))
			return false;
		if (amount != other.amount)
			return false;
		if (!data.equals(other.data))
			return false;
		return true;
	}
	
}
