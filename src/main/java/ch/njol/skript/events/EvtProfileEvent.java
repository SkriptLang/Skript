package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.events.bukkit.ProfileCompletedEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;

public class EvtProfileEvent extends SkriptEvent {
	static {
		Skript.registerEvent("Profiler Event Available", EvtProfileEvent.class, ProfileCompletedEvent.class,
			"(profile|profiler) [event] (complete|completed|available)"
		).description(
			"Called after a new profiler event is available"
		).since("2.11");

		EventValues.registerEventValue(
			ProfileCompletedEvent.class,
			String.class,
			ProfileCompletedEvent::getName
		);

		EventValues.registerEventValue(
			ProfileCompletedEvent.class,
			Number.class,
			ProfileCompletedEvent::getDurationMs
		);
	}

	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "profile event complete";
	}

	@Override
	public boolean isEventPrioritySupported() {
		return false;
	}

	@Override
	public boolean check(Event event) {
		return event instanceof ProfileCompletedEvent;
	}

}
