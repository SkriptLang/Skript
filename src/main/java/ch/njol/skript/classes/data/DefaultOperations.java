package ch.njol.skript.classes.data;

import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.util.Utils;
import ch.njol.util.Math2;
import org.bukkit.util.Vector;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;

public class DefaultOperations {

	static {
		// Number - Number
		Arithmetics.registerOperation(Operator.ADDITION, Number.class, (left, right) -> {
			if (Utils.isInteger(left, right))
				return left.longValue() + right.longValue();
			return left.doubleValue() + right.doubleValue();
		});
		Arithmetics.registerOperation(Operator.SUBTRACTION, Number.class, (left, right) -> {
			if (Utils.isInteger(left, right))
				return left.longValue() - right.longValue();
			return left.doubleValue() - right.doubleValue();
		});
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Number.class, (left, right) -> {
			if (Utils.isInteger(left, right))
				return left.longValue() * right.longValue();
			return left.doubleValue() * right.doubleValue();
		});
		Arithmetics.registerOperation(Operator.DIVISION, Number.class, (left, right) -> left.doubleValue() / right.doubleValue());
		Arithmetics.registerOperation(Operator.EXPONENTIATION, Number.class, (left, right) -> Math.pow(left.doubleValue(), right.doubleValue()));
		Arithmetics.registerDifference(Number.class, (left, right) -> {
			if (Utils.isInteger(left, right))
				return Math.abs(left.longValue() - right.longValue());
			return Math.abs(left.doubleValue() - right.doubleValue());
		});
		Arithmetics.registerDefaultValue(Number.class, () -> 0L);

		// Vector - Vector
		Arithmetics.registerOperation(Operator.ADDITION, Vector.class, (left, right) -> left.clone().add(right));
		Arithmetics.registerOperation(Operator.SUBTRACTION, Vector.class, (left, right) -> left.clone().subtract(right));
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Vector.class, (left, right) -> left.clone().multiply(right));
		Arithmetics.registerOperation(Operator.DIVISION, Vector.class, (left, right) -> left.clone().divide(right));
		Arithmetics.registerDifference(Vector.class,
			(left, right) -> new Vector(Math.abs(left.getX() - right.getX()), Math.abs(left.getY() - right.getY()), Math.abs(left.getZ() - right.getZ())));
		Arithmetics.registerDefaultValue(Vector.class, Vector::new);

		// Vector - Number
		// Number - Vector
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Vector.class, Number.class, (left, right) -> left.clone().multiply(right.doubleValue()), (left, right) -> {
			double number = left.doubleValue();
			Vector leftVector = new Vector(number, number, number);
			return leftVector.multiply(right);
		});
		Arithmetics.registerOperation(Operator.DIVISION, Vector.class, Number.class, (left, right) -> {
			double number = right.doubleValue();
			Vector rightVector = new Vector(number, number, number);
			return left.clone().divide(rightVector);
		}, (left, right) -> {
			double number = left.doubleValue();
			Vector leftVector = new Vector(number, number, number);
			return leftVector.divide(right);
		});

		// Timespan - Timespan
		Arithmetics.registerOperation(Operator.ADDITION, Timespan.class, (left, right) -> new Timespan(Math2.addClamped(left.getAs(TimePeriod.MILLISECOND), right.getAs(TimePeriod.MILLISECOND))));
		Arithmetics.registerOperation(Operator.SUBTRACTION, Timespan.class, (left, right) -> new Timespan(Math.max(0, left.getAs(TimePeriod.MILLISECOND) - right.getAs(TimePeriod.MILLISECOND))));
		Arithmetics.registerDifference(Timespan.class, (left, right) -> new Timespan(Math.abs(left.getAs(TimePeriod.MILLISECOND) - right.getAs(TimePeriod.MILLISECOND))));
		Arithmetics.registerDefaultValue(Timespan.class, Timespan::new);

		// Timespan - Number
		// Number - Timespan
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Timespan.class, Number.class, (left, right) -> {
			long scalar = right.longValue();
			if (scalar < 0)
				return null;
			return new Timespan(Math2.multiplyClamped(left.getAs(TimePeriod.MILLISECOND), scalar));
		}, (left, right) -> {
			long scalar = left.longValue();
			if (scalar < 0)
				return null;
			return new Timespan(scalar * right.getAs(TimePeriod.MILLISECOND));
		});
		Arithmetics.registerOperation(Operator.DIVISION, Timespan.class, Number.class, (left, right) -> {
			long scalar = right.longValue();
			if (scalar <= 0)
				return null;
			return new Timespan(left.getAs(TimePeriod.MILLISECOND) / scalar);
		});

		// Timespan / Timespan = Number
		Arithmetics.registerOperation(Operator.DIVISION, Timespan.class, Timespan.class, Number.class,
				(left, right) -> left.getAs(TimePeriod.MILLISECOND) / (double) right.getAs(TimePeriod.MILLISECOND));

		// Date - Timespan
		Arithmetics.registerOperation(Operator.ADDITION, Date.class, Timespan.class, Date::plus);
		Arithmetics.registerOperation(Operator.SUBTRACTION, Date.class, Timespan.class, Date::minus);
		Arithmetics.registerDifference(Date.class, Timespan.class, Date::difference);

		// String - String
		Arithmetics.registerOperation(Operator.ADDITION, String.class, String.class, String::concat);

	}

}
