package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;
import org.skriptlang.skript.common.function.Parameter.Modifier;

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

	private static final ArgsMessage UNEXPECTED_ARGUMENT = new ArgsMessage("functions.unexpected argument");
	private static final ArgsMessage INVALID_ARGUMENT = new ArgsMessage("functions.invalid argument");
	private static final ArgsMessage UNKNOWN_FUNCTION = new ArgsMessage("functions.unknown function");
	private static final ArgsMessage POTENTIAL_SIGNATURE = new ArgsMessage("functions.potential signature");

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

		ParserInstance parser = ParserInstance.get();
		String namespace;
		if (parser.isActive()) {
			namespace = parser.getCurrentScript().getConfig().getFileName();
		} else {
			namespace = null;
		}

		// try to find a matching signature to get which types to parse args with
		Set<Signature<?>> options = FunctionRegistry.getRegistry().getSignatures(namespace, name);

		if (options.isEmpty()) {
			doesNotExist(name, arguments, options);
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
			if (option.parameters().size() == 1 && !option.parameters().getFirst().isSingle()) {
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
			doesNotExist(name, arguments, Sets.union(exacts, lists));
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

		// whether the arguments array contains any NAMED type parameters
		// if there are no named parameters, then we consider the user arguments to already be in order
		boolean hasNames = Arrays.stream(arguments).anyMatch(argument -> argument.type() == ArgumentType.NAMED);

		// TODO! cache results
		// 'arguments' may be reassigned when processing a signature
		// retain a reference to the original array for resetting each time
		Argument<String>[] originalArguments = arguments;
		for (Signature<?> signature : signatures) {
			// if arguments arent possible, skip
			if (arguments.length > signature.getMaxParameters() || arguments.length < signature.getMinParameters()) {
				continue;
			}
			arguments = originalArguments;

			// all remaining arguments to parse
			// if a passed argument is named it bypasses the regular argument order of unnamed arguments
			SequencedMap<String, Parameter<?>> parameters = signature.parameters().sequencedMap();

			// if a parameter is not passed, we need to create a dummy argument to allow parsing in parseFunctionArguments
			//noinspection unchecked
			Argument<String>[] parseArguments = (Argument<String>[]) new Argument[parameters.size()];
			ArgumentParseTarget[] parseTargets = new ArgumentParseTarget[parameters.size()];

			// fill in named arguments, placeholders for unnamed arguments
			// essentially, we reorder the user provided arguments into the order defined by the parameters
			int index = 0;
			// whether, after this first pass completes, there are any placeholders to fill in
			boolean hasPlaceholders = false;
			for (var entry : parameters.entrySet()) {
				Parameter<?> parameter = entry.getValue();

				// attempt to match an argument to this parameter
				Argument<String> argument = null;
				if (hasNames) {
					for (var candidate : arguments) {
						if (entry.getKey().equals(candidate.name())) { // exact match
							argument = candidate;
							break;
						}
					}
				} else if (index < arguments.length) { // if there are no named arguments, simply take the next provided one
					argument = arguments[index];
				}
				if (argument == null) { // could not resolve an argument, use a placeholder
					argument = new Argument<>(ArgumentType.UNNAMED, entry.getKey(), null);
					hasPlaceholders = true;
				}
				parseArguments[index] = argument;

				// prepare type information for parsing
				Class<?> targetType = Utils.getComponentType(parameter.type());
				Expression<?> fallback;
				if (parameter instanceof ScriptParameter<?> sp) {
					fallback = sp.defaultValue();
				} else if (parameter.hasModifier(Modifier.OPTIONAL)) {
					fallback = new EmptyExpression();
				} else {
					fallback = null;
				}
				parseTargets[index] = new ArgumentParseTarget(targetType, fallback);

				index++;
			}
			if (hasPlaceholders) { // fill in placeholder arguments as necessary
				// some arguments may be erroneously interpreted as name, but their value just contains a colon
				// for example, minecraft:stone
				// convert these unexpected named arguments to unnamed arguments
				boolean copied = false;
				for (int i = 0; i < arguments.length; i++) {
					Argument<String> argument = arguments[i];
					if (argument.type() == ArgumentType.NAMED && !parameters.containsKey(argument.name())) {
						if (!copied) {
							arguments = Arrays.copyOf(arguments, arguments.length);
							copied = true;
						}
						// we retain the name for later purposes, such as logging
						arguments[i] = new Argument<>(ArgumentType.UNNAMED, argument.name(), argument.raw());
					}
				}

				// we track named arguments as we encounter them
				// we use this to avoid inserting the next unnamed argument too early
				// for example, consider func(x, optional y, z, optional aa)
				// for a call such as func(x: 1: z: 2, 3), '3' should map to 'aa' rather than 'y'
				List<String> priorNames = new ArrayList<>();
				// the index of the last verified argument index
				// this is used to avoid reverifying the position of named arguments multiple times
				int lastCheck = -1;
				// the index of the last unnamed argument slot that was filled
				int lastFilled = -1;
				fill: for (int i = 0; i < arguments.length; i++) {
					Argument<String> argument = arguments[i];
					if (argument.type() == ArgumentType.NAMED) {
						priorNames.add(argument.name());
						if (i > 0 && arguments[i - 1].type() == ArgumentType.NAMED) {
							// since the last argument was named, the position of this one has not been verified
							// thus, search through the parse arguments again
							lastCheck = -1;
						}
						continue;
					}
					// find the next named argument
					String nextName = null;
					for (int j = i + 1; j < arguments.length; j++) {
						Argument<String> nextArgument = arguments[j];
						if (nextArgument.type() == ArgumentType.NAMED) {
							nextName = nextArgument.name();
							break;
						}
					}
					// fill in using this unnamed argument
					for (int j = lastCheck + 1; j < parseArguments.length; j++) {
						Argument<String> parseArgument = parseArguments[j];
						if (parseArgument.type() == ArgumentType.NAMED) {
							if (parseArgument.name().equals(nextName)) { // this argument is in an invalid position
								break;
							}
							priorNames.remove(parseArgument.name());
						} else if (j > lastFilled && priorNames.isEmpty()) { // can occupy this slot
							parseArguments[j] = new Argument<>(ArgumentType.UNNAMED, parseArgument.name(), argument.value());
							lastCheck = j;
							lastFilled = j;
							continue fill;
						}
					}
					// unable to find a slot for this argument
					if (argument.name() != null) { // this was originally a named argument, error as if it is
						Skript.error(UNEXPECTED_ARGUMENT.toString(argument.name()));
					} else {
						Skript.error(Language.get("functions.mixing named and unnamed arguments"));
					}
					return null;
				}
			}

			ArgumentParseResult result = parseFunctionArguments(parseArguments, parseTargets);
			switch (result.type()) {
				case LIST_ERROR -> {
					return null;
				}
				case OK -> {
					//noinspection unchecked
					FunctionReference<T> reference = new FunctionReference<>(namespace, name, (Signature<T>) signature, result.parsed());
					if (!reference.validate()) {
						continue;
					}
					exactReferences.add(reference);
				}
				default -> {
					// continue
				}
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

				doesNotExist(name, arguments, signatures);
				return null;
			}
		}

		Set<FunctionReference<T>> references = new HashSet<>();

		signatures:
		for (Signature<?> signature : signatures) {
			Parameter<?> parameter = signature.parameters().getFirst();

			Class<?> targetType = parameter.type().componentType();
			Expression<?> fallback;
			if (parameter instanceof ScriptParameter<?> sp) {
				fallback = sp.defaultValue();
			} else if (parameter.hasModifier(Modifier.OPTIONAL)) {
				fallback = new EmptyExpression();
			} else {
				fallback = null;
			}
			ArgumentParseTarget[] parseTargets = new ArgumentParseTarget[]{new ArgumentParseTarget(targetType, fallback)};

			if (arguments.length == 1 && arguments[0].type() == ArgumentType.NAMED) {
				if (!arguments[0].name().equals(parameter.name())) {
					doesNotExist(name, arguments, signatures);
					continue;
				}
			}

			// join all args to a single arg
			String joined = arguments.length == 0 ? null : Arrays.stream(arguments).map(Argument::value)
					.collect(Collectors.joining(", "));
			Argument<String> argument = new Argument<>(ArgumentType.NAMED, parameter.name(), joined);
			Argument<String>[] array = CollectionUtils.array(argument);

			//noinspection DuplicatedCode
			ArgumentParseResult result = parseFunctionArguments(array, parseTargets);

			if (result.type() == ArgumentParseResultType.LIST_ERROR) {
				return null;
			}

			if (result.type() == ArgumentParseResultType.OK) {
				// avoid allowing lists inside lists
				if (result.parsed().length == 1 && result.parsed()[0].value() instanceof ExpressionList<?> list) {
					for (Expression<?> expression : list.getExpressions()) {
						if (expression instanceof ExpressionList<?>) {
							doesNotExist(name, arguments, signatures);
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
					Arrays.stream(reference.signature().parameters().all())
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
	 * @param possibleSignatures A set of signatures that may contain what the user intended to match.
	 */
	private void doesNotExist(String name, FunctionReference.Argument<String>[] arguments, Set<Signature<?>> possibleSignatures) {
		StringJoiner joiner = new StringJoiner(", ");

		List<Class<?>> argumentTypes = new ArrayList<>();
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

			Class<?> returnType = expression.getReturnType();
			argumentTypes.add(returnType);
			ClassInfo<?> classInfo = Classes.getSuperClassInfo(returnType);
			if (expression.isSingle()) {
				joiner.add(argumentName + classInfo.getName().getSingular());
			} else {
				joiner.add(argumentName + classInfo.getName().getPlural());
			}
		}

		String possibleMatch = "";
		var intended = possibleSignatures.stream()
			.filter(signature -> { // filter for signatures that contain all known arguments
				List<Class<?>> currentArgumentTypes = new ArrayList<>(argumentTypes);
				Arrays.stream(signature.parameters().all())
					.map(parameter -> Utils.getComponentType(parameter.type()))
					.forEach(type -> {
						var iterator = currentArgumentTypes.iterator();
						while (iterator.hasNext()) {
							if (type.isAssignableFrom(iterator.next())) {
								iterator.remove();
								break;
							}
						}
					});
				return currentArgumentTypes.isEmpty();
			})
			.min(Comparator.comparingInt(signature -> Math.abs(arguments.length - signature.getMaxParameters())));
		if (intended.isPresent()) {
			possibleMatch = " " + POTENTIAL_SIGNATURE.toString(intended.get().toString(false, false));
		}
		Skript.error(UNKNOWN_FUNCTION.toString(name, joiner) + possibleMatch);
	}

	/**
	 * The type of result from attempting to parse function arguments.
	 */
	private enum ArgumentParseResultType {

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
	private record ArgumentParseResult(
			@NotNull ArgumentParseResultType type,
			FunctionReference.Argument<Expression<?>>[] parsed) {

	}

	/**
	 * Represents the data related to parsing an argument to a target type.
	 *
	 * @param type     The target type.
	 * @param fallback If the argument cannot be parsed as the target type, the expression to use as fallback.
	 */
	private record ArgumentParseTarget(@NotNull Class<?> type, @Nullable Expression<?> fallback) {

	}

	/**
	 * Attempts to parse every argument in {@code arguments} as the specified type in {@code targets}.
	 *
	 * @param arguments The arguments to parse.
	 * @param targets   The target classes to parse.
	 * @return A {@link ArgumentParseResult} with the results.
	 */
	private ArgumentParseResult parseFunctionArguments(FunctionReference.Argument<String>[] arguments, ArgumentParseTarget[] targets) {
		assert arguments.length == targets.length;
		assert Arrays.stream(targets).map(ArgumentParseTarget::type).noneMatch(Class::isArray);

		//noinspection unchecked
		FunctionReference.Argument<Expression<?>>[] parsed = (FunctionReference.Argument<Expression<?>>[])
				new FunctionReference.Argument[arguments.length];

		for (int i = 0; i < arguments.length; i++) {
			FunctionReference.Argument<String> argument = arguments[i];
			ArgumentParseTarget targetData = targets[i];

			Expression<?> expression = parseExpression(argument, targetData);
			if (expression == null) {
				return new ArgumentParseResult(ArgumentParseResultType.PARSE_FAIL, null);
			}

			if (expression instanceof ExpressionList<?> list && !list.getAnd()) {
				Skript.error(Language.get("functions.or in arguments"));
				return new ArgumentParseResult(ArgumentParseResultType.LIST_ERROR, null);
			}

			parsed[i] = new FunctionReference.Argument<>(argument.type(), argument.name(), expression);
		}

		return new ArgumentParseResult(ArgumentParseResultType.OK, parsed);
	}

	/**
	 * Attempts to parse an argument into an expression.
	 * This method will log parsing errors.
	 *
	 * @param argument The argument.
	 * @param targetData The target type to parse to, and the fallback value.
	 * @return {@code targetData.fallback} if the argument does not have a value.
	 * 	Otherwise, the parsed expression or null if parsing failed.
	 */
	private Expression<?> parseExpression(Argument<String> argument, ArgumentParseTarget targetData) {
		if (argument.value() == null) {
			return targetData.fallback;
		}

		SkriptParser parser = new SkriptParser(argument.value(), flags | SkriptParser.PARSE_LITERALS, context);

		try (ParseLogHandler logHandler = new ParseLogHandler().start()) {
			Expression<?> expression = parser.parseExpression(targetData.type());
			if (expression == null) {
				logHandler.printError(INVALID_ARGUMENT.toString(
					Classes.getSuperClassInfo(targetData.type()).getName().getSingular(), argument.name(), argument.value()
				));
			}
			return expression;
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
