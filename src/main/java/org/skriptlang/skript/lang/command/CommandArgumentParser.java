package org.skriptlang.skript.lang.command;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import com.google.common.base.Preconditions;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class responsible for parsing command argument strings.
 * <p>
 * It tokenizes an input string into a list of {@link CommandArgument} objects.
 */
public final class CommandArgumentParser {

	private CommandArgumentParser() {
		throw new UnsupportedOperationException();
	}

	private static final Pattern ARGUMENT_TOKENIZER_PATTERN =
		Pattern.compile("<[^>]+>|\\S+");

	private static final Pattern TYPED_ARGUMENT_PARSER_PATTERN =
		Pattern.compile("\\s*([^:]+?)\\s*:\\s*(.+?)\\s*");

	/**
	 * Parses a raw string of arguments into a list of CommandArgument objects.
	 *
	 * @param arguments the raw argument string (e.g. "help <target: player>")
	 * @return a list of parsed command arguments
	 */
	public static List<CommandArgument> parse(String arguments) {
		if (arguments == null || arguments.isBlank())
			return Collections.emptyList();

		Matcher matcher = ARGUMENT_TOKENIZER_PATTERN.matcher(arguments);
		List<CommandArgument> argumentList = new LinkedList<>();

		while (matcher.find()) {
			String token = matcher.group();
			try {
				argumentList.add(parseToken(token));
			} catch (IllegalArgumentException e) {
				Skript.error("Failed to parse command argument '" + token + "': " + e.getMessage());
				// Stop parsing on the first error.
				return Collections.emptyList();
			}
		}
		return argumentList;
	}

	/**
	 * Parses a single argument token into a {@link CommandArgument} object.
	 *
	 * @param token a single token (e.g. "<name:type>" or "help").
	 * @return The parsed CommandArgument
	 */
	private static CommandArgument parseToken(String token) {
		if (token.startsWith("<") && token.endsWith(">")) {
			return parseTypedArgument(token);
		} else {
			return new CommandArgument.Literal(token);
		}
	}

	@SuppressWarnings("unchecked")
	private static CommandArgument.Typed<?> parseTypedArgument(String token) {
		// remove the outer brackets to get the content
		String content = token.substring(1, token.length() - 1);
		Matcher typedMatcher = TYPED_ARGUMENT_PARSER_PATTERN.matcher(content);

		Preconditions.checkArgument(typedMatcher.matches(), "Invalid format: must be '<name: type>', but got '" + token + "'");

		String name = typedMatcher.group(1);
		String typeExpression = typedMatcher.group(2);

		Preconditions.checkArgument(!name.isBlank(), "Argument name cannot be blank in '" + token + "'");
		Preconditions.checkArgument(!typeExpression.isBlank(), "Argument type cannot be blank in '" + token + "'");

		var argumentTypes = (Collection<SyntaxInfo<ArgumentTypeElement<?>>>) (Collection<?>)
			Skript.instance().syntaxRegistry().syntaxes(ArgumentTypeElement.REGISTRY_KEY);

		ArgumentTypeElement<?> parsedType = SkriptParser.parseStatic(typeExpression, argumentTypes.iterator(),
			ParseContext.DEFAULT, "Failed to parse argument type '" + typeExpression + "'");

		Preconditions.checkArgument(parsedType != null, "Unknown argument type in '" + token + "'");
		return new CommandArgument.Typed<>(name, parsedType);
	}

}
