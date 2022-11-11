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
package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.arithmetic.Operator;
import ch.njol.skript.hooks.economy.classes.Money;
import ch.njol.skript.registrations.Arithmetics;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetic;

public class DefaultArithmetics {

	public DefaultArithmetics() {}

	static {

		// Number
		Arithmetics.registerArithmetic(Number.class, new Arithmetic<Number>() {

			@Override
			public Class<?> @Nullable [] acceptOperator(Operator operator) {
				return CollectionUtils.array(Number.class);
			}

			@Override
			public Number calculate(Number first, Operator operator, Object second) {
				double one = first.doubleValue();
				double two = ((Number) second).doubleValue();
				double result = 0;
				switch (operator) {
					case MINUS:
						two *= -1;
					case PLUS:
						result = one + two;
						break;
					case MULT:
						result = one * two;
						break;
					case DIV:
						return one / two;
					case EXP:
						result = Math.pow(one, two);
						break;
				}
				if (result == (long) result)
					return (long) result;
				return result;
			}
		});
		Arithmetics.registerDifference(Number.class, (first, second) -> {
			double result = Math.abs(first.doubleValue() - second.doubleValue());
			if (result == (long) result)
				return (long) result;
			return result;
		});


		// Vector
		Arithmetics.registerArithmetic(Vector.class, new Arithmetic<Vector>() {

			@Override
			public Class<?> @Nullable [] acceptOperator(Operator operator) {
				switch (operator) {
					case PLUS:
					case MINUS:
						return CollectionUtils.array(Vector.class);
					case MULT:
					case DIV:
						return CollectionUtils.array(Vector.class, Number.class);
					default:
						return null;
				}
			}

			@Override
			public Vector calculate(Vector first, Operator operator, Object second) {
				Vector vector;
				if (second instanceof Number) {
					double d = ((Number) second).doubleValue();
					vector = new Vector(d, d, d);
				} else {
					vector = (Vector) second;
				}
				Vector result;
				switch (operator) {
					case PLUS:
						result = first.add(vector);
						break;
					case MINUS:
						result = first.subtract(vector);
						break;
					case MULT:
						result = first.multiply(vector);
						break;
					case DIV:
						result = first.divide(vector);
						break;
					default:
						result = new Vector();
						break;
				}
				return result;
			}
		});
		Arithmetics.registerDifference(Vector.class,
			(first, second) -> new Vector(Math.abs(first.getX() - second.getX()), Math.abs(first.getY() - second.getY()), Math.abs(first.getZ() - second.getZ())));


		// Timespan
		Arithmetics.registerArithmetic(Timespan.class, new Arithmetic<Timespan>() {

			@Override
			public Class<?> @Nullable [] acceptOperator(Operator operator) {
				switch (operator) {
					case PLUS:
					case MINUS:
						return CollectionUtils.array(Timespan.class);
					default:
						return null;
				}
			}

			@Override
			public Timespan calculate(Timespan first, Operator operator, Object second) {
				Timespan timespan = (Timespan) second;
				Timespan result;
				switch (operator) {
					case PLUS:
						result = new Timespan(first.getMilliSeconds() + timespan.getMilliSeconds());
						break;
					case MINUS:
						result = new Timespan(Math.max(0, first.getMilliSeconds() - timespan.getMilliSeconds()));
						break;
					default:
						result = new Timespan();
						break;
				}
				return result;
			}
		});
		Arithmetics.registerDifference(Timespan.class, ((first, second) -> new Timespan(Math.abs(first.getMilliSeconds() - second.getMilliSeconds()))));


		// Date
		Arithmetics.registerArithmetic(Date.class, new Arithmetic<Date>() {
			@Override
			public Class<?> @Nullable [] acceptOperator(Operator operator) {
				switch (operator) {
					case PLUS:
					case MINUS:
						return CollectionUtils.array(Timespan.class);
					default:
						return null;
				}
			}

			@Override
			public Date calculate(Date first, Operator operator, Object second) {
				Timespan timespan = (Timespan) second;
				Date result;
				switch (operator) {
					case PLUS:
						result = new Date(first.getTimestamp() + timespan.getMilliSeconds());
						break;
					case MINUS:
						result = new Date(first.getTimestamp() - timespan.getMilliSeconds());
						break;
					default:
						result = new Date();
						break;
				}
				return result;
			}
		});
		Arithmetics.registerDifference(Date.class, Timespan.class, Date::difference);


		// Money
		Arithmetics.registerArithmetic(Money.class, new Arithmetic<Money>() {
			@Override
			public Class<?> @Nullable [] acceptOperator(Operator operator) {
				return operator == Operator.EXP ? null : CollectionUtils.array(Money.class);
			}

			@Override
			public Money calculate(Money first, Operator operator, Object second) {
				Money money = (Money) second;
				Money result;
				switch (operator) {
					case PLUS:
						result = new Money(first.getAmount() + money.getAmount());
						break;
					case MINUS:
						result = new Money(first.getAmount() - money.getAmount());
						break;
					case MULT:
						result = new Money(first.getAmount() * money.getAmount());
						break;
					case DIV:
						result = new Money(first.getAmount() / money.getAmount());
						break;
					default:
						result = new Money(first.getAmount());
						break;
				}
				return result;
			}
		});
		Arithmetics.registerDifference(Money.class, (first, second) -> {
			double result = Math.abs(first.getAmount() - second.getAmount());
			if (result < Skript.EPSILON)
				return new Money(0);
			return new Money(result);
		});

	}

}
