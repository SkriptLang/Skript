package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Utils;
import ch.njol.util.Math2;
import ch.njol.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultFunctions {

	private static String str(double n) {
		return StringUtils.toString(n, 4);
	}

	private static final DecimalFormat DEFAULT_INTEGER_FORMAT = new DecimalFormat("###,###");
	private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("###,###.##");

	static {
		SkriptAddon skript = Skript.instance();

		// basic math functions

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
		.parameter("d", Number.class, Modifier.OPTIONAL)
		.build(args -> {
			if (args.get("n") instanceof Long longValue)
				return longValue;
			double value = args.<Number>get("n").doubleValue();
			if (!Double.isFinite(value))
				return value;

			double placementDouble = args.<Number>getOrDefault("d", 0).doubleValue();
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
			if (num instanceof Long)
				return (Long) num;
			return Math2.ceil(num.doubleValue());
		}));

	Functions.register(DefaultFunction.builder(skript, "ceiling", Long.class)
		.description("Alias of <a href='#ceil'>ceil</a>.")
		.examples("ceiling(2.34) = 3", "ceiling(2) = 2", "ceiling(2.99) = 3")
		.since("2.2")
		.parameter("n", Number.class)
		.build(args -> {
			Number num = args.get("n");
			if (num instanceof Long)
				return (Long) num;
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
		.description("The exponential function. You probably don't need this if you don't know what this is.")
		.examples("exp(0) = 1", "exp(1) = " + str(Math.exp(1)))
		.since("2.2")
		.parameter("n", Number.class)
		.build(args -> Math.exp(args.<Number>get("n").doubleValue())));

	Functions.register(DefaultFunction.builder(skript, "ln", Number.class)
		.description("The natural logarithm. You probably don't need this if you don't know what this is.",
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
		.parameter("values", Number[].class)
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
		.build(args -> {
			Number[] values = args.get("values");
			Double[] clampedValues = new Double[values.length];
			double min = args.<Number>get("min").doubleValue();
			double max = args.<Number>get("max").doubleValue();
			// we'll be nice and swap them if they're in the wrong order
			double trueMin = Math.min(min, max);
			double trueMax = Math.max(min, max);
			for (int i = 0; i < values.length; i++) {
				double value = values[i].doubleValue();
				clampedValues[i] = Math.max(Math.min(value, trueMax), trueMin);
			}
			return clampedValues;
		}));

		Functions.register(DefaultFunction.builder(skript, "toBase", String[].class)
			.description("""
				Turns a number in a string using a specific base (decimal, hexadecimal, octal).
				For example, converting 32 to hexadecimal (base 16) would be 'toBase(32, 16)', which would return "20".
				You can use any base between 2 and 36.
				""")
			.examples(
				"send \"Decode this binary number for a prize! %toBase({_guess}, 2)%\""
			)
			.since("2.14")
			.parameter("n", Long[].class)
			.parameter("base", Long.class, Modifier.ranged(2, 36))
			.contract(new Contract() {
				@Override
				public boolean isSingle(Expression<?>... arguments) {
					return arguments[0].isSingle();
				}

				@Override
				public Class<?> getReturnType(Expression<?>... arguments) {
					return String.class;
				}
			})
			.build(args -> {
				Long[] n = args.get("n");
				Long base = args.get("base");
				String[] results = new String[n.length];
				for (int i = 0; i < n.length; i++) {
					results[i] = Long.toString(n[i], base.intValue());
				}
				return results;
			}));

		Functions.register(DefaultFunction.builder(skript, "fromBase", Long[].class)
			.description("""
				Turns a text version of a number in a specific base (decimal, hexadecimal, octal) into an actual number.
				For example, converting "20" in hexadecimal (base 16) would be 'fromBase("20", 16)', which would return 32.
				You can use any base between 2 and 36.
				""")
			.examples("""
				# /binaryText 01110011 01101011 01110010 01101001 01110000 01110100 00100001
				# sends "skript!"
				command binaryText <text>:
					trigger:
					set {_characters::*} to argument split at " " without trailing empty string
						transform {_characters::*} with fromBase(input, 2) # convert to codepoints
						transform {_characters::*} with character from codepoint input # convert to characters
						send join {_characters::*}
				""")
			.since("2.14")
			.parameter("string value", String[].class)
			.parameter("base", Long.class, Modifier.ranged(2, 36))
			.contract(new Contract() {
				@Override
				public boolean isSingle(Expression<?>... arguments) {
					return arguments[0].isSingle();
				}

				@Override
				public Class<?> getReturnType(Expression<?>... arguments) {
					return Long.class;
				}
			})
			.build(args -> {
				String[] n = args.get("string value");
				Long base = args.get("base");
				Long[] results = new Long[n.length];
				try {
					for (int i = 0; i < n.length; i++) {
						results[i] = Long.parseLong(n[i], base.intValue());
					}
				} catch (NumberFormatException e) {
					return null;
				}
				return results;
			}));

		// misc

	Functions.register(DefaultFunction.builder(skript, "world", World.class)
		.description("Gets a world from its name.")
		.examples("set {_nether} to world(\"%{_world}%_nether\")")
		.since("2.2")
		.parameter("name", String.class)
		.build(args -> Bukkit.getWorld((String) args.get("name"))));

		Functions.register(DefaultFunction.builder(skript, "location", Location.class)
			.description(
				"Creates a location from a world and 3 coordinates, with an optional yaw and pitch.",
				"If for whatever reason the world is not found, it will fallback to the server's main world."
			)
			.examples("""
				# TELEPORTING
				teleport player to location(1,1,1, world "world")
				teleport player to location(1,1,1, world "world", 100, 0)
				teleport player to location(1,1,1, world "world", yaw of player, pitch of player)
				teleport player to location(1,1,1, world of player)
				teleport player to location(1,1,1, world("world"))
				teleport player to location({_x}, {_y}, {_z}, {_w}, {_yaw}, {_pitch})
				
				# SETTING BLOCKS
				set block at location(1,1,1, world "world") to stone
				set block at location(1,1,1, world "world", 100, 0) to stone
				set block at location(1,1,1, world of player) to stone
				set block at location(1,1,1, world("world")) to stone
				set block at location({_x}, {_y}, {_z}, {_w}) to stone
				
				# USING VARIABLES
				set {_l1} to location(1,1,1)
				set {_l2} to location(10,10,10)
				set blocks within {_l1} and {_l2} to stone
				if player is within {_l1} and {_l2}:
				
				# OTHER
				kill all entities in radius 50 around location(1,65,1, world "world")
				delete all entities in radius 25 around location(50,50,50, world "world_nether")
				ignite all entities in radius 25 around location(1,1,1, world of player)
				"""
			)
			.since("2.2")
			.parameter("x", Number.class)
			.parameter("y", Number.class)
			.parameter("z", Number.class)
			.parameter("world", World.class, Modifier.OPTIONAL)
			.parameter("yaw", Float.class, Modifier.OPTIONAL)
			.parameter("pitch", Float.class, Modifier.OPTIONAL)
			.build(args -> {
				World world = args.getOrDefault("world", Bukkit.getWorlds().get(0));

				return new Location(world,
					args.<Number>get("x").doubleValue(), args.<Number>get("y").doubleValue(), args.<Number>get("z").doubleValue(),
					args.getOrDefault("yaw", 0f), args.getOrDefault("pitch", 0f));
			}));

	Functions.register(DefaultFunction.builder(skript, "date", Date.class)
		.description("Creates a date from a year, month, and day, and optionally also from hour, minute, second and millisecond.",
						"A time zone and DST offset can be specified as well (in minutes), if they are left out the server's time zone and DST offset are used (the created date will not retain this information).")
		.examples("date(2014, 10, 1) # 0:00, 1st October 2014", "date(1990, 3, 5, 14, 30) # 14:30, 5th May 1990", "date(1999, 12, 31, 23, 59, 59, 999, -3*60, 0) # almost year 2000 in parts of Brazil (-3 hours offset, no DST)")
		.since("2.2")
		.parameter("year", Number.class)
		.parameter("month", Number.class)
		.parameter("day", Number.class)
		.parameter("hour", Number.class, Modifier.OPTIONAL)
		.parameter("minute", Number.class, Modifier.OPTIONAL)
		.parameter("second", Number.class, Modifier.OPTIONAL)
		.parameter("millisecond", Number.class, Modifier.OPTIONAL)
		.parameter("zone_offset", Number.class, Modifier.OPTIONAL)
		.parameter("dst_offset", Number.class, Modifier.OPTIONAL)
		.build(args -> {
			int[] fields = {
				Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
				Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND,
				Calendar.ZONE_OFFSET, Calendar.DST_OFFSET
			};
			int[] offsets = {
				0, -1, 0,
				0, 0, 0, 0,
				0, 0
			};
			double[] scale = {
				1, 1, 1,
				1, 1, 1, 1,
				1000 * 60, 1000 * 60
			};
			double[] relations = {
				1. / 12, 1. / 30,
				1. / 24, 1. / 60, 1. / 60, 1. / 1000,
				0, 0,
				0
			};
			
			String[] paramNames = {"year", "month", "day", "hour", "minute", "second", "millisecond", "zone_offset", "dst_offset"};
			Calendar c = Calendar.getInstance();
			c.setLenient(true);
			double carry = 0;
			for (int i = 0; i < fields.length; i++) {
				Number n = args.getOrDefault(paramNames[i], i < 3 ? 0 : (i == 8 ? Double.NaN : 0));
				double value = n.doubleValue() * scale[i] + offsets[i] + carry;
				int v = (int) Math2.floor(value);
				carry = (value - v) * relations[i];
				//noinspection MagicConstant
				c.set(fields[i], v);
			}

			return new Date(c.getTimeInMillis(), c.getTimeZone());
		}));

		Functions.register(DefaultFunction.builder(skript, "vector", Vector.class)
			.description("Creates a vector from a single argument. Equivalent to vector(n, n, n).")
			.examples("vector(1) # = vector(1, 1, 1)")
			.since("2.15")
			.parameter("n", Number.class)
			.build(args -> {
				double value = args.<Number>get("n").doubleValue();
				return new Vector(value, value, value);
			}));

		Functions.register(DefaultFunction.builder(skript, "vector", Vector.class)
			.description("Creates a new vector, which can be used with various expressions, effects and functions.")
			.examples("vector(0, 0, 0)")
			.since("2.2-dev23")
			.parameter("x", Number.class)
			.parameter("y", Number.class)
			.parameter("z", Number.class)
			.build(args -> new Vector(
				args.<Number>get("x").doubleValue(),
				args.<Number>get("y").doubleValue(),
				args.<Number>get("z").doubleValue()
			)));

	Functions.register(DefaultFunction.builder(skript, "calcExperience", Long.class)
		.description("Calculates the total amount of experience needed to achieve given level from scratch in Minecraft.")
		.since("2.2-dev32")
		.parameter("level", Long.class)
		.build(args -> {
			long level = args.get("level");
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

			return exp;
		}));

	Functions.register(DefaultFunction.builder(skript, "rgb", ColorRGB.class)
		.description("Returns a RGB color from the given red, green and blue parameters. Alpha values can be added optionally, " +
						"but these only take affect in certain situations, like text display backgrounds.")
		.examples(
			"dye player's leggings rgb(120, 30, 45)",
			"set the colour of a text display to rgb(10, 50, 100, 50)"
		)
		.since("2.5, 2.10 (alpha)")
		.parameter("red", Long.class)
		.parameter("green", Long.class)
		.parameter("blue", Long.class)
		.parameter("alpha", Long.class, Modifier.OPTIONAL)
		.build(args -> {
			Long red = args.get("red");
			Long green = args.get("green");
			Long blue = args.get("blue");
			Long alpha = args.getOrDefault("alpha", 255L);

			return ColorRGB.fromRGBA(red.intValue(), green.intValue(), blue.intValue(), alpha.intValue());
		}));

		Functions.register(DefaultFunction.builder(skript, "player", Player.class)
			.description(
				"Returns an online player from their name or UUID, if player is offline function will return nothing.",
				"Setting 'getExactPlayer' parameter to true will return the player whose name is exactly equal to the provided name instead of returning a player that their name starts with the provided name."
			)
			.examples(
				"set {_p} to player(\"Notch\") # will return an online player whose name is or starts with 'Notch'",
				"set {_p} to player(\"Notch\", true) # will return the only online player whose name is 'Notch'",
				"set {_p} to player(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\") # <none> if player is offline"
			)
			.since("2.8.0")
			.parameter("nameOrUUID", String.class)
			.parameter("getExactPlayer", Boolean.class, Modifier.OPTIONAL)
			.build(args -> {
				String name = args.get("nameOrUUID");
				boolean isExact = args.getOrDefault("getExactPlayer", false);

				UUID uuid = null;
				if (name.length() > 16 || name.contains("-")) {
					if (Utils.isValidUUID(name))
						uuid = UUID.fromString(name);
				}

				return uuid != null ? Bukkit.getPlayer(uuid) : (isExact ? Bukkit.getPlayerExact(name) : Bukkit.getPlayer(name));
			}));

	{ // offline player function
		// TODO - remove this when Spigot support is dropped
		boolean hasIfCached = Skript.methodExists(Bukkit.class, "getOfflinePlayerIfCached", String.class);

		DefaultFunction.Builder<OfflinePlayer> builder = DefaultFunction.builder(skript, "offlineplayer", OfflinePlayer.class)
			.description(
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
			.since("2.8.0, 2.9.0 (prevent lookups)")
			.parameter("nameOrUUID", String.class);

		if (hasIfCached)
			builder.parameter("allowLookups", Boolean.class, Modifier.OPTIONAL);

		Functions.register(builder.build(args -> {
			String name = args.get("nameOrUUID");
			UUID uuid = null;
			if (name.length() > 16 || name.contains("-")) { // shortcut
				if (Utils.isValidUUID(name))
					uuid = UUID.fromString(name);
			}
			OfflinePlayer result;

			if (uuid != null) {
				result = Bukkit.getOfflinePlayer(uuid); // doesn't do lookups
			} else if (hasIfCached && !args.getOrDefault("allowLookups", true)) {
				result = Bukkit.getOfflinePlayerIfCached(name);
				if (result == null)
					return null;
			} else {
				result = Bukkit.getOfflinePlayer(name);
			}

			return result;
		}));
	} // end offline player function

	Functions.register(DefaultFunction.builder(skript, "isNaN", Boolean.class)
		.description("Returns true if the input is NaN (not a number).")
		.examples("isNaN(0) # false", "isNaN(0/0) # true", "isNaN(sqrt(-1)) # true")
		.since("2.8.0")
		.parameter("n", Number.class)
		.build(args -> Double.isNaN(args.<Number>get("n").doubleValue())));

		Functions.register(DefaultFunction.builder(skript, "concat", String.class)
			.description("Joins the provided texts (and other things) into a single text.")
			.examples(
				"concat(\"hello \", \"there\") # hello there",
				"concat(\"foo \", 100, \" bar\") # foo 100 bar"
			)
			.since("2.9.0")
			.parameter("texts", Object[].class)
			.build(args -> {
				StringBuilder builder = new StringBuilder();
				Object[] objects = args.get("texts");
				for (Object object : objects) {
					builder.append(Classes.toString(object));
				}
				return builder.toString();
			}));

	// joml functions - for display entities
	{
		if (Skript.classExists("org.joml.Quaternionf")) {
			Functions.register(DefaultFunction.builder(skript, "quaternion", Quaternionf.class)
				.description("Returns a quaternion from the given W, X, Y and Z parameters. ")
				.examples("quaternion(1, 5.6, 45.21, 10)")
				.since("2.10")
				.parameter("w", Number.class)
				.parameter("x", Number.class)
				.parameter("y", Number.class)
				.parameter("z", Number.class)
				.build(args -> {
					double w = args.<Number>get("w").doubleValue();
					double x = args.<Number>get("x").doubleValue();
					double y = args.<Number>get("y").doubleValue();
					double z = args.<Number>get("z").doubleValue();
					return new Quaternionf(x, y, z, w);
				}));
		}

		if (Skript.classExists("org.joml.AxisAngle4f")) {
			Functions.register(DefaultFunction.builder(skript, "axisAngle", Quaternionf.class)
				.description("Returns a quaternion from the given angle (in degrees) and axis (as a vector). This represents a rotation around the given axis by the given angle.")
				.examples("axisangle(90, (vector from player's facing))")
				.since("2.10")
				.parameter("angle", Number.class)
				.parameter("axis", Vector.class)
				.build(args -> {
					float angle = (float) (args.<Number>get("angle").floatValue() / 180 * Math.PI);
					Vector v = args.get("axis");
					if (v.isZero() || !Double.isFinite(v.getX()) || !Double.isFinite(v.getY()) || !Double.isFinite(v.getZ()))
						return null;
					Vector3f axis = v.toVector3f();
					return new Quaternionf(new AxisAngle4f(angle, axis));
				}));
		}
	} // end joml functions

	Functions.register(DefaultFunction.builder(skript, "formatNumber", String.class)
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
		.since("2.10")
		.parameter("number", Number.class)
		.parameter("format", String.class, Modifier.OPTIONAL)
		.build(args -> {
			Number number = args.get("number");
			String format = args.getOrDefault("format", "");

			if (format.isEmpty()) {
				if (number instanceof Double || number instanceof Float) {
					return DEFAULT_DECIMAL_FORMAT.format(number);
				} else {
					return DEFAULT_INTEGER_FORMAT.format(number);
				}
			}

			try {
				return new DecimalFormat(format).format(number);
			} catch (IllegalArgumentException e) {
				return null; // invalid format
			}
		}));

	Functions.register(DefaultFunction.builder(skript, "uuid", UUID.class)
		.description("Returns a UUID from the given string. The string must be in the format of a UUID.")
		.examples("uuid(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\")")
		.since("2.11")
		.parameter("uuid", String.class)
		.build(args -> {
			String uuid = args.get("uuid");
			if (Utils.isValidUUID(uuid))
				return UUID.fromString(uuid);
			return null;
		}));

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
				return sorted[Math2.ceil(size /2)];
			// If not, we grab the rounded up and rounded down numbers, then get the average of those
			int half = size / 2;
			double first = (sorted[half - 1]).doubleValue();
			double second = (sorted[half]).doubleValue();
			return (first+second)/2;
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
		.parameter("number", Number.class)
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
		.parameter("number", Number.class)
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
		.parameter("options", Number.class)
		.parameter("selected", Number.class)
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
		.parameter("options", Number.class)
		.parameter("selected", Number.class)
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
			return top/bottom;
		}));

	}

}
