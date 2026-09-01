package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.patterns.MalformedPatternException;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;
import org.skriptlang.skript.bukkit.command.custom.ScriptArgumentType;
import org.skriptlang.skript.bukkit.command.custom.ScriptArgumentType.NativeArgumentData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
	public static class CommandElement {

		protected final Set<CommandElement> children;

		private CommandElement() {
			// we want to keep orderings consistent for the same definition
			this(new LinkedHashSet<>());
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
		public void append(Collection<? extends CommandElement> elements) {
			// check whether elements have already been appended to this element (likely resulting from a shared choice)
			// if one of elements has been appended, then all have been appended, so we only need to check for one
			if (!Collections.disjoint(children, elements)) {
				return;
			}
			boolean addToElement = false;
			for (CommandElement child : children) {
				if (child == null) { // null child indicates that this element is a command edge
					addToElement = true;
				} else {
					child.append(elements);
				}
			}
			if (addToElement || children.isEmpty()) {
				children.remove(null);
				children.addAll(elements);
			}
		}

	}

	/**
	 * Represents a literal (constant) argument of a command.
	 */
	public static class LiteralCommandElement extends CommandElement {

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
	public static class ArgumentCommandElement extends CommandElement {

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
			children.add(EMPTY);
		}

		@Override
		public void append(Collection<? extends CommandElement> elements) {
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
	 *
	 * @param root A plain {@link CommandElement} containing all children.
	 *  For a regular input, such as {@code "heal <number>"}, this element contains a single {@link LiteralCommandElement}.
	 * @param arguments Data of all arguments contained within the element tree.
	 */
	public record CompilationResult(CommandElement root, List<ArgumentData<?>> arguments) { }

	/**
	 * Compiles a string command definition into an element tree.
	 * @param pattern The command definition to compile.
	 * @param existingArguments Existing arguments to consider during compilation.
	 * @return A result of the compilation, or null if compilation failed.
	 */
	public static @Nullable CompilationResult compile(final String pattern, List<ArgumentData<?>> existingArguments) {
		List<ArgumentData<?>> arguments = new ArrayList<>();
		CommandElement compiled = compile(pattern, existingArguments, arguments);
		return compiled == null ? null :  new CompilationResult(compiled, arguments);
	}

	private static @Nullable CommandElement compile(final String pattern,
		List<ArgumentData<?>> existingArguments, List<ArgumentData<?>> arguments) {
		List<LiteralCommandElement> pendingLiterals = new ArrayList<>();
		CommandElement first = new CommandElement();

		int patternLength = pattern.length();
		for (int i = 0; i < patternLength; i++) {
			char c = pattern.charAt(i);
			if (c == '[' || c == '(') { // indicates optional choice or general grouping
				boolean isOptional = c == '[';
				int end;
				try {
					end = SkriptParser.nextBracket(pattern, isOptional ? ']' : ')', c, i + 1, true);
				} catch (MalformedPatternException ignored) {
					Skript.error("Missing expected closing bracket '" + (isOptional ? ']' : ')') + "' starting from input: " +
						pattern.substring(i));
					return null;
				}
				CommandElement commandElement = compile(pattern.substring(i + 1, end), existingArguments, arguments);
				if (commandElement == null) {
					return null;
				}

				// determine elements to append
				Collection<CommandElement> toAppend;
				if (isOptional) {
					toAppend = new ArrayList<>(commandElement.children());
					toAppend.add(null);
				} else {
					toAppend = commandElement.children();
				}

				if (toAppend.stream().anyMatch(element -> element instanceof ArgumentCommandElement)) {
					if (pendingLiterals.isEmpty()) { // [<arg>] is valid
						first.append(toAppend);
					} else { // argument placed attached to literals, e.g. 'lit<arg>'
						Skript.error("Literals cannot be placed directly next to arguments. Separate them with a space.");
						return null;
					}
				} else {
					//noinspection rawtypes, unchecked
					pendingLiterals = appendToLiterals(pendingLiterals, (Collection) toAppend);
				}

				i = end;
			} else if (c == '|') { // indicates the end of a single choice
				if (!pendingLiterals.isEmpty()) {
					first.append(pendingLiterals);
					pendingLiterals.clear();
				}

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
			} else if (c == '<') { // indicates an argument
				if (!pendingLiterals.isEmpty()) { // an argument cannot be legally placed here (ex. 'lit<arg>')
					Skript.error("Literals cannot be placed directly next to arguments. Separate them with a space.");
					return null;
				}

				int end;
				try {
					end = SkriptParser.nextBracket(pattern, '>', c, i + 1, true);
				} catch (MalformedPatternException ignored) {
					Skript.error("Missing expected closing bracket '>' starting from input: " + pattern.substring(i));
					return null;
				}
				ArgumentData<?> argument = parseArgument(pattern.substring(i + 1, end),
					Stream.concat(existingArguments.stream(), arguments.stream()).toList());
				if (argument == null) {
					return null;
				}

				arguments.add(argument);
				first.append(List.of(new ArgumentCommandElement(argument)));

				i = end;
			} else if (c == '\\' && i + 1 < patternLength) { // escaping
				i++;
				appendToLiterals(pendingLiterals, pattern.charAt(i));
			} else if (c == ' ') { // literal terminator
				if (!pendingLiterals.isEmpty()) {
					first.append(pendingLiterals);
					pendingLiterals.clear();
				}
			} else {
				appendToLiterals(pendingLiterals, c);
			}
		}

		if (!pendingLiterals.isEmpty()) { // append any outstanding literal(s)
			first.append(pendingLiterals);
		}

		return first;
	}

	/**
	 * Appends {@code character} to all elements of {@code literals}.
	 * @param literals The literals to append to.
	 * @param character The character to append.
	 */
	private static void appendToLiterals(List<LiteralCommandElement> literals, char character) {
		if (literals.isEmpty()) {
			literals.add(new LiteralCommandElement(String.valueOf(character)));
			return;
		}
		literals.replaceAll(element -> new LiteralCommandElement(element.literal() + character));
	}

	/**
	 * Appends every element in {@code elements} to every element in {@code literal}.
	 * Optional markers (null) are considered and preserved.
	 * @param literals The literals to append to.
	 * @param elements The elements to append.
	 * @return New list of literals, after appending.
	 */
	private static List<LiteralCommandElement> appendToLiterals(Collection<LiteralCommandElement> literals,
		Collection<LiteralCommandElement> elements) {
		if (literals.isEmpty()) {
			return new ArrayList<>(elements);
		}
		List<LiteralCommandElement> newLiterals = new ArrayList<>();
		for (LiteralCommandElement literal : literals) {
			if (literal == null) { // preserve optional marker (entire list is optional)
				newLiterals.add(null);
				continue;
			}
			for (LiteralCommandElement element : elements) {
				if (element == null) { // null means the literal itself should still be valid
					newLiterals.add(literal);
				} else {
					newLiterals.add(new LiteralCommandElement(literal.literal() + element.literal()));
				}
			}
		}
		return newLiterals;
	}

	/*
	 * Argument Parsing
	 */

	private static final Pattern ARGUMENT_PATTERN =
		Pattern.compile("^\\s*(?:([^>]+?)\\s*:\\s*)?(.+?)\\s*(?:=\\s*(" + SkriptParser.WILDCARD + "))?\\s*$");

	private static final Pattern TYPE_PATTERN =
		Pattern.compile("^(.+?)\\s*(?: (?:from|above|greater than|>|between) (.+?))?(?:(?: (?:to|(?:and )?(?:below|less than|<)|and) (.+?))?)?$");

	private static @Nullable ArgumentData<?> parseArgument(String argument, List<ArgumentData<?>> arguments) {
		Matcher argumentMatcher = ARGUMENT_PATTERN.matcher(argument);
		if (!argumentMatcher.find()) {
			Skript.error("'" + argument + "' is not a properly formatted argument.");
			return null;
		}

		// first, parse the type
		Matcher typeMatcher = TYPE_PATTERN.matcher(argumentMatcher.group(2));
		if (!typeMatcher.find()) {
			Skript.error("'" + argumentMatcher.group(2) + "' is not a known type.");
			return null;
		}
		String rawType = typeMatcher.group(1);
		var plural = Utils.isPlural(rawType);
		ClassInfo<?> type = Classes.getClassInfoFromUserInput(plural.updated());
		if (type == null) {
			Skript.error("'" + rawType + "' is not a known type.");
			return null;
		} else if (type.getParser() == null || !type.getParser().canParse(ParseContext.COMMAND)) {
			Skript.error("The type '" + type.getName().getSingular() + "' cannot be used as a command argument.");
			return null;
		}
		Object min = null;
		Object max = null;
		if (typeMatcher.group(2) != null) { // has min
			min = type.getParser().parse(typeMatcher.group(2), ParseContext.COMMAND);
			if (min == null) {
				Skript.error("Invalid minimum range: " + typeMatcher.group(2));
				return null;
			}
		}
		if (typeMatcher.group(3) != null) { // has max
			max = type.getParser().parse(typeMatcher.group(3), ParseContext.COMMAND);
			if (max == null) {
				Skript.error("Invalid maximum range: " + typeMatcher.group(3));
				return null;
			}
		}
		// type validation
		NativeArgumentData nativeMapping = ScriptArgumentType.getNativeData(type);
		if (min != null || max != null) {
			if (nativeMapping == null || !nativeMapping.supportsRange()) {
				String typeName = plural.plural() ? type.getName().getPlural() : type.getName().getSingular();
				Skript.error(typeName + " arguments do not support minimum or maximum values.");
				return null;
			}
			if (!nativeMapping.supportsPlural() && plural.plural()) {
				Skript.error("Only single " + type.getName().getSingular() + " arguments support minimum or maximum values.");
				return null;
			}
		}

		// next, parse the name
		String name = argumentMatcher.group(1);
		boolean isAutomaticName = false;
		if (name == null) { // user did not specify, manually create one
			isAutomaticName = true;
			name = type.getName().getSingular();
			String finalName = name;
			// first, try just the classinfo name as the argument name (ex: 'number')
			if (arguments.stream().anyMatch(arg -> arg.name().equals(finalName))) {
				// otherwise, append an index (ex: 'number2')
				int index = 2;
				while (true) {
					int finalIndex = index;
					if (arguments.stream().anyMatch(arg -> arg.name().equals(finalName + finalIndex))) {
						index++;
						continue;
					}
					break;
				}
				name = name + index;
			}
		} else {
			String finalName = name;
			if (arguments.stream().anyMatch(arg -> arg.name().equals(finalName))) {
				Skript.error("The argument name '" + finalName + "' was already used.");
				return null;
			}
		}

		// finally, parse the default value
		Expression<?> defaultValue = null;
		String rawDefaultValue = argumentMatcher.group(3);
		if (rawDefaultValue != null) {
			int parseType;
			if (rawDefaultValue.startsWith("%") && rawDefaultValue.endsWith("%")) {
				parseType = SkriptParser.PARSE_EXPRESSIONS;
				rawDefaultValue = rawDefaultValue.substring(1, rawDefaultValue.length() - 1);
			} else if (type.getC() == String.class && rawDefaultValue.startsWith("\"") && rawDefaultValue.endsWith("\"")) {
				parseType = SkriptParser.PARSE_EXPRESSIONS;
			} else {
				parseType = SkriptParser.PARSE_LITERALS;
			}
			try (var logHandler = new RetainingLogHandler().start()) {
				defaultValue = new SkriptParser(rawDefaultValue, parseType, ParseContext.COMMAND)
					.parseExpression(type.getC());
				if (defaultValue == null) {
					logHandler.printErrors("Can't understand this expression: '" + rawDefaultValue + "'."
						+ " The default value will be ignored for this argument.");
				} else if (!plural.plural() && !defaultValue.canBeSingle()) {
					logHandler.printErrors("Expected a single value but got many: " + defaultValue.toString(null, false));
				} else {
					logHandler.printLog();
				}
			}
		}

		//noinspection unchecked, rawtypes
		return new ArgumentData(name, isAutomaticName, type, !plural.plural(), defaultValue, min, max);
	}

}
