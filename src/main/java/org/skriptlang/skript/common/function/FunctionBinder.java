package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Result;
import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;
import org.skriptlang.skript.common.function.FunctionReferenceParser.EmptyExpression;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.SequencedMap;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Resolves a function call to a {@link FunctionReference}, selecting between overloads and
 * binding the passed arguments to the chosen signature's parameters.
 * <p>
 * This class owns everything about that process which does not depend on how an argument's value
 * is represented. The representation-specific steps are supplied by an {@link ArgumentBinder},
 * so that the same resolution can be driven both from the raw argument text of a function call
 * written in a script and from already-parsed expressions.
 * </p>
 *
 * @param <T> The type an argument's value is represented by.
 */
public final class FunctionBinder<T> {

	private static final ArgsMessage UNEXPECTED_ARGUMENT = new ArgsMessage("functions.unexpected argument");
	private static final ArgsMessage UNKNOWN_FUNCTION = new ArgsMessage("functions.unknown function");
	private static final ArgsMessage POTENTIAL_SIGNATURE = new ArgsMessage("functions.potential signature");

	private final ArgumentBinder<T> binder;
	private final Mode mode;
	/**
	 * Stands in for the reason a binder which reports to the log fails with, since that binder's
	 * caller reads the reason out of its own log rather than off the result. Never shown.
	 */
	private static final String REPORTED_IN_LOG = "The reason was left in the log.";

	/**
	 * The reason given when a binder which returns its reasons somehow has none, which the paths
	 * out of {@link #match(String, String, Argument[], Predicate)} should make unreachable.
	 */
	private static final String NO_REASON_GIVEN = "No matching function was found.";

	private final boolean report;

	/**
	 * @param binder The strategy for the arguments being bound.
	 * @param mode   How arguments are bound to parameters.
	 * @param report Whether a failure is left in the log for the caller to print, rather than
	 *               returned. Only a caller which is parsing a script has a log to print.
	 */
	FunctionBinder(@NotNull ArgumentBinder<T> binder, @NotNull Mode mode, boolean report) {
		this.binder = binder;
		this.mode = mode;
		this.report = report;
	}

	/**
	 * Returns a binder for arguments which have already been parsed into expressions.
	 *
	 * @param mode How to bind the passed arguments to a signature's parameters.
	 * @return The binder.
	 */
	public static FunctionBinder<Expression<?>> forExpressions(@NotNull Mode mode) {
		// a caller resolving at runtime has no log to print, so it is given the reason instead
		return new FunctionBinder<>(new ExpressionArgumentBinder(), mode, false);
	}

	/**
	 * How the passed arguments are bound to the parameters of a signature.
	 */
	public enum Mode {

		/**
		 * Every parameter takes exactly one of the passed arguments, except that a signature whose
		 * only parameter accepts a list may take all of them.
		 */
		STRICT,

		/**
		 * As {@link #STRICT}, but if no signature can be bound that way, a signature containing a
		 * list parameter may have it take several of the passed arguments. See
		 * {@link #allocations(Parameter[], int)}.
		 */
		GREEDY

	}

	/**
	 * Attempts to resolve a function call to a single {@link FunctionReference}.
	 *
	 * @param namespace The namespace to resolve local functions in, or null for global functions
	 *                  only.
	 * @param name      The function name.
	 * @param arguments The passed arguments.
	 * @param <R>       The return type of the function.
	 * @return Either the matched reference, or why there is none.
	 */
	public <R> @NotNull Result<FunctionReference<R>> resolve(
		@Nullable String namespace, @NotNull String name, @NotNull Argument<T>[] arguments
	) {
		return resolve(namespace, name, arguments, null);
	}

	/**
	 * Attempts to resolve a function call to a single {@link FunctionReference}, considering only
	 * the overloads {@code only} accepts.
	 *
	 * @param namespace The namespace to resolve local functions in, or null for global functions
	 *                  only.
	 * @param name      The function name.
	 * @param arguments The passed arguments.
	 * @param only      Which overloads may be used, or null for any of them.
	 * @param <R>       The return type of the function.
	 * @return Either the matched reference, or why there is none. The reason is only meaningful
	 * 	for a binder which returns it; one which reports to the log fails without a usable reason,
	 * 	since its caller reads that out of the log instead.
	 */
	public <R> @NotNull Result<FunctionReference<R>> resolve(
		@Nullable String namespace, @NotNull String name, @NotNull Argument<T>[] arguments,
		@Nullable Predicate<Signature<?>> only
	) {
		if (report) {
			// the caller prints its own log, which is what lets the best of the errors from the
			// candidates which did not match win
			FunctionReference<R> reference = match(namespace, name, arguments, only);
			return reference != null
				? Result.success(reference)
				: Result.failure(REPORTED_IN_LOG);
		}

		try (RetainingLogHandler log = SkriptLogger.startRetainingLog()) {
			FunctionReference<R> reference = match(namespace, name, arguments, only);

			String rejection = null;
			if (reference == null) {
				rejection = reject(log.getErrors(), overloadCount(namespace, name, only));
			}

			log.clear();
			log.printLog(); // clearing alone does not count as having handled the log

			return reference != null
				? Result.success(reference)
				: Result.failure(rejection != null ? rejection : NO_REASON_GIVEN);
		}
	}

	/**
	 * Picks which of the errors logged while matching to show the user.
	 * <p>
	 * With a single overload the first error is that overload's own reason -- which argument did
	 * not fit and why -- and the summary logged after it only says that the call matched nothing,
	 * so the first is the more useful of the two. With several overloads each one logged its own
	 * reason and picking one of those would be arbitrary, since they are visited in the iteration
	 * order of a {@link HashSet}; the summary, which names the call and suggests the closest
	 * signature, is the only answer that does not depend on that order.
	 * </p>
	 *
	 * @param errors    The errors logged while matching, in the order they were logged.
	 * @param overloads How many overloads the call could have matched.
	 * @return The message to report, or null if nothing was logged.
	 */
	private static @Nullable String reject(@NotNull Collection<LogEntry> errors, int overloads) {
		String first = null;
		String last = null;

		for (LogEntry error : errors) {
			if (first == null) {
				first = error.getMessage();
			}
			last = error.getMessage();
		}

		return overloads > 1 ? last : first;
	}

	/**
	 * Counts the overloads a call could have matched, which is only needed to choose a rejection
	 * message and so is worked out on the failing path rather than kept from the matching itself.
	 *
	 * @param namespace The namespace to resolve local functions in, or null for global functions
	 *                  only.
	 * @param name      The function name.
	 * @param only      Which overloads may be used, or null for any of them.
	 * @return How many overloads of {@code name} were available to the call.
	 */
	private int overloadCount(
		@Nullable String namespace, @NotNull String name, @Nullable Predicate<Signature<?>> only
	) {
		Set<Signature<?>> options = FunctionRegistry.getRegistry().getSignatures(namespace, name);
		if (only == null) {
			return options.size();
		}

		int count = 0;
		for (Signature<?> option : options) {
			if (only.test(option)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Binds a function call to one signature which is already known to be the right one, without
	 * searching the overloads of {@code name} again.
	 * <p>
	 * This is for a caller which has resolved the same call before and remembered what it resolved
	 * to. It returns null when {@code signature} does not fit the arguments after all, which the
	 * caller answers by resolving properly; it never reports anything, since not fitting is not an
	 * error here.
	 * </p>
	 *
	 * @param namespace The namespace to resolve local functions in, or null for global functions
	 *                  only.
	 * @param name      The function name.
	 * @param signature  The signature to bind to.
	 * @param allocation A division of the arguments which fit this signature before, tried first so
	 *                   that the divisions are not searched again, or null if none is known.
	 * @param arguments  The passed arguments.
	 * @return The bound reference and the division which fit, or null if the arguments do not fit
	 * 	{@code signature}.
	 */
	public @Nullable Division bindTo(
		@Nullable String namespace, @NotNull String name, @NotNull Signature<?> signature,
		int @Nullable [] allocation, @NotNull Argument<T>[] arguments
	) {
		Parameter<?>[] parameters = signature.parameters().all();
		boolean list = parameters.length == 1 && !parameters[0].isSingle();
		Set<Signature<?>> only = Set.of(signature);

		try (RetainingLogHandler log = SkriptLogger.startRetainingLog()) {
			try {
				// a division which fit before is tried on its own first, so that a call site which
				// has already bound does not search the divisions again on every execution
				if (allocation != null) {
					Attempt<Object> attempt =
						this.bindDivision(namespace, name, signature, parameters, allocation, arguments);
					if (attempt.reference() != null) {
						return new Division(attempt.reference(), allocation);
					}
				}

				Set<FunctionReference<Object>> references = list
					? this.getListReferences(namespace, name, only, arguments)
					: this.getExactReferences(namespace, name, only, arguments);

				if (references != null && references.size() == 1) {
					// bound one argument per parameter, so there is no division to remember
					return new Division(references.iterator().next(), null);
				}

				if (!list && mode == Mode.GREEDY) {
					for (int[] division : allocations(parameters, arguments.length)) {
						Attempt<Object> attempt =
							this.bindDivision(namespace, name, signature, parameters, division, arguments);

						if (attempt.listError()) {
							return null;
						}
						if (attempt.reference() != null) {
							return new Division(attempt.reference(), division);
						}
					}
				}

				return null;
			} finally {
				log.clear();
				log.printLog(); // clearing alone does not count as having handled the log
			}
		}
	}

	/**
	 * A reference bound to a known signature, and how the arguments were divided to do it.
	 *
	 * @param reference  The bound reference.
	 * @param allocation How many arguments each parameter took, or null if each parameter took one
	 *                   and there is therefore no division worth remembering.
	 */
	public record Division(@NotNull FunctionReference<?> reference, int @Nullable [] allocation) {

	}

	/**
	 * Matches a function call against the overloads {@code only} accepts, reporting any failure
	 * through {@link Skript#error(String)}.
	 *
	 * @param namespace The namespace to resolve local functions in, or null for global functions
	 *                  only.
	 * @param name      The function name.
	 * @param arguments The passed arguments.
	 * @param only      Which overloads may be used, or null for any of them.
	 * @param <R>       The return type of the function.
	 * @return The matched reference, or null if none could be matched.
	 */
	private <R> @Nullable FunctionReference<R> match(
		@Nullable String namespace, @NotNull String name, @NotNull Argument<T>[] arguments,
		@Nullable Predicate<Signature<?>> only
	) {
		// avoid assigning values to a parameter multiple times
		Set<String> named = new HashSet<>();
		for (Argument<T> argument : arguments) {
			if (argument.type() != ArgumentType.NAMED) {
				continue;
			}

			boolean added = named.add(argument.name());
			if (added) {
				continue;
			}

			Skript.error(Language.get("functions.already assigned value to parameter"), argument.name());
			return null;
		}

		// try to find a matching signature to get which types to parse args with
		Set<Signature<?>> options = FunctionRegistry.getRegistry().getSignatures(namespace, name);

		if (only != null) {
			// a plain loop rather than a stream: this runs on every resolution, and the dynamic
			// path always passes a predicate
			Set<Signature<?>> accepted = new HashSet<>(options.size());
			for (Signature<?> option : options) {
				if (only.test(option)) {
					accepted.add(option);
				}
			}
			options = accepted;
		}

		if (options.isEmpty()) {
			doesNotExist(name, arguments, options);
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
		Set<FunctionReference<R>> exactReferences = getExactReferences(namespace, name, exacts, arguments);
		if (exactReferences == null) { // a list error, so quit parsing
			return null;
		}

		// if we found an exact one, return first to avoid conflict with list references
		if (exactReferences.size() == 1) {
			return exactReferences.stream().findAny().orElse(null);
		}

		// last, find single list functions
		Set<FunctionReference<R>> listReferences = getListReferences(namespace, name, lists, arguments);
		if (listReferences == null) { // a list error, so quit parsing
			return null;
		}

		exactReferences.addAll(listReferences);

		// only when nothing could be bound one argument per parameter may a list parameter
		// take several of the passed arguments
		if (exactReferences.isEmpty() && mode == Mode.GREEDY) {
			Set<FunctionReference<R>> greedyReferences = getGreedyReferences(namespace, name, exacts, arguments);
			if (greedyReferences == null) { // a list error, so quit parsing
				return null;
			}

			exactReferences.addAll(greedyReferences);
		}

		if (exactReferences.isEmpty()) {
			doesNotExist(name, arguments, Sets.union(exacts, lists));
			return null;
		} else if (exactReferences.size() == 1) {
			return exactReferences.stream().findAny().orElse(null);
		} else {
			ambiguousError(name, exactReferences);
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
	 * @param <R>        The return type of the references.
	 * @return All possible exact {@link FunctionReference FunctionReferences}.
	 */
	private <R> @Nullable Set<FunctionReference<R>> getExactReferences(
		String namespace, String name,
		Set<Signature<?>> signatures, Argument<T>[] arguments
	) {
		Set<FunctionReference<R>> exactReferences = new HashSet<>();

		// whether the arguments array contains any NAMED type parameters
		// if there are no named parameters, then we consider the user arguments to already be in order
		boolean hasNames = Arrays.stream(arguments).anyMatch(argument -> argument.type() == ArgumentType.NAMED);

		// TODO! cache results
		// 'arguments' may be reassigned when processing a signature
		// retain a reference to the original array for resetting each time
		Argument<T>[] originalArguments = arguments;
		for (Signature<?> signature : signatures) {
			// if arguments arent possible, skip
			if (arguments.length > signature.getMaxParameters() || arguments.length < signature.getMinParameters()) {
				continue;
			}
			arguments = originalArguments;

			// all remaining arguments to parse
			// if a passed argument is named it bypasses the regular argument order of unnamed arguments
			SequencedMap<String, Parameter<?>> parameters = signature.parameters().sequencedMap();

			// if a parameter is not passed, we need to create a dummy argument to allow binding in bindArguments
			//noinspection unchecked
			Argument<T>[] parseArguments = (Argument<T>[]) new Argument[parameters.size()];
			ArgumentParseTarget[] parseTargets = new ArgumentParseTarget[parameters.size()];

			// fill in named arguments, placeholders for unnamed arguments
			// essentially, we reorder the user provided arguments into the order defined by the parameters
			int index = 0;
			// whether, after this first pass completes, there are any placeholders to fill in
			boolean hasPlaceholders = false;
			for (var entry : parameters.entrySet()) {
				Parameter<?> parameter = entry.getValue();

				// attempt to match an argument to this parameter
				Argument<T> argument = null;
				if (hasNames) {
					for (var candidate : arguments) {
						if (entry.getKey().equals(candidate.name())) { // exact match
							argument = candidate;
							break;
						}
					}
				} else if (index < arguments.length) { // if there are no named arguments, simply take the next provided one
					argument = new Argument<>(ArgumentType.UNNAMED, entry.getKey(), arguments[index].value());
				}
				if (argument == null) { // could not resolve an argument, use a placeholder
					argument = new Argument<>(ArgumentType.UNNAMED, entry.getKey(), null);
					hasPlaceholders = true;
				}
				parseArguments[index] = argument;

				// prepare type information for binding
				parseTargets[index] = targetFor(parameter, Utils.getComponentType(parameter.type()));

				index++;
			}
			if (hasPlaceholders) { // fill in placeholder arguments as necessary
				// some arguments may be erroneously interpreted as name, but their value just contains a colon
				// for example, minecraft:stone
				// convert these unexpected named arguments to unnamed arguments
				boolean copied = false;
				for (int i = 0; i < arguments.length; i++) {
					Argument<T> argument = arguments[i];
					if (argument.type() == ArgumentType.NAMED && !parameters.containsKey(argument.name())) {
						if (!copied) {
							arguments = Arrays.copyOf(arguments, arguments.length);
							copied = true;
						}
						// we retain the name for later purposes, such as logging
						arguments[i] = new Argument<>(ArgumentType.UNNAMED, argument.name(), binder.rawValue(argument));
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
				fill:
				for (int i = 0; i < arguments.length; i++) {
					Argument<T> argument = arguments[i];
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
						Argument<T> nextArgument = arguments[j];
						if (nextArgument.type() == ArgumentType.NAMED) {
							nextName = nextArgument.name();
							break;
						}
					}
					// fill in using this unnamed argument
					for (int j = lastCheck + 1; j < parseArguments.length; j++) {
						Argument<T> parseArgument = parseArguments[j];
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

			ArgumentParseResult result = bindArguments(parseArguments, parseTargets);
			switch (result.type()) {
				case LIST_ERROR -> {
					return null;
				}
				case OK -> {
					//noinspection unchecked
					FunctionReference<R> reference =
						new FunctionReference<>(namespace, name, (Signature<R>) signature, result.parsed());
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
	 * @param <R>        The return type of the references.
	 * @return All possible {@link FunctionReference FunctionReferences} which contain a single list parameter.
	 */
	private <R> @Nullable Set<FunctionReference<R>> getListReferences(
		String namespace, String name,
		Set<Signature<?>> signatures, Argument<T>[] arguments
	) {
		// disallow naming any arguments other than the first
		if (arguments.length > 1) {
			for (Argument<T> argument : arguments) {
				if (argument.type() != ArgumentType.NAMED) {
					continue;
				}

				doesNotExist(name, arguments, signatures);
				return null;
			}
		}

		Set<FunctionReference<R>> references = new HashSet<>();

		signatures:
		for (Signature<?> signature : signatures) {
			Parameter<?> parameter = signature.parameters().getFirst();

			ArgumentParseTarget[] parseTargets = new ArgumentParseTarget[]{
				targetFor(parameter, parameter.type().componentType())
			};

			if (arguments.length == 1 && arguments[0].type() == ArgumentType.NAMED) {
				if (!arguments[0].name().equals(parameter.name())) {
					doesNotExist(name, arguments, signatures);
					continue;
				}
			}

			// join all args to a single arg
			Argument<T> argument = binder.joinForList(arguments, parameter.name());
			Argument<T>[] array = CollectionUtils.array(argument);

			ArgumentParseResult result = bindArguments(array, parseTargets);

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
				FunctionReference<R> reference =
					new FunctionReference<>(namespace, name, (Signature<R>) signature, result.parsed());

				if (!reference.validate()) {
					continue;
				}

				references.add(reference);
			}
		}

		return references;
	}

	/**
	 * The outcome of binding the arguments to one signature under one division of them.
	 *
	 * @param reference The reference, or null if the arguments do not fit that division.
	 * @param listError Whether a list was passed where a single value is required, which is a
	 *                  mistake in the call rather than the wrong division, so no other division
	 *                  of the arguments is worth trying.
	 * @param <R>       The return type of the function.
	 */
	private record Attempt<R>(@Nullable FunctionReference<R> reference, boolean listError) {

	}

	/**
	 * Binds the arguments to {@code signature} with {@code allocation} deciding how many of them
	 * each parameter takes.
	 *
	 * @param namespace  The current namespace.
	 * @param name       The name of the function.
	 * @param signature  The signature to bind to.
	 * @param parameters The parameters of {@code signature}.
	 * @param allocation How many arguments each parameter takes.
	 * @param arguments  The passed arguments.
	 * @param <R>        The return type of the reference.
	 * @return The outcome of the attempt.
	 */
	private <R> @NotNull Attempt<R> bindDivision(
		String namespace, String name, Signature<?> signature, Parameter<?>[] parameters,
		int[] allocation, Argument<T>[] arguments
	) {
		//noinspection unchecked
		Argument<T>[] parseArguments = (Argument<T>[]) new Argument[parameters.length];
		ArgumentParseTarget[] parseTargets = new ArgumentParseTarget[parameters.length];

		int next = 0;
		for (int i = 0; i < parameters.length; i++) {
			Parameter<?> parameter = parameters[i];
			int take = allocation[i];

			if (take == 0) { // nothing was allocated, so fall back to the default value
				parseArguments[i] = new Argument<>(ArgumentType.UNNAMED, parameter.name(), null);
			} else if (take == 1) {
				// a single argument is passed through as it is, so that an argument which is
				// itself a list stays one argument rather than being spread
				parseArguments[i] = new Argument<>(ArgumentType.UNNAMED, parameter.name(), arguments[next].value());
			} else {
				Argument<T>[] allocated = Arrays.copyOfRange(arguments, next, next + take);
				parseArguments[i] = binder.joinForList(allocated, parameter.name());
			}
			next += take;

			parseTargets[i] = targetFor(parameter, Utils.getComponentType(parameter.type()));
		}

		ArgumentParseResult result = bindArguments(parseArguments, parseTargets);
		if (result.type() == ArgumentParseResultType.LIST_ERROR) {
			return new Attempt<>(null, true);
		}
		if (result.type() != ArgumentParseResultType.OK) {
			return new Attempt<>(null, false);
		}

		//noinspection unchecked
		FunctionReference<R> reference =
			new FunctionReference<>(namespace, name, (Signature<R>) signature, result.parsed());
		if (!reference.validate()) {
			return new Attempt<>(null, false);
		}

		return new Attempt<>(reference, false);
	}

	/**
	 * Returns all possible {@link FunctionReference FunctionReferences} which can be bound by
	 * letting a list parameter take several of the passed arguments.
	 * <p>
	 * Only signatures containing a list parameter are considered, as a signature of only single
	 * parameters can never bind anything here that {@link #getExactReferences} did not already
	 * bind. Named arguments are not supported.
	 * </p>
	 *
	 * @param namespace  The current namespace.
	 * @param name       The name of the function.
	 * @param signatures The possible signatures.
	 * @param arguments  The passed arguments.
	 * @param <R>        The return type of the references.
	 * @return All possible greedily bound {@link FunctionReference FunctionReferences}.
	 */
	private <R> @Nullable Set<FunctionReference<R>> getGreedyReferences(
		String namespace, String name,
		Set<Signature<?>> signatures, Argument<T>[] arguments
	) {
		for (Argument<T> argument : arguments) {
			if (argument.type() == ArgumentType.NAMED) {
				return Set.of();
			}
		}

		Set<FunctionReference<R>> references = new HashSet<>();

		for (Signature<?> signature : signatures) {
			Parameter<?>[] parameters = signature.parameters().all();

			if (Arrays.stream(parameters).allMatch(Parameter::isSingle)) {
				continue;
			}

			// a division is by count alone, so the first one may put an argument in a parameter
			// whose type it does not fit. The rest are tried in turn rather than giving up, which
			// is what lets f(a: texts, b: numbers, c: texts) given "1", "2", -1, "3", "4" divide
			// them 2/1/2 instead of failing on 3/1/1
			for (int[] allocation : allocations(parameters, arguments.length)) {
				Attempt<R> attempt =
					this.bindDivision(namespace, name, signature, parameters, allocation, arguments);

				if (attempt.listError()) {
					return null;
				}
				if (attempt.reference() != null) {
					// the arguments fit this signature, so no further division is considered
					references.add(attempt.reference());
					break;
				}
			}
		}

		return references;
	}

	/**
	 * @param signature The signature.
	 * @return The declared types of the parameters of {@code signature}, in order.
	 */
	public static Class<?>[] declaredTypes(@NotNull Signature<?> signature) {
		return Arrays.stream(signature.parameters().all())
			.map(Parameter::type)
			.toArray(Class<?>[]::new);
	}

	/**
	 * How many allocations {@link #allocations(Parameter[], int)} will consider for one signature
	 * before giving up. A signature with several list parameters and many arguments has a great
	 * many ways to divide them, and the useful ones come first.
	 */
	private static final int MAX_ALLOCATIONS = 64;

	/**
	 * Every way {@code slots} passed arguments can be divided across {@code parameters}, greediest
	 * on the left first: the leftmost list parameter takes everything the parameters after it do
	 * not require, so the division a call bound to before is the first one tried again.
	 * <p>
	 * A division is by count alone, so the first one may put an argument in a parameter whose type
	 * it does not fit. The caller tries them in turn and keeps the first which binds, which is what
	 * lets {@code f(a: texts, b: numbers, c: texts)} given {@code "1", "2", -1, "3", "4"} divide
	 * them 2/1/2 rather than failing on 3/1/1.
	 * </p>
	 *
	 * @param parameters The parameters to allocate to.
	 * @param slots      The number of passed arguments.
	 * @return The divisions, in the order they should be tried. Empty if there are none.
	 */
	static @NotNull List<int[]> allocations(Parameter<?>[] parameters, int slots) {
		List<int[]> divisions = new ArrayList<>();
		divide(parameters, slots, required(parameters), 0, 0, new int[parameters.length], divisions);
		return divisions;
	}

	private static void divide(
		Parameter<?>[] parameters, int slots, int required, int index, int used,
		int[] current, List<int[]> divisions
	) {
		if (divisions.size() >= MAX_ALLOCATIONS) {
			return;
		}

		if (index == parameters.length) {
			if (used == slots) {
				divisions.add(current.clone());
			}
			return;
		}

		// arguments which must be left over for the parameters after this one
		int reserved = Math.max(0, required - (index + 1));
		int available = slots - used - reserved;
		int minimum = index < required ? 1 : 0;

		if (available < minimum) {
			return;
		}

		int most = parameters[index].isSingle() ? Math.min(1, available) : available;
		for (int take = most; take >= minimum; take--) {
			current[index] = take;
			divide(parameters, slots, required, index + 1, used + take, current, divisions);

			if (divisions.size() >= MAX_ALLOCATIONS) {
				return;
			}
		}
		current[index] = 0;
	}

	/**
	 * @param parameters The parameters.
	 * @return How many leading parameters must be given at least one argument. A parameter may only
	 * 	be omitted if it and every parameter after it is optional.
	 */
	private static int required(Parameter<?>[] parameters) {
		for (int i = parameters.length - 1; i >= 0; i--) {
			if (!parameters[i].hasModifier(Modifier.OPTIONAL)) {
				return i + 1;
			}
		}
		return 0;
	}

	/**
	 * Builds the target type and fallback expression used to bind an argument to {@code parameter}.
	 *
	 * @param parameter  The parameter being bound to.
	 * @param targetType The type an argument for this parameter must be bound as. Never an array.
	 * @return The target data.
	 */
	private static ArgumentParseTarget targetFor(Parameter<?> parameter, Class<?> targetType) {
		Expression<?> fallback;
		if (parameter instanceof ScriptParameter<?> sp) {
			fallback = sp.defaultValue();
		} else if (parameter.hasModifier(Modifier.OPTIONAL)) {
			fallback = new EmptyExpression();
		} else {
			fallback = null;
		}
		return new ArgumentParseTarget(targetType, fallback);
	}

	/**
	 * Prints the error for when multiple function references have been matched.
	 *
	 * @param name       The function name.
	 * @param references The possible references.
	 * @param <R>        The return types of the references.
	 */
	private <R> void ambiguousError(String name, Set<FunctionReference<R>> references) {
		List<String> parts = new ArrayList<>();

		for (FunctionReference<R> reference : references) {
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
	private void doesNotExist(String name, Argument<T>[] arguments, Set<Signature<?>> possibleSignatures) {
		StringJoiner joiner = new StringJoiner(", ");

		List<Class<?>> argumentTypes = new ArrayList<>();
		for (Argument<T> argument : arguments) {
			Expression<?> expression = binder.describe(argument);

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
	 * Attempts to bind every argument in {@code arguments} to the matching target in {@code targets}.
	 *
	 * @param arguments The arguments to bind.
	 * @param targets   The target data to bind each argument with.
	 * @return An {@link ArgumentParseResult} with the results.
	 */
	private ArgumentParseResult bindArguments(Argument<T>[] arguments, ArgumentParseTarget[] targets) {
		assert arguments.length == targets.length;
		assert Arrays.stream(targets).map(ArgumentParseTarget::type).noneMatch(Class::isArray);

		//noinspection unchecked
		Argument<Expression<?>>[] parsed = (Argument<Expression<?>>[]) new Argument[arguments.length];

		for (int i = 0; i < arguments.length; i++) {
			Argument<T> argument = arguments[i];
			ArgumentParseTarget targetData = targets[i];

			Expression<?> expression = binder.bind(argument, targetData);
			if (expression == null) {
				return new ArgumentParseResult(ArgumentParseResultType.PARSE_FAIL, null);
			}

			if (expression instanceof ExpressionList<?> list && !list.getAnd()) {
				Skript.error(Language.get("functions.or in arguments"));
				return new ArgumentParseResult(ArgumentParseResultType.LIST_ERROR, null);
			}

			parsed[i] = new Argument<>(argument.type(), argument.name(), expression);
		}

		return new ArgumentParseResult(ArgumentParseResultType.OK, parsed);
	}

	/**
	 * Supplies the steps of {@link FunctionBinder} which depend on how an argument's value is
	 * represented.
	 *
	 * @param <T> The type an argument's value is represented by.
	 */
	interface ArgumentBinder<T> {

		/**
		 * Binds a single argument to an expression of the target type.
		 *
		 * @param argument The argument.
		 * @param target   The target type to bind to, and the fallback value.
		 * @return {@code target.fallback()} if the argument does not have a value.
		 * 	Otherwise, the bound expression, or null if it could not be bound.
		 */
		@Nullable Expression<?> bind(Argument<T> argument, ArgumentParseTarget target);

		/**
		 * Joins every passed argument into a single argument, for binding against a signature
		 * whose only parameter accepts a list.
		 *
		 * @param arguments     The passed arguments. May be empty.
		 * @param parameterName The name of the parameter the result will be bound to.
		 * @return The joined argument.
		 */
		Argument<T> joinForList(Argument<T>[] arguments, String parameterName);

		/**
		 * Returns the value of {@code argument} as it was originally written, used when an argument
		 * turns out not to have been named after all.
		 *
		 * @param argument The argument.
		 * @return The raw value.
		 */
		@Nullable T rawValue(Argument<T> argument);

		/**
		 * Describes an argument as an expression, so that its type can be named in an error
		 * message. The result is only used for logging and need not be initialisable.
		 *
		 * @param argument The argument.
		 * @return An expression describing the argument, or null if it cannot be described.
		 */
		@Nullable Expression<?> describe(Argument<T> argument);

	}

	/**
	 * Binds arguments which have already been parsed into expressions, as is the case when a
	 * function is called through a reference obtained at runtime rather than through a call
	 * written in a script.
	 * <p>
	 * Each argument is passed through as it is; converting it to the parameter's type and
	 * checking that it is acceptable is left to {@link FunctionReference#validate()}, exactly as
	 * it is for a call written in a script.
	 * </p>
	 */
	private static final class ExpressionArgumentBinder implements ArgumentBinder<Expression<?>> {

		@Override
		public @Nullable Expression<?> bind(Argument<Expression<?>> argument, ArgumentParseTarget target) {
			if (argument.value() == null) {
				return target.fallback();
			}
			return argument.value();
		}

		@Override
		public Argument<Expression<?>> joinForList(Argument<Expression<?>>[] arguments, String parameterName) {
			if (arguments.length == 0) {
				return new Argument<>(ArgumentType.NAMED, parameterName, null);
			}

			// a plain loop rather than a stream: this runs on every execution of a call site
			// which spreads values across a list parameter, where building the pipeline costs
			// more than the array it produces
			Expression<?>[] values = new Expression[arguments.length];
			int size = 0;
			for (Argument<Expression<?>> argument : arguments) {
				// an argument with no value is one the caller omitted, as the positional form of
				// DynamicFunctionReference#execute produces for a null. It contributes nothing to
				// the list; copying it in would put a null in the ExpressionList, which throws as
				// soon as anything reads it
				if (argument.value() != null) {
					values[size++] = argument.value();
				}
			}

			if (size == 0) { // every argument was omitted, so the parameter takes its default
				return new Argument<>(ArgumentType.NAMED, parameterName, null);
			}

			if (size != values.length) {
				values = Arrays.copyOf(values, size);
			}

			return new Argument<>(ArgumentType.NAMED, parameterName,
				new ExpressionList<>(values, Object.class, true));
		}

		@Override
		public @Nullable Expression<?> rawValue(Argument<Expression<?>> argument) {
			return argument.value();
		}

		@Override
		public @Nullable Expression<?> describe(Argument<Expression<?>> argument) {
			return argument.value();
		}

	}

	/**
	 * The type of result from attempting to bind function arguments.
	 */
	enum ArgumentParseResultType {

		/**
		 * All arguments were successfully bound to the specified type.
		 */
		OK,

		/**
		 * An argument failed to bind to the specified target type.
		 */
		PARSE_FAIL,

		/**
		 * An expression list contained "or", thus parsing should be stopped.
		 */
		LIST_ERROR

	}

	/**
	 * The results of attempting to bind function arguments.
	 *
	 * @param type   The type of result.
	 * @param parsed The resulting bound arguments, or null if binding was not successful.
	 */
	record ArgumentParseResult(
		@NotNull ArgumentParseResultType type,
		Argument<Expression<?>>[] parsed) {

	}

	/**
	 * Represents the data related to binding an argument to a target type.
	 *
	 * @param type     The target type.
	 * @param fallback If the argument cannot be bound as the target type, the expression to use as fallback.
	 */
	record ArgumentParseTarget(@NotNull Class<?> type, @Nullable Expression<?> fallback) {

	}

}
