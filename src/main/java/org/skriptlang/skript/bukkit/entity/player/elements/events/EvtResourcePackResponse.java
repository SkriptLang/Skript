package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtResourcePackResponse extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtResourcePackResponse.class, "Resource Pack Request Response")
			.supplier(EvtResourcePackResponse::new)
			.addEvent(PlayerResourcePackStatusEvent.class)
			.addPatterns(
				"resource pack [request] response",
				"resource pack [request] %resourcepackstates%"
			)
			.addDescription("""
				Called when a player takes action on a resource pack request sent via the\s
				<a href='#EffSendResourcePack'>send resource pack</a> effect.
				The <a href='#CondResourcePack'>resource pack</a> condition can be used\s
				to check the resource pack state.
				This event will be triggered once when the player accepts or declines the resource pack request,\s
				and once when the resource pack is successfully installed or failed to download.
				""")
			.addExample("""
				on resource pack request response:
					if the resource pack was declined or failed to download:
						kick player due to "<red>You must accept the resource pack to play!"
				""")
			.addExample("""
				on resource pack accept:
					send "Welcome to the server!" to player
				""")
			.addSince("2.4")
			.build());
	}

	private @Nullable Literal<Status> states;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		if (matchedPattern == 1)
			states = (Literal<Status>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (states == null)
			return true;

		PlayerResourcePackStatusEvent playerEvent = (PlayerResourcePackStatusEvent) event;

		Status state = playerEvent.getStatus();
		return states.check(event, state::equals);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(states != null, "resource pack", states)
			.appendIf(states == null, "resource pack request response")
			.toString();
	}

}
