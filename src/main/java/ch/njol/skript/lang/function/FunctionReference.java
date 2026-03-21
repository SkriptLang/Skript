package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.FunctionRegistry.Retrieval;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;
import org.skriptlang.skript.common.function.Parameter;
import org.skriptlang.skript.common.function.Parameter.Modifier;
import org.skriptlang.skript.common.function.Parameter.Modifier.RangedModifier;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.Executable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @deprecated Use {@link org.skriptlang.skript.common.function.FunctionReference} instead.
 */
@Deprecated(forRemoval = true, since = "2.14")
public class FunctionReference<T> implements Contract, Executable<Event, T[]> {

	private static final String AMBIGUOUS_ERROR =
		"Skript cannot determine which function named '%s' to call. " +
		"The following functions were matched: %s. " +
		"Try clarifying the type of the arguments using the 'value within' expression.";

	/**
	 * Name of function that is called, for logging purposes.
	 */
	final String functionName;

	/**
	 * Signature of referenced function. If {@link #validateFunction(boolean)}
	 * succeeds, this is not null.
	 */
	private @Nullable Signature<? extends T> signature;

	/**
	 * Actual function reference. Null before the function is called for first
	 * time.
	 */
	private @Nullable Function<? extends T> function;

	/**
	 * If all function parameters can be condensed to a single list.
	 */
	private boolean singleListParam;

	/**
	 * Definitions of function parameters.
	 */
	private final Expression<?>[] parameters;

	/**
	 * Indicates if the caller expects this function to return a single value.
	 * Used for verifying correctness of the function signature.
	 */
	private boolean single;

	/**
	 * Return types expected from this function. Used for verifying correctness
	 * of the function signature.
	 */
	@Nullable
	final Class<? extends T>[] returnTypes;

	/**
	 * Node for {@link #validateFunction(boolean)} to use for logging.
	 */
	private final @Nullable Node node;

	/**
	 * Script in which this reference is found. Used for function unload
	 * safety checks.
	 */
	public final @Nullable String script;

	/**
	 * The contract for this function (typically the function reference itself).
	 * Used to determine input-based return types and simple behaviour.
	 */
	private Contract contract;

	public FunctionReference(
		String functionName, @Nullable Node node, @Nullable String script,
		@Nullable Class<? extends T>[] returnTypes, Expression<?>[] params
	) {
		this.functionName = functionName;
		this.node = node;
		this.script = script;
		this.returnTypes = returnTypes;
		this.parameters = params;
		this.contract = this;
	}

	public boolean validateParameterArity(boolean first) {
		if (!first && script == null)
			return false;

		Signature<?> sign = getRegisteredSignature();

		if (sign == null)
			return false;

		// Not enough parameters
		return parameters.length >= sign.getMinParameters();
	}

	private Class<?>[] parameterTypes;

	/**
	 * Validates this function reference. Prints errors if needed.
	 *
	 * @param first True if this is called while loading a script. False when
	 *              this is called when the function signature changes.
	 * @return True if validation succeeded.
	 */
	public boolean validateFunction(boolean first) {
		if (!first && script == null)
			return false;
		Function<? extends T> previousFunction = function;
		function = null;
		SkriptLogger.setNode(node);
		Skript.debug("Validating function " + functionName);
		Signature<?> sign = getRegisteredSignature();

		StringJoiner args = new StringJoiner(", ");
		for (Class<?> parameterType : parameterTypes) {
			args.add(Classes.getSuperClassInfo(Utils.getComponentType(parameterType)).getCodeName());
		}
		String stringified = "%s(%s)".formatted(functionName, args);

		// Check if the requested function exists
		if (sign == null) {
			if (first) {
				Skript.error("The function '" + stringified + "' does not exist.");
			} else {
				Skript.error("The function '" + stringified + "' was deleted or renamed, but is still used in other script(s)."
					+ " These will continue to use the old version of the function until Skript restarts.");
				function = previousFunction;
			}
			return false;
		}

		// Validate that return types are what caller expects they are
		Class<? extends T>[] expectedReturnTypes = this.returnTypes;
		if (expectedReturnTypes != null) {
			Class<?> candidateReturnType = sign.returnType();
			if (candidateReturnType == null) {
				if (first) {
					Skript.error("The function '" + stringified + "' doesn't return any value.");
				} else {
					Skript.error("The function '" + stringified + "' was redefined with no return value, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
					function = previousFunction;
				}
				return false;
			}

			if (!Converters.converterExists(candidateReturnType, expectedReturnTypes)) {
				if (first) {
					Skript.error("The returned value of the function '" + stringified + "', " + candidateReturnType + ", is " + SkriptParser.notOfType(expectedReturnTypes) + ".");
				} else {
					Skript.error("The function '" + stringified + "' was redefined with a different, incompatible return type, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
					function = previousFunction;
				}
				return false;
			}
			if (first) {
				single = sign.isSingle();
			} else if (single && !sign.isSingle()) {
				Skript.error("The function '" + functionName + "' was redefined with a different, incompatible return type, but is still used in other script(s)."
					+ " These will continue to use the old version of the function until Skript restarts.");
				function = previousFunction;
				return false;
			}
		}

		// Validate parameter count
		singleListParam = sign.getMaxParameters() == 1 && !sign.parameters().getFirst().isSingle();
		if (!singleListParam) { // Check that parameter count is within allowed range
			// Too many parameters
			if (parameters.length > sign.getMaxParameters()) {
				if (first) {
					if (sign.getMaxParameters() == 0) {
						Skript.error("The function '" + stringified + "' has no arguments, but " + parameters.length + " are given."
							+ " To call a function without parameters, just write the function name followed by '()', e.g. 'func()'.");
					} else {
						Skript.error("The function '" + stringified + "' has only " + sign.getMaxParameters() + " argument" + (sign.getMaxParameters() == 1 ? "" : "s") + ","
							+ " but " + parameters.length + " are given."
							+ " If you want to use lists in function calls, you have to use additional parentheses, e.g. 'give(player, (iron ore and gold ore))'");
					}
				} else {
					Skript.error("The function '" + stringified + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
					function = previousFunction;
				}
				return false;
			}
		}

		// Not enough parameters
		if (parameters.length < sign.getMinParameters()) {
			if (first) {
				Skript.error("The function '" + stringified + "' requires at least " + sign.getMinParameters() + " argument" + (sign.getMinParameters() == 1 ? "" : "s") + ","
					+ " but only " + parameters.length + " " + (parameters.length == 1 ? "is" : "are") + " given.");
			} else {
				Skript.error("The function '" + stringified + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s)."
					+ " These will continue to use the old version of the function until Skript restarts.");
				function = previousFunction;
			}
			return false;
		}

		// Check parameter types
		for (int i = 0; i < parameters.length; i++) {
			Parameter<?> signatureParam = sign.parameters().get(singleListParam ? 0 : i);
			RetainingLogHandler log = SkriptLogger.startRetainingLog();
			try {
				Class<?> target = Utils.getComponentType(signatureParam.type());

				//noinspection unchecked
				Expression<?> exprParam = parameters[i].getConvertedExpression(target);
				if (exprParam == null) {
					if (first) {
						if (LiteralUtils.hasUnparsedLiteral(parameters[i])) {
							Skript.error("Can't understand this expression: " + parameters[i].toString());
						} else {
							String type = Classes.toString(Classes.getSuperClassInfo(target));

							Skript.error("The " + StringUtils.fancyOrderNumber(i + 1) + " argument given to the function '" + stringified + "' is not of the required type " + type + "."
								+ " Check the correct order of the arguments and put lists into parentheses if appropriate (e.g. 'give(player, (iron ore and gold ore))')."
								+ " Please note that storing the value in a variable and then using that variable as parameter may suppress this error, but it still won't work.");
						}
					} else {
						Skript.error("The function '" + stringified + "' was redefined with different, incompatible arguments, but is still used in other script(s)."
							+ " These will continue to use the old version of the function until Skript restarts.");
						function = previousFunction;
					}
					return false;
				} else if (signatureParam.isSingle() && !exprParam.isSingle()) {
					if (first) {
						Skript.error("The " + StringUtils.fancyOrderNumber(i + 1) + " argument given to the function '" + functionName + "' is plural, "
							+ "but a single argument was expected");
					} else {
						Skript.error("The function '" + stringified + "' was redefined with different, incompatible arguments, but is still used in other script(s)."
							+ " These will continue to use the old version of the function until Skript restarts.");
						function = previousFunction;
					}
					return false;
				}

				// check ranged parameters
				if (signatureParam.hasModifier(Modifier.RANGED) && exprParam instanceof Literal<?> literalParam) {
					RangedModifier<?> range = signatureParam.getModifier(RangedModifier.class);
					if (!range.inRange(literalParam.getArray())) {
						Skript.error("The argument '" + signatureParam.name() +"' only accepts values between "
							+ Classes.toString(range.getMin()) + " and " + Classes.toString(range.getMax()) + ". "
							+ "Provided: " + literalParam.toString(null, Skript.debug()));
						return false;
					}
				}

				parameters[i] = exprParam;
			} finally {
				log.printLog();
			}
		}

		//noinspection unchecked
		signature = (Signature<? extends T>) sign;

		//noinspection unchecked
		Argument<Expression<?>>[] arguments = (Argument<Expression<?>>[]) Arrays.stream(parameters)
			.map(it -> new Argument<>(ArgumentType.UNNAMED, null, it))
			.toArray(Argument[]::new);

		sign.calls().add(new org.skriptlang.skript.common.function.FunctionReference<>(script, functionName, signature, arguments));

		Contract contract = sign.getContract();
		if (contract != null)
			this.contract = contract;

		return true;
	}

	// attempt to get the types of the parameters for this function reference
	private void parseParameters() {
		if (parameterTypes != null) {
			return;
		}

		parameterTypes = new Class<?>[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Expression<?> parsed = LiteralUtils.defendExpression(parameters[i]);
			parameterTypes[i] = parsed.getReturnType();
		}
	}

	/**
	 * Attempts to get this function's signature.
	 */
	private Signature<?> getRegisteredSignature() {
		parseParameters();

		if (Skript.debug()) {
			Skript.debug("Getting signature for '%s' with types %s",
				functionName, Arrays.toString(Arrays.stream(parameterTypes).map(Class::getSimpleName).toArray()));
		}

		Retrieval<Signature<?>> attempt = FunctionRegistry.getRegistry().getSignature(script, functionName, parameterTypes);
		if (attempt.result() == RetrievalResult.EXACT) {
			return attempt.retrieved();
		}

		if (attempt.result() == RetrievalResult.AMBIGUOUS) {
			ambiguousError(attempt.conflictingArgs());
		}

		return null;
	}

	/**
	 * Attempts to get this function's registered implementation.
	 */
	private Function<?> getRegisteredFunction() {
		parseParameters();

		if (Skript.debug()) {
			Skript.debug("Getting function '%s' with types %s",
				functionName, Arrays.toString(Arrays.stream(parameterTypes).map(Class::getSimpleName).toArray()));
		}

		Retrieval<Function<?>> attempt = FunctionRegistry.getRegistry().getFunction(script, functionName, parameterTypes);

		if (attempt.result() == RetrievalResult.EXACT) {
			return attempt.retrieved();
		}

		if (attempt.result() == RetrievalResult.AMBIGUOUS) {
			ambiguousError(attempt.conflictingArgs());
		}

		return null;
	}

	public @Nullable Function<? extends T> getFunction() {
		return function;
	}

	public String @Nullable [] returnedKeys() {
		if (function != null)
			return function.returnedKeys();
		return null;
	}

	public boolean resetReturnValue() {
		if (function != null)
			return function.resetReturnValue();
		return false;
	}

	protected T @Nullable [] execute(Event event) {
		// If needed, acquire the function reference
		if (function == null)
			//noinspection unchecked
			function = (Function<? extends T>) getRegisteredFunction();

		if (function == null) { // It might be impossible to resolve functions in some cases!
			Skript.error("Couldn't resolve call for '" + functionName + "'.");
			return null; // Return nothing and hope it works
		}

		// Prepare parameter values for calling
		Object[][] params = new Object[singleListParam ? 1 : parameters.length][];
		if (singleListParam) { // All parameters to one list
			params[0] = evaluateSingleListParameter(function.signature().parameters().getFirst(), parameters, event);
		} else { // Use parameters in normal way
			for (int i = 0; i < parameters.length; i++)
				//noinspection rawtypes,unchecked
				params[i] = function.signature().parameters().get(i).evaluate((Expression) parameters[i], event);
		}

		// Execute the function
		return function.execute(params);
	}

	private Object[] evaluateSingleListParameter(Parameter<?> parameter, Expression<?>[] arguments, Event event) {
		if (arguments.length == 0)
			return null;

		if (arguments.length == 1)
			//noinspection unchecked,rawtypes
			return parameter.evaluate((Expression) arguments[0], event);

		if (!parameter.hasModifier(Modifier.KEYED)) {
			List<Object> list = new ArrayList<>();
			for (Expression<?> argument : arguments)
				//noinspection unchecked,rawtypes
				list.addAll(Arrays.asList(parameter.evaluate((Expression) argument, event)));
			return list.toArray();
		}

		List<Object> values = new ArrayList<>();
		Set<String> keys = new LinkedHashSet<>();
		int keyIndex = 1;
		for (Expression<?> argument : arguments) {
			Object[] valuesArray = argument.getArray(event);
			String[] keysArray = KeyProviderExpression.areKeysRecommended(argument)
				? ((KeyProviderExpression<?>) argument).getArrayKeys(event)
				: null;

			for (int i = 0; i < valuesArray.length; i++) {
				if (keysArray == null) {
					while (keys.contains(String.valueOf(keyIndex)))
						keyIndex++;
					keys.add(String.valueOf(keyIndex++));
				} else if (!keys.add(keysArray[i])) {
					continue;
				}
				// Don't allow mutating across function boundary; same hack is applied to variables
				values.add(Classes.clone(valuesArray[i]));
			}
		}
		return KeyedValue.zip(values.toArray(), keys.toArray(new String[0]));
	}

	public boolean isSingle() {
		return contract.isSingle(parameters);
	}

	@Override
	public boolean isSingle(Expression<?>... arguments) {
		return single;
	}

	public @Nullable Class<? extends T> getReturnType() {
		//noinspection unchecked
		return (Class<? extends T>) contract.getReturnType(parameters);
	}

	@Override
	public @Nullable Class<?> getReturnType(Expression<?>... arguments) {
		if (signature == null)
			throw new SkriptAPIException("Signature of function is null when return type is asked!");

		return signature.returnType();
	}

	/**
	 * The contract is used in preference to the function for determining return type, etc.
	 *
	 * @return The contract determining this function's parse-time hints, potentially this reference
	 */
	public Contract getContract() {
		return contract;
	}

	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder b = new StringBuilder(functionName + "(");
		for (int i = 0; i < parameters.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(parameters[i].toString(event, debug));
		}
		b.append(")");
		return b.toString();
	}

	@Override
	public T[] execute(Event event, Object... arguments) {
		// If needed, acquire the function reference
		if (function == null)
			//noinspection unchecked
			function = (Function<? extends T>) getRegisteredFunction();

		if (function == null) { // It might be impossible to resolve functions in some cases!
			Skript.error("Couldn't resolve call for '" + functionName + "'.");
			return null; // Return nothing and hope it works
		}
		// We shouldn't trust the caller provided an array of arrays
		Object[][] consigned = consign(arguments);
		try {
			return function.execute(consigned);
		} finally {
			this.resetReturnValue();
		}
	}

	static Object[][] consign(Object... arguments) {
		Object[][] consigned = new Object[arguments.length][];
		for (int i = 0; i < consigned.length; i++) {
			if (arguments[i] instanceof Object[] || arguments[i] == null) {
				consigned[i] = (Object[]) arguments[i];
			} else {
				consigned[i] = new Object[]{arguments[i]};
			}
		}
		return consigned;

	}

	private void ambiguousError(Class<?>[][] conflictingArgs) {
		List<String> parts = new ArrayList<>();
		for (Class<?>[] args : conflictingArgs) {
			String argNames = Arrays.stream(args).map(arg -> {
				String name = Classes.getExactClassName(arg);

				if (name == null) {
					return arg.getSimpleName();
				} else {
					return name.toLowerCase();
				}
			}).collect(Collectors.joining(", "));

			parts.add("%s(%s)".formatted(functionName, argNames));
		}

		Skript.error(AMBIGUOUS_ERROR, functionName, StringUtils.join(parts, ", ", " and "));
	}

}
