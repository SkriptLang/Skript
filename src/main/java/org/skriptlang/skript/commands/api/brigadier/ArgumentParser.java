package org.skriptlang.skript.commands.api.brigadier;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class ArgumentParser<Source> {

	/*

	text -> literal
	[...] -> optional
	<...> -> type
	(...) -> group
	...|... -> choice

	TODO: figure out how to keep references to argument position to allow future references by users
			e.g. how to ensure arg-2 is consistent in 'command test [<text>] <text> <text>'

	 */

	private final Pattern controlCharacterPattern = Pattern.compile("[(\\[<]");

	private final LiteralArgumentBuilder<Source> root;

	public ArgumentParser(String command, String arguments) {

		// 1. read until control character - ([<
		// 2. trim, create argument based on state
		// 3. consume control character
		root = literal(command);
		parse(root, arguments);
	}

	public LiteralArgumentBuilder<Source> getRoot() {
		return root;
	}

	private void parseOptional(ArgumentBuilder<Source,?> parent, String arguments) {
		int end = findClosingCharacter(arguments, '[', ']');
		if (end == -1)
			throw new IllegalArgumentException("unmatched [ in arguments");

		parse(parent, arguments.substring(end + 1));

		String optional = arguments.substring(0, end);
		parse(parent, optional + arguments.substring(end + 1));
	}

	private void parseChoice(ArgumentBuilder<Source, ?> parent, String arguments) {
		int end = findClosingCharacter(arguments, '(', ')');
		if (end == -1)
			throw new IllegalArgumentException("unmatched ( in arguments");

		String[] options = arguments.substring(0, end).split("\\|");
		String theRest = arguments.substring(end + 1);
		for (String option : options) {
			parse(parent, option + theRest);
		}
	}

	private void parseType(ArgumentBuilder<Source, ?> parent, String arguments) {
		int end = arguments.indexOf('>');
		ArgumentBuilder<Source, ?> arg = literal(arguments.substring(0, end).trim() + "!");

		parse(arg, arguments.substring(end + 1));

		parent.then(arg);
	}

	private void parseLiteral(ArgumentBuilder<Source, ?> parent, String arguments) {
		Matcher matcher = controlCharacterPattern.matcher(arguments);
		ArgumentBuilder<Source, ?> arg;
		if (matcher.find()) {
			arg = literal(arguments.substring(0, matcher.start()).trim());
			parse(arg, arguments.substring(matcher.start()));
		} else {
			arg = literal(arguments.trim());
		}
		parent.then(arg);
	}

	private <T extends ArgumentBuilder<Source, T>> void parse(ArgumentBuilder<Source, T> parent, String arguments) {
		arguments = arguments.trim();
		if (arguments.isEmpty())
			return;
		char first = arguments.charAt(0);
		if (first == '[') {
			// find closing [
			// remove [], parse remaining
			// keep [], parse remaining
			// creates a branch in the tree
			parseOptional(parent, arguments.substring(1));
		} else if (first == '(') {
			// find closing )
			// if all contents are literals, create multi-literal argument
			// otherwise, parse with each option replacing the choice
			parseChoice(parent, arguments.substring(1));
		} else if (first == '<') {
			// find closing >
			// parse as argument type
			parseType(parent, arguments.substring(1));
		} else {
			// has literal
			parseLiteral(parent, arguments);
		}
	}

	private int findClosingCharacter(String input, char open, char close) {
		int depth = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == open) {
				depth++;
			} else if (input.charAt(i) == close) {
				if (depth == 0)
					return i;
				depth--;
			}
		}
		return -1;
	}
}
