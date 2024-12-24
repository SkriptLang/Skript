package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprColoured;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.structures.StructVariables.DefaultVariables;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.chat.MessageComponent;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a string that may contain expressions, and is thus "variable".
 */
public class VariableString implements Expression<String> {

	private final @Nullable Script script;
	protected final String original;


	private final Object @Nullable [] strings;


	private Object @Nullable [] stringsUnformatted;
	private final boolean isSimple;

	private final @Nullable String simple, simpleUnformatted;
	private final StringMode mode;

	/**
	 * Message components that this string consists of. Only simple parts have
	 * been evaluated here.
	 */
	private final MessageComponent[] components;

	/**
	 * Creates a new VariableString which does not contain variables.
	 *
	 * @param input Content for string.
	 */
	protected VariableString(String input) {
		this.isSimple = true;
		this.simpleUnformatted = input.replace("%%", "%"); // This doesn't contain variables, so this wasn't done in newInstance!
		this.simple = Utils.replaceChatStyles(simpleUnformatted);

		this.original = simple;
		this.strings = null;
		this.mode = StringMode.MESSAGE;

		ParserInstance parser = getParser();
		this.script = parser.isActive() ? parser.getCurrentScript() : null;

		this.components = new MessageComponent[] {ChatMessages.plainText(simpleUnformatted)};
	}

	/**
	 * Creates a new VariableString which contains variables.
	 *
	 * @param original Original string (unparsed).
	 * @param strings Objects, some of them are variables.
	 * @param mode String mode.
	 */
	private VariableString(String original, Object[] strings, StringMode mode) {
		this.original = original;
		this.strings = new Object[strings.length];
		this.stringsUnformatted = new Object[strings.length];

		ParserInstance parser = getParser();
		this.script = parser.isActive() ? parser.getCurrentScript() : null;

		// Construct unformatted string and components
		List<MessageComponent> components = new ArrayList<>(strings.length);
		for (int i = 0; i < strings.length; i++) {
			Object object = strings[i];
			if (object instanceof String) {
				this.strings[i] = Utils.replaceChatStyles((String) object);
				components.addAll(ChatMessages.parse((String) object));
			} else {
				this.strings[i] = object;
				components.add(null); // Not known parse-time
			}

			// For unformatted string, don't format stuff
			this.stringsUnformatted[i] = object;
		}
		this.components = components.toArray(new MessageComponent[0]);

		this.mode = mode;

		this.isSimple = false;
		this.simple = null;
		this.simpleUnformatted = null;
	}

	/**
	 * Prints errors
	 */
	public static @Nullable VariableString newInstance(String input) {
		return newInstance(input, StringMode.MESSAGE);
	}

	/**
	 * Creates an instance of VariableString by parsing given string.
	 * Prints errors and returns null if it is somehow invalid.
	 *
	 * @param original Unquoted string to parse.
	 * @return A new VariableString instance.
	 */
	public static @Nullable VariableString newInstance(String original, StringMode mode) {
		if (mode != StringMode.VARIABLE_NAME && !isQuotedCorrectly(original, false))
			return null;

		int percentCount = StringUtils.count(original, '%');
		if (percentCount % 2 != 0) {
			Skript.error("The percent sign is used for expressions (e.g. %player%). To insert a '%' type it twice: %%.");
			return null;
		}

		// We must not parse color codes yet, as JSON support would be broken :(
		if (mode != StringMode.VARIABLE_NAME) {
			// Replace every double " character with a single ", except for those in expressions (between %)
			StringBuilder stringBuilder = new StringBuilder();

			boolean expression = false;
			for (int i = 0; i < original.length(); i++) {
				char c = original.charAt(i);
				stringBuilder.append(c);

				if (c == '%')
					expression = !expression;

				if (!expression && c == '"')
					i++;
			}
			original = stringBuilder.toString();
		}

		List<Object> strings = new ArrayList<>(percentCount / 2 + 2); // List of strings and expressions
		int exprStart = original.indexOf('%');
		if (exprStart != -1) {
			if (exprStart != 0)
				strings.add(original.substring(0, exprStart));
			while (exprStart != original.length()) {
				int exprEnd = original.indexOf('%', exprStart + 1);

				int variableEnd = exprStart;
				int variableStart;
				while (exprEnd != -1 && (variableStart = original.indexOf('{', variableEnd + 1)) != -1 && variableStart < exprEnd) {
					variableEnd = nextVariableBracket(original, variableStart + 1);
					if (variableEnd == -1) {
						Skript.error("Missing closing bracket '}' to end variable");
						return null;
					}
					exprEnd = original.indexOf('%', variableEnd + 1);
				}
				if (exprEnd == -1) {
					assert false;
					return null;
				}
				if (exprStart + 1 == exprEnd) {
					// %% escaped -> one % in result string
					if (strings.size() > 0 && strings.get(strings.size() - 1) instanceof String) {
						strings.set(strings.size() - 1, strings.get(strings.size() - 1) + "%");
					} else {
						strings.add("%");
					}
				} else {
					RetainingLogHandler log = SkriptLogger.startRetainingLog();
					try {
						Expression<?> expr =
							new SkriptParser(original.substring(exprStart + 1, exprEnd), SkriptParser.PARSE_EXPRESSIONS, ParseContext.DEFAULT)
								.parseExpression(Object.class);
						if (expr == null) {
							log.printErrors("Can't understand this expression: " + original.substring(exprStart + 1, exprEnd));
							return null;
						} else {
							strings.add(expr);
						}
						log.printLog();
					} finally {
						log.stop();
					}
				}
				exprStart = original.indexOf('%', exprEnd + 1);
				if (exprStart == -1)
					exprStart = original.length();
				String literalString = original.substring(exprEnd + 1, exprStart); // Try to get string (non-variable) part
				if (!literalString.isEmpty()) { // This is string part (no variables)
					if (strings.size() > 0 && strings.get(strings.size() - 1) instanceof String) {
						// We can append last string part in the list, so let's do so
						strings.set(strings.size() - 1, strings.get(strings.size() - 1) + literalString);
					} else { // Can't append, just add new part
						strings.add(literalString);
					}
				}
			}
		} else {
			// Only one string, no variable parts
			strings.add(original);
		}

		// Check if this isn't actually variable string, and return
		if (strings.size() == 1 && strings.get(0) instanceof String)
			return new LiteralString(original);

		if (strings.size() == 1 && strings.get(0) instanceof Expression &&
				((Expression<?>) strings.get(0)).getReturnType() == String.class &&
				((Expression<?>) strings.get(0)).isSingle() &&
				mode == StringMode.MESSAGE) {
			String expr = ((Expression<?>) strings.get(0)).toString(null, false);
			Skript.warning(expr + " is already a text, so you should not put it in one (e.g. " + expr + " instead of " + "\"%" + expr.replace("\"", "\"\"") + "%\")");
		}
		return new VariableString(original, strings.toArray(), mode);
	}

	/**
	 * Attempts to properly quote a string (e.g. double the double quotations).
	 * Please note that the string itself will not be surrounded with double quotations.
	 * @param string The string to properly quote.
	 * @return The input where all double quotations outside of expressions have been doubled.
	 */
	public static String quote(String string) {
		StringBuilder fixed = new StringBuilder();
		boolean inExpression = false;
		for (char character : string.toCharArray()) {
			if (character == '%') // If we are entering an expression, quotes should NOT be doubled
				inExpression = !inExpression;
			if (!inExpression && character == '"')
				fixed.append('"');
			fixed.append(character);
		}
		return fixed.toString();
	}

	/**
	 * Tests whether a string is correctly quoted, i.e. only has doubled double quotes in it.
	 * Singular double quotes are only allowed between percentage signs.
	 *
	 * @param string The string to test
	 * @param withQuotes Whether the string must be surrounded by double quotes or not
	 * @return Whether the string is quoted correctly
	 */
	public static boolean isQuotedCorrectly(String string, boolean withQuotes) {
		if (withQuotes && (!string.startsWith("\"") || !string.endsWith("\"") || string.length() < 2))
			return false;
		boolean quote = false;
		boolean percentage = false;
		if (withQuotes)
			string = string.substring(1, string.length() - 1);
		for (char character : string.toCharArray()) {
			if (percentage) {
				if (character == '%')
					percentage = false;
				continue;
			}
			if (quote && character != '"')
				return false;
			if (character == '"') {
				quote = !quote;
			} else if (character == '%') {
				percentage = true;
			}
		}
		return !quote;
	}

	/**
	 * Removes quoted quotes from a string.
	 *
	 * @param string The string to remove quotes from
	 * @param surroundingQuotes Whether the string has quotes at the start & end that should be removed
	 * @return The string with double quotes replaced with single ones and optionally with removed surrounding quotes.
	 */
	public static String unquote(String string, boolean surroundingQuotes) {
		assert isQuotedCorrectly(string, surroundingQuotes);
		if (surroundingQuotes)
			return string.substring(1, string.length() - 1).replace("\"\"", "\"");
		return string.replace("\"\"", "\"");
	}

	/**
	 * Copied from {@code SkriptParser#nextBracket(String, char, char, int, boolean)}, but removed escaping & returns -1 on error.
	 *
	 * @param string The string to search in
	 * @param start Index after the opening bracket
	 * @return The next closing curly bracket
	 */
	public static int nextVariableBracket(String string, int start) {
		int variableDepth = 0;
		for (int index = start; index < string.length(); index++) {
			if (string.charAt(index) == '}') {
				if (variableDepth == 0)
					return index;
				variableDepth--;
			} else if (string.charAt(index) == '{') {
				variableDepth++;
			}
		}
		return -1;
	}

	public static VariableString[] makeStrings(String[] args) {
		VariableString[] strings = new VariableString[args.length];
		int j = 0;
		for (String arg : args) {
			VariableString variableString = newInstance(arg);
			if (variableString != null)
				strings[j++] = variableString;
		}
		if (j != args.length)
			strings = Arrays.copyOf(strings, j);
		return strings;
	}

	/**
	 * @param args Quoted strings - This is not checked!
	 * @return a new array containing all newly created VariableStrings, or null if one is invalid
	 */
	public static VariableString @Nullable [] makeStringsFromQuoted(List<String> args) {
		VariableString[] strings = new VariableString[args.size()];
		for (int i = 0; i < args.size(); i++) {
			assert args.get(i).startsWith("\"") && args.get(i).endsWith("\"");
			VariableString variableString = newInstance(args.get(i).substring(1, args.get(i).length() - 1));
			if (variableString == null)
				return null;
			strings[i] = variableString;
		}
		return strings;
	}

	/**
	 * Parses all expressions in the string and returns it.
	 * Does not parse formatting codes!
	 * @param event Event to pass to the expressions.
	 * @return The input string with all expressions replaced.
	 */
	public String toUnformattedString(Event event) {
		if (isSimple) {
			assert simpleUnformatted != null;
			return simpleUnformatted;
		}
		Object[] strings = this.stringsUnformatted;
		assert strings != null;
		StringBuilder builder = new StringBuilder();
		for (Object string : strings) {
			if (string instanceof Expression<?>) {
				builder.append(Classes.toString(((Expression<?>) string).getArray(event), true, mode));
			} else {
				builder.append(string);
			}
		}
		return builder.toString();
	}

	/**
	 * Gets message components from this string. Formatting is parsed only
	 * in simple parts for security reasons.
	 * @param event Currently running event.
	 * @return Message components.
	 */
	public List<MessageComponent> getMessageComponents(Event event) {
		if (isSimple) { // Trusted, constant string in a script
			assert simpleUnformatted != null;
			return ChatMessages.parse(simpleUnformatted);
		}

		// Parse formatting
		Object[] strings = this.stringsUnformatted;
		assert strings != null;
		List<MessageComponent> message = new ArrayList<>(components.length); // At least this much space
		int stringPart = -1;
		MessageComponent previous = null;
		for (MessageComponent component : components) {
			if (component == null) { // This component holds place for variable part
				// Go over previous expression part (stringPart >= 0) or take first part (stringPart == 0)
				stringPart++;
				if (previous != null) { // Also jump over literal part
					stringPart++;
				}
				Object string = strings[stringPart];
				previous = null;

				// Convert it to plain text
				String text = null;
				if (string instanceof ExprColoured && ((ExprColoured) string).isUnsafeFormat()) { // Special case: user wants to process formatting
					String unformatted = Classes.toString(((ExprColoured) string).getArray(event), true, mode);
					if (unformatted != null) {
						message.addAll(ChatMessages.parse(unformatted));
					}
					continue;
				} else if (string instanceof Expression<?>) {
					text = Classes.toString(((Expression<?>) string).getArray(event), true, mode);
				}

				assert text != null;
				List<MessageComponent> components = ChatMessages.fromParsedString(text);
				if (!message.isEmpty()) { // Copy styles from previous component
					int startSize = message.size();
					for (int i = 0; i < components.size(); i++) {
						MessageComponent plain = components.get(i);
						ChatMessages.copyStyles(message.get(startSize + i - 1), plain);
						message.add(plain);
					}
				} else {
					message.addAll(components);
				}
			} else {
				MessageComponent componentCopy = component.copy();
				if (!message.isEmpty()) { // Copy styles from previous component
					ChatMessages.copyStyles(message.get(message.size() - 1), componentCopy);
				}
				message.add(componentCopy);
				previous = componentCopy;
			}
		}

		return message;
	}

	/**
	 * Gets message components from this string. Formatting is parsed
	 * everywhere, which is a potential security risk.
	 * @param event Currently running event.
	 * @return Message components.
	 */
	public List<MessageComponent> getMessageComponentsUnsafe(Event event) {
		if (isSimple) { // Trusted, constant string in a script
			assert simpleUnformatted != null;
			return ChatMessages.parse(simpleUnformatted);
		}

		return ChatMessages.parse(toUnformattedString(event));
	}

	/**
	 * Parses all expressions in the string and returns it in chat JSON format.
	 *
	 * @param event Event to pass to the expressions.
	 * @return The input string with all expressions replaced.
	 */
	public String toChatString(Event event) {
		return ChatMessages.toJson(getMessageComponents(event));
	}

	private static @Nullable ChatColor getLastColor(CharSequence sequence) {
		for (int i = sequence.length() - 2; i >= 0; i--) {
			if (sequence.charAt(i) == ChatColor.COLOR_CHAR) {
				ChatColor color = ChatColor.getByChar(sequence.charAt(i + 1));
				if (color != null && (color.isColor() || color == ChatColor.RESET))
					return color;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return toString(null, false);
	}

	/**
	 * Parses all expressions in the string and returns it.
	 * If this is a simple string, the event may be null.
	 *
	 * @param event Event to pass to the expressions.
	 * @return The input string with all expressions replaced.
	 */
	public String toString(@Nullable Event event) {
		if (isSimple) {
			assert simple != null;
			return simple;
		}
		if (event == null)
			throw new IllegalArgumentException("Event may not be null in non-simple VariableStrings!");

		Object[] string = this.strings;
		assert string != null;
		StringBuilder builder = new StringBuilder();
		List<Class<?>> types = new ArrayList<>();
		for (Object object : string) {
			if (object instanceof Expression<?>) {
				Object[] objects = ((Expression<?>) object).getArray(event);
				if (objects != null && objects.length > 0)
					types.add(objects[0].getClass());
				builder.append(Classes.toString(objects, true, mode));
			} else {
				builder.append(object);
			}
		}
		String complete = builder.toString();
		if (script != null && mode == StringMode.VARIABLE_NAME && !types.isEmpty()) {
			DefaultVariables data = script.getData(DefaultVariables.class);
			if (data != null)
				data.add(complete, types.toArray(new Class<?>[0]));
		}
		return complete;
	}

	/**
	 * Use {@link #toString(Event)} to get the actual string. This method is for debugging.
	 */
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (isSimple) {
			assert simple != null;
			return '"' + simple + '"';
		}
		Object[] string = this.strings;
		assert string != null;
		StringBuilder builder = new StringBuilder("\"");
		for (Object object : string) {
			if (object instanceof Expression) {
				builder.append("%").append(((Expression<?>) object).toString(event, debug)).append("%");
			} else {
				builder.append(object);
			}
		}
		builder.append('"');
		return builder.toString();
	}

	/**
	 * Builds all possible default variable type hints based on the super type of the expression.
	 *
	 * @return List<String> of all possible super class code names.
	 */
	public @NotNull List<String> getDefaultVariableNames(String variableName, Event event) {
		if (script == null || mode != StringMode.VARIABLE_NAME)
			return Lists.newArrayList();

		if (isSimple) {
			assert simple != null;
			return Lists.newArrayList(simple, "object");
		}

		DefaultVariables data = script.getData(DefaultVariables.class);
		// Checked in Variable#getRaw already
		assert data != null : "default variables not present in current script";

		Class<?>[] savedHints = data.get(variableName);
		if (savedHints == null || savedHints.length == 0)
			return Lists.newArrayList();

		List<StringBuilder> typeHints = Lists.newArrayList(new StringBuilder());
		// Represents the index of which expression in a variable string, example name::%entity%::%object% the index of 0 will be entity.
		int hintIndex = 0;
		assert strings != null;
		for (Object object : strings) {
			if (!(object instanceof Expression)) {
				typeHints.forEach(builder -> builder.append(object));
				continue;
			}
			StringBuilder[] current = typeHints.toArray(new StringBuilder[0]);
			for (ClassInfo<?> classInfo : Classes.getAllSuperClassInfos(savedHints[hintIndex])) {
				for (StringBuilder builder : current) {
					String hint = builder.toString() + "<" + classInfo.getCodeName() + ">";
					// Has to duplicate the builder as it builds multiple off the last builder.
					typeHints.add(new StringBuilder(hint));
					typeHints.remove(builder);
				}
			}
			hintIndex++;
		}
		return typeHints.stream().map(StringBuilder::toString).collect(Collectors.toList());
	}

	public boolean isSimple() {
		return isSimple;
	}

	public StringMode getMode() {
		return mode;
	}

	public VariableString setMode(StringMode mode) {
		if (this.mode == mode || isSimple)
			return this;
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			VariableString variableString = newInstance(original, mode);
			if (variableString == null) {
				assert false : this + "; " + mode;
				return this;
			}
			return variableString;
		}
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSingle(Event event) {
		return toString(event);
	}

	@Override
	public String[] getArray(Event event) {
		return new String[] {toString(event)};
	}

	@Override
	public String[] getAll(Event event) {
		return new String[] {toString(event)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean check(Event event, Checker<? super String> checker, boolean negated) {
		return SimpleExpression.check(getAll(event), checker, negated, false);
	}

	@Override
	public boolean check(Event event, Checker<? super String> checker) {
		return SimpleExpression.check(getAll(event), checker, false, false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, String.class))
			return (Expression<? extends R>) this;
		return ConvertedExpression.newInstance(this, to);
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getAnd() {
		return true;
	}

	@Override
	public boolean setTime(int time) {
		return false;
	}

	@Override
	public int getTime() {
		return 0;
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public Iterator<? extends String> iterator(Event event) {
		return new SingleItemIterator<>(toString(event));
	}

	@Override
	public boolean isLoopOf(String input) {
		return false;
	}

	@Override
	public Expression<?> getSource() {
		return this;
	}

	public static <T> Expression<T> setStringMode(Expression<T> expression, StringMode mode) {
		if (expression instanceof ExpressionList) {
			Expression<?>[] expressions = ((ExpressionList<?>) expression).getExpressions();
			for (int i = 0; i < expressions.length; i++) {
				Expression<?> expr = expressions[i];
				assert expr != null;
				expressions[i] = setStringMode(expr, mode);
			}
		} else if (expression instanceof VariableString) {
			//noinspection unchecked
			return (Expression<T>) ((VariableString) expression).setMode(mode);
		}
		return expression;
	}

	@Override
	public Expression<String> simplify() {
		return this;
	}

}
