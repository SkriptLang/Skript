package ch.njol.skript.events;

import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtFertilizeEgg extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtFertilizeEgg.class, "Entity Fertilize")
				.addEvent(EntityFertilizeEggEvent.class)
				.addPatterns("[entity] fertiliz(e|ing) [an] egg [of %-entitytypes%]")
				.addDescription(
					"Called whenever an entity fertilizes an egg (e.g. a turtle has an egg, a frog becomes pregnant, or a " +
						"sniffer finds a sniffer egg).")
				.addExample("""
					on fertilizing egg of turtles:
						broadcast "A turtle just fertilized an egg!"
					""")
				.addExample("""
					on fertilizing egg:
						if event-entity is a frog:
							broadcast "A frog just became pregnant!"
					""")
				.addSince("INSERT VERSION")
				.supplier(EvtFertilizeEgg::new)
				.build()
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
		return "on fertilizing egg" + (entitiesLiteral == null ? "" : " of " + entitiesLiteral.toString(event, debug));
	}

}
