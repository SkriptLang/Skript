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

import org.bukkit.event.Event;
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
@Since("2.0")
@Events("command")
public class ExprCommand extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprCommand.class, String.class, ExpressionType.SIMPLE,
				"[the] (full|complete|whole) command", "[the] command [label]", "[the] arguments");
	}
	
	private final static int FULL = 0, LABEL = 1, ARGS = 2;
	private int what;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		what = matchedPattern;
		if (!getParser().isCurrentEvent(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class)) {
			if (what != ARGS) // ExprArgument has the same syntax
				Skript.error("The 'command' expression can only be used in a command event");
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(final Event e) {
		final String s;
		if (e instanceof PlayerCommandPreprocessEvent) {
			s = ((PlayerCommandPreprocessEvent) e).getMessage().substring(1).trim();
		} else if (e instanceof ServerCommandEvent) {
			s = ((ServerCommandEvent) e).getCommand().trim();
		} else {
			return new String[0];
		}
		if (what == FULL)
			return new String[] {s};
		final int c = s.indexOf(' ');
		if (what == ARGS) {
			if (c == -1)
				return new String[0];
			return new String[] {s.substring(c + 1).trim()};
		}
		assert what == LABEL;
		return new String[] {c == -1 ? s : s.substring(0, c)};
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
	public String toString(final @Nullable Event e, final boolean debug) {
		return what == 0 ? "the full command" : what == 1 ? "the command" : "the arguments";
	}
	
}
