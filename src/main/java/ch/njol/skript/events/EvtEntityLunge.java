package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import io.papermc.paper.event.entity.EntityLungeEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class EvtEntityLunge extends SkriptEvent {

	static {
		// Since paper 26.1.2
		if (Skript.classExists("io.papermc.paper.event.entity.EntityLungeEvent")) {
			Skript.registerEvent("Entity Lunge", EvtEntityLunge.class, EntityLungeEvent.class, "[%-entitytypes%] lunge")
				.description("Called when an entity lunges.",
					"Entity can perform lunge attack when holding a spear enchanted with the lunge enchantment.",
					"Lunge attack propels entity forward horizontally.")
				.examples(
					"""
					on lunge:
						set lunge power to 4
					""",
					"""
					on zombie lunge:
						cancel event
					"""
				)
				.since("INSERT VERSION");
		}
	}

	private @Nullable Literal<EntityType> entityTypes;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		entityTypes = (Literal<EntityType>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (entityTypes == null) {
			return true;
		}

		EntityLungeEvent lungeEvent = (EntityLungeEvent) event;

		for (EntityType entityType : entityTypes.getAll()) {
			if (entityType.isInstance(lungeEvent.getEntity())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(entityTypes != null, entityTypes)
			.append("lunge")
			.toString();
	}

}
