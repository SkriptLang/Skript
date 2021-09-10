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

import java.io.StreamCorruptedException;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.NumberArithmetic;
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

/**
 * @author Peter Güttinger
 */
public class JavaClasses {
	public JavaClasses() {}
	
	public final static int VARIABLENAME_NUMBERACCURACY = 8;
	
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
				.defaultExpression(new SimpleLiteral<Number>(1, true))
				.parser(new Parser<Number>() {
					@Override
					@Nullable
					public Number parse(final String s, final ParseContext context) {
						try {
							return Long.valueOf(s);
						} catch (final NumberFormatException e) {}
						try {
							Double d = s.endsWith("%") ? Double.parseDouble(s.substring(0, s.length() - 1)) / 100 : Double.parseDouble(s);
							if (d.isNaN() || d.isInfinite()) {
								return null;
							}
							return d;
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Number n, final int flags) {
						return StringUtils.toString(n.doubleValue(), SkriptConfig.numberAccuracy.value());
					}
					
					@Override
					public String toVariableNameString(final Number n) {
						return StringUtils.toString(n.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+(\\.\\d+)?";
					}
				}).serializer(new Serializer<Number>() {
					@Override
					public Fields serialize(final Number n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(final Number o, final Fields f) throws StreamCorruptedException {
						assert false;
					}
					
//						return "" + n;
					@Override
					@Nullable
					public Number deserialize(final String s) {
						try {
							return Integer.valueOf(s);
						} catch (final NumberFormatException e) {}
						try {
							return Double.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}).math(Number.class, new NumberArithmetic()));
		
		Classes.registerClass(new ClassInfo<>(Long.class, "long")
				.user("int(eger)?s?")
				.name(ClassInfo.NO_DOC)
				.before("integer", "short", "byte")
				.defaultExpression(new SimpleLiteral<>((long) 1, true))
				.parser(new Parser<Long>() {
					@Override
					@Nullable
					public Long parse(final String s, final ParseContext context) {
						try {
							return Long.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Long l, final int flags) {
						return "" + l;
					}
					
					@Override
					public String toVariableNameString(final Long l) {
						return "" + l;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+";
					}
				}).serializer(new Serializer<Long>() {
					@Override
					public Fields serialize(final Long n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(final Long o, final Fields f) throws StreamCorruptedException {
						assert false;
					}
					
//						return "" + l;
					@Override
					@Nullable
					public Long deserialize(final String s) {
						try {
							return Long.parseLong(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}).math(Number.class, new NumberArithmetic()));
		
		Classes.registerClass(new ClassInfo<>(Integer.class, "integer")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>(1, true))
				.parser(new Parser<Integer>() {
					@Override
					@Nullable
					public Integer parse(final String s, final ParseContext context) {
						try {
							return Integer.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Integer i, final int flags) {
						return "" + i;
					}
					
					@Override
					public String toVariableNameString(final Integer i) {
						return "" + i;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+";
					}
				}).serializer(new Serializer<Integer>() {
					@Override
					public Fields serialize(final Integer n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(final Integer o, final Fields f) throws StreamCorruptedException {
						assert false;
					}
					
//						return "" + i;
					@Override
					@Nullable
					public Integer deserialize(final String s) {
						try {
							return Integer.parseInt(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}).math(Number.class, new NumberArithmetic()));
		
		Classes.registerClass(new ClassInfo<>(Double.class, "double")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>(1., true))
				.after("long")
				.before("float", "integer", "short", "byte")
				.parser(new Parser<Double>() {
					@Override
					@Nullable
					public Double parse(final String s, final ParseContext context) {
						try {
							Double d = s.endsWith("%") ? Double.parseDouble(s.substring(0, s.length() - 1)) / 100 : Double.parseDouble(s);
							if (d.isNaN() || d.isInfinite()) {
								return null;
							}
							return d;
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Double d, final int flags) {
						return StringUtils.toString(d, SkriptConfig.numberAccuracy.value());
					}
					
					@Override
					public String toVariableNameString(final Double d) {
						return StringUtils.toString(d, VARIABLENAME_NUMBERACCURACY);
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+(\\.\\d+)?";
					}
				}).serializer(new Serializer<Double>() {
					@Override
					public Fields serialize(final Double n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(final Double o, final Fields f) throws StreamCorruptedException {
						assert false;
					}
					
//						return "" + d;
					@Override
					@Nullable
					public Double deserialize(final String s) {
						try {
							return Double.parseDouble(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}).math(Number.class, new NumberArithmetic()));
		
		Classes.registerClass(new ClassInfo<>(Float.class, "float")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>(1f, true))
				.parser(new Parser<Float>() {
					@Override
					@Nullable
					public Float parse(final String s, final ParseContext context) {
						try {
							Float f = s.endsWith("%") ? Float.parseFloat(s.substring(0, s.length() - 1)) / 100 : Float.parseFloat(s);
							if (f.isNaN() || f.isInfinite()) {
								return null;
							}
							return f;
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Float f, final int flags) {
						return StringUtils.toString(f, SkriptConfig.numberAccuracy.value());
					}
					
					@Override
					public String toVariableNameString(final Float f) {
						return StringUtils.toString(f.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+(\\.\\d+)?";
					}
				}).serializer(new Serializer<Float>() {
					@Override
					public Fields serialize(final Float n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(final Float o, final Fields f) throws StreamCorruptedException {
						assert false;
					}
					
//						return "" + f;
					@Override
					@Nullable
					public Float deserialize(final String s) {
						try {
							return Float.parseFloat(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}).math(Number.class, new NumberArithmetic()));
		
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
					public Boolean parse(final String s, final ParseContext context) {
						if (truePattern.matcher(s).matches())
							return Boolean.TRUE;
						if (falsePattern.matcher(s).matches())
							return Boolean.FALSE;
						return null;
					}
					
					private final Message trueName = new Message("boolean.true.name");
					private final Message falseName = new Message("boolean.false.name");
					
					@Override
					public String toString(final Boolean b, final int flags) {
						return b ? trueName.toString() : falseName.toString();
					}
					
					@Override
					public String toVariableNameString(final Boolean b) {
						return "" + b;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "(true|false)";
					}
				}).serializer(new Serializer<Boolean>() {
					@Override
					public Fields serialize(final Boolean n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(final Boolean o, final Fields f) throws StreamCorruptedException {
						assert false;
					}
					
//						return "" + b;
					@Override
					@Nullable
					public Boolean deserialize(final String s) {
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
					public Short parse(final String s, final ParseContext context) {
						try {
							return Short.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Short s, final int flags) {
						return "" + s;
					}
					
					@Override
					public String toVariableNameString(final Short s) {
						return "" + s;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+";
					}
				}).serializer(new Serializer<Short>() {
					@Override
					public Fields serialize(final Short n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(final Short o, final Fields f) throws StreamCorruptedException {
						assert false;
					}
					
//						return "" + s;
					@Override
					@Nullable
					public Short deserialize(final String s) {
						try {
							return Short.parseShort(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}).math(Number.class, new NumberArithmetic()));
		
		Classes.registerClass(new ClassInfo<>(Byte.class, "byte")
				.name(ClassInfo.NO_DOC)
				.defaultExpression(new SimpleLiteral<>((byte) 1, true))
				.parser(new Parser<Byte>() {
					@Override
					@Nullable
					public Byte parse(final String s, final ParseContext context) {
						try {
							return Byte.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Byte b, final int flags) {
						return "" + b;
					}
					
					@Override
					public String toVariableNameString(final Byte b) {
						return "" + b;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+";
					}
				}).serializer(new Serializer<Byte>() {
					@Override
					public Fields serialize(final Byte n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(final Byte o, final Fields f) throws StreamCorruptedException {
						assert false;
					}
					
//						return "" + b;
					@Override
					@Nullable
					public Byte deserialize(final String s) {
						try {
							return Byte.parseByte(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}).math(Number.class, new NumberArithmetic()));
		
		Classes.registerClass(new ClassInfo<>(String.class, "string")
				.user("(text|string)s?")
				.name("Text")
				.description("Text is simply text, i.e. a sequence of characters, which can optionally contain expressions which will be replaced with a meaningful representation " +
						"(e.g. %player% will be replaced with the player's name).",
						"Because scripts are also text, you have to put text into double quotes to tell Skript which part of the line is an effect/expression and which part is the text.",
						"Please read the article on <a href='./strings/'>Texts and Variable Names</a> to learn more.")
				.usage("simple: \"...\"",
						"quotes: \"...\"\"...\"",
						"expressions: \"...%expression%...\"",
						"percent signs: \"...%%...\"")
				.examples("broadcast \"Hello World!\"",
						"message \"Hello %player%\"",
                        "message \"The id of \"\"%type of tool%\"\" is %id of tool%.\" # Before v2.6 (Double quotes)",
                        "message \"Type \"Yes\" in chat to accept.\" # After v2.6 (Single quote)")
				.since("1.0")
				.parser(new Parser<String>() {
					@Override
					@Nullable
					public String parse(final String s, final ParseContext context) {
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
								return s;
						}
						assert false;
						return null;
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						if (context == ParseContext.DEFAULT)
							return false;
						return true;
					}
					
					@Override
					public String toString(final String s, final int flags) {
						return s;
					}
					
					@Override
					public String getDebugMessage(final String s) {
						return '"' + s + '"';
					}
					
					@Override
					public String toVariableNameString(final String s) {
						return s;
					}
					
					@Override
					public String getVariableNamePattern() {
						return ".*";
					}
				}).serializer(new Serializer<String>() {
					@Override
					public Fields serialize(final String n) {
						throw new IllegalStateException(); // serialised natively by Yggdrasil
					}
					
					@Override
					public boolean canBeInstantiated() {
						return true;
					}
					
					@Override
					public void deserialize(final String o, final Fields f) throws StreamCorruptedException {
						assert false;
					}
					
//						return s;
					@Override
					public String deserialize(final String s) {
						return s;
					}
					
					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				}));
	}
}
