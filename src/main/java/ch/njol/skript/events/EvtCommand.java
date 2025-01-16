package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

import java.util.Arrays;

public class EvtCommand extends SkriptEvent { // TODO condition to check whether a given command exists, & a conditon to check whether it's a custom skript command
	static {
		Skript.registerEvent("Command", EvtCommand.class, CollectionUtils.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class), "command [%-strings%]")
				.description("Called when a player enters a command (not necessarily a Skript command) but you can check if command is a skript command, see <a href='conditions.html#CondIsSkriptCommand'>Is a Skript command condition</a>.")
				.examples("on command:", "on command \"/stop\":", "on command \"pm Njol \":")
				.since("2.0");
	}
	
	private String @Nullable [] commands = null;
	private Literal<String> commandsLit;

	@Override
	@SuppressWarnings("null")
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (args[0] != null) {
			//noinspection unchecked
			commandsLit = ((Literal<String>) args[0]);
			commands = commandsLit.getAll();
			for (int i = 0; i < commands.length; i++) {
				if (commands[i].startsWith("/"))
					commands[i] = commands[i].substring(1);
			}
		}
		return true;
	}

	@Override
	@SuppressWarnings("null")
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

		return Arrays.stream(commands).anyMatch(candidateCommand ->
				StringUtils.startsWithIgnoreCase(message, candidateCommand) // matches the command label
				&& (candidateCommand.contains(" ") // if candidate contains arguments, then any command that starts with the candidate is a match
					|| message.length() == candidateCommand.length() // exact match
					|| Character.isWhitespace(message.charAt(candidateCommand.length()) // matches label with space after
				)));
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "command" + (commandsLit != null ? " " + commandsLit.toString(event, debug) : "");
	}
	
}
