package ch.njol.skript.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

public class EvtAttemptAttack extends SkriptEvent {
	static {
		Skript.registerEvent("Attempt Attack", EvtAttemptAttack.class, PrePlayerAttackEntityEvent.class, "attack attempt", "attempt[ing] to attack %entitydatas%")
				.description("""
                    Called when a player attempts to attack an entity.
                    The event will be cancelled as soon as it is fired for non-living entities.
                    Cancelling this event will prevent the attack and any sounds from being played when attacking.
                    Any damage events will not be called if this is cancelled.
                    """) 
				.examples("""
                    on attack attempt:
                        if event is cancelled:
                            broadcast "%attacker% failed to attack %victim%!"
                        else:
                            broadcast "%attacker% damaged %victim%!"

                    on attempt to attack an animal:
                        cancel event        

                    on attempting to attack an entity:
                        if victim is a creeper:
                            cancel event     

                    on attempt to attack a zombie or creeper:
                        attacker isn't holding a diamond sword
                        cancel event
                    """)
				.since("INSERT VERSION");
	}
	
	@Nullable
	private EntityData<?>[] types;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = args.length == 0 ? null : ((Literal<EntityData<?>>) args[0]).getAll();
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		if (types == null)
			return true;
		final Entity en = ((PrePlayerAttackEntityEvent) e).getAttacked();
		for (final EntityData<?> d : types) {
			if (d.isInstance(en))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "attempt attack" + (types != null ? " of " + Classes.toString(types, false) : "");
	}
	
}
