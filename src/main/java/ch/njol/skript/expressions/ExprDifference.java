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
import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.ClassInfo;
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
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Difference")
@Description("The difference between two values, e.g. <a href='../classes.html#number'>numbers</a>, <a href='../classes/#date'>dates</a> or <a href='../classes/#time'>times</a>.")
@Examples({"if difference between {command::%player%::lastuse} and now is smaller than a minute:",
		"\tmessage \"You have to wait a minute before using this command again!\""})
@Since("1.4")
public class ExprDifference extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprDifference.class, Object.class, ExpressionType.COMBINED, "difference (between|of) %object% and %object%");
	}
	
	@SuppressWarnings("null")
	private Expression<?> first, second;
	
	@SuppressWarnings("rawtypes")
	@Nullable
	private Arithmetic math;
	@SuppressWarnings("null")
	private Class<?> relativeType;
	
	@SuppressWarnings({"unchecked", "null", "unused"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		first = exprs[0];
		second = exprs[1];
		final ClassInfo<?> ci;
		if (first instanceof Variable && second instanceof Variable) {
			ci = DefaultClasses.OBJECT;
		} else if (first instanceof Literal<?> && second instanceof Literal<?>) {
			first = first.getConvertedExpression(Object.class);
			second = second.getConvertedExpression(Object.class);
			if (first == null || second == null)
				return false;
			ci = Classes.getSuperClassInfo(Utils.getSuperType(first.getReturnType(), second.getReturnType()));
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
			ci = Classes.getSuperClassInfo(Utils.getSuperType(first.getReturnType(), second.getReturnType()));
		}
		assert ci != null;
		if (!ci.getC().equals(Object.class) && ci.getMath() == null) {
			Skript.error("Can't get the difference of " + CondCompare.f(first) + " and " + CondCompare.f(second), ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (ci.getC().equals(Object.class)) {
			// Initialize less stuff, basically
			relativeType = Object.class; // Relative math type would be null which the parser doesn't like
		} else {
			math = ci.getMath();
			relativeType = ci.getMathRelativeType();
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	protected Object[] get(final Event e) {
		final Object f = first.getSingle(e), s = second.getSingle(e);
		if (f == null || s == null)
			return null;
		final Object[] one = (Object[]) Array.newInstance(relativeType, 1);
		
		// If we're comparing object expressions, such as variables, math is null right now
		if (relativeType.equals(Object.class)) {
			ClassInfo<?> info = Classes.getSuperClassInfo(Utils.getSuperType(f.getClass(), s.getClass()));
			math = info.getMath();
			if (math == null) { // User did something stupid, just return <none> for them
				return one;
			}
		}
		
		assert math != null; // NOW it cannot be null
		one[0] = math.difference(f, s);
		
		return one;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return relativeType;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "difference between " + first.toString(e, debug) + " and " + second.toString(e, debug);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
