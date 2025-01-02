package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.RegexMessage;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Fields;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaClasses {

	public static final int VARIABLENAME_NUMBERACCURACY = 8;

	/**
	 * The format of an integer.
	 * <p>
	 * Has an optional negative sign and may contain one underscores followed by any number of digits.
	 * </p>
	 */
	public static final String INTEGER_NUMBER_PATTERN = "-?\\d+(_\\d+)*";

	/**
	 * Matches an integer with an optional unit of radians or degrees.
	 * <p>
	 * First, the actual number format {@code num} is specified. Then, an optional angle unit is specified.
	 * For this, the {@code rad} group is used. This specifies that the number is in radians.
	 * This is used to determine if the number should be converted to degrees.
	 * Degrees is not a named group because it just returns the value in the {@code num} group, which
	 * is the default behaviour.
	 * Optionally, the user can use {@code x in degrees} instead of {@code x degrees}.
	 * </p>
	 */
	public static final Pattern INTEGER_PATTERN =
		Pattern.compile("(?<num>" + INTEGER_NUMBER_PATTERN + ")" +
			"(?: (?:in )?(?:(?<rad>rad(?:ian)?s?)|deg(?:ree)?s?))?");

	/**
	 * The format of a decimal number.
	 * <p>
	 * Has an optional negative sign and may contain one underscores followed by any number of digits,
	 * in the whole part or the fractional part. The fractional part is optional. May be followed by a percentage sign,
	 * to indicate that the number is a percentage.
	 * </p>
	 */
	public static final String DECIMAL_NUMBER_PATTERN = "-?\\d+(_\\d+)*(?>\\.\\d+(_\\d+)*)?%?";

	/**
	 * Matches a decimal number with an optional unit of radians or degrees.
	 * <p>
	 * First, the actual number format {@code num} is specified. Then, an optional angle unit is specified.
	 * For this, the {@code rad} group is used. This specifies that the number is in radians.
	 * This is used to determine if the number should be converted to degrees.
	 * Degrees is not a named group because it just returns the value in the {@code num} group, which
	 * is the default behaviour.
	 * Optionally, the user can use {@code x in degrees} instead of {@code x degrees}.
	 * </p>
	 */
	public static final Pattern DECIMAL_PATTERN =
		Pattern.compile("(?<num>" + DECIMAL_NUMBER_PATTERN + ")" +
			"(?: (?:in )?(?:(?<rad>rad(?:ian)?s?)|deg(?:ree)?s?))?");

	static {
		Classes.registerClass(new ClassInfo<>(Object.class, "object")
				.user("objects?")
				.name("Object")
				.description("The supertype of all types, meaning that if %object% is used in e.g. a condition it will accept all kinds of expressions.")
				.usage("")
				.examples("")
				.since("1.0"));

		Classes.registerClass(new ClassInfo<>(Number.class, "number")
				.user("num(ber)?s?")
				.name("Number")
				.description(
					"A number, e.g. 2.5, 3, -9812454, 30 degrees or 3.14 radians.",
					"Please note that many expressions only need integers, i.e. " +
						"will discard any fractional parts of any numbers without producing an error.",
					"Radians will be converted to degrees.")
				.usage("[-]###[.###] [[in ](rad[ian][s]|deg[ree][s])]</code> (any amount of digits; very large numbers will be truncated though)")
				.examples(
					"set the player's health to 5.5",
					"set {_temp} to 2*{_temp} - 2.5",
					"set {_angle} to 3.14 in radians # will be converted to degrees"
				)
				.since("1.0")
				// is registered after all other number classes
				.defaultExpression(new SimpleLiteral<>(1, true))
				.parser(new NumberParser())
				.serializer(new NumberSerializer()));

		Classes.registerClass(new ClassInfo<>(Long.class, "long")
				.user("int(eger)?s?")
				.name(ClassInfo.NO_DOC)
				.before("integer", "short", "byte")
				.defaultExpression(new SimpleLiteral<>((long) 1, true))
				.parser(new LongParser())
				.serializer(new LongSerializer()));

		Classes.registerClass(new ClassInfo<>(Integer.class, "integer")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>(1, true))
				.parser(new IntegerParser())
				.serializer(new IntegerSerializer()));

		Classes.registerClass(new ClassInfo<>(Double.class, "double")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>(1., true))
				.after("long")
				.before("float", "integer", "short", "byte")
				.parser(new DoubleParser())
				.serializer(new DoubleSerializer()));

		Classes.registerClass(new ClassInfo<>(Float.class, "float")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>(1f, true))
				.parser(new FloatParser())
				.serializer(new FloatSerializer()));

		Classes.registerClass(new ClassInfo<>(Boolean.class, "boolean")
				.user("booleans?")
				.name("Boolean")
				.description("A boolean is a value that is either true or false. Other accepted names are 'on' and 'yes' for true, and 'off' and 'no' for false.")
				.usage("true/yes/on or false/no/off")
				.examples("set {config.%player%.use mod} to false")
				.since("1.0")
				.parser(new Parser<>() {
					private final RegexMessage truePattern = new RegexMessage("boolean.true.pattern");
					private final RegexMessage falsePattern = new RegexMessage("boolean.false.pattern");

					@Override
					@Nullable
					public Boolean parse(String s, ParseContext context) {
						if (truePattern.matcher(s).matches())
							return Boolean.TRUE;
						if (falsePattern.matcher(s).matches())
							return Boolean.FALSE;
						return null;
					}

					private final Message trueName = new Message("boolean.true.name");
					private final Message falseName = new Message("boolean.false.name");

					@Override
					public String toString(Boolean b, int flags) {
						return b ? trueName.toString() : falseName.toString();
					}

					@Override
					public String toVariableNameString(Boolean b) {
						return "" + b;
					}
				}).serializer(new Serializer<Boolean>() {
					@Override
					public Fields serialize(Boolean n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}

					@Override
					public boolean canBeInstantiated() {
						return true;
					}

					@Override
					public void deserialize(Boolean o, Fields f) {
						assert false;
					}

					@Override
					@Nullable
					public Boolean deserialize(String s) {
						if (s.equals("true"))
							return Boolean.TRUE;
						if (s.equals("false"))
							return Boolean.FALSE;
						return null;
					}

					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));

		Classes.registerClass(new ClassInfo<>(Short.class, "short")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>((short) 1, true))
				.parser(new ShortParser())
				.serializer(new ShortSerializer()));

		Classes.registerClass(new ClassInfo<>(Byte.class, "byte")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>((byte) 1, true))
				.parser(new ByteParser())
				.serializer(new ByteSerializer()));

		Classes.registerClass(new ClassInfo<>(String.class, "string")
				.user("(text|string)s?")
				.name("Text")
				.description("Text is simply text, i.e. a sequence of characters, which can optionally contain expressions which will be replaced with a meaningful representation " +
						"(e.g. %player% will be replaced with the player's name).",
						"Because scripts are also text, you have to put text into double quotes to tell Skript which part of the line is an effect/expression and which part is the text.",
						"Please read the article on <a href='./text.html'>Texts and Variable Names</a> to learn more.")
				.usage("simple: \"...\"",
						"quotes: \"...\"\"...\"",
						"expressions: \"...%expression%...\"",
						"percent signs: \"...%%...\"")
				.examples("broadcast \"Hello World!\"",
						"message \"Hello %player%\"",
						"message \"The id of \"\"%type of tool%\"\" is %id of tool%.\"")
				.since("1.0")
				.parser(new Parser<>() {
					@Override
					@Nullable
					public String parse(String s, ParseContext context) {
						switch (context) {
							case DEFAULT: // in DUMMY, parsing is handled by VariableString
								assert false;
								return null;
							case CONFIG: // duh
								return s;
							case SCRIPT:
							case EVENT:
								if (VariableString.isQuotedCorrectly(s, true))
									return Utils.replaceChatStyles("" + s.substring(1, s.length() - 1).replace("\"\"", "\""));
								return null;
							case COMMAND:
							case PARSE:
								return s;
						}
						assert false;
						return null;
					}

					@Override
					public boolean canParse(ParseContext context) {
						return context != ParseContext.DEFAULT;
					}

					@Override
					public String toString(String s, int flags) {
						return s;
					}

					@Override
					public String getDebugMessage(String s) {
						return '"' + s + '"';
					}

					@Override
					public String toVariableNameString(String s) {
						return s;
					}
				}).serializer(new Serializer<String>() {
					@Override
					public Fields serialize(String n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}

					@Override
					public boolean canBeInstantiated() {
						return true;
					}

					@Override
					public void deserialize(String o, Fields f) {
						assert false;
					}

					@Override
					public String deserialize(String s) {
						return s;
					}

					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));

		// joml type - for display entities
		if (Skript.classExists("org.joml.Quaternionf"))
			Classes.registerClass(new ClassInfo<>(Quaternionf.class, "quaternion")
				.user("quaternionf?s?")
				.name("Quaternion")
				.description("Quaternions are four dimensional vectors, often used for representing rotations.")
				.since("2.10")
				.parser(new Parser<>() {
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(Quaternionf quaternion, int flags) {
						return "w:" + Skript.toString(quaternion.w()) + ", x:" + Skript.toString(quaternion.x()) + ", y:" + Skript.toString(quaternion.y()) + ", z:" + Skript.toString(quaternion.z());
					}

					@Override
					public String toVariableNameString(Quaternionf quaternion) {
						return quaternion.w() + "," + quaternion.x() + "," + quaternion.y() + "," + quaternion.z();
					}
				})
				.defaultExpression(new EventValueExpression<>(Quaternionf.class))
				.cloner(quaternion -> {
					try {
						// Implements cloneable, but doesn't return a Quaternionf.
						// org.joml improper override. Returns Object.
						return (Quaternionf) quaternion.clone();
					} catch (CloneNotSupportedException e) {
						return null;
					}
				}));
	}

	/**
	 * Converts a string to a number formatted as an integer.
	 * <p>
	 * Applies {@code stringToNumber} for parsing the number, then tries to
	 * convert radians to degrees if the string contains a radian group.
	 * </p>
	 *
	 * @param string The string with the possible number.
	 * @param stringToNumber The function to parse the number, e.g. {@link Integer#parseInt(String)}.
	 * @return The parsed string, or null if the string could not be parsed.
	 */
	@Contract(pure = true)
	private static <T extends Number> @Nullable T convertIntegerFormatted(
		String string,
		Function<String, T> stringToNumber
	) {
		Matcher matcher = INTEGER_PATTERN.matcher(string);

		if (!matcher.matches())
			return null;

		String number = matcher.group("num").replace("_", "");
		if (matcher.group("rad") != null) {
			try {
				//noinspection unchecked
				return (T) (Double) Math.toDegrees(stringToNumber.apply(number).doubleValue());
			} catch (NumberFormatException ignored) {
			}
		} else {
			try {
				return stringToNumber.apply(number);
			} catch (NumberFormatException ignored) {
			}
		}

		return null;
	}

	/**
	 * Converts a string to a number formatted as a decimal.
	 * <p>
	 * Applies {@code stringToNumber} for parsing the number.
	 * If the number is a percentage, it gets parsed using the double value divided by 100, and {@code fromDouble}.
	 * Then tries to convert radians to degrees if the string contains a radian group.
	 * </p>
	 *
	 * @param string The string with the possible number.
	 * @param stringToNumber The function to parse the number, e.g. {@link Integer#parseInt(String)}.
	 * @return The parsed string, or null if the string could not be parsed.
	 */
	@Contract(pure = true)
	private static <T extends Number> @Nullable T convertDecimalFormatted(
		String string,
		Function<String, T> stringToNumber
	) {
		Matcher matcher = DECIMAL_PATTERN.matcher(string);

		if (!matcher.matches())
			return null;

		String number = matcher.group("num").replace("_", "");
		try {
			T result;
			if (number.endsWith("%")) {
				T extracted = stringToNumber.apply(number.substring(0, number.length() - 1));
				//noinspection unchecked
				result = (T) (Double) (extracted.doubleValue() / 100.0);
			} else {
				result = stringToNumber.apply(number);
			}

			if (matcher.group("rad") != null) {
				try {
					//noinspection unchecked
					return (T) (Double) Math.toDegrees(result.doubleValue());
				} catch (NumberFormatException ignored) {
				}
			}

			return result;
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static class NumberParser extends Parser<Number> {

		@Override
		public @Nullable Number parse(String string, ParseContext context) {
			Matcher numberMatcher = DECIMAL_PATTERN.matcher(string);
			if (!numberMatcher.matches())
				return null;

			Integer integerAttempt = convertIntegerFormatted(string, Integer::parseInt);
			if (integerAttempt != null)
				return integerAttempt;

			Double parsed = convertDecimalFormatted(string, Double::parseDouble);
			return parsed == null || parsed.isInfinite() || parsed.isNaN() ? null : parsed;
		}

		@Override
		public String toString(Number number, int flags) {
			return StringUtils.toString(number.doubleValue(), SkriptConfig.numberAccuracy.value());
		}

		@Override
		public String toVariableNameString(Number number) {
			return StringUtils.toString(number.doubleValue(), VARIABLENAME_NUMBERACCURACY);
		}

	}

	private static class NumberSerializer extends Serializer<Number> {

		@Override
		public Fields serialize(Number number) {
			throw new IllegalStateException(); // serialised natively by Yggdrasil
		}

		@Override
		public boolean canBeInstantiated() {
			return true;
		}

		@Override
		public void deserialize(Number number, Fields fields) {
			assert false;
		}

		@Override
		public @Nullable Number deserialize(String string) {
			try {
				return Integer.valueOf(string);
			} catch (NumberFormatException ignored) {
			}
			try {
				return Double.valueOf(string);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

	}

	private static class LongParser extends Parser<Long> {

		@Override
		public @Nullable Long parse(String string, ParseContext context) {
			return convertIntegerFormatted(string, Long::parseLong);
		}

		@Override
		public String toString(Long l, int flags) {
			return l.toString();
		}

		@Override
		public String toVariableNameString(Long l) {
			return l.toString();
		}

	}

	private static class LongSerializer extends Serializer<Long> {

		@Override
		public Fields serialize(Long l) {
			throw new IllegalStateException(); // serialised natively by Yggdrasil
		}

		@Override
		public boolean canBeInstantiated() {
			return true;
		}

		@Override
		public void deserialize(Long l, Fields fields) {
			assert false;
		}

		@Override
		public @Nullable Long deserialize(String string) {
			try {
				return Long.parseLong(string);
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

	}

	private static class IntegerParser extends Parser<Integer> {

		@Override
		public @Nullable Integer parse(String string, ParseContext context) {
			return convertIntegerFormatted(string, Integer::parseInt);
		}

		@Override
		public String toString(Integer i, int flags) {
			return i.toString();
		}

		@Override
		public String toVariableNameString(Integer i) {
			return i.toString();
		}

	}

	private static class IntegerSerializer extends Serializer<Integer> {

		@Override
		public Fields serialize(Integer i) {
			throw new IllegalStateException(); // serialised natively by Yggdrasil
		}

		@Override
		public boolean canBeInstantiated() {
			return true;
		}

		@Override
		public void deserialize(Integer i, Fields fields) {
			assert false;
		}

		@Override
		public @Nullable Integer deserialize(String string) {
			try {
				return Integer.parseInt(string);
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

	}

	private static class DoubleParser extends Parser<Double> {

		@Override
		public @Nullable Double parse(String string, ParseContext context) {
			Double parsed = convertDecimalFormatted(string, Double::parseDouble);

			return parsed == null || parsed.isInfinite() || parsed.isNaN() ? null : parsed;
		}

		@Override
		public String toString(Double d, int flags) {
			return StringUtils.toString(d, SkriptConfig.numberAccuracy.value());
		}

		@Override
		public String toVariableNameString(Double d) {
			return StringUtils.toString(d, VARIABLENAME_NUMBERACCURACY);
		}

	}

	private static class DoubleSerializer extends Serializer<Double> {

		@Override
		public Fields serialize(Double d) {
			throw new IllegalStateException(); // serialised natively by Yggdrasil
		}

		@Override
		public boolean canBeInstantiated() {
			return true;
		}

		@Override
		public void deserialize(Double d, Fields fields) {
			assert false;
		}

		@Override
		public @Nullable Double deserialize(String string) {
			try {
				return Double.parseDouble(string);
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

	}

	private static class FloatParser extends Parser<Float> {

		@Override
		public @Nullable Float parse(String string, ParseContext context) {
			Float parsed = convertDecimalFormatted(string, Float::parseFloat);

			return parsed == null || parsed.isInfinite() || parsed.isNaN() ? null : parsed;
		}

		@Override
		public String toString(Float f, int flags) {
			return StringUtils.toString(f, SkriptConfig.numberAccuracy.value());
		}

		@Override
		public String toVariableNameString(Float f) {
			return StringUtils.toString(f.doubleValue(), VARIABLENAME_NUMBERACCURACY);
		}

	}

	private static class FloatSerializer extends Serializer<Float> {

		@Override
		public Fields serialize(Float f) {
			throw new IllegalStateException(); // serialised natively by Yggdrasil
		}

		@Override
		public boolean canBeInstantiated() {
			return true;
		}

		@Override
		public void deserialize(Float f, Fields fields) {
			assert false;
		}

		@Override
		public @Nullable Float deserialize(String string) {
			try {
				return Float.parseFloat(string);
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

	}

	private static class ShortParser extends Parser<Short> {

		@Override
		public @Nullable Short parse(String string, ParseContext context) {
			return convertIntegerFormatted(string, Short::parseShort);
		}

		@Override
		public String toString(Short s, int flags) {
			return s.toString();
		}

		@Override
		public String toVariableNameString(Short s) {
			return s.toString();
		}

	}

	private static class ShortSerializer extends Serializer<Short> {

		@Override
		public Fields serialize(Short s) {
			throw new IllegalStateException(); // serialised natively by Yggdrasil
		}

		@Override
		public boolean canBeInstantiated() {
			return true;
		}

		@Override
		public void deserialize(Short s, Fields fields) {
			assert false;
		}

		@Override
		public @Nullable Short deserialize(String string) {
			try {
				return Short.parseShort(string);
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

	}

	private static class ByteParser extends Parser<Byte> {

		@Override
		public @Nullable Byte parse(String string, ParseContext context) {
			return convertIntegerFormatted(string, Byte::parseByte);
		}

		@Override
		public String toString(Byte b, int flags) {
			return b.toString();
		}

		@Override
		public String toVariableNameString(Byte b) {
			return b.toString();
		}

	}

	private static class ByteSerializer extends Serializer<Byte> {

		@Override
		public Fields serialize(Byte b) {
			throw new IllegalStateException(); // serialised natively by Yggdrasil
		}

		@Override
		public boolean canBeInstantiated() {
			return true;
		}

		@Override
		public void deserialize(Byte b, Fields fields) {
			assert false;
		}

		@Override
		public @Nullable Byte deserialize(String string) {
			try {
				return Byte.parseByte(string);
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

	}

}
