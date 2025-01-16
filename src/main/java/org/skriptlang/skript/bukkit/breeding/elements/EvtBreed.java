package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityBreedEvent;
import org.jetbrains.annotations.Nullable;

public class EvtBreed extends SkriptEvent {

	static {
		Skript.registerEvent("Entity Breed", EvtBreed.class, EntityBreedEvent.class,
				"[entity] breed[ing] [of %-entitytypes%]")
			.description("Called whenever two animals begin to conceive a child. The type can be specified.")
			.examples(
				"on breeding of llamas:",
					"\tsend \"When a %breeding mother% and %breeding father% love each " +
						"other very much they make %offspring%\" to breeder"
			)
			.since("2.10");
	}

	private @Nullable Literal<EntityType> entitiesLiteral;
	private EntityType @Nullable [] entities;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			//noinspection unchecked
			entitiesLiteral = ((Literal<EntityType>) args[0]);
			entities = entitiesLiteral.getAll();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		return event instanceof EntityBreedEvent breedEvent && checkEntity(breedEvent.getEntity());
	}

	private boolean checkEntity(Entity entity) {
		if (entities != null) {
			for (EntityType entityType : entities) {
				if (entityType.isInstance(entity))
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "on breeding" + (entitiesLiteral == null ? "" : " of " + entitiesLiteral);
	}
	
}
