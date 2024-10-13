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
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Former/Future State")
@Description({"Represents the value of an expression before an event happened or the value it will have directly after the event, e.g. the old or new level respectively in a <a href='./events.html#level_change'>level change event</a>.",
		"Note: The past, future and present states of an expression are sometimes called 'time states' of an expression.",
		"Note 2: If you don't specify whether to use the past or future state of an expression that has different values, its default value will be used which is usually the value after the event."})
@Examples({"on teleport:",
		"	former world was \"world_nether\" # or 'world was'",
		"	world will be \"world\" # or 'world after the event is'",
		"on tool change:",
		"	past tool is an axe",
		"	the tool after the event will be air",
		"on weather change:",
		"	set {weather::%world%::old} to past weather",
		"	set {weather::%world%::current} to the new weather"})
@Since("1.1")
public class ExprTimeState extends WrapperExpression<Object> {
	static {
		Skript.registerExpression(ExprTimeState.class, Object.class, ExpressionType.PROPERTY,
				"[the] (former|past|old) [state] [of] %~object%", "%~object% before [the event]",
				"[the] (future|to-be|new) [state] [of] %~object%", "%~object%(-to-be| after[(wards| the event)])");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		final Expression<?> expr = exprs[0];
		if (isDelayed == Kleenean.TRUE) {
			Skript.error("Cannot use time states after the event has already passed", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (!expr.setTime(matchedPattern >= 2 ? 1 : -1)) {
			Skript.error(expr + " does not have a " + (matchedPattern >= 2 ? "future" : "past") + " state", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		setExpr(expr);
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + (getTime() == -1 ? "past" : "future") + " state of " + getExpr().toString(e, debug);
	}
	
	@Override
	public boolean setTime(final int time) {
		return time == getTime();
	}
	
}
