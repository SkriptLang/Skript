package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class EvtEntityTarget extends SkriptEvent {
	static {
		Skript.registerEvent("Target", EvtEntityTarget.class, EntityTargetEvent.class, "[entity] target", "[entity] un[-]target")
				.description("Called when a mob starts/stops following/attacking another entity, usually a player.")
				.examples("on entity target:",
						"\ttarget is a player")
				.since("1.0");
	}
	
	private boolean target;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		target = matchedPattern == 0;
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return ((EntityTargetEvent) e).getTarget() == null ^ target;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "entity " + (target ? "" : "un") + "target";
	}
	
}
