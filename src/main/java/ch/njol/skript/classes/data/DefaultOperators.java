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
import ch.njol.skript.hooks.economy.classes.Money;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import org.bukkit.util.Vector;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;

public class DefaultOperators {

	static {

		Operator.ADDITION.addHandler(Number.class, (left, right) -> {
			if (Utils.isInteger(left, right))
				return left.longValue() + right.longValue();
			return left.doubleValue() + right.doubleValue();
		});
		Operator.SUBTRACTION.addHandler(Number.class, (left, right) -> {
			if (Utils.isInteger(left, right))
				return left.longValue() - right.longValue();
			return left.doubleValue() - right.doubleValue();
		});
		Operator.MULTIPLICATION.addHandler(Number.class, (left, right) -> {
			if (Utils.isInteger(left, right))
				return left.longValue() * right.longValue();
			return left.doubleValue() * right.doubleValue();
		});
		Operator.DIVISION.addHandler(Number.class, (left, right) -> left.doubleValue() / right.doubleValue());
		Operator.EXPONENTIATION.addHandler(Number.class, (left, right) -> {
			if (Utils.isInteger(left, right))
				return (long) Math.pow(left.longValue(), right.longValue());
			return Math.pow(left.doubleValue(), right.doubleValue());
		});
		Arithmetics.registerDifference(Number.class, (left, right) -> {
			if (Utils.isInteger(left, right))
				return Math.abs(left.longValue() - right.longValue());
			return Math.abs(left.doubleValue() - right.doubleValue());
		});
		Arithmetics.registerDefaultValue(Number.class, 0L);

		Operator.ADDITION.addHandler(Vector.class, (left, right) -> left.clone().add(right));
		Operator.SUBTRACTION.addHandler(Vector.class, (left, right) -> left.clone().subtract(right));
		Operator.MULTIPLICATION.addHandler(Vector.class, (left, right) -> left.clone().multiply(right));
		Operator.MULTIPLICATION.addHandler(Vector.class, Number.class, (left, right) -> left.clone().multiply(right.doubleValue()), (left, right) -> {
			double number = left.doubleValue();
			Vector leftVector = new Vector(number, number, number);
			return leftVector.multiply(right);
		});
		Operator.DIVISION.addHandler(Vector.class, (left, right) -> left.clone().divide(right));
		Operator.DIVISION.addHandler(Vector.class, Number.class, (left, right) -> {
			double number = right.doubleValue();
			Vector rightVector = new Vector(number, number, number);
			return left.clone().divide(rightVector);
		}, (left, right) -> {
			double number = left.doubleValue();
			Vector leftVector = new Vector(number, number, number);
			return leftVector.divide(right);
		});
		Arithmetics.registerDifference(Vector.class,
			(left, right) -> new Vector(Math.abs(left.getX() - right.getX()), Math.abs(left.getY() - right.getY()), Math.abs(left.getZ() - right.getZ())));
		Arithmetics.registerDefaultValue(Vector.class, new Vector());

		Operator.ADDITION.addHandler(Timespan.class, (left, right) -> new Timespan(left.getMilliSeconds() + right.getMilliSeconds()));
		Operator.SUBTRACTION.addHandler(Timespan.class, (left, right) -> new Timespan(Math.max(0, left.getMilliSeconds() - right.getMilliSeconds())));
		Arithmetics.registerDifference(Timespan.class, (left, right) -> new Timespan(Math.abs(left.getMilliSeconds() - right.getMilliSeconds())));
		Arithmetics.registerDefaultValue(Timespan.class, new Timespan());

		Operator.ADDITION.addHandler(Date.class, Timespan.class, Date::plus);
		Operator.SUBTRACTION.addHandler(Date.class, Timespan.class, Date::minus);
		Arithmetics.registerDifference(Date.class, Timespan.class, Date::difference);

		Operator.ADDITION.addHandler(Money.class, (left, right) -> new Money(left.getAmount() + right.getAmount()));
		Operator.SUBTRACTION.addHandler(Money.class, (left, right) -> new Money(left.getAmount() - right.getAmount()));
		Operator.MULTIPLICATION.addHandler(Money.class, (left, right) -> new Money(left.getAmount() * right.getAmount()));
		Operator.DIVISION.addHandler(Money.class, (left, right) -> new Money(left.getAmount() / right.getAmount()));
		Arithmetics.registerDifference(Money.class, (left, right) -> {
			double result = Math.abs(left.getAmount() - right.getAmount());
			if (result < Skript.EPSILON)
				return new Money(0);
			return new Money(result);
		});
		Arithmetics.registerDefaultValue(Money.class, new Money(0));

	}

}
