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
import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.DifferenceInfo;

import java.lang.reflect.Array;

@Name("Difference")
@Description({
	"The difference between two values",
	"Supported types include <a href='./classes.html#number'>numbers</a>, <a href='./classes/#date'>dates</a> and <a href='./classes/#time'>times</a>."
})
@Examples({
	"if difference between {command::%player%::lastuse} and now is smaller than a minute:",
		"\tmessage \"You have to wait a minute before using this command again!\""
})
@Since("1.4")
public class ExprDifference extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprDifference.class, Object.class, ExpressionType.COMBINED,
				"difference (between|of) %object% and %object%"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> first, second;

	@Nullable
	@SuppressWarnings("rawtypes")
	private DifferenceInfo differenceInfo;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Expression<?> first = LiteralUtils.defendExpression(exprs[0]);
		Expression<?> second = LiteralUtils.defendExpression(exprs[1]);
		if (!LiteralUtils.canInitSafely(first, second)) {
			return false;
		}

		Class<?> firstReturnType = first.getReturnType();
		Class<?> secondReturnType = second.getReturnType();
		Class<?> superType = Utils.getSuperType(firstReturnType, secondReturnType);

		boolean fail = false;

		if (superType == Object.class && (firstReturnType != Object.class || secondReturnType != Object.class)) {
			// We may not have a way to obtain the difference between these two values. Further checks needed.

			// These two types are unrelated, meaning conversion is needed
			if (firstReturnType != Object.class && secondReturnType != Object.class) {

				// We will work our way out of failure
				fail = true;

				// Attempt to use first type's math
				differenceInfo = Arithmetics.getDifferenceInfo(firstReturnType);
				if (differenceInfo != null) { // Try to convert second to first
					Expression<?> secondConverted = second.getConvertedExpression(firstReturnType);
					if (secondConverted != null) {
						second = secondConverted;
						fail = false;
					}
				}

				if (fail) { // First type won't work, try second type
					differenceInfo = Arithmetics.getDifferenceInfo(secondReturnType);
					if (differenceInfo != null) { // Try to convert first to second
						Expression<?> firstConverted = first.getConvertedExpression(secondReturnType);
						if (firstConverted != null) {
							first = firstConverted;
							fail = false;
						}
					}
				}

			} else { // It may just be the case that the type of one of our values cannot be known at parse time
				Expression<?> converted;
				if (firstReturnType == Object.class) {
					converted = first.getConvertedExpression(secondReturnType);
					if (converted != null) { // This may fail if both types are Object
						first = converted;
					}
				} else { // This is an else statement to avoid X->Object conversions
					converted = second.getConvertedExpression(firstReturnType);
					if (converted != null) {
						second = converted;
					}
				}

				if (converted == null) { // It's unlikely that these two can be compared
					fail = true;
				} else { // Attempt to resolve a better class info
					superType = Utils.getSuperType(first.getReturnType(), second.getReturnType());
				}
			}

		}

		if (superType != Object.class && (differenceInfo = Arithmetics.getDifferenceInfo(superType)) == null) {
			fail = true;
		}

		if (fail) {
			Skript.error("Can't get the difference of " + CondCompare.f(first) + " and " + CondCompare.f(second));
			return false;
		}

		this.first = first;
		this.second = second;

		return true;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	protected Object[] get(Event event) {
		Object first = this.first.getSingle(event);
		Object second = this.second.getSingle(event);
		if (first == null || second == null) {
			return new Object[0];
		}

		DifferenceInfo differenceInfo = this.differenceInfo;
		if (differenceInfo == null) { // Try to determine now that actual types are known
			Class<?> superType = Utils.getSuperType(first.getClass(), second.getClass());
			differenceInfo = Arithmetics.getDifferenceInfo(superType);
			if (differenceInfo == null) { // User did something stupid, just return <none> for them
				return new Object[0];
			}
		}

		assert differenceInfo != null; // it cannot be null here
		Object[] one = (Object[]) Array.newInstance(differenceInfo.getReturnType(), 1);

		one[0] = differenceInfo.getOperation().calculate(first, second);

		return one;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return differenceInfo == null ? Object.class : differenceInfo.getReturnType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "difference between " + first.toString(event, debug) + " and " + second.toString(event, debug);
	}

}
