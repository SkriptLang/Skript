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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compiles a string command definition into a tree of literals ({@link LiteralCommandElement})
 *  and arguments {@link ArgumentCommandElement}.
 * @see #compile(String, List)
 */
final class CommandCompiler {

	/*
	 * Tree Structures
	 */

	/**
	 * A node within a command tree.
	 */
	static class CommandElement {

		protected final Set<CommandElement> children;

		private CommandElement() {
			this(new HashSet<>());
		}

		private CommandElement(Set<CommandElement> children) {
			this.children = children;
		}

		/**
		 * @return Elements representing the branches of the command from this element.
		 * If there is at least one child, it may also contain {@code null},
		 *  indicating that command execution can occur at this element.
		 */
		public Collection<CommandElement> children() {
			return children;
		}

		/**
		 * @return Whether command execution can occur at this element.
		 */
		public boolean isLeaf() {
			return children.isEmpty() || children.contains(null);
		}

		/**
		 * Appends elements to the end of the tree, from this element.
		 * If the {@link #children()} of an element contains a {@code null},
		 *  {@code elements} will also be appended to that element.
		 * @param elements Elements to append.
		 */
		public void append(Collection<CommandElement> elements) {
			boolean addToElement = children.isEmpty();
			for (CommandElement child : children) {
				if (child == null) { // null child indicates that this element is a command edge
					addToElement = true;
				} else {
					child.append(elements);
				}
			}
			if (addToElement) {
				children.remove(null);
				for (CommandElement element : elements) {
					// for certain nested commands, an element could potentially be appended to itself
					// consider:
					// A
					// |- B
					//    |- D
					// |- C
					//    |- D
					// when appending "E", it will append to the first "D" under "B".
					// this "D" is the same instance as the one under "C", so when this method moves
					// to next append "E" to "C", it will have already been handled.
					if (element != this) {
						children.add(element);
					}
				}
			}
		}

	}

	/**
	 * Represents a literal (constant) argument of a command.
	 */
	static class LiteralCommandElement extends CommandElement {

		private final String string;

		private LiteralCommandElement(String string) {
			super();
			this.string = string;
		}

		/**
		 * @return Literal argument this element represents.
		 */
		public String literal() {
			return string;
		}

	}

	/**
	 * Represents a dynamic argument of a command.
	 */
	static class ArgumentCommandElement extends CommandElement {

		private final ArgumentData<?> argument;

		private ArgumentCommandElement(ArgumentData<?> argument) {
			super();
			this.argument = argument;
		}

		/**
		 * @return Data describing the argument this element represents.
		 */
		public ArgumentData<?> argument() {
			return argument;
		}

	}

	/**
	 * Internal helper element for building choices during the compilation process.
	 * Unlike a regular {@link CommandElement}, when this element is appended to, only its last child is appended to.
	 */
	private static class ChoiceCommandElement extends CommandElement {

		/**
		 * Placeholder element representing a slot for the next element(s) to append onto.
		 */
		private static final CommandElement EMPTY = new CommandElement(null);

		private ChoiceCommandElement() {
			// we use a linked hash set as ordering is now necessary
			super(new LinkedHashSet<>());
			children.add(EMPTY);
		}

		@Override
		public void append(Collection<CommandElement> elements) {
			if (elements.isEmpty()) {
				return;
			}
			LinkedHashSet<CommandElement> children = (LinkedHashSet<CommandElement>) this.children;
			CommandElement last = children.getLast();
			if (last == EMPTY) {
				children.removeLast();
				children.addAll(elements);
			} else {
				last.append(elements);
			}
		}

		public void appendEmpty() {
			LinkedHashSet<CommandElement> children = (LinkedHashSet<CommandElement>) this.children;
			if (children.getLast() == EMPTY) { // nothing was ever appended, meaning the previous choice was empty/blank
				children.removeLast();
				children.add(null); // meaning this choice group is optional
			}
			children.add(EMPTY);
		}

		@Override
		public Collection<CommandElement> children() {
			LinkedHashSet<CommandElement> children = (LinkedHashSet<CommandElement>) this.children;
			if (children.getLast() == EMPTY) {
				children.removeLast();
				children.add(null);
			}
			return super.children();
		}

	}

	/*
	 * Compilation
	 */

	/**
	 * Compiles a string command definition into an element tree.
	 * @param pattern The command definition to compile.
	 * @param arguments A list to store argument information in.
	 * @return A plain {@link CommandElement} containing all children.
	 * For a regular input, such as {@code "heal <number>"}, this element contains a single {@link LiteralCommandElement}.
	 */
	public static CommandElement compile(final String pattern, List<ArgumentData<?>> arguments) {
		StringBuilder literalBuilder = new StringBuilder();
		CommandElement first = new CommandElement();

		int patternLength = pattern.length();
		for (int i = 0; i < patternLength; i++) {
			char c = pattern.charAt(i);
			if (c == '[') { // indicates an optional element
				literalBuilder = tryLiteralAppend(first, literalBuilder);

				int end = SkriptParser.nextBracket(pattern, ']', c, i + 1, true);
				CommandElement commandElement = compile(pattern.substring(i + 1, end), arguments);
				if (commandElement == null) {
					return null;
				}

				List<CommandElement> toAppend = new ArrayList<>(commandElement.children());
				toAppend.add(null);
				first.append(toAppend);

				i = end;
			} else if (c == '(') { // indicates an optional element
				literalBuilder = tryLiteralAppend(first, literalBuilder);

				int end = SkriptParser.nextBracket(pattern, ')', c, i + 1, true);
				CommandElement commandElement = compile(pattern.substring(i + 1, end), arguments);
				if (commandElement == null) {
					return null;
				}

				first.append(commandElement.children());

				i = end;
			} else if (c == '|') { // indicates the end of a single choice
				literalBuilder = tryLiteralAppend(first, literalBuilder);

				ChoiceCommandElement choiceElement;
				if (first instanceof ChoiceCommandElement choiceCommandElement) {
					choiceElement = choiceCommandElement;
				} else { // indicates that we finished compiling the first choice
					// thus, we create a new choice element with everything compiled so far as one of the choices
					// the root element then becomes the choice element for further choice appending to occur
					choiceElement = new ChoiceCommandElement();
					choiceElement.append(first.children());
					first = choiceElement;
				}
				// append an empty space for the following content to append to
				choiceElement.appendEmpty();
			} else if (c == '<') { //
				literalBuilder = tryLiteralAppend(first, literalBuilder);

				int end = SkriptParser.nextBracket(pattern, '>', c, i + 1, true);
				ArgumentData<?> argument = parseArgument(pattern.substring(i + 1, end));
				if (argument == null) {
					return null;
				}

				arguments.add(argument);
				first.append(List.of(new ArgumentCommandElement(argument)));

				i = end;
			} else if (c == '\\' && i + 1 < patternLength) {
				i++;
				literalBuilder.append(pattern.charAt(i));
			} else {
				literalBuilder.append(c);
			}
		}

		tryLiteralAppend(first, literalBuilder);

		return first;
	}

	/**
	 * Helper for appending literal content to an element during compilation.
	 * @param first The element to append to.
	 * @param literalBuilder Builder representing the literal to append.
	 * @return A new builder if appending was successful, otherwise {@code literalBuilder}.
	 */
	private static StringBuilder tryLiteralAppend(CommandElement first, StringBuilder literalBuilder) {
		if (literalBuilder.isEmpty()) {
			return literalBuilder;
		}
		String literal = literalBuilder.toString().trim();
		if (!literal.isEmpty()) { // blank literals are not meaningful to append
			first.append(List.of(new LiteralCommandElement(literal)));
		}
		return new StringBuilder();
	}

	/*
	 * Argument Parsing
	 */

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
