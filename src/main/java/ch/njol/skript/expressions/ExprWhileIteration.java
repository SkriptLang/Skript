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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.sections.SecWhile;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("While Loop Iteration")
@Description("Returns the while loop's current iteration count.")
@Examples({"while player is online:",
		"\tgive player 1 stone",
		"\twait 5 ticks",
		"\tif current iteration > 30:",
		"\t\tstop loop"})
@Since("INSERT VERSION")
public class ExprWhileIteration extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprWhileIteration.class, Number.class, ExpressionType.SIMPLE, "[the] [current] [loop] iteration [count[er]]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	SecWhile loop;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		loop = getParser().getCurrentSection(SecWhile.class);
		return true;
	}

	@Override
	protected Number[] get(Event e) {
		return new Number[] { loop.getWalkCounter() };
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "iteration counter of " + loop.toString(e, debug);
	}
	
}
