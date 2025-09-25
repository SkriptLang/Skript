package org.skriptlang.skript.bukkit.whitelist.elements;

import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;

public class EvtServerWhitelist extends SkriptEvent {

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
			.since("");
	}

	private Kleenean state = Kleenean.UNKNOWN;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (parseResult.hasTag("on"))
			state = Kleenean.TRUE;
		else if (parseResult.hasTag("off"))
			state = Kleenean.FALSE;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!state.isUnknown())
			return state.isTrue() == ((WhitelistToggleEvent) event).isEnabled();
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.append("server whitelist toggled");
		if (!state.isUnknown())
			builder.append(state.isTrue() ? "on" : "off");
		return builder.toString();
	}

}
