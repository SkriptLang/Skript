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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Random")
@Description("Gets a random item out of a set, e.g. a random player out of all players online.")
@Examples({
	"give a diamond to a random player out of all players",
	"give a random item out of all items to the player"
})
@Since("1.4.9")
public class ExprRandom extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprRandom.class, Object.class, ExpressionType.COMBINED, "[a] random %*classinfo% [out] of %objects%");
	}

	private Expression<?> expr;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (LiteralUtils.hasUnparsedLiteral(exprs[1])) {
			expr = LiteralUtils.defendExpression(exprs[1]);
			if (expr instanceof ExpressionList) {
				Class<?> type = (((Literal<ClassInfo<?>>) exprs[0]).getSingle()).getC();
				List<Expression<?>> list = Arrays.stream(((ExpressionList<?>) expr).getExpressions())
						.map(expression -> (Expression<?>) expression.getConvertedExpression(type))
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
				if (list.isEmpty()) {
					Skript.error("There are no objects of type '" + exprs[0].toString() + "' in the list " + exprs[1].toString());
					return false;
				}
				expr = CollectionUtils.getRandom(list);
			}
		} else {
			expr = exprs[1].getConvertedExpression((((Literal<ClassInfo<?>>) exprs[0]).getSingle()).getC());
		}
		if (expr == null)
			return false;
		return true;
	}

	@Override
	protected Object[] get(Event event) {
		Object[] set = expr.getAll(event);
		if (set.length <= 1)
			return set;
		Object[] one = (Object[]) Array.newInstance(set.getClass().getComponentType(), 1);
		one[0] = CollectionUtils.getRandom(set);
		return one;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Object> getReturnType() {
		return expr.getReturnType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a random element out of " + expr.toString(event, debug);
	}

}
