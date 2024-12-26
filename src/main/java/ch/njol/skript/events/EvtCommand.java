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

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class EvtCommand extends SkriptEvent { // TODO condition to check whether a given command exists, & a conditon to check whether it's a custom skript command
	static {
		Skript.registerEvent("Command", EvtCommand.class, CollectionUtils.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class), "command [%-string%]")
				.description("Called when a player enters a command (not necessarily a Skript command) but you can check if command is a skript command, see <a href='conditions.html#CondIsSkriptCommand'>Is a Skript command condition</a>.")
				.examples("on command:", "on command \"/stop\":", "on command \"pm Njol \":")
				.since("2.0");
	}
	
	@Nullable
	private String command = null;

	@Override
	@SuppressWarnings("null")
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (args[0] != null) {
			command = ((Literal<String>) args[0]).getSingle();
			if (command.startsWith("/"))
				command = command.substring(1);
		}
		return true;
	}

	@Override
	@SuppressWarnings("null")
	public boolean check(final Event e) {
		if (e instanceof ServerCommandEvent && ((ServerCommandEvent) e).getCommand().isEmpty())
			return false;

		if (command == null)
			return true;
		final String message;
		if (e instanceof PlayerCommandPreprocessEvent) {
			assert ((PlayerCommandPreprocessEvent) e).getMessage().startsWith("/");
			message = ((PlayerCommandPreprocessEvent) e).getMessage().substring(1);
		} else {
			message = ((ServerCommandEvent) e).getCommand();
		}
		return StringUtils.startsWithIgnoreCase(message, command)
				&& (command.contains(" ") || message.length() == command.length() || Character.isWhitespace(message.charAt(command.length()))); // if only the command is given, match that command only
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "command" + (command != null ? " /" + command : "");
	}
	
}
