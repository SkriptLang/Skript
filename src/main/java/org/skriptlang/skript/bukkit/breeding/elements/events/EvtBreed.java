package org.skriptlang.skript.bukkit.breeding.elements.events;

import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBreed extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBreed.class, "Entity Breed")
				.addEvent(EntityBreedEvent.class)
				.addPatterns("[entity] breed[ing] [of %-entitytypes%]")
				.addDescription("Called whenever two animals begin to conceive a child. The type can be specified.")
				.addExample("""
					on breeding of llamas:
						send "When a %breeding mother% and %breeding father% love each other very much they make %offspring%" to breeder
					""")
				.addSince("2.10")
				.supplier(EvtBreed::new)
				.build()
		);

		EventValues.registerEventValue(EntityBreedEvent.class, ItemStack.class, EntityBreedEvent::getBredWith);
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
