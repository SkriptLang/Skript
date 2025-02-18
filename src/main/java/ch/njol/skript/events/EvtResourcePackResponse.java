package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class EvtResourcePackResponse extends SkriptEvent {

	static {
		Skript.registerEvent("Resource Pack Request Response", EvtResourcePackResponse.class, PlayerResourcePackStatusEvent.class,
				"resource pack [request] response",
				"resource pack [request] %resourcepackstates%")
				.description("Called when a player takes action on a resource pack request sent via the ",
						"<a href='effects.html#EffSendResourcePack'>send resource pack</a> effect. ",
						"The <a href='conditions.html#CondResourcePack'>resource pack</a> condition can be used ",
						"to check the resource pack state.",
						"",
						"This event will be triggered once when the player accepts or declines the resource pack request, ",
						"and once when the resource pack is successfully installed or failed to download.")
				.examples("on resource pack request response:",
						"	if the resource pack was declined or failed to download:",
						"",
						"on resource pack deny:",
						"	kick the player due to \"You have to install the resource pack to play in this server!\"")
				.since("2.4");
	}

	@Nullable
	private Literal<Status> states;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (matchedPattern == 1)
			states = (Literal<Status>) args[0];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	public boolean check(final Event e) {
		if (states != null) {
			Status state = ((PlayerResourcePackStatusEvent) e).getStatus();
			return states.check(e, state::equals);
		}
		return true;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return states != null ? "resource pack " + states.toString(e, debug) : "resource pack request response";
	}

}
