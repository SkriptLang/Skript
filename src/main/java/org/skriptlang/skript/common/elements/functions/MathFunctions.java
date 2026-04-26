package org.skriptlang.skript.common.elements.functions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.util.Contract;
import ch.njol.util.Math2;
import ch.njol.util.StringUtils;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Contains all generic math functions.
 */
public class MathFunctions {

	/**
	 * @param n The number.
	 * @return a number in a string representation with at most 4 digits after the decimal point.
	 */
	private static String str(double n) {
		return StringUtils.toString(n, 4);
	}

	static {
		SkriptAddon skript = Skript.instance();

		Functions.register(DefaultFunction.builder(skript, "isNaN", Boolean.class)
				.description("Returns true if the input is NaN (not a number).")
				.examples("isNaN(0) # false", "isNaN(0/0) # true", "isNaN(sqrt(-1)) # true")
				.since("2.8.0")
				.parameter("n", Number.class)
				.build(args -> Double.isNaN(args.<Number>get("n").doubleValue())));

		Functions.register(DefaultFunction.builder(skript, "floor", Long.class)
				.description("Rounds a number down, i.e. returns the closest integer smaller than or equal to the argument.")
				.examples("floor(2.34) = 2", "floor(2) = 2", "floor(2.99) = 2")
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> {
					Number value = args.get("n");

					if (value instanceof Long l)
						return l;

					return Math2.floor(value.doubleValue());
				}));

		Functions.register(DefaultFunction.builder(skript, "round", Number.class)
				.description("Rounds a number, i.e. returns the closest integer to the argument. Place a second argument to define the decimal placement.")
				.examples("round(2.34) = 2", "round(2) = 2", "round(2.99) = 3", "round(2.5) = 3")
				.since("2.2, 2.7 (decimal placement)")
				.parameter("n", Number.class)
				.parameter("decimals", Number.class, Modifier.OPTIONAL)
				.build(args -> {
					if (args.get("n") instanceof Long longValue)
						return longValue;
					double value = args.<Number>get("n").doubleValue();
					if (!Double.isFinite(value))
						return value;

					double placementDouble = args.<Number>getOrDefault("decimals", 0).doubleValue();
					if (!Double.isFinite(placementDouble) || placementDouble >= Integer.MAX_VALUE || placementDouble <= Integer.MIN_VALUE)
						return Double.NaN;
					int placement = (int) placementDouble;
					if (placement == 0)
						return Math2.round(value);
					if (placement >= 0) {
						BigDecimal decimal = new BigDecimal(Double.toString(value));
						decimal = decimal.setScale(placement, RoundingMode.HALF_UP);
						return decimal.doubleValue();
					}
					long rounded = Math2.round(value);
					return (int) Math2.round(rounded * Math.pow(10.0, placement)) / Math.pow(10.0, placement);
				}));

		Functions.register(DefaultFunction.builder(skript, "ceil", Long.class)
				.description("Rounds a number up, i.e. returns the closest integer larger than or equal to the argument.")
				.examples("ceil(2.34) = 3", "ceil(2) = 2", "ceil(2.99) = 3")
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> {
					Number num = args.get("n");
					if (num instanceof Long lng)
						return lng;
					return Math2.ceil(num.doubleValue());
				}));

		Functions.register(DefaultFunction.builder(skript, "ceiling", Long.class)
				.description("Alias of <a href='#ceil'>ceil</a>.")
				.examples("ceiling(2.34) = 3", "ceiling(2) = 2", "ceiling(2.99) = 3")
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> {
					Number num = args.get("n");
					if (num instanceof Long lng)
						return lng;
					return Math2.ceil(num.doubleValue());
				}));

		Functions.register(DefaultFunction.builder(skript, "abs", Number.class)
				.description("Returns the absolute value of the argument, i.e. makes the argument positive.")
				.examples("abs(3) = 3", "abs(-2) = 2")
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> {
					Number n = args.get("n");
					if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long)
						return Math.abs(n.longValue());
					return Math.abs(n.doubleValue());
				}));

		Functions.register(DefaultFunction.builder(skript, "mod", Number.class)
				.description("Returns the modulo of the given arguments, i.e. the remainder of the division <code>d/m</code>, where d and m are the arguments of this function.",
						"The returned value is always positive. Returns NaN (not a number) if the second argument is zero.")
				.examples("mod(3, 2) = 1", "mod(256436, 100) = 36", "mod(-1, 10) = 9")
				.since("2.2")
				.parameter("d", Number.class)
				.parameter("m", Number.class)
				.build(args -> {
					Number d = args.get("d");
					Number m = args.get("m");
					double mm = m.doubleValue();
					if (mm == 0)
						return Double.NaN;
					return Math2.mod(d.doubleValue(), mm);
				}));

		Functions.register(DefaultFunction.builder(skript, "exp", Number.class)
				.description("The exponential function.")
				.examples("exp(0) = 1", "exp(1) = " + str(Math.exp(1)))
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> Math.exp(args.<Number>get("n").doubleValue())));

		Functions.register(DefaultFunction.builder(skript, "ln", Number.class)
				.description("The natural logarithm.",
						"Returns NaN (not a number) if the argument is negative.")
				.examples("ln(1) = 0", "ln(exp(5)) = 5", "ln(2) = " + StringUtils.toString(Math.log(2), 4))
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> Math.log(args.<Number>get("n").doubleValue())));

		Functions.register(DefaultFunction.builder(skript, "log", Number.class)
				.description("A logarithm, with base 10 if none is specified. This is the inverse operation to exponentiation (for positive bases only), i.e. <code>log(base ^ exponent, base) = exponent</code> for any positive number 'base' and any number 'exponent'.",
						"Another useful equation is <code>base ^ log(a, base) = a</code> for any numbers 'base' and 'a'.",
						"Please note that due to how numbers are represented in computers, these equations do not hold for all numbers, as the computed values may slightly differ from the correct value.",
						"Returns NaN (not a number) if any of the arguments are negative.")
				.examples("log(100) = 2 # 10^2 = 100", "log(16, 2) = 4 # 2^4 = 16")
				.since("2.2")
				.parameter("n", Number.class)
				.parameter("base", Number.class, Modifier.OPTIONAL)
				.build(args -> {
					double base = args.<Number>getOrDefault("base", 10).doubleValue();
					double n = args.<Number>get("n").doubleValue();
					return Math.log10(n) / Math.log10(base);
				}));

		Functions.register(DefaultFunction.builder(skript, "sqrt", Number.class)
				.description("The square root, which is the inverse operation to squaring a number (for positive numbers only). This is the same as <code>(argument) ^ (1/2)</code> – other roots can be calculated via <code>number ^ (1/root)</code>, e.g. <code>set {_l} to {_volume}^(1/3)</code>.",
						"Returns NaN (not a number) if the argument is negative.")
				.examples("sqrt(4) = 2", "sqrt(2) = " + str(Math.sqrt(2)), "sqrt(-1) = " + str(Math.sqrt(-1)))
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> Math.sqrt(args.<Number>get("n").doubleValue())));

		// trigonometry

		Functions.register(DefaultFunction.builder(skript, "sin", Number.class)
				.description("The sine function. It starts at 0° with a value of 0, goes to 1 at 90°, back to 0 at 180°, to -1 at 270° and then repeats every 360°. Uses degrees, not radians.")
				.examples("sin(90) = 1", "sin(60) = " + str(Math.sin(Math.toRadians(60))))
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> Math.sin(Math.toRadians(args.<Number>get("n").doubleValue()))));

		Functions.register(DefaultFunction.builder(skript, "cos", Number.class)
				.description("The cosine function. This is basically the <a href='#sin'>sine</a> shifted by 90°, i.e. <code>cos(a) = sin(a + 90°)</code>, for any number a. Uses degrees, not radians.")
				.examples("cos(0) = 1", "cos(90) = 0")
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> Math.cos(Math.toRadians(args.<Number>get("n").doubleValue()))));

		Functions.register(DefaultFunction.builder(skript, "tan", Number.class)
				.description("The tangent function. This is basically <code><a href='#sin'>sin</a>(arg)/<a href='#cos'>cos</a>(arg)</code>. Uses degrees, not radians.")
				.examples("tan(0) = 0", "tan(45) = 1", "tan(89.99) = " + str(Math.tan(Math.toRadians(89.99))))
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> Math.tan(Math.toRadians(args.<Number>get("n").doubleValue()))));

		Functions.register(DefaultFunction.builder(skript, "asin", Number.class)
				.description("The inverse of the <a href='#sin'>sine</a>, also called arcsin. Returns result in degrees, not radians. Only returns values from -90 to 90.")
				.examples("asin(0) = 0", "asin(1) = 90", "asin(0.5) = " + str(Math.toDegrees(Math.asin(0.5))))
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> Math.toDegrees(Math.asin(args.<Number>get("n").doubleValue()))));

		Functions.register(DefaultFunction.builder(skript, "acos", Number.class)
				.description("The inverse of the <a href='#cos'>cosine</a>, also called arccos. Returns result in degrees, not radians. Only returns values from 0 to 180.")
				.examples("acos(0) = 90", "acos(1) = 0", "acos(0.5) = " + str(Math.toDegrees(Math.asin(0.5))))
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> Math.toDegrees(Math.acos(args.<Number>get("n").doubleValue()))));

		Functions.register(DefaultFunction.builder(skript, "atan", Number.class)
				.description("The inverse of the <a href='#tan'>tangent</a>, also called arctan. Returns result in degrees, not radians. Only returns values from -90 to 90.")
				.examples("atan(0) = 0", "atan(1) = 45", "atan(10000) = " + str(Math.toDegrees(Math.atan(10000))))
				.since("2.2")
				.parameter("n", Number.class)
				.build(args -> Math.toDegrees(Math.atan(args.<Number>get("n").doubleValue()))));

		Functions.register(DefaultFunction.builder(skript, "atan2", Number.class)
				.description("Similar to <a href='#atan'>atan</a>, but requires two coordinates and returns values from -180 to 180.",
						"The returned angle is measured counterclockwise in a standard mathematical coordinate system (x to the right, y to the top).")
				.examples("atan2(0, 1) = 0", "atan2(10, 0) = 90", "atan2(-10, 5) = " + str(Math.toDegrees(Math.atan2(-10, 5))))
				.since("2.2")
				.parameter("x", Number.class)
				.parameter("y", Number.class)
				.build(args -> Math.toDegrees(Math.atan2(args.<Number>get("y").doubleValue(), args.<Number>get("x").doubleValue()))));

		// more stuff

		Functions.register(DefaultFunction.builder(skript, "sum", Number.class)
				.description("Sums a list of numbers.")
				.examples("sum(1) = 1", "sum(2, 3, 4) = 9", "sum({some list variable::*})", "sum(2, {_v::*}, and the player's y-coordinate)")
				.since("2.2")
				.parameter("ns", Number[].class)
				.build(args -> {
					Number[] ns = args.get("ns");
					if (ns.length == 0)
						return null;

					double sum = ns[0].doubleValue();
					for (int i = 1; i < ns.length; i++)
						sum += ns[i].doubleValue();
					return sum;
				}));

		Functions.register(DefaultFunction.builder(skript, "product", Number.class)
				.description("Calculates the product of a list of numbers.")
				.examples("product(1) = 1", "product(2, 3, 4) = 24", "product({some list variable::*})", "product(2, {_v::*}, and the player's y-coordinate)")
				.since("2.2")
				.parameter("ns", Number[].class)
				.build(args -> {
					Number[] ns = args.get("ns");
					if (ns.length == 0)
						return null;

					double product = ns[0].doubleValue();
					for (int i = 1; i < ns.length; i++)
						product *= ns[i].doubleValue();
					return product;
				}));

		Functions.register(DefaultFunction.builder(skript, "max", Number.class)
				.description("Returns the maximum number from a list of numbers.")
				.examples("max(1) = 1", "max(1, 2, 3, 4) = 4", "max({some list variable::*})")
				.since("2.2")
				.parameter("ns", Number[].class)
				.build(args -> {
					Number[] ns = args.get("ns");
					if (ns.length == 0)
						return null;

					double max = ns[0].doubleValue();
					for (int i = 1; i < ns.length; i++) {
						double d = ns[i].doubleValue();
						if (d > max || Double.isNaN(max))
							max = d;
					}
					return max;
				}));

		Functions.register(DefaultFunction.builder(skript, "min", Number.class)
				.description("Returns the minimum number from a list of numbers.")
				.examples("min(1) = 1", "min(1, 2, 3, 4) = 1", "min({some list variable::*})")
				.since("2.2")
				.parameter("ns", Number[].class)
				.build(args -> {
					Number[] ns = args.get("ns");
					if (ns.length == 0)
						return null;

					double min = ns[0].doubleValue();
					for (int i = 1; i < ns.length; i++) {
						double d = ns[i].doubleValue();
						if (d < min || Double.isNaN(min))
							min = d;
					}
					return min;
				}));

		Functions.register(DefaultFunction.builder(skript, "clamp", Number[].class)
				.description("Clamps one or more values between two numbers.", "This function retains indices")
				.examples(
						"clamp(5, 0, 10) = 5",
						"clamp(5.5, 0, 5) = 5",
						"clamp(0.25, 0, 0.5) = 0.25",
						"clamp(5, 7, 10) = 7",
						"clamp((5, 0, 10, 9, 13), 7, 10) = (7, 7, 10, 9, 10)",
						"set {_clamped::*} to clamp({_values::*}, 0, 10)")
				.since("2.8.0")
				.parameter("values", Number[].class, Modifier.KEYED)
				.parameter("min", Number.class)
				.parameter("max", Number.class)
				.contract(new Contract() {
					@Override
					public boolean isSingle(Expression<?>... arguments) {
						return arguments[0].isSingle();
					}

					@Override
					public Class<?> getReturnType(Expression<?>... arguments) {
						return Number.class;
					}
				})
				.buildKeyed(args -> {
					KeyedValue<Number>[] values = args.get("values");
					return KeyedValue.unzip(values).keys();
				}, args -> {
					KeyedValue<Number>[] values = args.get("values");
					Double[] clampedValues = new Double[values.length];
					double min = args.<Number>get("min").doubleValue();
					double max = args.<Number>get("max").doubleValue();
					// we'll be nice and swap them if they're in the wrong order
					double trueMin = Math.min(min, max);
					double trueMax = Math.max(min, max);
					for (int i = 0; i < values.length; i++) {
						double value = values[i].value().doubleValue();
						if (!Double.isNaN(value) && !Double.isNaN(trueMin) && !Double.isNaN(trueMax)) {
							clampedValues[i] = Math.clamp(value, trueMin, trueMax);
						} else {
							clampedValues[i] = Double.NaN;
						}
					}
					return clampedValues;
				}));

		// statistics

		Functions.register(DefaultFunction.builder(skript, "mean", Number.class)
				.description(
						"Get the mean (average) of a list of numbers.",
						"You cannot get the mean of a set of numbers that includes infinity or NaN."
				)
				.examples(
						"mean(1, 2, 3) = 2",
						"mean(0, 5, 10) = 5",
						"mean(13, 97, 376, 709) = 298.75"
				)
				.since("2.11")
				.parameter("numbers", Number[].class)
				.build(args -> {
					Number[] numbers = args.get("numbers");
					Double total = 0d;
					int length = numbers.length;
					for (Number number : numbers) {
						if (Double.isInfinite(number.doubleValue()) || Double.isNaN(number.doubleValue()))
							return null;
						if (total.isInfinite() || total.isNaN())
							return null;
						total += number.doubleValue() / length;
					}
					return total;
				}));

		Functions.register(DefaultFunction.builder(skript, "median", Number.class)
				.description(
						"Get the middle value of a sorted list of numbers. "
								+ "If the list has an even number of values, the median is the average of the two middle numbers.",
						"You cannot get the median of a set of numbers that includes NaN."
				)
				.examples(
						"median(1, 2, 3, 4, 5) = 3",
						"median(1, 2, 3, 4, 5, 6) = 3.5",
						"median(0, 123, 456, 789) = 289.5"
				)
				.since("2.11")
				.parameter("numbers", Number[].class)
				.build(args -> {
					Number[] params = args.get("numbers");
					AtomicBoolean invalid = new AtomicBoolean(false);
					// median requires the numbers to be sorted from lowest to biggest
					Number[] sorted = Arrays.stream(params)
							.filter(object -> {
								if (!(object instanceof Number number))
									return false;
								if (Double.isNaN(number.doubleValue())) {
									invalid.set(true);
									return false;
								}
								return true;
							})
							.sorted(((o1, o2) -> {
								Double n1 = o1.doubleValue();
								Double n2 = o2.doubleValue();
								return n1.compareTo(n2);
							}))
							.toArray(Number[]::new);
					if (invalid.get())
						return null;
					int size = sorted.length;
					// If the size of numbers provided is odd, we can just grab the middle number
					if (size % 2 == 1)
						return sorted[Math2.ceil(size / 2)];
					// If not, we grab the rounded up and rounded down numbers, then get the average of those
					int half = size / 2;
					double first = (sorted[half - 1]).doubleValue();
					double second = (sorted[half]).doubleValue();
					return (first + second) / 2;
				}));

		Functions.register(DefaultFunction.builder(skript, "factorial", Number.class)
				.description(
						"Get the factorial of a number.",
						"Getting the factorial of any number above 21 will return an approximation, not an exact value.",
						"Any number after 170 will always return Infinity.",
						"Should not be used to calculate permutations or combinations manually."
				)
				.examples(
						"factorial(0) = 1",
						"factorial(3) = 3*2*1 = 6",
						"factorial(5) = 5*4*3*2*1 = 120",
						"factorial(171) = Infinity"
				)
				.since("2.11")
				.parameter("number", Number.class, Modifier.ranged(0L, Long.MAX_VALUE))
				.build(args -> {
					Double number = args.<Number>get("number").doubleValue();
					if (number < 0) {
						return null;
					} else if (number <= 1) { // 0 and 1
						return 1;
					} else if (number > 170) {
						return Double.POSITIVE_INFINITY;
					}
					Double result = 1d;
					for (double i = number; i > 1; i--) {
						if (result.isInfinite() || result.isNaN())
							break;
						result *= i;
					}
					return result;
				}));

		Functions.register(DefaultFunction.builder(skript, "root", Number.class)
				.description("Calculates the <i>n</i>th root of a number.")
				.examples(
						"root(2, 4) = 2 # same as sqrt(4)",
						"root(4, 16) = 2",
						"root(-4, 16) = 0.5 # same as 16^(-1/4)"
				)
				.since("2.11")
				.parameter("n", Number.class)
				.parameter("number", Number.class, Modifier.ranged(0L, Long.MAX_VALUE))
				.build(args -> {
					Double n = args.<Number>get("n").doubleValue();
					Double number = args.<Number>get("number").doubleValue();
					if (n == 0) {
						return null;
					} else if (n == 1) {
						return number;
					} else if (n == 2) {
						return Math.sqrt(number);
					}
					return Math.pow(number, (1 / n));
				}));

		Functions.register(DefaultFunction.builder(skript, "permutations", Number.class)
				.description(
						"Get the number of possible ordered arrangements from 1 to 'options' with each arrangement having a size equal to 'selected'",
						"For example, permutations with 3 options and an arrangement size of 1, returns 3: (1), (2), (3)",
						"Permutations with 3 options and an arrangement size of 2 returns 6: (1, 2), (1, 3), (2, 1), (2, 3), (3, 1), (3, 2)",
						"Note that the bigger the 'options' and lower the 'selected' may result in approximations or even infinity values.",
						"Permutations differ from combinations in that permutations account for the arrangement of elements within the set, "
								+ "whereas combinations focus on unique sets and ignore the order of elements.",
						"Example: (1, 2) and (2, 1) are two distinct permutations because the positions of '1' and '2' are different, "
								+ "but they represent a single combination since order doesn't matter in combinations."
				)
				.examples(
						"permutations(10, 2) = 90",
						"permutations(10, 4) = 5040",
						"permutations(size of {some list::*}, 2)"
				)
				.since("2.11")
				.parameter("options", Number.class, Modifier.ranged(0L, Long.MAX_VALUE))
				.parameter("selected", Number.class, Modifier.ranged(0L, Long.MAX_VALUE))
				.build(args -> {
					Double options = args.<Number>get("options").doubleValue();
					Double selected = args.<Number>get("selected").doubleValue();
					if (selected > options || selected < 0) { // Illegal argument
						return null;
					} else if (selected.equals(0d)) { // Will always be 1
						return 1;
					} else if (selected.equals(1d)) { // Will always be the number from 'options'
						return options;
					}
					// We can simplify this as there will always be a factorial that can cancel out
					// Example: options = 10, selected = 2; 10!/(10-2)! = 10!/8!
					// We can deduce that 10! = (10)(9)(8!) ; allowing the '8!' factorial to cancel out, leaving us with: (10)(9)
					// Which allows us to start from 10 and go down to 10-2, but will never reach 8 as 'i' needs to be higher
					Double result = 1d;
					for (double i = options; i > options - selected; i--) {
						if (result.isInfinite() || result.isNaN())
							break;
						result *= i;
					}
					return result;
				}));

		Functions.register(DefaultFunction.builder(skript, "combinations", Number.class)
				.description(
						"Get the number of possible sets from 1 to 'options' with each set having a size equal to 'selected'",
						"For example, a combination with 3 options and a set size of 1, returns 3: (1), (2), (3)",
						"A combination of 3 options with a set size of 2 returns 3: (1, 2), (1, 3), (2, 3)",
						"Note that the bigger the 'options' and lower the 'selected' may result in approximations or even infinity values.",
						"Combinations differ from permutations in that combinations focus on unique sets, ignoring the order of elements, "
								+ "whereas permutations account for the arrangement of elements within the set.",
						"Example: (1, 2) and (2, 1) represent a single combination since order doesn't matter in combinations, "
								+ "but they are two distinct permutations because permutations consider the order."
				)
				.examples(
						"combinations(10, 8) = 45",
						"combinations(5, 3) = 10",
						"combinations(size of {some list::*}, 2)"
				)
				.since("2.11")
				.parameter("options", Number.class, Modifier.ranged(0L, Long.MAX_VALUE))
				.parameter("selected", Number.class, Modifier.ranged(0L, Long.MAX_VALUE))
				.build(args -> {
					Double options = args.<Number>get("options").doubleValue();
					Double selected = args.<Number>get("selected").doubleValue();
					if (selected > options || selected < 0) { // Illegal arguments
						return null;
					} else if (selected.equals(0d)) { // Will always return 1
						return 1;
					} else if (selected.equals(1d)) { // Will always be the number from 'options'
						return options;
					}
					// By the same reasoning from 'permutations' there will always be a factorial that can cancel out
					// Example: options = 10, selected = 2 ; 10!/(10-2)!(2!) = 10!/(8!)(2!)
					// 10! = (10)(9)(8!) ; the 8! cancel out, leaving us with: (10)(9)/2!
					// 'top' will calculate the leftovers in the numerator: (10)(9)
					Double top = 1d;
					for (double i = options; i > options - selected; i--) {
						if (top.isInfinite() || top.isNaN())
							return top;
						top *= i;
					}
					// 'bottom' will calculate the leftovers in the denominator: 2!
					Double bottom = selected;
					for (double i = selected - 1; i > 1; i--) {
						if (bottom.isInfinite() || bottom.isNaN())
							break;
						bottom *= i;
					}
					// Then we divide
					return top / bottom;
				}));
	}

}
