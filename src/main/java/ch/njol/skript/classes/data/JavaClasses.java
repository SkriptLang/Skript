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

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.RegexMessage;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Fields;
import org.eclipse.jdt.annotation.Nullable;

import java.util.regex.Pattern;

public class JavaClasses {

	public static final int VARIABLENAME_NUMBERACCURACY = 8;
	public static final Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");
	public static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(?>\\.[0-9]+)?%?");

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
				.description("A number, e.g. 2.5, 3, or -9812454.",
						"Please note that many expressions only need integers, i.e. will discard any fractional parts of any numbers without producing an error.")
				.usage("[-]###[.###]</code> (any amount of digits; very large numbers will be truncated though)")
				.examples("set the player's health to 5.5",
						"set {_temp} to 2*{_temp} - 2.5")
				.since("1.0")
				// is registered after all other number classes
				.defaultExpression(new SimpleLiteral<>(1, true))
				.parser(new Parser<Number>() {
					@Override
					@Nullable
					public Number parse(String s, ParseContext context) {
						if (!NUMBER_PATTERN.matcher(s).matches())
							return null;
						if (INTEGER_PATTERN.matcher(s).matches()) {
							try {
								return Long.valueOf(s);
							} catch (NumberFormatException ignored) { }
						}
						try {
							Double d = s.endsWith("%") ? Double.parseDouble(s.substring(0, s.length() - 1)) / 100 : Double.parseDouble(s);
							if (d.isNaN() || d.isInfinite())
								return null;
							return d;
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(Number n, int flags) {
						return StringUtils.toString(n.doubleValue(), SkriptConfig.numberAccuracy.value());
					}
					
					@Override
					public String toVariableNameString(Number n) {
						return StringUtils.toString(n.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
                }).serializer(new Serializer<Number>() {
					@Override
					public Fields serialize(Number n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(Number o, Fields f) {
						assert false;
					}

					@Override
					@Nullable
					public Number deserialize(String s) {
						try {
							return Integer.valueOf(s);
						} catch (NumberFormatException ignored) {}
						try {
							return Double.valueOf(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));
		
		Classes.registerClass(new ClassInfo<>(Long.class, "long")
				.user("int(eger)?s?")
				.name(ClassInfo.NO_DOC)
				.before("integer", "short", "byte")
				.defaultExpression(new SimpleLiteral<>((long) 1, true))
				.parser(new Parser<Long>() {
					@Override
					@Nullable
					public Long parse(String s, ParseContext context) {
						if (!INTEGER_PATTERN.matcher(s).matches())
							return null;
						try {
							return Long.valueOf(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(Long l, int flags) {
						return "" + l;
					}
					
					@Override
					public String toVariableNameString(Long l) {
						return "" + l;
					}
                }).serializer(new Serializer<Long>() {
					@Override
					public Fields serialize(Long n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(Long o, Fields f) {
						assert false;
					}

					@Override
					@Nullable
					public Long deserialize(String s) {
						try {
							return Long.parseLong(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));
		
		Classes.registerClass(new ClassInfo<>(Integer.class, "integer")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>(1, true))
				.parser(new Parser<Integer>() {
					@Override
					@Nullable
					public Integer parse(String s, ParseContext context) {
						if (!INTEGER_PATTERN.matcher(s).matches())
							return null;
						try {
							return Integer.valueOf(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(Integer i, int flags) {
						return "" + i;
					}
					
					@Override
					public String toVariableNameString(Integer i) {
						return "" + i;
					}
                }).serializer(new Serializer<Integer>() {
					@Override
					public Fields serialize(Integer n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(Integer o, Fields f) {
						assert false;
					}

					@Override
					@Nullable
					public Integer deserialize(String s) {
						try {
							return Integer.parseInt(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));
		
		Classes.registerClass(new ClassInfo<>(Double.class, "double")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>(1., true))
				.after("long")
				.before("float", "integer", "short", "byte")
				.parser(new Parser<Double>() {
					@Override
					@Nullable
					public Double parse(String s, ParseContext context) {
						if (!NUMBER_PATTERN.matcher(s).matches())
							return null;
						try {
							Double d = s.endsWith("%") ? Double.parseDouble(s.substring(0, s.length() - 1)) / 100 : Double.parseDouble(s);
							if (d.isNaN() || d.isInfinite())
								return null;
							return d;
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(Double d, int flags) {
						return StringUtils.toString(d, SkriptConfig.numberAccuracy.value());
					}
					
					@Override
					public String toVariableNameString(Double d) {
						return StringUtils.toString(d, VARIABLENAME_NUMBERACCURACY);
					}
                }).serializer(new Serializer<Double>() {
					@Override
					public Fields serialize(Double n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(Double o, Fields f) {
						assert false;
					}

					@Override
					@Nullable
					public Double deserialize(String s) {
						try {
							return Double.parseDouble(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));
		
		Classes.registerClass(new ClassInfo<>(Float.class, "float")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>(1f, true))
				.parser(new Parser<Float>() {
					@Override
					@Nullable
					public Float parse(String s, ParseContext context) {
						if (!NUMBER_PATTERN.matcher(s).matches())
							return null;
						try {
							Float f = s.endsWith("%") ? Float.parseFloat(s.substring(0, s.length() - 1)) / 100 : Float.parseFloat(s);
							if (f.isNaN() || f.isInfinite()) {
								return null;
							}
							return f;
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(Float f, int flags) {
						return StringUtils.toString(f, SkriptConfig.numberAccuracy.value());
					}
					
					@Override
					public String toVariableNameString(Float f) {
						return StringUtils.toString(f.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
                }).serializer(new Serializer<Float>() {
					@Override
					public Fields serialize(Float n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(Float o, Fields f) {
						assert false;
					}

					@Override
					@Nullable
					public Float deserialize(String s) {
						try {
							return Float.parseFloat(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));
		
		Classes.registerClass(new ClassInfo<>(Boolean.class, "boolean")
				.user("booleans?")
				.name("Boolean")
				.description("A boolean is a value that is either true or false. Other accepted names are 'on' and 'yes' for true, and 'off' and 'no' for false.")
				.usage("true/yes/on or false/no/off")
				.examples("set {config.%player%.use mod} to false")
				.since("1.0")
				.parser(new Parser<Boolean>() {
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
				.parser(new Parser<Short>() {
					@Override
					@Nullable
					public Short parse(String s, ParseContext context) {
						if (!INTEGER_PATTERN.matcher(s).matches())
							return null;
						try {
							return Short.valueOf(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(Short s, int flags) {
						return "" + s;
					}
					
					@Override
					public String toVariableNameString(Short s) {
						return "" + s;
					}
                }).serializer(new Serializer<Short>() {
					@Override
					public Fields serialize(Short n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(Short o, Fields f) {
						assert false;
					}

					@Override
					@Nullable
					public Short deserialize(String s) {
						try {
							return Short.parseShort(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));
		
		Classes.registerClass(new ClassInfo<>(Byte.class, "byte")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>((byte) 1, true))
				.parser(new Parser<Byte>() {
					@Override
					@Nullable
					public Byte parse(String s, ParseContext context) {
						if (!INTEGER_PATTERN.matcher(s).matches())
							return null;
						try {
							return Byte.valueOf(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(Byte b, int flags) {
						return "" + b;
					}
					
					@Override
					public String toVariableNameString(Byte b) {
						return "" + b;
					}
                }).serializer(new Serializer<Byte>() {
					@Override
					public Fields serialize(Byte n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(Byte o, Fields f) {
						assert false;
					}

					@Override
					@Nullable
					public Byte deserialize(String s) {
						try {
							return Byte.parseByte(s);
						} catch (NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));
		
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
				.parser(new Parser<String>() {
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
	}
}
