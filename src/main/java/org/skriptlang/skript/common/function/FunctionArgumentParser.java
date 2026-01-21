package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the arguments of a function reference.
 */
final class FunctionArgumentParser {

	private static final Pattern PART_PATTERN = Pattern.compile("(?:\\s*(?<name>[_a-zA-Z0-9]+):)?(?<value>.+)");

	/**
	 * The input string.
	 */
	private final String args;

	/**
	 * The list of arguments that have been found so far.
	 */
	private final List<Argument<String>> arguments = new ArrayList<>();

	/**
	 * Constructs a new function argument parser based on the
	 * input string and instantly calculates the result.
	 *
	 * @param args The input string.
	 */
	public FunctionArgumentParser(String args) {
		this.args = args;

		parse();
	}

	/**
	 * The char index.
	 */
	private int index = 0;

	/**
	 * Parses the input string into arguments.
	 */
	private void parse() {
		// if we have no args to parse, give up instantly
		if (args.isEmpty()) {
			return;
		}

		int next = 0;
		while (next < args.length()) {
			next = SkriptParser.next(args, next, ParseContext.DEFAULT);
			if (next == -1) {
				// if no end is found, just parse the whole passed string as an argument and pray it works
				index = 0;
				next = args.length();
			}

			if (next < args.length() && args.charAt(next) != ',') {
				continue;
			}

			String part = args.substring(index, next);
			index = next + 1;

			Matcher matcher = PART_PATTERN.matcher(part);
			if (!matcher.matches()) {
				continue;
			}

			String name = matcher.group("name");
			String value = matcher.group("value");
			if (name == null) {
				arguments.add(new Argument<>(ArgumentType.UNNAMED,
						null,
						value.trim(),
						value));
			} else  {
				arguments.add(new Argument<>(ArgumentType.NAMED,
						name.trim(),
						value.trim(),
						name + ":" + value));
			}
		}
	}

	/**
	 * Returns all arguments.
	 *
	 * @return All arguments.
	 */
	public Argument<String>[] getArguments() {
		//noinspection unchecked
		return (Argument<String>[]) arguments.toArray(new Argument[0]);
	}

}
