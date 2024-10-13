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
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;

@Name("Now")
@Description("The current <a href='classes.html#date'>system time</a> of the server. Use <a href='#ExprTime'>time</a> to get the <a href='classes.html#time'>Minecraft time</a> of a world.")
@Examples({"broadcast \"Current server time: %now%\""})
@Since("1.4")
public class ExprNow extends SimpleExpression<Date> {
	
	static {
		Skript.registerExpression(ExprNow.class, Date.class, ExpressionType.SIMPLE, "now");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		return true;
	}
	
	@Override
	protected Date[] get(final Event e) {
		return new Date[] {new Date()};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "now";
	}
	
}
