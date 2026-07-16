package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

// TODO condition to check whether a given command exists.
public class EvtCommand extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtCommand.class, "Command")
			.supplier(EvtCommand::new)
			.addEvents(CollectionUtils.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class))
			.addPatterns("command [%-strings%]")
			.addDescription("""
				Called when a player or console executes any command (including ones from outside of Skript).
				See <a href='#CondIsSkriptCommand'>Is a Skript command condition</a> to check if it is a skript command.
				""")
			.addExample("""
				on command "/ban":
					player is not op
					send "Nice try.." to player
					cancel event
				""")
			.addExample("""
				on command:
					add command to {commands::%player's uuid%}
				""")
			.addExample("""
				on command:
					if {combat::%player's uuid%} is set:
						cancel event
						send "No commands for you!" to player
				""")
			.addSince("2.0")
			.build());
	}

	@Nullable Literal<String> commandsLiteral;
	private @Nullable String[] commands;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			commandsLiteral = ((Literal<String>) args[0]);
			commands = Arrays.stream(commandsLiteral.getAll())
				.map(string -> string.startsWith("/") ? string.substring(1) : string)
				.toArray(String[]::new);
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (event instanceof ServerCommandEvent serverEvent && serverEvent.getCommand().isEmpty())
			return false;

		if (commands == null)
			return true;

		String message = switch(event) {
			case PlayerCommandPreprocessEvent playerEvent -> playerEvent.getMessage().substring(1);
			case ServerCommandEvent serverEvent -> serverEvent.getCommand();
			default -> null;
		};

		if (message == null)
			return false;

		return Arrays.stream(commands).anyMatch(candidate ->
			StringUtils.startsWithIgnoreCase(message, candidate)
				&& (candidate.contains(" ")
				|| message.length() == candidate.length()
				|| Character.isWhitespace(message.charAt(candidate.length()))));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("command")
			.appendIf(commandsLiteral != null, commandsLiteral)
			.toString();
	}

}
