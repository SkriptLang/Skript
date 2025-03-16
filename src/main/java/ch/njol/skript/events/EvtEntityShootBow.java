package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.jetbrains.annotations.Nullable;

public class EvtEntityShootBow extends SkriptEvent {

	static {
		Skript.registerEvent("On Entity Shoot Bow", EvtEntityShootBow.class, EntityShootBowEvent.class,
			"%entitydata% shoot[ing] bow")
			.description("""
				Called when an entity shoots a bow.
				event-entity refers to the shot projectile/entity.
				""")
			.examples("""
				on player shoot bow:
					chance of 30%:
						damage event-slot by 10
						send "Your bow has taken increased damage!" to shooter
				""")
			.since("INSERT VERSION");
	}

	private Literal<EntityData<?>> entityData;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		//noinspection unchecked
		entityData = (Literal<EntityData<?>>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EntityShootBowEvent shootBowEvent))
			return false;
		EntityData<?> entityData = this.entityData.getSingle();
		if (entityData == null) return false;
		return entityData.isInstance(shootBowEvent.getEntity());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return this.entityData.toString(event, debug) + " shoot bow";
	}

}
