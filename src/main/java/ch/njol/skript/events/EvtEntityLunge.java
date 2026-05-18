package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventConverter;
import ch.njol.skript.registrations.EventValues;
import io.papermc.paper.event.entity.EntityLungeEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class EvtEntityLunge extends SkriptEvent {

	@Nullable
	private Literal<EntityType> entityTypes;

	static {
		// Since paper 26.1.2
		if (Skript.classExists("io.papermc.paper.event.entity.EntityLungeEvent")) {
			Skript.registerEvent("Entity Lunge", EvtEntityLunge.class, EntityLungeEvent.class, "[%-entitytypes%] lunge")
				.description("Called when an entity lunges.",
					"Either by using a spear enchanted with the lunge enchantment (e.g. players, skeletons)",
					"or because of a mob's natural lunge attack (e.g. ravagers).",
					"Lunge attack propels entity forward horizontally.")
				.examples("on lunge:",
						      "\tset lunge power to 4",
						  "on ravager lunge:",
							  "\tcancel event"
				)
				.since("INSERT VERSION");

			EventValues.registerEventValue(EntityLungeEvent.class, Integer.class, new EventConverter<>() {
				@Override
				public void set(EntityLungeEvent event, Integer value) {
					event.setLungePower(value);
				}

				@Override
				public Integer convert(EntityLungeEvent event) {
					return event.getLungePower();
				}
			});
		}
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		entityTypes = (Literal<EntityType>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EntityLungeEvent lungeEvent)) {
			return false;
		}

		if (entityTypes == null) {
			return true;
		}

		for (EntityType entityType : entityTypes.getAll()) {
			if (entityType.isInstance(lungeEvent.getEntity())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (entityTypes != null) {
			builder.append(entityTypes).append(" ");
		}
		builder.append("lunge");
		return builder.toString();
	}

}
