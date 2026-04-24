package org.skriptlang.skript.common.elements.functions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.Utils;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.text.DecimalFormat;
import java.util.UUID;

public class StringFunctions {

	private static final DecimalFormat DEFAULT_INTEGER_FORMAT = new DecimalFormat("###,###");
	private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("###,###.##");

	static {
		SkriptAddon skript = Skript.instance();

		Functions.register(DefaultFunction.builder(skript, "formatNumber", String.class)
				.description(
						"Converts numbers to human-readable format. By default, '###,###' (e.g. '123,456,789') " +
								"will be used for whole numbers and '###,###.##' (e.g. '123,456,789.00) will be used for decimal numbers. " +
								"A hashtag '#' represents a digit, a comma ',' is used to separate numbers, and a period '.' is used for decimals. ",
						"Will return none if the format is invalid.",
						"For further reference, see this <a href=\"https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html\" target=\"_blank\">article</a>.")
				.examples("""
						command /balance:
							aliases: bal
							executable by: players
							trigger:
								set {_money} to formatNumber({money::%sender's uuid%})
								send "Your balance: %{_money}%" to sender
						""")
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

		Functions.register(DefaultFunction.builder(skript, "concat", String.class)
				.description("Joins the provided texts (and other things) into a single text.")
				.examples("""
						concat("hello ", "there") # hello there
						concat("foo ", 100, " bar") # foo 100 bar
						"""
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
	}

}
