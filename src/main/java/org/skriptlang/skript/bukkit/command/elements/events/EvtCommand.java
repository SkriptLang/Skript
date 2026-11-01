package org.skriptlang.skript.bukkit.command.elements.events;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.List;

public class EvtCommand extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtCommand.class, "Command")
			.supplier(EvtCommand::new)
			.addEvents(List.of(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class))
			.addPattern("command [%-strings%]")
			.addDescription("""
				Called when a player enters a command. \
				You can check whether the command is a script command using the \
				'<a href='#CondIsScriptCommand'>Is a Script Command</a>' condition.
				""")
			.addExample("""
				on command:
					the player is in water
					cancel the event
					send "<red>Commands cannot be executed in water!" to the player
				""")
			.addSince("2.0")
			.build());
	}

	private Literal<String> commands;
	private String @Nullable [] adjustedCommands;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			//noinspection unchecked
			commands = ((Literal<String>) args[0]);
			adjustedCommands = commands.getAll();
			for (int i = 0; i < adjustedCommands.length; i++) { // strip leading slashes
				if (adjustedCommands[i].startsWith("/"))
					adjustedCommands[i] = adjustedCommands[i].substring(1);
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (event instanceof ServerCommandEvent serverCommandEvent && serverCommandEvent.getCommand().isEmpty())
			return false;

		if (commands == null)
			return true;

		String message;
		if (event instanceof PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
			assert playerCommandPreprocessEvent.getMessage().startsWith("/");
			message = playerCommandPreprocessEvent.getMessage().substring(1);
		} else {
			assert event instanceof ServerCommandEvent;
			message = ((ServerCommandEvent) event).getCommand();
		}

		assert adjustedCommands != null;
		return Arrays.stream(adjustedCommands)
			.filter(command -> StringUtils.startsWithIgnoreCase(message, command)) // matches the command label
			.anyMatch(command ->
				command.contains(" ") // if candidate contains arguments, then any command that starts with the candidate is a match
					|| message.length() == command.length() // exact match
					|| Character.isWhitespace(message.charAt(command.length())) // matches label with space after
			);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "command" + (commands == null ? "" : (" " + commands.toString(event, debug)));
	}

}
