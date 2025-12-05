package org.skriptlang.skript.bukkit.whitelist.elements;

import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;

public class EvtServerWhitelist extends SkriptEvent {

	private enum EventState {

		ON("on"),
		OFF("off");

		final String toString;

		EventState(String toString) {
			this.toString = toString;
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	static {
		Skript.registerEvent("Whitelist Toggled", EvtServerWhitelist.class, WhitelistToggleEvent.class, "whitelist toggle[d] [:on|:off]")
			.description(
				"Called whenever the server's whitelist has been toggled on or off.",
				"Use <a href='conditions.html#CondWillBeWhitelisted'>will be whitelisted</a> condition to check with its state.")
			.examples(
				"on whitelist toggled on:",
				"on whitelist toggled off:",
				"",
				"on whitelist toggled:",
					"\tsend \"Server whitelist has been set to %whether server will be whitelisted%\" to all ops")
			.since("INSERT VERSION");
	}

	private @Nullable EventState state = null;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (parseResult.hasTag("on"))
			state = EventState.ON;
		else if (parseResult.hasTag("off"))
			state = EventState.OFF;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (state != null)
			return (state == EventState.ON) == ((WhitelistToggleEvent) event).isEnabled();
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.append("server whitelist toggled");
		if (state != null)
			builder.append(state);
		return builder.toString();
	}

}
