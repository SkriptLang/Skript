package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.lang.ParseContext;
import org.jetbrains.annotations.Nullable;

public class EntityTypeClassInfo extends ClassInfo<EntityType> {

	public EntityTypeClassInfo() {
		super(EntityType.class, "entitytype");

		this.name("Entity Type with Amount")
			.description("An <a href='#entitydata'>entity type</a> with an amount, e.g. '2 zombies'.")
			.usage("<number> <entity type>")
			.examples("spawn 5 creepers behind the player")
			.since("1.3")
			.parser(new EntityTypeParser())
			.serializer(new YggdrasilSerializer<>());
	}

	public static class EntityTypeParser extends Parser<EntityType> {
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
	}

}
