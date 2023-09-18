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

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
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
import org.eclipse.jdt.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.UUID;

public class DefaultFunctions {
	
	private static String str(double n) {
		return StringUtils.toString(n, 4);
	}
	
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

		Functions.registerFunction(new SimpleJavaFunction<Number>("clamp", new Parameter[]{
			new Parameter<>("values", DefaultClasses.NUMBER, false, null),
			new Parameter<>("min", DefaultClasses.NUMBER, true, null),
			new Parameter<>("max", DefaultClasses.NUMBER, true, null)
		}, DefaultClasses.NUMBER, false) {
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
			.since("INSERT VERSION");

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
			public Location[] execute(FunctionEvent<?> e, Object[][] params) {
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
			.examples("location(0, 128, 0)",
						"location(player's x-coordinate, player's y-coordinate + 5, player's z-coordinate, player's world, 0, 90)",
						"location(0, 64, 0, world \"world_nether\")",
						"location(100, 110, -145, world(\"my_custom_world\"))")
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
			new Parameter<>("blue", DefaultClasses.LONG, true, null)
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Long red = (Long) params[0][0];
				Long green = (Long) params[1][0];
				Long blue = (Long) params[2][0];
				
				return CollectionUtils.array(new ColorRGB(red.intValue(), green.intValue(), blue.intValue()));
			}
		}).description("Returns a RGB color from the given red, green and blue parameters.")
			.examples("dye player's leggings rgb(120, 30, 45)")
			.since("2.5");

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
			.since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<OfflinePlayer>("offlineplayer", new Parameter[] {
			new Parameter<>("nameOrUUID", DefaultClasses.STRING, true, null)
		}, DefaultClasses.OFFLINE_PLAYER, true) {
			@Override
			public OfflinePlayer[] executeSimple(Object[][] params) {
				String name = (String) params[0][0];
				UUID uuid = null;
				if (name.length() > 16 || name.contains("-")) { // shortcut
					try {
						uuid = UUID.fromString(name);
					} catch (IllegalArgumentException ignored) {}
				}
				return CollectionUtils.array(uuid != null ? Bukkit.getOfflinePlayer(uuid) : Bukkit.getOfflinePlayer(name));
			}
		}).description("Returns a offline player from their name or UUID. This function will still return the player if they're online.")
			.examples("set {_p} to offlineplayer(\"Notch\")", "set {_p} to offlineplayer(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\")")
			.since("INSERT VERSION");
	}
	
}
