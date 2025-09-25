package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class containing the methods to parse an expression to a {@link FunctionReference}.
 *
 * @param context The context of parsing.
 * @param flags   The active parsing flags.
 */
public record FunctionReferenceParser(ParseContext context, int flags) {

	private final static Pattern FUNCTION_CALL_PATTERN =
		Pattern.compile("(?<name>[\\p{IsAlphabetic}_][\\p{IsAlphabetic}\\d_]*)\\((?<args>.*)\\)");

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
		// avoid assigning values to a parameter multiple times
		Set<String> named = new HashSet<>();
		for (Argument<String> argument : arguments) {
            if (argument.type() != ArgumentType.NAMED) {
                continue;
            }

            boolean added = named.add(argument.name());
            if (added) {
                continue;
            }

            Skript.error(Language.get("functions.already assigned value to parameter"), argument.name());
            log.printError();
            return null;
        }

		String namespace;
		if (ParserInstance.get().isActive()) {
			namespace = ParserInstance.get().getCurrentScript().getConfig().getFileName();
		} else {
			namespace = null;
		}

		// try to find a matching signature to get which types to parse args with
		Set<Signature<?>> options = FunctionRegistry.getRegistry().getSignatures(namespace, name);

		if (options.isEmpty()) {
			doesNotExist(name, arguments);
			log.printError();
			return null;
		}

		// all signatures that have no single list param
		// example: function add(x: int, y: int)
		Set<Signature<?>> exacts = new HashSet<>();
		// all signatures with only single list params
		// these are functions that accept any number of arguments given a specific type
		// example: function sum(ns: numbers)
		Set<Signature<?>> lists = new HashSet<>();

		// first, sort into types
		for (Signature<?> option : options) {
			if (option.parameters().size() == 1 && !option.parameters().firstEntry().getValue().single()) {
				lists.add(option);
			} else {
				exacts.add(option);
			}
		}

		// second, try to match any exact functions
		Set<FunctionReference<T>> exactReferences = getExactReferences(namespace, name, exacts, arguments);
		if (exactReferences == null) { // a list error, so quit parsing
			log.printError();
			return null;
		}

		// if we found an exact one, return first to avoid conflict with list references
		if (exactReferences.size() == 1) {
			return exactReferences.stream().findAny().orElse(null);
		}

		// last, find single list functions
		Set<FunctionReference<T>> listReferences = getListReferences(namespace, name, lists, arguments);
		if (listReferences == null) { // a list error, so quit parsing
			log.printError();
			return null;
		}

		exactReferences.addAll(listReferences);

		if (exactReferences.isEmpty()) {
			doesNotExist(name, arguments);
			log.printError();
			return null;
		} else if (exactReferences.size() == 1) {
			return exactReferences.stream().findAny().orElse(null);
		} else {
			ambiguousError(name, exactReferences);
			log.printError();
			return null;
		}
	}

	/**
	 * Returns all possible {@link FunctionReference FunctionReferences} given the list of signatures
	 * which do not contain a single list parameter.
	 *
	 * @param namespace  The current namespace.
	 * @param name       The name of the function.
	 * @param signatures The possible signatures.
	 * @param arguments  The passed arguments.
	 * @param <T>        The return type of the references.
	 * @return All possible exact {@link FunctionReference FunctionReferences}.
	 */
	private <T> Set<FunctionReference<T>> getExactReferences(
		String namespace, String name,
		Set<Signature<?>> signatures, Argument<String>[] arguments
	) {
		Set<FunctionReference<T>> exactReferences = new HashSet<>();

		signatures:
		for (Signature<?> signature : signatures) {
			// if arguments arent possible, skip
			if (arguments.length > signature.getMaxParameters() || arguments.length < signature.getMinParameters()) {
				continue;
			}

			// all remaining arguments to parse
			// if a passed argument is named it bypasses the regular argument order of unnamed arguments
			LinkedHashSet<String> remaining = new LinkedHashSet<>(signature.parameters().keySet());

			Class<?>[] targets = new Class<?>[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				Argument<String> argument = arguments[i];

				if (remaining.isEmpty()) {
					break;
				}

				Parameter<?> parameter;
				if (argument.type() == ArgumentType.NAMED) {
					parameter = signature.getParameter(argument.name());
				} else {
					parameter = signature.getParameter(remaining.getFirst());
				}

				if (parameter == null) {
					continue signatures;
				}

				if (parameter.type().isArray()) {
					targets[i] = parameter.type().componentType();
				} else {
					targets[i] = parameter.type();
				}

				remaining.remove(parameter.name());
			}

			//noinspection DuplicatedCode
			FunctionArgumentParseResult result = parseFunctionArguments(arguments, targets);

			if (result.type() == FunctionArgumentParseResultType.LIST_ERROR) {
				return null;
			}

			if (result.type() == FunctionArgumentParseResultType.OK) {
				//noinspection unchecked
				FunctionReference<T> reference = new FunctionReference<>(namespace, name, (Signature<T>) signature, result.parsed());

				if (!reference.validate()) {
					continue;
				}

				exactReferences.add(reference);
			}
		}
		return exactReferences;
	}

	/**
	 * Returns all possible {@link FunctionReference FunctionReferences} given the list of signatures
	 * which only contain a single list parameter.
	 *
	 * @param namespace  The current namespace.
	 * @param name       The name of the function.
	 * @param signatures The possible signatures.
	 * @param arguments  The passed arguments.
	 * @param <T>        The return type of the references.
	 * @return All possible {@link FunctionReference FunctionReferences} which contain a single list parameter.
	 */
	private <T> Set<FunctionReference<T>> getListReferences(
		String namespace, String name,
		Set<Signature<?>> signatures, Argument<String>[] arguments
	) {
		// disallow naming any arguments other than the first
		if (arguments.length > 1) {
			for (Argument<String> argument : arguments) {
                if (argument.type() != ArgumentType.NAMED) {
                    continue;
                }

                doesNotExist(name, arguments);
                return null;
            }
		}

		Set<FunctionReference<T>> references = new HashSet<>();

		signatures:
		for (Signature<?> signature : signatures) {
			Parameter<?> parameter = signature.parameters().firstEntry().getValue();

			Class<?> target = parameter.type().componentType();
			Class<?>[] targets = new Class<?>[]{target};

			if (arguments.length == 1 && arguments[0].type() == ArgumentType.NAMED) {
				if (!arguments[0].name().equals(parameter.name())) {
					doesNotExist(name, arguments);
					continue;
				}
			}

			// join all args to a single arg
			String joined = Arrays.stream(arguments).map(Argument::value)
				.collect(Collectors.joining(", "));
			Argument<String> argument = new Argument<>(ArgumentType.NAMED, parameter.name(), joined);
			Argument<String>[] array = CollectionUtils.array(argument);

			//noinspection DuplicatedCode
			FunctionArgumentParseResult result = parseFunctionArguments(array, targets);

			if (result.type() == FunctionArgumentParseResultType.LIST_ERROR) {
				return null;
			}

			if (result.type() == FunctionArgumentParseResultType.OK) {
				// avoid allowing lists inside lists
				if (result.parsed.length == 1 && result.parsed[0].value() instanceof ExpressionList<?> list) {
					for (Expression<?> expression : list.getExpressions()) {
						if (expression instanceof ExpressionList<?>) {
							doesNotExist(name, arguments);
							continue signatures;
						}
					}
				}

				//noinspection unchecked
				FunctionReference<T> reference = new FunctionReference<>(namespace, name, (Signature<T>) signature, result.parsed());

				if (!reference.validate()) {
					continue;
				}

				references.add(reference);
			}
		}

		return references;
	}

	/**
	 * Prints the error for when multiple function references have been matched.
	 *
	 * @param name       The function name.
	 * @param references The possible references.
	 * @param <T>        The return types of the references.
	 */
	private <T> void ambiguousError(String name, Set<FunctionReference<T>> references) {
		List<String> parts = new ArrayList<>();

		for (FunctionReference<T> reference : references) {
			String builder = reference.name() +
				"(" +
				reference.signature().parameters().values().stream()
					.map(it -> {
						if (it.type().isArray()) {
							return Classes.getSuperClassInfo(it.type().componentType()).getName().getPlural();
						} else {
							return Classes.getSuperClassInfo(it.type()).getName().getSingular();
						}
					})
					.collect(Collectors.joining(", ")) +
				")";

			parts.add(builder);
		}

		Skript.error(Language.get("functions.ambiguous function call"),
			name, StringUtils.join(parts, ", ", " or "));
	}

	/**
	 * Prints the error for when a function does not exist.
	 *
	 * @param name      The function name.
	 * @param arguments The passed arguments to the function call.
	 */
	private void doesNotExist(String name, FunctionReference.Argument<String>[] arguments) {
		StringJoiner joiner = new StringJoiner(", ");

		for (FunctionReference.Argument<String> argument : arguments) {
			SkriptParser parser = new SkriptParser(argument.value(), flags | SkriptParser.PARSE_LITERALS, context);

			Expression<?> expression = LiteralUtils.defendExpression(parser.parseExpression(Object.class));

			String argumentName;
			if (argument.type() == ArgumentType.NAMED) {
				argumentName = argument.name() + ": ";
			} else {
				argumentName = "";
			}

			if (!LiteralUtils.canInitSafely(expression)) {
				joiner.add(argumentName + "?");
				continue;
			}

			if (expression.isSingle()) {
				joiner.add(argumentName + Classes.getSuperClassInfo(expression.getReturnType()).getName().getSingular());
			} else {
				joiner.add(argumentName + Classes.getSuperClassInfo(expression.getReturnType()).getName().getPlural());
			}
		}

		Skript.error("The function %s(%s) does not exist.", name, joiner);
	}

	/**
	 * The type of result from attempting to parse function arguments.
	 */
	private enum FunctionArgumentParseResultType {

		/**
		 * All arguments were successfully parsed to the specified type.
		 */
		OK,

		/**
		 * An argument failed to parse to the specified target type.
		 */
		PARSE_FAIL,

		/**
		 * An expression list contained "or", thus parsing should be stopped.
		 */
		LIST_ERROR

	}

	/**
	 * The results of attempting to parse function arguments.
	 *
	 * @param type   The type of result.
	 * @param parsed The resulting parsed arguments, or null if parsing was not successful.
	 */
	private record FunctionArgumentParseResult(FunctionArgumentParseResultType type,
											   FunctionReference.Argument<Expression<?>>[] parsed) {

	}

	/**
	 * Attempts to parse every argument in {@code arguments} as the specified type in {@code targets}.
	 *
	 * @param arguments The arguments to parse.
	 * @param targets   The target classes to parse.
	 * @return A {@link FunctionArgumentParseResult} with the results.
	 */
	private FunctionArgumentParseResult parseFunctionArguments(FunctionReference.Argument<String>[] arguments, Class<?>[] targets) {
		assert arguments.length == targets.length;
		assert Arrays.stream(targets).noneMatch(Class::isArray);

		//noinspection unchecked
		FunctionReference.Argument<Expression<?>>[] parsed = (FunctionReference.Argument<Expression<?>>[])
			new FunctionReference.Argument[arguments.length];

		for (int i = 0; i < arguments.length; i++) {
			FunctionReference.Argument<String> argument = arguments[i];

			SkriptParser parser = new SkriptParser(argument.value(), flags | SkriptParser.PARSE_LITERALS, context);

			Expression<?> expression = parser.parseExpression(targets[i]);

			if (expression == null) {
				return new FunctionArgumentParseResult(FunctionArgumentParseResultType.PARSE_FAIL, null);
			}

			if (expression instanceof ExpressionList<?> list && !list.getAnd()) {
				Skript.error(Language.get("functions.or in arguments"));
				return new FunctionArgumentParseResult(FunctionArgumentParseResultType.LIST_ERROR, null);
			}

			parsed[i] = new FunctionReference.Argument<>(argument.type(), argument.name(), expression);
		}

		return new FunctionArgumentParseResult(FunctionArgumentParseResultType.OK, parsed);
	}
}
