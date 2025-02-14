package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.function.*;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.Date;
import ch.njol.util.Math2;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultFunctions {

	private static String str(double n) {
		return StringUtils.toString(n, 4);
	}

	private static final DecimalFormat DEFAULT_INTEGER_FORMAT = new DecimalFormat("###,###");
	private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("###,###.##");

	static {
		Parameter<?>[] numberParam = new Parameter[] {new Parameter<>("n", DefaultClasses.NUMBER, true, null)};
		Parameter<?>[] numbersParam = new Parameter[] {new Parameter<>("ns", DefaultClasses.NUMBER, false, null)};

		// basic math functions

		Functions.registerFunction(new SimpleJavaFunction<Long>("floor", numberParam, DefaultClasses.LONG, true) {
			@Override
			public Long[] executeSimple(Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.floor(((Number) params[0][0]).doubleValue())};
			}
		}.description("Rounds a number down, i.e. returns the closest integer smaller than or equal to the argument.")
			.examples("floor(2.34) = 2", "floor(2) = 2", "floor(2.99) = 2")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("round", new Parameter[] {new Parameter<>("n", DefaultClasses.NUMBER, true, null), new Parameter<>("d", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, false))}, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				double value = ((Number) params[0][0]).doubleValue();
				int placement = ((Number) params[1][0]).intValue();
				if (placement == 0)
					return new Long[] {Math2.round(value)};
				if (placement >= 0) {
					BigDecimal decimal = new BigDecimal(Double.toString(value));
					decimal = decimal.setScale(placement, RoundingMode.HALF_UP);
					return new Double[] {decimal.doubleValue()};
				}
				long rounded = Math2.round(value);
				return new Double[] {(int) Math2.round(rounded * Math.pow(10.0, placement)) / Math.pow(10.0, placement)};
			}
		}.description("Rounds a number, i.e. returns the closest integer to the argument. Place a second argument to define the decimal placement.")
			.examples("round(2.34) = 2", "round(2) = 2", "round(2.99) = 3", "round(2.5) = 3")
			.since("2.2, 2.7 (decimal placement)"));

		Functions.registerFunction(new SimpleJavaFunction<Long>("ceil", numberParam, DefaultClasses.LONG, true) {
			@Override
			public Long[] executeSimple(Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.ceil(((Number) params[0][0]).doubleValue())};
			}
		}.description("Rounds a number up, i.e. returns the closest integer larger than or equal to the argument.")
			.examples("ceil(2.34) = 3", "ceil(2) = 2", "ceil(2.99) = 3")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Long>("ceiling", numberParam, DefaultClasses.LONG, true) {
			@Override
			public Long[] executeSimple(Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.ceil(((Number) params[0][0]).doubleValue())};
			}
		}.description("Alias of <a href='#ceil'>ceil</a>.")
			.examples("ceiling(2.34) = 3", "ceiling(2) = 2", "ceiling(2.99) = 3")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("abs", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				Number n = (Number) params[0][0];
				if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long)
					return new Long[] {Math.abs(n.longValue())};
				return new Double[] {Math.abs(n.doubleValue())};
			}
		}.description("Returns the absolute value of the argument, i.e. makes the argument positive.")
			.examples("abs(3) = 3", "abs(-2) = 2")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("mod", new Parameter[] {new Parameter<>("d", DefaultClasses.NUMBER, true, null), new Parameter<>("m", DefaultClasses.NUMBER, true, null)}, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				Number d = (Number) params[0][0];
				Number m = (Number) params[1][0];
				double mm = m.doubleValue();
				if (mm == 0)
					return new Double[] {Double.NaN};
				return new Double[] {Math2.mod(d.doubleValue(), mm)};
			}
		}.description("Returns the modulo of the given arguments, i.e. the remainder of the division <code>d/m</code>, where d and m are the arguments of this function.",
						"The returned value is always positive. Returns NaN (not a number) if the second argument is zero.")
			.examples("mod(3, 2) = 1", "mod(256436, 100) = 36", "mod(-1, 10) = 9")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("exp", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.exp(((Number) params[0][0]).doubleValue())};
			}
		}.description("The exponential function. You probably don't need this if you don't know what this is.")
			.examples("exp(0) = 1", "exp(1) = " + str(Math.exp(1)))
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("ln", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.log(((Number) params[0][0]).doubleValue())};
			}
		}.description("The natural logarithm. You probably don't need this if you don't know what this is.",
						"Returns NaN (not a number) if the argument is negative.")
			.examples("ln(1) = 0", "ln(exp(5)) = 5", "ln(2) = " + StringUtils.toString(Math.log(2), 4))
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("log", new Parameter[] {new Parameter<>("n", DefaultClasses.NUMBER, true, null), new Parameter<>("base", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(10, false))}, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.log10(((Number) params[0][0]).doubleValue()) / Math.log10(((Number) params[1][0]).doubleValue())};
			}
		}.description("A logarithm, with base 10 if none is specified. This is the inverse operation to exponentiation (for positive bases only), i.e. <code>log(base ^ exponent, base) = exponent</code> for any positive number 'base' and any number 'exponent'.",
						"Another useful equation is <code>base ^ log(a, base) = a</code> for any numbers 'base' and 'a'.",
						"Please note that due to how numbers are represented in computers, these equations do not hold for all numbers, as the computed values may slightly differ from the correct value.",
						"Returns NaN (not a number) if any of the arguments are negative.")
			.examples("log(100) = 2 # 10^2 = 100", "log(16, 2) = 4 # 2^4 = 16")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("sqrt", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.sqrt(((Number) params[0][0]).doubleValue())};
			}
		}.description("The square root, which is the inverse operation to squaring a number (for positive numbers only). This is the same as <code>(argument) ^ (1/2)</code> – other roots can be calculated via <code>number ^ (1/root)</code>, e.g. <code>set {_l} to {_volume}^(1/3)</code>.",
						"Returns NaN (not a number) if the argument is negative.")
			.examples("sqrt(4) = 2", "sqrt(2) = " + str(Math.sqrt(2)), "sqrt(-1) = " + str(Math.sqrt(-1)))
			.since("2.2"));

		// trigonometry

		Functions.registerFunction(new SimpleJavaFunction<Number>("sin", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.sin(Math.toRadians(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The sine function. It starts at 0° with a value of 0, goes to 1 at 90°, back to 0 at 180°, to -1 at 270° and then repeats every 360°. Uses degrees, not radians.")
			.examples("sin(90) = 1", "sin(60) = " + str(Math.sin(Math.toRadians(60))))
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("cos", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.cos(Math.toRadians(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The cosine function. This is basically the <a href='#sin'>sine</a> shifted by 90°, i.e. <code>cos(a) = sin(a + 90°)</code>, for any number a. Uses degrees, not radians.")
			.examples("cos(0) = 1", "cos(90) = 0")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("tan", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.tan(Math.toRadians(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The tangent function. This is basically <code><a href='#sin'>sin</a>(arg)/<a href='#cos'>cos</a>(arg)</code>. Uses degrees, not radians.")
			.examples("tan(0) = 0", "tan(45) = 1", "tan(89.99) = " + str(Math.tan(Math.toRadians(89.99))))
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("asin", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.toDegrees(Math.asin(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The inverse of the <a href='#sin'>sine</a>, also called arcsin. Returns result in degrees, not radians. Only returns values from -90 to 90.")
			.examples("asin(0) = 0", "asin(1) = 90", "asin(0.5) = " + str(Math.toDegrees(Math.asin(0.5))))
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("acos", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.toDegrees(Math.acos(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The inverse of the <a href='#cos'>cosine</a>, also called arccos. Returns result in degrees, not radians. Only returns values from 0 to 180.")
			.examples("acos(0) = 90", "acos(1) = 0", "acos(0.5) = " + str(Math.toDegrees(Math.asin(0.5))))
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("atan", numberParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.toDegrees(Math.atan(((Number) params[0][0]).doubleValue()))};
			}
		}.description("The inverse of the <a href='#tan'>tangent</a>, also called arctan. Returns result in degrees, not radians. Only returns values from -90 to 90.")
			.examples("atan(0) = 0", "atan(1) = 45", "atan(10000) = " + str(Math.toDegrees(Math.atan(10000))))
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("atan2", new Parameter[] {
			new Parameter<>("x", DefaultClasses.NUMBER, true, null),
			new Parameter<>("y", DefaultClasses.NUMBER, true, null)
		}, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				return new Double[] {Math.toDegrees(Math.atan2(((Number) params[1][0]).doubleValue(), ((Number) params[0][0]).doubleValue()))};
			}
		}.description("Similar to <a href='#atan'>atan</a>, but requires two coordinates and returns values from -180 to 180.",
			"The returned angle is measured counterclockwise in a standard mathematical coordinate system (x to the right, y to the top).")
			.examples("atan2(0, 1) = 0", "atan2(10, 0) = 90", "atan2(-10, 5) = " + str(Math.toDegrees(Math.atan2(-10, 5))))
			.since("2.2"));

		// more stuff

		Functions.registerFunction(new SimpleJavaFunction<Number>("sum", numbersParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				Object[] ns = params[0];
				double sum = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++)
					sum += ((Number) ns[i]).doubleValue();
				return new Double[] {sum};
			}
		}.description("Sums a list of numbers.")
			.examples("sum(1) = 1", "sum(2, 3, 4) = 9", "sum({some list variable::*})", "sum(2, {_v::*}, and the player's y-coordinate)")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("product", numbersParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				Object[] ns = params[0];
				double product = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++)
					product *= ((Number) ns[i]).doubleValue();
				return new Double[] {product};
			}
		}.description("Calculates the product of a list of numbers.")
			.examples("product(1) = 1", "product(2, 3, 4) = 24", "product({some list variable::*})", "product(2, {_v::*}, and the player's y-coordinate)")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("max", numbersParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				Object[] ns = params[0];
				double max = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++) {
					double d = ((Number) ns[i]).doubleValue();
					if (d > max || Double.isNaN(max))
						max = d;
				}
				return new Double[] {max};
			}
		}.description("Returns the maximum number from a list of numbers.")
			.examples("max(1) = 1", "max(1, 2, 3, 4) = 4", "max({some list variable::*})")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("min", numbersParam, DefaultClasses.NUMBER, true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				Object[] ns = params[0];
				double min = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++) {
					double d = ((Number) ns[i]).doubleValue();
					if (d < min || Double.isNaN(min))
						min = d;
				}
				return new Double[] {min};
			}
		}.description("Returns the minimum number from a list of numbers.")
			.examples("min(1) = 1", "min(1, 2, 3, 4) = 1", "min({some list variable::*})")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Number>("clamp", new Parameter[] {
					 new Parameter<>("values", DefaultClasses.NUMBER, false, null),
					 new Parameter<>("min", DefaultClasses.NUMBER, true, null),
					 new Parameter<>("max", DefaultClasses.NUMBER, true, null)
				 }, DefaultClasses.NUMBER, false, new Contract() {

					 @Override
					 public boolean isSingle(Expression<?>... arguments) {
						 return arguments[0].isSingle();
					 }

					 @Override
					 public Class<?> getReturnType(Expression<?>... arguments) {
						 return Number.class;
					 }
				 }) {
			@Override
			public @Nullable Number[] executeSimple(Object[][] params) {
				Number[] values = (Number[]) params[0];
				Double[] clampedValues = new Double[values.length];
				double min = ((Number) params[1][0]).doubleValue();
				double max = ((Number) params[2][0]).doubleValue();
				// we'll be nice and swap them if they're in the wrong order
				double trueMin = Math.min(min, max);
				double trueMax = Math.max(min, max);
				for (int i = 0; i < values.length; i++) {
					double value = values[i].doubleValue();
					clampedValues[i] = Math.max(Math.min(value, trueMax), trueMin);
				}
				return clampedValues;
			}
		}).description("Clamps one or more values between two numbers.")
			.examples(
					"clamp(5, 0, 10) = 5",
					"clamp(5.5, 0, 5) = 5",
					"clamp(0.25, 0, 0.5) = 0.25",
					"clamp(5, 7, 10) = 7",
					"clamp((5, 0, 10, 9, 13), 7, 10) = (7, 7, 10, 9, 10)",
					"set {_clamped::*} to clamp({_values::*}, 0, 10)")
			.since("2.8.0");

		// misc

		Functions.registerFunction(new SimpleJavaFunction<World>("world", new Parameter[] {
			new Parameter<>("name", DefaultClasses.STRING, true, null)
		}, DefaultClasses.WORLD, true) {
			@Override
			public World[] executeSimple(Object[][] params) {
				World w = Bukkit.getWorld((String) params[0][0]);
				return w == null ? new World[0] : new World[] {w};
			}
		}).description("Gets a world from its name.")
			.examples("set {_nether} to world(\"%{_world}%_nether\")")
			.since("2.2");

		Functions.registerFunction(new JavaFunction<Location>("location", new Parameter[] {
			new Parameter<>("x", DefaultClasses.NUMBER, true, null),
			new Parameter<>("y", DefaultClasses.NUMBER, true, null),
			new Parameter<>("z", DefaultClasses.NUMBER, true, null),
			new Parameter<>("world", DefaultClasses.WORLD, true, new EventValueExpression<>(World.class)),
			new Parameter<>("yaw", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
			new Parameter<>("pitch", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true))
		}, DefaultClasses.LOCATION, true) {
			@Override
			@Nullable
			public Location[] execute(FunctionEvent<?> event, Object[][] params) {
				for (int i : new int[] {0, 1, 2, 4, 5}) {
					if (params[i] == null || params[i].length == 0 || params[i][0] == null)
						return null;
				}

				World world = params[3].length == 1 ? (World) params[3][0] : Bukkit.getWorlds().get(0); // fallback to main world of server

				return new Location[] {new Location(world,
					((Number) params[0][0]).doubleValue(), ((Number) params[1][0]).doubleValue(), ((Number) params[2][0]).doubleValue(),
					((Number) params[4][0]).floatValue(), ((Number) params[5][0]).floatValue())};
			}
		}.description("Creates a location from a world and 3 coordinates, with an optional yaw and pitch.",
						"If for whatever reason the world is not found, it will fallback to the server's main world.")
			.examples("# TELEPORTING",
					"teleport player to location(1,1,1, world \"world\")",
					"teleport player to location(1,1,1, world \"world\", 100, 0)",
					"teleport player to location(1,1,1, world \"world\", yaw of player, pitch of player)",
					"teleport player to location(1,1,1, world of player)",
					"teleport player to location(1,1,1, world(\"world\"))",
					"teleport player to location({_x}, {_y}, {_z}, {_w}, {_yaw}, {_pitch})",
					"# SETTING BLOCKS",
					"set block at location(1,1,1, world \"world\") to stone",
					"set block at location(1,1,1, world \"world\", 100, 0) to stone",
					"set block at location(1,1,1, world of player) to stone",
					"set block at location(1,1,1, world(\"world\")) to stone",
					"set block at location({_x}, {_y}, {_z}, {_w}) to stone",
					"# USING VARIABLES",
					"set {_l1} to location(1,1,1)",
					"set {_l2} to location(10,10,10)",
					"set blocks within {_l1} and {_l2} to stone",
					"if player is within {_l1} and {_l2}:",
					"# OTHER",
					"kill all entities in radius 50 around location(1,65,1, world \"world\")",
					"delete all entities in radius 25 around location(50,50,50, world \"world_nether\")",
					"ignite all entities in radius 25 around location(1,1,1, world of player)")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Date>("date", new Parameter[] {
			new Parameter<>("year", DefaultClasses.NUMBER, true, null),
			new Parameter<>("month", DefaultClasses.NUMBER, true, null),
			new Parameter<>("day", DefaultClasses.NUMBER, true, null),
			new Parameter<>("hour", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
			new Parameter<>("minute", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
			new Parameter<>("second", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
			new Parameter<>("millisecond", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
			new Parameter<>("zone_offset", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(Double.NaN, true)),
			new Parameter<>("dst_offset", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(Double.NaN, true))
		}, DefaultClasses.DATE, true) {
			private final int[] fields = {
				Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
				Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND,
				Calendar.ZONE_OFFSET, Calendar.DST_OFFSET
			};
			private final int[] offsets = {
				0, -1, 0,
				0, 0, 0, 0,
				0, 0
			};
			private final double[] scale = {
				1, 1, 1,
				1, 1, 1, 1,
				1000 * 60, 1000 * 60
			};
			private final double[] relations = {
				1. / 12, 1. / 30,
				1. / 24, 1. / 60, 1. / 60, 1. / 1000,
				0, 0,
				0
			};

			{
				int length = getSignature().getMaxParameters();
				assert fields.length == length
					&& offsets.length == length
					&& scale.length == length
					&& relations.length == length;
			}

			@Override
			public Date[] executeSimple(Object[][] params) {
				Calendar c = Calendar.getInstance();
				c.setLenient(true);
				double carry = 0;
				for (int i = 0; i < fields.length; i++) {
					int field = fields[i];
					Number n = (Number) params[i][0];

					double value = n.doubleValue() * scale[i] + offsets[i] + carry;
					int v = (int) Math2.floor(value);
					carry = (value - v) * relations[i];
					//noinspection MagicConstant
					c.set(field, v);
				}

				return new Date[] {new Date(c.getTimeInMillis(), c.getTimeZone())};
			}
		}.description("Creates a date from a year, month, and day, and optionally also from hour, minute, second and millisecond.",
						"A time zone and DST offset can be specified as well (in minutes), if they are left out the server's time zone and DST offset are used (the created date will not retain this information).")
			.examples("date(2014, 10, 1) # 0:00, 1st October 2014", "date(1990, 3, 5, 14, 30) # 14:30, 5th May 1990", "date(1999, 12, 31, 23, 59, 59, 999, -3*60, 0) # almost year 2000 in parts of Brazil (-3 hours offset, no DST)")
			.since("2.2"));

		Functions.registerFunction(new SimpleJavaFunction<Vector>("vector", new Parameter[] {
			new Parameter<>("x", DefaultClasses.NUMBER, true, null),
			new Parameter<>("y", DefaultClasses.NUMBER, true, null),
			new Parameter<>("z", DefaultClasses.NUMBER, true, null)
		}, DefaultClasses.VECTOR, true) {
			@Override
			public Vector[] executeSimple(Object[][] params) {
				return new Vector[] {new Vector(
					((Number)params[0][0]).doubleValue(),
					((Number)params[1][0]).doubleValue(),
					((Number)params[2][0]).doubleValue()
				)};
			}

		}.description("Creates a new vector, which can be used with various expressions, effects and functions.")
			.examples("vector(0, 0, 0)")
			.since("2.2-dev23"));

		Functions.registerFunction(new SimpleJavaFunction<Long>("calcExperience", new Parameter[] {
			new Parameter<>("level", DefaultClasses.LONG, true, null)
		}, DefaultClasses.LONG, true) {
			@Override
			public Long[] executeSimple(Object[][] params) {
				long level = (long) params[0][0];
				long exp;
				if (level <= 0) {
					exp = 0;
				} else if (level >= 1 && level <= 15) {
					exp = level * level + 6 * level;
				} else if (level >= 16 && level <= 30) { // Truncating decimal parts probably works
					exp = (int) (2.5 * level * level - 40.5 * level + 360);
				} else { // Half experience points do not exist, anyway
					exp = (int) (4.5 * level * level - 162.5 * level + 2220);
				}

				return new Long[] {exp};
			}

		}.description("Calculates the total amount of experience needed to achieve given level from scratch in Minecraft.")
			.since("2.2-dev32"));

		Functions.registerFunction(new SimpleJavaFunction<Color>("rgb", new Parameter[] {
			new Parameter<>("red", DefaultClasses.LONG, true, null),
			new Parameter<>("green", DefaultClasses.LONG, true, null),
			new Parameter<>("blue", DefaultClasses.LONG, true, null),
			new Parameter<>("alpha", DefaultClasses.LONG, true, new SimpleLiteral<>(255L,true))
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Long red = (Long) params[0][0];
				Long green = (Long) params[1][0];
				Long blue = (Long) params[2][0];
				Long alpha = (Long) params[3][0];

				return CollectionUtils.array(ColorRGB.fromRGBA(red.intValue(), green.intValue(), blue.intValue(), alpha.intValue()));
			}
		}).description("Returns a RGB color from the given red, green and blue parameters. Alpha values can be added optionally, " +
						"but these only take affect in certain situations, like text display backgrounds.")
			.examples(
				"dye player's leggings rgb(120, 30, 45)",
				"set the colour of a text display to rgb(10, 50, 100, 50)"
			)
			.since("2.5, 2.10 (alpha)");

		Functions.registerFunction(new SimpleJavaFunction<Player>("player", new Parameter[] {
			new Parameter<>("nameOrUUID", DefaultClasses.STRING, true, null),
			new Parameter<>("getExactPlayer", DefaultClasses.BOOLEAN, true, new SimpleLiteral<Boolean>(false, true)) // getExactPlayer -- grammar ¯\_ (ツ)_/¯
		}, DefaultClasses.PLAYER, true) {
			@Override
			public Player[] executeSimple(Object[][] params) {
				String name = (String) params[0][0];
				boolean isExact = (boolean) params[1][0];
				UUID uuid = null;
				if (name.length() > 16 || name.contains("-")) { // shortcut
					try {
						uuid = UUID.fromString(name);
					} catch (IllegalArgumentException ignored) {}
				}
				return CollectionUtils.array(uuid != null ? Bukkit.getPlayer(uuid) : (isExact ? Bukkit.getPlayerExact(name) : Bukkit.getPlayer(name)));
			}
		}).description("Returns an online player from their name or UUID, if player is offline function will return nothing.", "Setting 'getExactPlayer' parameter to true will return the player whose name is exactly equal to the provided name instead of returning a player that their name starts with the provided name.")
			.examples("set {_p} to player(\"Notch\") # will return an online player whose name is or starts with 'Notch'", "set {_p} to player(\"Notch\", true) # will return the only online player whose name is 'Notch'", "set {_p} to player(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\") # <none> if player is offline")
			.since("2.8.0");

		{ // offline player function
			boolean hasIfCached = Skript.methodExists(Bukkit.class, "getOfflinePlayerIfCached", String.class);

			List<Parameter<?>> params = new ArrayList<>();
			params.add(new Parameter<>("nameOrUUID", DefaultClasses.STRING, true, null));
			if (hasIfCached)
				params.add(new Parameter<>("allowLookups", DefaultClasses.BOOLEAN, true, new SimpleLiteral<>(true, true)));

			Functions.registerFunction(new SimpleJavaFunction<OfflinePlayer>("offlineplayer", params.toArray(new Parameter[0]),
				DefaultClasses.OFFLINE_PLAYER, true) {
				@Override
				public OfflinePlayer[] executeSimple(Object[][] params) {
					String name = (String) params[0][0];
					UUID uuid = null;
					if (name.length() > 16 || name.contains("-")) { // shortcut
						try {
							uuid = UUID.fromString(name);
						} catch (IllegalArgumentException ignored) {
						}
					}
					OfflinePlayer result;

					if (uuid != null) {
						result = Bukkit.getOfflinePlayer(uuid); // doesn't do lookups
					} else if (hasIfCached && !((Boolean) params[1][0])) {
						result = Bukkit.getOfflinePlayerIfCached(name);
						if (result == null)
							return new OfflinePlayer[0];
					} else {
						result = Bukkit.getOfflinePlayer(name);
					}

					return CollectionUtils.array(result);
				}

			}).description(
				"Returns a offline player from their name or UUID. This function will still return the player if they're online. " +
				"If Paper 1.16.5+ is used, the 'allowLookup' parameter can be set to false to prevent this function from doing a " +
				"web lookup for players who have not joined before. Lookups can cause lag spikes of up to multiple seconds, so " +
				"use offline players with caution."
			)
			.examples(
				"set {_p} to offlineplayer(\"Notch\")",
				"set {_p} to offlineplayer(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\")",
				"set {_p} to offlineplayer(\"Notch\", false)"
			)
			.since("2.8.0, 2.9.0 (prevent lookups)");
		} // end offline player function

		Functions.registerFunction(new SimpleJavaFunction<Boolean>("isNaN", numberParam, DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean[] executeSimple(Object[][] params) {
				return new Boolean[] {Double.isNaN(((Number) params[0][0]).doubleValue())};
			}
		}).description("Returns true if the input is NaN (not a number).")
			.examples("isNaN(0) # false", "isNaN(0/0) # true", "isNaN(sqrt(-1)) # true")
			.since("2.8.0");

		Functions.registerFunction(new SimpleJavaFunction<String>("concat", new Parameter[] {
			 new Parameter<>("texts", DefaultClasses.OBJECT, false, null)
		}, DefaultClasses.STRING, true) {
			@Override
			public String[] executeSimple(Object[][] params) {
				StringBuilder builder = new StringBuilder();
				for (Object object : params[0]) {
					builder.append(Classes.toString(object));
				}
				return new String[] {builder.toString()};
			}
		}).description("Joins the provided texts (and other things) into a single text.")
			.examples(
				"concat(\"hello \", \"there\") # hello there",
				"concat(\"foo \", 100, \" bar\") # foo 100 bar"
			).since("2.9.0");

		// joml functions - for display entities
		{
			if (Skript.classExists("org.joml.Quaternionf")) {
				Functions.registerFunction(new SimpleJavaFunction<>("quaternion", new Parameter[]{
						new Parameter<>("w", DefaultClasses.NUMBER, true, null),
						new Parameter<>("x", DefaultClasses.NUMBER, true, null),
						new Parameter<>("y", DefaultClasses.NUMBER, true, null),
						new Parameter<>("z", DefaultClasses.NUMBER, true, null)
					}, Classes.getExactClassInfo(Quaternionf.class), true) {
						@Override
						public Quaternionf[] executeSimple(Object[][] params) {
							double w = ((Number) params[0][0]).doubleValue();
							double x = ((Number) params[1][0]).doubleValue();
							double y = ((Number) params[2][0]).doubleValue();
							double z = ((Number) params[3][0]).doubleValue();
							return CollectionUtils.array(new Quaternionf(x, y, z, w));
						}
					})
					.description("Returns a quaternion from the given W, X, Y and Z parameters. ")
					.examples("quaternion(1, 5.6, 45.21, 10)")
					.since("2.10");
			}

			if (Skript.classExists("org.joml.AxisAngle4f")) {
				Functions.registerFunction(new SimpleJavaFunction<>("axisAngle", new Parameter[]{
						new Parameter<>("angle", DefaultClasses.NUMBER, true, null),
						new Parameter<>("axis", DefaultClasses.VECTOR, true, null)
					}, Classes.getExactClassInfo(Quaternionf.class), true) {
						@Override
						public Quaternionf[] executeSimple(Object[][] params) {
							float angle = (float) (((Number) params[0][0]).floatValue() / 180 * Math.PI);
							Vector v = ((Vector) params[1][0]);
							if (v.isZero() || !Double.isFinite(v.getX()) || !Double.isFinite(v.getY()) || !Double.isFinite(v.getZ()))
								return new Quaternionf[0];
							Vector3f axis = ((Vector) params[1][0]).toVector3f();
							return CollectionUtils.array(new Quaternionf(new AxisAngle4f(angle, axis)));
						}
					})
					.description("Returns a quaternion from the given angle (in degrees) and axis (as a vector). This represents a rotation around the given axis by the given angle.")
					.examples("axisangle(90, (vector from player's facing))")
					.since("2.10");
			}
		} // end joml functions

		Functions.registerFunction(new SimpleJavaFunction<>("formatNumber", new Parameter[]{
				new Parameter<>("number", DefaultClasses.NUMBER, true, null),
				new Parameter<>("format", DefaultClasses.STRING, true, new SimpleLiteral<>("", true))
			}, DefaultClasses.STRING, true) {
				@Override
				public String[] executeSimple(Object[][] params) {
					Number number = (Number) params[0][0];
					String format = (String) params[1][0];

					if (format.isEmpty()) {
						if (number instanceof Double || number instanceof Float) {
							return new String[]{DEFAULT_DECIMAL_FORMAT.format(number)};
						} else {
							return new String[]{DEFAULT_INTEGER_FORMAT.format(number)};
						}
					}

					try {
						return new String[]{new DecimalFormat(format).format(number)};
					} catch (IllegalArgumentException e) {
						return null; // invalid format
					}
				}
			})
			.description(
				"Converts numbers to human-readable format. By default, '###,###' (e.g. '123,456,789') " +
				"will be used for whole numbers and '###,###.##' (e.g. '123,456,789.00) will be used for decimal numbers. " +
				"A hashtag '#' represents a digit, a comma ',' is used to separate numbers, and a period '.' is used for decimals. ",
				"Will return none if the format is invalid.",
				"For further reference, see this <a href=\"https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html\" target=\"_blank\">article</a>.")
			.examples(
				"command /balance:",
					"\taliases: bal",
					"\texecutable by: players",
					"\ttrigger:",
						"\t\tset {_money} to formatNumber({money::%sender's uuid%})",
						"\t\tsend \"Your balance: %{_money}%\" to sender")
			.since("2.10");

		Functions.registerFunction(new SimpleJavaFunction<Number>("mean", new Parameter[]{
			new Parameter<>("numbers", DefaultClasses.NUMBER, false, null)
		}, DefaultClasses.NUMBER, true) {
			@Override
			public Number @Nullable [] executeSimple(Object[][] params) {
				Double total = 0d;
				int length = params[0].length;
				for (int i = 0; i < length; i++) {
					Number number = (Number) params[0][i];
					if (Double.isInfinite(number.doubleValue()) || Double.isNaN(number.doubleValue()))
						return null;
					if (total.isInfinite() || total.isNaN())
						return null;
					total += number.doubleValue() / length;
				}
				return new Number[]{total};
			}
		})
			.description(
				"Get the mean (average) of a list of numbers.",
				"You cannot get the mean of a set of numbers that includes infinity or NaN."
			)
			.examples(
				"mean(1, 2, 3) = 2",
				"mean(0, 5, 10) = 5",
				"mean(13, 97, 376, 709) = 298.75"
			)
			.since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Number>("median", new Parameter[]{
			new Parameter<>("numbers", DefaultClasses.NUMBER, false, null)
		}, DefaultClasses.NUMBER, true) {
			@Override
			public Number @Nullable [] executeSimple(Object[][] params) {
				AtomicBoolean invalid = new AtomicBoolean(false);
				// median requires the numbers to be sorted from lowest to biggest
				Number[] sorted = Arrays.stream(params[0])
					.filter(object -> {
						if (!(object instanceof Number number))
							return false;
						if (Double.isNaN(number.doubleValue())) {
							invalid.set(true);
							return false;
						}
						return true;
					})
					.map(object -> (Number) object)
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
					return new Number[]{sorted[Math2.ceil(size /2)]};
				// If not, we grab the rounded up and rounded down numbers, then get the average of those
				int half = size / 2;
				double first = (sorted[half - 1]).doubleValue();
				double second = (sorted[half]).doubleValue();
				double median = (first+second)/2;
				return new Number[]{median};
			}
		})
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
			.since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<>("factorial", new Parameter[]{
			new Parameter<>("number", DefaultClasses.NUMBER, true, null)
		}, DefaultClasses.NUMBER, true) {
			@Override
			public Number @Nullable [] executeSimple(Object[][] params) {
				Double number = ((Number) params[0][0]).doubleValue();
				if (number < 0) {
					return null;
				} else if (number <= 1) { // 0 and 1
					return new Number[]{1};
				} else if (number > 170) {
					return new Number[]{Double.POSITIVE_INFINITY};
				}
				Double result = 1d;
				for (double i = number; i > 1; i--) {
					if (result.isInfinite() || result.isNaN())
						break;
					result *= i;
				}
				return new Number[]{result};
			}
		})
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
			.since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Number>("root", new Parameter[]{
			new Parameter<>("n", DefaultClasses.NUMBER, true, null),
			new Parameter<>("number", DefaultClasses.NUMBER, true, null)
		}, DefaultClasses.NUMBER, true) {
			@Override
			public Number @Nullable [] executeSimple(Object[][] params) {
				Double n = ((Number) params[0][0]).doubleValue();
				Double number = ((Number) params[1][0]).doubleValue();
				if (n == 0) {
					return null;
				} else if (n == 1) {
					return new Number[]{number};
				} else if (n == 2) {
					return new Number[]{Math.sqrt(number)};
				}
				return new Number[]{Math.pow(number, (1 / n))};
			}
		})
			.description("Calculates the <i>n</i>th root of a number.")
			.examples(
				"root(2, 4) = 2 # same as sqrt(4)",
				"root(4, 16) = 2",
				"root(-4, 16) = 0.5 # same as 16^(-1/4)"
			)
			.since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Number>("permutations", new Parameter[]{
			new Parameter<>("options", DefaultClasses.NUMBER, true, null),
			new Parameter<>("selected", DefaultClasses.NUMBER, true, null)
		}, DefaultClasses.NUMBER, true) {
			@Override
			public Number @Nullable [] executeSimple(Object[][] params) {
				Double options = ((Number) params[0][0]).doubleValue();
				Double selected = ((Number) params[1][0]).doubleValue();
				if (selected > options || selected < 0) { // Illegal argument
					return null;
				} else if (selected.equals(0d)) { // Will always be 1
					return new Number[]{1};
				} else if (selected.equals(1d)) { // Will always be the number from 'options'
					return new Number[]{options};
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
				return new Number[]{result};
			}
		})
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
			.since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Number>("combinations", new Parameter[]{
				new Parameter<>("options", DefaultClasses.NUMBER, true, null),
				new Parameter<>("selected", DefaultClasses.NUMBER, true, null)
			}, DefaultClasses.NUMBER, true) {
				@Override
				public Number @Nullable [] executeSimple(Object[][] params) {
					Double options = ((Number) params[0][0]).doubleValue();
					Double selected = ((Number) params[1][0]).doubleValue();
					if (selected > options || selected < 0) { // Illegal arguments
						return null;
					} else if (selected.equals(0d)) { // Will always return 1
						return new Number[]{1};
					} else if (selected.equals(1d)) { // Will always be the number from 'options'
						return new Number[]{options};
					}
					// By the same reasoning from 'permutations' there will always be a factorial that can cancel out
					// Example: options = 10, selected = 2 ; 10!/(10-2)!(2!) = 10!/(8!)(2!)
					// 10! = (10)(9)(8!) ; the 8! cancel out, leaving us with: (10)(9)/2!
					// 'top' will calculate the leftovers in the numerator: (10)(9)
					Double top = 1d;
					for (double i = options; i > options - selected; i--) {
						if (top.isInfinite() || top.isNaN())
							return new Number[]{top};
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
					return new Number[]{top/bottom};
				}
			})
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
			.since("INSERT VERSION");

	}

}
