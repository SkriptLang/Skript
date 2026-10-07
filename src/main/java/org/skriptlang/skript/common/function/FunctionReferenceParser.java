package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionBinder.ArgumentParseTarget;
import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class containing the methods to parse an expression to a {@link FunctionReference}.
 * <p>
 * This turns the raw text of a function call into arguments. Selecting between overloads and binding
 * those arguments to a signature is done by {@link FunctionBinder}.
 * </p>
 *
 * @param context The context of parsing.
 * @param flags   The active parsing flags.
 */
public record FunctionReferenceParser(ParseContext context, int flags) {

	private final static Pattern FUNCTION_CALL_PATTERN =
			Pattern.compile("(?<name>[\\p{IsAlphabetic}_][\\p{IsAlphabetic}\\d_]*)\\((?<args>.*)\\)");

	private static final ArgsMessage INVALID_ARGUMENT = new ArgsMessage("functions.invalid argument");

	/**
	 * Attempts to parse {@code expr} as a function reference.
	 *
	 * @param <T> The return type of the function.
	 * @return A {@link FunctionReference} if a function is found, or {@code null} if none is found.
	 */
	public <T> FunctionReference<T> parseFunctionReference(String expr) {
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			if (!expr.endsWith(")")) {
				log.printLog();
				return null;
			}

			Matcher matcher = FUNCTION_CALL_PATTERN.matcher(expr);
			if (!matcher.matches()) {
				log.printLog();
				return null;
			}

			String functionName = matcher.group("name");
			String args = matcher.group("args");

			// Check for incorrect quotes, e.g. "myFunction() + otherFunction()" being parsed as one function
			// See https://github.com/SkriptLang/Skript/issues/1532
			for (int i = 0; i < args.length(); i = SkriptParser.next(args, i, context)) {
				if (i != -1) {
					continue;
				}
				log.printLog();
				return null;
			}

			if ((flags & SkriptParser.PARSE_EXPRESSIONS) == 0) {
				Skript.error("Functions cannot be used here (or there is a problem with your arguments).");
				log.printError();
				return null;
			}

			FunctionReference.Argument<String>[] arguments = new FunctionArgumentParser(args).getArguments();
			return parseFunctionReference(functionName, arguments, log);
		}
	}

	/**
	 * Attempts to parse a function reference.
	 *
	 * @param name      The function name.
	 * @param arguments The passed arguments to the function as an array of {@link Argument Arguments},
	 *                  usually parsed with a {@link FunctionArgumentParser}.
	 * @param log       The log handler.
	 * @param <T>       The return type of the function.
	 * @return A {@link FunctionReference} if a function is found, or {@code null} if none is found.
	 */
	public <T> FunctionReference<T> parseFunctionReference(String name, FunctionReference.Argument<String>[] arguments, ParseLogHandler log) {
		ParserInstance parser = ParserInstance.get();
		String namespace;
		if (parser.isActive()) {
			namespace = parser.getCurrentScript().getConfig().getFileName();
		} else {
			namespace = null;
		}

		FunctionBinder<String> binder =
			new FunctionBinder<>(new StringArgumentBinder(), FunctionBinder.Mode.STRICT, true);

		FunctionReference<T> reference = binder.<T>resolve(namespace, name, arguments).value();
		if (reference == null) {
			log.printError();
			return null;
		}

		// this call belongs to a script, so it has to be revalidated when the function it calls
		// is replaced.
		reference.track();
		return reference;
	}

	/**
	 * Binds the arguments of a function call, taking in the raw string values and parsing them into expressions.
	 */
	private final class StringArgumentBinder implements FunctionBinder.ArgumentBinder<String> {

		@Override
		public @Nullable Expression<?> bind(Argument<String> argument, ArgumentParseTarget target) {
			if (argument.value() == null) {
				return target.fallback();
			}

			SkriptParser parser = new SkriptParser(argument.value(), flags | SkriptParser.PARSE_LITERALS, context);

			try (ParseLogHandler logHandler = new ParseLogHandler().start()) {
				Expression<?> expression = parser.parseExpression(target.type());
				if (expression == null) {
					logHandler.printError(INVALID_ARGUMENT.toString(
						argument.name(), Classes.getSuperClassInfo(target.type()).getName().getSingular(), argument.value()
					));
				}
				return expression;
			}
		}

		@Override
		public Argument<String> joinForList(Argument<String>[] arguments, String parameterName) {
			String joined = arguments.length == 0 ? null : Arrays.stream(arguments).map(Argument::value)
					.collect(Collectors.joining(", "));
			return new Argument<>(ArgumentType.NAMED, parameterName, joined);
		}

		@Override
		public @Nullable String rawValue(Argument<String> argument) {
			return argument.raw();
		}

		@Override
		public @Nullable Expression<?> describe(Argument<String> argument) {
			SkriptParser parser = new SkriptParser(argument.value(), flags | SkriptParser.PARSE_LITERALS, context);

			return LiteralUtils.defendExpression(parser.parseExpression(Object.class));
		}

	}

	/**
	 * Represents an empty value used to make DefaultFunction calling work correctly.
	 */
	public static class EmptyExpression extends SimpleLiteral<Integer> {

		public EmptyExpression() {
			super(1, true);
		}

	}

}
