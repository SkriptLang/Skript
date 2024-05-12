package ch.njol.skript.events;

import java.util.ArrayList;
import java.util.List;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent.WhitelistStatus;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EvtWhitelist extends SkriptEvent {

	private static final boolean HAS_PLAYER_WHITELIST_EVENT = Skript.classExists("io.papermc.paper.event.server.WhitelistStateUpdateEvent");
	private static final boolean HAS_SERVER_WHITELIST_EVENT = Skript.classExists("com.destroystokyo.paper.event.server.WhitelistToggleEvent");

	static {
		List<Class<? extends Event>> events = new ArrayList<>();
		List<String> patterns = new ArrayList<>();
		if (HAS_PLAYER_WHITELIST_EVENT) {
			events.add(WhitelistStateUpdateEvent.class);
			patterns.add("player whitelist [toggle[d]|state:(0:add[ed]|2:remove[d])]");
		}
		if (HAS_SERVER_WHITELIST_EVENT) {
			events.add(WhitelistToggleEvent.class);
			patterns.add("server whitelist [toggle[d]|state:(0:disable[d]|2:enable[d])]");
		}
		if (!events.isEmpty()) {
			//noinspection unchecked
			Skript.registerEvent("Player / Server Whitelist", EvtWhitelist.class, events.toArray(new Class[0]), patterns.toArray(new String[0]))
				.description("Called when the whitelist state of a player or the server changes. The player whitelist event is cancellable, and doing so will prevent its state from changing.")
				.examples(
					"on player whitelist:",
					"on player whitelist added:",
					"on player whitelist removed:",
					"on server whitelist:",
					"on server whitelist enabled:",
					"on server whitelist disabled:")
				.keywords(
					"player",
					"server")
				.since("INSERT VERSION")
				.requiredPlugins("Paper (server), Paper 1.20+ (player)");
		}
	}

	private Kleenean state = Kleenean.UNKNOWN;
	private boolean isPlayerEvent;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		isPlayerEvent = matchedPattern == 0;
		if (parseResult.hasTag("state"))
			state = Kleenean.values()[parseResult.mark];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (HAS_PLAYER_WHITELIST_EVENT && isPlayerEvent && event instanceof WhitelistStateUpdateEvent) {
			if (state == Kleenean.UNKNOWN)
				return true;
			Kleenean added = Kleenean.get(((WhitelistStateUpdateEvent) event).getStatus() == WhitelistStatus.ADDED);
			return added == state;
		}
		if (HAS_SERVER_WHITELIST_EVENT && !isPlayerEvent && event instanceof WhitelistToggleEvent) {
			if (state == Kleenean.UNKNOWN)
				return true;
			return ((WhitelistToggleEvent) event).isEnabled() == state.isTrue();
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String state = "";
		if (this.state != Kleenean.UNKNOWN) {
			state = this.state == Kleenean.TRUE ? " enable" : " disable";
		}
		return (isPlayerEvent ? "player" : "server") + " whitelist" + state;
	}

}
