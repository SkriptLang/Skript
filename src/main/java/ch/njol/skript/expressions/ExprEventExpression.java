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
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * Provided for convenience: one can write 'event-world' instead of only 'world' to distinguish between the event-world and the loop-world.
 * 
 * @author Peter Güttinger
 */
@NoDoc
public class ExprEventExpression extends WrapperExpression<Object> {
	static {
		Skript.registerExpression(ExprEventExpression.class, Object.class, ExpressionType.PROPERTY, "[the] event-%*classinfo%");// property so that it is parsed after most other expressions
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		@SuppressWarnings("unchecked")
		final ClassInfo<?> ci = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
		final EventValueExpression<?> e = new EventValueExpression<Object>(ci.getC());
		setExpr(e);
		return e.init();
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return getExpr().toString(event, debug);
	}
	
}
