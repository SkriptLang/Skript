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
import org.bukkit.event.player.PlayerLoginEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Hostname")
@Description("The hostname used by the connecting player to connect to the server in a <a href='events.html#connect'>connect</a> event.")
@Examples({
		"on connect:",
		"\thostname is \"testers.example.com\"",
		"\tsend \"Welcome back tester!\""})
@Since("2.6.1")
public class ExprHostname extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprHostname.class, String.class, ExpressionType.SIMPLE, "[the] (host|domain)[ ][name]");
	}

	@Override
	@SuppressWarnings({"null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerLoginEvent.class)) {
			Skript.error("The hostname expression must be used in a player connect event");
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(Event e) {
		return new String[] {((PlayerLoginEvent) e).getHostname()};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}	
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "hostname";
	}
	
}
