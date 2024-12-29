package ch.njol.skript.expressions;

import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;

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

/**
 * @author Peter Güttinger
 */
@Name("Command")
@Description("The command that caused an 'on command' event (excluding the leading slash and all arguments)")
@Examples({"# prevent any commands except for the /exit command during some game",
		"on command:",
		"\tif {game::%player%::playing} is true:",
		"\t\tif the command is not \"exit\":",
		"\t\t\tmessage \"You're not allowed to use commands during the game\"",
		"\t\t\tcancel the event"})
@Since("2.0, 2.7 (support for script commands)")
@Events("command")
public class ExprCommand extends SimpleExpression<String> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprCommand.class, String.class, ExpressionType.SIMPLE,
				"[the] (full|complete|whole) command",
				"[the] command [(label|alias)]"
		);
	}

	private boolean fullCommand;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		fullCommand = matchedPattern == 0;
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class, ScriptCommandEvent.class);
	}
	
	@Override
	@Nullable
	protected String[] get(final Event e) {
		final String s;

		if (e instanceof PlayerCommandPreprocessEvent) {
			s = ((PlayerCommandPreprocessEvent) e).getMessage().substring(1).trim();
		} else if (e instanceof ServerCommandEvent) {
			s = ((ServerCommandEvent) e).getCommand().trim();
		} else { // It's a script command event
			ScriptCommandEvent event = (ScriptCommandEvent) e;
			s = event.getCommandLabel() + " " + event.getArgsString();
		}

		if (fullCommand) {
			return new String[]{s};
		} else {
			int c = s.indexOf(' ');
			return new String[] {c == -1 ? s : s.substring(0, c)};
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
	public String toString(@Nullable Event e, boolean debug) {
		return fullCommand ? "the full command" : "the command";
	}

}
