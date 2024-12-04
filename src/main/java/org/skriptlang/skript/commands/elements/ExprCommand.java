package org.skriptlang.skript.commands.elements;

import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.commands.api.ScriptCommandEvent;

@Name("Command")
@Description("The command that caused an 'on command' event (excluding the leading slash and all arguments)")
@Examples({
	"# prevent any commands except for the /exit command during some game",
	"on command:",
		"\tif {game::%player%::playing} is true:",
			"\t\tif the command is not \"exit\":",
				"\t\t\tmessage \"You're not allowed to use commands during the game\"",
				"\t\t\tcancel the event"
})
@Since("2.0, 2.7 (support for script commands)")
@Events("command")
public class ExprCommand extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprCommand.class, String.class, ExpressionType.SIMPLE,
				"[the] (full|complete|whole) command",
				"[the] command [label|alias]"
		);
	}

	private boolean fullCommand;
	
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class, ScriptCommandEvent.class)) {
			Skript.error("The 'command' expression can only be used in a script command or command event");
			return false;
		}
		fullCommand = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected String @Nullable [] get(Event event) {
		String command;

		if (event instanceof PlayerCommandPreprocessEvent) {
			command = ((PlayerCommandPreprocessEvent) event).getMessage().substring(1).trim();
		} else if (event instanceof ServerCommandEvent) {
			command = ((ServerCommandEvent) event).getCommand().trim();
		} else {
			ScriptCommandEvent scriptEvent = (ScriptCommandEvent) event;
			if (!fullCommand) // do this now to avoid doing anything with the args array
				return new String[]{scriptEvent.getLabel()};
			command = scriptEvent.getLabel() + " " + StringUtils.join(scriptEvent.getArgs(), " ");
		}

		if (fullCommand) {
			return new String[]{command};
		} else {
			int firstSpace = command.indexOf(' ');
			return new String[]{firstSpace == -1 ? command : command.substring(0, firstSpace)};
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return fullCommand ? "the full command" : "the command";
	}
	
}
