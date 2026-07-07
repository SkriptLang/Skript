package org.skriptlang.skript.bukkit.entity.player.elements.events;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Collection;

public class EvtPlayerCommandSend extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerCommandSend.class, "Player First Join")
			.supplier(EvtPlayerCommandSend::new)
			.addEvent(PlayerCommandSendEvent.class)
			.addPatterns(
				"send[ing] [of [the]] [server] command[s] list",
				"[server] command list send"
			)
			.addDescription("""
				Called when the server sends a list of commands to the player.
				This usually happens on join. The sent commands\s
				can be modified via the <a href='#ExprSentCommands'>sent commands expression</a>.
				Modifications will affect what commands show up for the player to tab complete.
				They will not affect what commands the player can actually run.
				Adding new commands to the list is illegal behavior and will be ignored.
				""")
			.addExample("""
				on send command list:
					set command list to command list where [input does not contain ":"]
				    remove "help" from command list
				""")
			.addSince("2.8.0")
			.build());
	}

	private final Collection<String> originalCommands = new ArrayList<>();

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		originalCommands.clear();
		originalCommands.addAll(((PlayerCommandSendEvent) event).getCommands());
		return true;
	}

	public ImmutableCollection<String> getOriginalCommands() {
		return ImmutableList.copyOf(originalCommands);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "sending of the server command list";
	}

}
