/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.command.ScriptCommandEvent;
import org.bukkit.event.Event;
import org.bukkit.event.command.UnknownCommandEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.eclipse.jdt.annotation.Nullable;

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

@Name("Command")
@Description("The command that caused an 'on command' event (excluding the leading slash and all arguments)")
@Examples({
	"# prevents any commands except for the /exit command during some game",
	"on command:",
	"\tif {game::%player%::playing} is true:",
	"\t\tif the command is not \"exit\":",
	"\t\t\tmessage \"You're not allowed to use commands during the game\"",
	"\t\t\tcancel the event"})
@Since("2.0, INSERT VERSION (support for script commands, unknown command event)")
@Events({"command", "unknown command"})
public class ExprCommand extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprCommand.class, String.class, ExpressionType.SIMPLE,
			"[the] (full|complete|whole) command",
			"[the] command [(label|alias)]"
		);
	}

	private boolean fullCommand;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class, ScriptCommandEvent.class, UnknownCommandEvent.class)) {
			Skript.error("The 'command' expression can only be used in a script command or command event");
			return false;
		}
		fullCommand = matchedPattern == 0;
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(Event event) {
		final String command;

		if (event instanceof PlayerCommandPreprocessEvent) {
			command = ((PlayerCommandPreprocessEvent) event).getMessage().substring(1).trim();
		} else if (event instanceof ServerCommandEvent) {
			command = ((ServerCommandEvent) event).getCommand().trim();
		} else if (event instanceof UnknownCommandEvent) {
			command = ((UnknownCommandEvent) event).getCommandLine().trim();
		} else { // It's a script command event
			ScriptCommandEvent evt = (ScriptCommandEvent) event;
			command = evt.getCommandLabel() + " " + evt.getArgsString();
		}

		if (fullCommand) {
			return new String[]{command};
		} else {
			int cmdIndex = command.indexOf(' ');
			return new String[] {cmdIndex == -1 ? command : command.substring(0, cmdIndex)};
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
	@Nullable
	public String toString(Event event, boolean debug) {
		return fullCommand ? "the full command" : "the command";
	}
	
}
