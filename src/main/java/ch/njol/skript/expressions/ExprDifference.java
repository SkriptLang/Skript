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

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.lang.arithmetic.DifferenceInfo;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;

@Name("Difference")
@Description("The difference between two values, e.g. <a href='./classes.html#number'>numbers</a>, <a href='./classes/#date'>dates</a> or <a href='./classes/#time'>times</a>.")
@Examples({
	"if difference between {command::%player%::lastuse} and now is smaller than a minute:",
		"\tmessage \"You have to wait a minute before using this command again!\""
})
@Since("1.4")
public class ExprDifference extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprDifference.class, Object.class, ExpressionType.COMBINED, "[the] difference (between|of) %object% and %object%");
	}
	
	private Expression<?> first, second;
	
	@Nullable
	@SuppressWarnings("rawtypes")
	private DifferenceInfo differenceInfo;
	@SuppressWarnings("null")
	private Class<?> returnType;
	
	@Override
	@SuppressWarnings({"unchecked", "unused", "ConstantConditions"})
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		first = exprs[0];
		second = exprs[1];
		final Class<?> returnType;
		if (first instanceof Variable && second instanceof Variable) {
			returnType = Object.class;
		} else if (first instanceof Literal<?> && second instanceof Literal<?>) {
			first = first.getConvertedExpression(Object.class);
			second = second.getConvertedExpression(Object.class);
			if (first == null || second == null)
				return false;
			returnType = Utils.getSuperType(first.getReturnType(), second.getReturnType());
		} else {
			if (first instanceof Literal<?>) {
				first = first.getConvertedExpression(second.getReturnType());
				if (first == null)
					return false;
			} else if (second instanceof Literal<?>) {
				second = second.getConvertedExpression(first.getReturnType());
				if (second == null)
					return false;
			}
			if (first instanceof Variable) {
				first = first.getConvertedExpression(second.getReturnType());
			} else if (second instanceof Variable) {
				second = second.getConvertedExpression(first.getReturnType());
			}
			assert first != null && second != null;
			returnType = Utils.getSuperType(first.getReturnType(), second.getReturnType());
		}
		assert returnType != null;

		if (!returnType.equals(Object.class) && (differenceInfo = Arithmetics.getDifferenceInfo(returnType)) == null) {
			Skript.error("Can't get the difference of " + CondCompare.f(first) + " and " + CondCompare.f(second), ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (returnType.equals(Object.class)) {
			// Initialize less stuff, basically
			this.returnType = Object.class; // Return type would be null, which the parser doesn't like
		} else {
			this.returnType = differenceInfo.getReturnType();
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	protected Object[] get(Event event) {
		final Object first = this.first.getSingle(event), second = this.second.getSingle(event);
		if (first == null || second == null)
			return null;
		final Object[] one = (Object[]) Array.newInstance(returnType, 1);
		
		// If we're comparing object expressions, such as variables, difference info is null right now
		if (returnType.equals(Object.class)) {
			Class<?> returnType = Utils.getSuperType(first.getClass(), second.getClass());
			differenceInfo = Arithmetics.getDifferenceInfo(returnType);
			if (differenceInfo == null) { // User did something stupid, just return <none> for them
				return one;
			}
		}

		assert differenceInfo != null; // NOW it cannot be null
		one[0] = differenceInfo.getOperation().calculate(first, second);

		return one;
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "difference between " + first.toString(event, debug) + " and " + second.toString(event, debug);
	}

}
