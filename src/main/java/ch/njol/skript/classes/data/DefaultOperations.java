package ch.njol.skript.classes.data;

import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.util.Utils;
import org.joml.Vector3d;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;

public class DefaultOperations {

	static {
		// Number - Number
		Arithmetics.registerOperation(Operator.ADDITION, Number.class, (left, right) -> {
			if (Utils.isInteger(left, right)) {
				long result = left.longValue() + right.longValue();
				// catches overflow, from Math.addExact(long, long)
				if (((left.longValue() ^ result) & (right.longValue() ^ result)) >= 0)
					return result;
			}
			return left.doubleValue() + right.doubleValue();
		});
		Arithmetics.registerOperation(Operator.SUBTRACTION, Number.class, (left, right) -> {
			if (Utils.isInteger(left, right)) {
				long result = left.longValue() - right.longValue();
				// catches overflow, from Math.addExact(long, long)
				if (((left.longValue() ^ result) & (right.longValue() ^ result)) >= 0)
					return result;
			}
			return left.doubleValue() - right.doubleValue();
		});
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Number.class, (left, right) -> {
			if (!Utils.isInteger(left, right))
				return left.doubleValue() * right.doubleValue();

			// catch overflow, from Math.multiplyExact(long, long)
			long longLeft = left.longValue();
			long longRight = right.longValue();
			long ax = Math.abs(longLeft);
			long ay = Math.abs(longRight);

			long result = left.longValue() * right.longValue();

			if (((ax | ay) >>> 31 != 0)) {
				// Some bits greater than 2^31 that might cause overflow
				// Check the result using the divide operator
				// and check for the special case of Long.MIN_VALUE * -1
				if (((longRight != 0) && (result / longRight != longLeft)) ||
					(longLeft == Long.MIN_VALUE && longRight == -1)) {
					return left.doubleValue() * right.doubleValue();
				}
			}
			return result;
		});
		Arithmetics.registerOperation(Operator.DIVISION, Number.class, (left, right) -> left.doubleValue() / right.doubleValue());
		Arithmetics.registerOperation(Operator.EXPONENTIATION, Number.class, (left, right) -> Math.pow(left.doubleValue(), right.doubleValue()));
		Arithmetics.registerDifference(Number.class, (left, right) -> {
			double result = Math.abs(left.doubleValue() - right.doubleValue());
			if (Utils.isInteger(left, right) && result < Long.MAX_VALUE && result > Long.MIN_VALUE)
				return (long) result;
			return result;
		});
		Arithmetics.registerDefaultValue(Number.class, () -> 0L);

		// Vector - Vector
		Arithmetics.registerOperation(Operator.ADDITION, Vector3d.class, (left, right) -> left.add(right, new Vector3d()));
		Arithmetics.registerOperation(Operator.SUBTRACTION, Vector3d.class, (left, right) -> left.sub(right, new Vector3d()));
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Vector3d.class, (left, right) -> left.mul(right, new Vector3d()));
		Arithmetics.registerOperation(Operator.DIVISION, Vector3d.class, (left, right) -> left.div(right, new Vector3d()));
		Arithmetics.registerDifference(Vector3d.class,
			(left, right) -> new Vector3d(Math.abs(left.x() - right.x()), Math.abs(left.y() - right.y()), Math.abs(left.z() - right.z())));
		Arithmetics.registerDefaultValue(Vector3d.class, Vector3d::new);

		// Vector - Number
		// Number - Vector
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Vector3d.class, Number.class,
			(left, right) -> left.mul(right.doubleValue(), new Vector3d()),
			(left, right) -> right.mul(left.doubleValue(), new Vector3d()));
		Arithmetics.registerOperation(Operator.DIVISION, Vector3d.class, Number.class,
			(left, right) -> left.div(right.doubleValue(), new Vector3d()),
			(left, right) -> new Vector3d(left.doubleValue()).div(right));

		// Timespan - Timespan
		Arithmetics.registerOperation(Operator.ADDITION, Timespan.class, Timespan::add);
		Arithmetics.registerOperation(Operator.SUBTRACTION, Timespan.class, Timespan::subtract);
		Arithmetics.registerDifference(Timespan.class, Timespan::difference);
		Arithmetics.registerDefaultValue(Timespan.class, Timespan::new);

		// Timespan - Number
		// Number - Timespan
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Timespan.class, Number.class, (left, right) -> {
			double scalar = right.doubleValue();
			if (scalar < 0 || !Double.isFinite(scalar))
				return null;
			double value = left.getAs(TimePeriod.MILLISECOND) * scalar;
			return new Timespan((long) Math.min(value, Long.MAX_VALUE));
		}, (left, right) -> {
			double scalar = left.doubleValue();
			if (scalar < 0 || !Double.isFinite(scalar))
				return null;
			double value = right.getAs(TimePeriod.MILLISECOND) * scalar;
			return new Timespan((long) Math.min(value, Long.MAX_VALUE));
		});
		Arithmetics.registerOperation(Operator.DIVISION, Timespan.class, Number.class, (left, right) -> {
			double scalar = right.doubleValue();
			if (scalar <= 0 || !Double.isFinite(scalar))
				return null;
			double value = left.getAs(TimePeriod.MILLISECOND) / scalar;
			return new Timespan((long) Math.min(value, Long.MAX_VALUE));
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
