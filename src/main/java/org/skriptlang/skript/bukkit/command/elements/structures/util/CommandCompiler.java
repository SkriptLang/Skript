package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CommandCompiler {

	public static class CommandElement {

		public LinkedHashSet<CommandElement> children = new LinkedHashSet<>();

		public void append(Collection<CommandElement> elements) {
			// Element validation
			elements = elements.stream()
				.flatMap(element -> {
					if (element instanceof ChoiceCommandElement choice) {
						return choice.children.stream();
					}
					if (element == this) {
						return Stream.empty();
					}
					return Stream.of(element);
				})
				.toList();

			if (children.isEmpty()) { // we reached the end
				children.addAll(elements);
				return;
			}

			boolean hasNull = false;
			for (CommandElement child : children) {
				if (child == null) {
					hasNull = true;
				} else {
					child.append(elements);
				}
			}
			if (hasNull) {
				children.remove(null);
				children.addAll(elements);
			}
		}

	}

	public static class LiteralCommandElement extends CommandElement {

		private final String string;

		public LiteralCommandElement(String string) {
			this.string = string;
		}

		public String literal() {
			return string;
		}

	}

	public static class ArgumentCommandElement extends CommandElement {

		private final ArgumentData<?> argument;

		public ArgumentCommandElement(ArgumentData<?> argument) {
			this.argument = argument;
		}

		public ArgumentData<?> argument() {
			return argument;
		}

	}

	public static class ChoiceCommandElement extends CommandElement {

		@Override
		public void append(Collection<CommandElement> elements) {
			CommandElement last = children.getLast();
			if (last == null) {
				children.removeLast();
				children.addAll(elements);
			} else {
				children.getLast().append(elements);
			}
		}

	}

	private static StringBuilder tryLiteralAppend(CommandElement first, StringBuilder literalBuilder) {
		if (literalBuilder.isEmpty()) {
			return literalBuilder;
		}
		String literal = literalBuilder.toString().trim();
		if (!literal.isEmpty()) {
			first.append(List.of(new LiteralCommandElement(literal)));
		}
		return new StringBuilder();
	}

	public static CommandElement compile(String pattern, List<ArgumentData<?>> arguments) {
		StringBuilder literalBuilder = new StringBuilder();
		CommandElement first = new CommandElement();

		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c == '[') {
				literalBuilder = tryLiteralAppend(first, literalBuilder);

				int end = SkriptParser.nextBracket(pattern, ']', c, i + 1, true);
				CommandElement commandElement = compile(pattern.substring(i + 1, end), arguments);
				if (commandElement == null) {
					return null;
				}

				List<CommandElement> toAppend = new ArrayList<>(commandElement.children);
				toAppend.add(null);
				first.append(toAppend);

				i = end;
			} else if (c == '(') {
				literalBuilder = tryLiteralAppend(first, literalBuilder);

				int end = SkriptParser.nextBracket(pattern, ')', c, i + 1, true);
				CommandElement commandElement = compile(pattern.substring(i + 1, end), arguments);
				if (commandElement == null) {
					return null;
				}

				first.append(commandElement.children);

				i = end;
			} else if (c == '|') {
				literalBuilder = tryLiteralAppend(first, literalBuilder);

				ChoiceCommandElement choiceElement;
				if (first instanceof ChoiceCommandElement choiceCommandElement) {
					choiceElement = choiceCommandElement;
				} else {
					choiceElement = new ChoiceCommandElement();
					choiceElement.children.addAll(first.children);
					first = choiceElement;
				}
				choiceElement.children.add(null);
			} else if (c == '<') {
				literalBuilder = tryLiteralAppend(first, literalBuilder);

				int end = SkriptParser.nextBracket(pattern, '>', c, i + 1, true);
				ArgumentData<?> argument = parseArgument(pattern.substring(i + 1, end));
				if (argument == null) {
					return null;
				}

				arguments.add(argument);
				first.append(List.of(new ArgumentCommandElement(argument)));

				i = end;
			} else if (c == '\\') {
				i++;
				literalBuilder.append(pattern.charAt(i));
			} else {
				literalBuilder.append(c);
			}
		}

		tryLiteralAppend(first, literalBuilder);

		return first;
	}

	private static final Pattern ARGUMENT_PATTERN =
		Pattern.compile("^\\s*(?:([^>]+?)\\s*:\\s*)?(.+?)\\s*(?:=\\s*(" + SkriptParser.WILDCARD + "))?\\s*$");

	private static @Nullable ArgumentData<?> parseArgument(String argument) {
		Matcher argumentMatcher = ARGUMENT_PATTERN.matcher(argument);
		if (!argumentMatcher.find()) {
			return null;
		}

		// first, parse the type
		String rawType = argumentMatcher.group(2);
		var plural = Utils.isPlural(rawType);
		ClassInfo<?> type = Classes.getClassInfoFromUserInput(plural.updated());
		if (type == null) {
			Skript.error("'" + rawType + "' is not a known type.");
			return null;
		} else if (type.getParser() == null || !type.getParser().canParse(ParseContext.COMMAND)) {
			Skript.error("The type '" + type.getName().getSingular() + "' cannot be used as a command argument.");
			return null;
		}

		// next, parse the name
		String name = argumentMatcher.group(1);
		boolean isAutomaticName = false;
		if (name == null) { // user did not specify, manually create one
			isAutomaticName = true;
			name = type.getName().getSingular();
			String finalName = name;
			// TODO verify not duplicate argument name
		}

		// finally, parse the default value
		Expression<?> defaultValue = null;
		String rawDefaultValue = argumentMatcher.group(3);
		if (rawDefaultValue != null) {
			int parseType;
			if (rawDefaultValue.startsWith("%") && rawDefaultValue.endsWith("%")) {
				parseType = SkriptParser.PARSE_EXPRESSIONS;
				rawDefaultValue = rawDefaultValue.substring(1, rawDefaultValue.length() - 1);
			} else {
				parseType = SkriptParser.PARSE_LITERALS;
			}
			try (var logHandler = new RetainingLogHandler().start()) {
				defaultValue = new SkriptParser(rawDefaultValue, parseType, ParseContext.COMMAND)
					.parseExpression(type.getC());
				if (defaultValue == null) {
					logHandler.printErrors("Can't understand this expression: '" + rawDefaultValue + "'."
						+ " The default value will be ignored for this argument.");
				} else {
					logHandler.printLog();
				}
			}
		}

		//noinspection unchecked, rawtypes
		return new ArgumentData(name, isAutomaticName, type, defaultValue);
	}

}
