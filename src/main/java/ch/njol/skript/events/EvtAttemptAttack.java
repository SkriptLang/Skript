package ch.njol.skript.events;

import ch.njol.skript.util.StringMode;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
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
                    """,
                    """
                    on attempt to attack an animal:
                        cancel event
                    """,
                    """ 
                    on attempting to attack an entity:
                        if victim is a creeper:
                            cancel event
                    """,
                    """
                    on attempt to attack a zombie or creeper:
                        attacker isn't holding a diamond sword
                        cancel event
                    """)
				.since("2.15");
	}
	
	private EntityData<?> @Nullable [] types;
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		//noinspection unchecked
		types = args.length == 0 ? null : ((Literal<EntityData<?>>) args[0]).getAll();
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		if (types == null)
			return true;

		if (!(event instanceof PrePlayerAttackEntityEvent preEvent))
			return false;	
		Entity entity = preEvent.getAttacked();

		for (final EntityData<?> data : types) {
			if (data.isInstance(entity))
				return true;
		}
		return false;
	}
	
    @Override
    public String toString(@Nullable Event event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (types == null) {
			builder.append("attack attempt");
		} else {
            builder.append("attempting to attack", Classes.toString(types, debug ? StringMode.DEBUG : StringMode.MESSAGE));
        }
        return builder.toString();
    }
	
}
