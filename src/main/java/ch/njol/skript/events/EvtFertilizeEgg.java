package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EvtFertilizeEgg extends SkriptEvent {
	static {
		Skript.registerEvent("Entity Fertilize", EvtFertilizeEgg.class, EntityFertilizeEggEvent.class,
				"[entity] fertiliz(e|ing) [an] egg [of %-entitytypes%]")
			.description(
				"Called whenever an entity fertilizes an egg (e.g. a turtle has an egg, a frog becomes pregnant, or a " +
					"sniffer finds a sniffer egg).")
			.examples(
				"on fertilizing egg of turtles:",
				"\tsend \"A turtle just fertilized an egg!\"",
				"on fertilizing egg:",
				"\tif event-entity is a frog:",
				"\t\tsend \"A frog just became pregnant!\""
			)
			.since("2.14");

		EventValues.registerEventValue(EntityFertilizeEggEvent.class, Entity.class, event -> {
			assert false;
			return event.getEntity();
		}, EventValues.TIME_NOW,
			"Use 'mother' and/or 'father' in fertilize egg events",
			EntityFertilizeEggEvent.class
		);
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
		return event instanceof EntityFertilizeEggEvent fertilizeEvent && checkEntity(fertilizeEvent.getEntity());
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
		return "on fertilizing egg" + (entitiesLiteral == null ? "" : " of " + entitiesLiteral);
	}

}
