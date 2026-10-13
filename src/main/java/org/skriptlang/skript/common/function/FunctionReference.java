package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.FunctionRegistry.Retrieval;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionReferenceParser.EmptyExpression;
import org.skriptlang.skript.common.function.Parameter.Modifier;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.*;

/**
 * A reference to a {@link Function<T>} found in a script.
 *
 * @param <T> The return type of this reference.
 */
public final class FunctionReference<T> implements Debuggable {

	private final String namespace;
	private final String name;
	private final @Nullable Expression<String> nameExpression;
	private final Argument<Expression<?>>[] arguments;

	private Signature<T> cachedSignature;
	private boolean validSignature = true;
	private boolean printedInvalidSignatureWarning;
	private Function<T> cachedFunction;
	private @Nullable Function<T> lastResolvedFunction;
	private final Set<String> warnedCalls = new HashSet<>();
	private LinkedHashMap<String, ArgInfo> cachedArguments;

	private record ArgInfo(Expression<?> expression, Class<?> type, Set<Modifier> modifiers) {

	}

	public FunctionReference(@Nullable String namespace,
	                         @NotNull String name,
	                         @NotNull Signature<T> signature,
	                         @NotNull Argument<Expression<?>>[] arguments) {
		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(signature, "signature cannot be null");
		Preconditions.checkNotNull(arguments, "arguments cannot be null");

		this.namespace = namespace;
		this.name = name;
		this.nameExpression = null;
		this.cachedSignature = signature;
		this.arguments = arguments;
	}

	/**
	 * Creates a function reference whose name is resolved at runtime,
	 * e.g. {@code myFunc%{_name}%(...)}.
	 *
	 * <p>Since the function is not known during parsing, arguments are parsed
	 * generically and validated against the resolved function at runtime.</p>
	 *
	 * @param namespace The name of the script this call originates from (for local functions).
	 * @param name The function name, containing at least one interpolated expression.
	 * @param arguments The passed arguments.
	 */
	public FunctionReference(@Nullable String namespace,
							 @NotNull Expression<String> name,
							 @NotNull Argument<Expression<?>>[] arguments) {
		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(arguments, "arguments cannot be null");

		this.namespace = namespace;
		this.name = name.toString(null, false);
		this.nameExpression = name;
		this.arguments = arguments;
		this.cachedSignature = wildcardSignature(namespace, this.name);
	}

	/**
	 * Whether the function name of this reference is resolved at runtime.
	 */
	private boolean isDynamic() {
		return nameExpression != null;
	}

	@SuppressWarnings("unchecked")
	private static <T> Signature<T> wildcardSignature(@Nullable String namespace, String name) {
		return (Signature<T>) new ch.njol.skript.lang.function.Signature<>(
				namespace, name, new Parameters(new LinkedHashMap<>()), (Class<T>) Object.class, false);
	}

	/**
	 * Validates this function reference.
	 *
	 * @return True if this is a valid function reference, false if not.
	 */
	public boolean validate() {
		if (isDynamic())
			return true;

		if (!validSignature) {
			Class<?>[] parameters = Arrays.stream(cachedSignature.parameters().all())
				.map(Parameter::type)
				.toArray(Class[]::new);
			var result = FunctionRegistry.getRegistry().getSignature(namespace, name, parameters);
			if (result.result() == RetrievalResult.EXACT) {
				//noinspection unchecked
				cachedSignature = (Signature<T>) result.retrieved();
				validSignature = true;
				printedInvalidSignatureWarning = false;
				cachedFunction = null; // need to re-obtain function now since signature changed
			} else {
				if (!printedInvalidSignatureWarning) {
					printedInvalidSignatureWarning = true;
					Skript.warning(String.format("The function '%s' from the script '%s' no longer exists."
						+ " Skript will continue to use the old function until this function is registered again."
						+ " Function call: %s",
						name, namespace, toString(null, false)));
				}
			}
		}

		if (cachedArguments == null) {
			cachedArguments = new LinkedHashMap<>();

			Parameter<?>[] targets = cachedSignature.parameters().all();
			for (int i = 0; i < arguments.length; i++) {
				Argument<Expression<?>> argument = arguments[i];
				Parameter<?> target = targets[i];

				if (argument.value instanceof EmptyExpression) {
					continue;
				}

				// try to parse value in the argument
				//noinspection unchecked
				Expression<?> converted = argument.value.getConvertedExpression(Utils.getComponentType(target.type()));

				// failed to parse value
				if (!validateArgument(target, argument.value, converted)) {
					return false;
				}

				// allows "keyed x" to pass all recursive values of x
				if (converted != null && KeyProviderExpression.areKeysRecommended(converted)) {
					converted.returnNestedStructures(true);
				}

				// all good
				cachedArguments.put(target.name(), new ArgInfo(converted, target.type(), target.modifiers()));
			}
		}

		cachedSignature.addCall(this);

		return true;
	}

	private boolean validateArgument(Parameter<?> target, Expression<?> original, Expression<?> converted) {
		if (converted == null) {
			if (LiteralUtils.hasUnparsedLiteral(original)) {
				Skript.error("Can't understand this expression: %s", original);
			} else {
				Skript.error("Expected type %s for argument '%s', but %s is of type %s.",
						getName(target.type(), target.isSingle()), target.name(), original,
						getName(original.getReturnType(), original.isSingle()));
			}
			return false;
		}

		if (target.isSingle() && !converted.isSingle()) {
			Skript.error("Expected type %s for argument '%s', but %s is of type %s.",
					getName(target.type(), target.isSingle()), target.name(), converted, getName(converted.getReturnType(), converted.isSingle()));
			return false;
		}

		return true;
	}

	@ApiStatus.Internal
	public void invalidate() {
		validSignature = false;
	}

	private String getName(Class<?> clazz, boolean single) {
		if (single) {
			return Classes.getSuperClassInfo(clazz).getName().getSingular();
		} else {
			return Classes.getSuperClassInfo(Utils.getComponentType(clazz)).getName().getPlural();
		}
	}

	/**
	 * Executes the function referred to by this reference.
	 *
	 * @param event The event to use for execution.
	 * @return The return value of the function.
	 */
	public T execute(Event event) {
		if (isDynamic())
			return executeDynamic(event);

		if (!validate()) {
			Skript.error("Failed to verify function %s before execution.", name);
			return null;
		}

		SequencedMap<String, Object> args = new LinkedHashMap<>();
		cachedArguments.forEach((k, v) -> {
			if (v.modifiers().contains(Modifier.KEYED)) {
				args.put(k, Classes.clone(evaluateKeyed(v.expression(), event)));
				return;
			}

			if (!v.type().isArray()) {
				args.put(k, Classes.clone(v.expression().getSingle(event)));
			} else {
				args.put(k, Classes.clone(v.expression().getArray(event)));
			}
		});

		Function<T> function = function();
		if (function == null) { // probably shouldn't be possible?
			Skript.error("Failed to obtain function for call: %s", toString(null, false));
			return null;
		}
		FunctionEvent<?> fnEvent = new FunctionEvent<>(function);

		if (Functions.callFunctionEvents)
			Bukkit.getPluginManager().callEvent(fnEvent);

		return function.execute(fnEvent, new FunctionArgumentsImpl(args));
	}

	/**
	 * Executes a reference whose name is resolved at runtime.
	 * The resolved function's signature is used to map and convert the
	 * generically-parsed arguments on each call.
	 *
	 * @param event The event to use for execution.
	 * @return The return value of the function.
	 */
	@SuppressWarnings("unchecked")
	private @Nullable T executeDynamic(Event event) {
		assert nameExpression != null;
		String resolvedName = nameExpression.getSingle(event);
		if (resolvedName == null)
			return null;

		ch.njol.skript.lang.function.Function<?> resolved = Functions.getFunction(resolvedName, namespace);
		if (resolved == null) {
			if (warnedCalls.add(resolvedName))
				Skript.warning("The function '%s' does not exist; this call will do nothing. Call: %s"
						.formatted(resolvedName, toString(null, false)));
			return null;
		}
		ch.njol.skript.lang.function.Signature<?> signature = resolved.getSignature();
		Parameter<?>[] parameters = signature.parameters().all();

		// a single non-single parameter takes all arguments, e.g. sum(ns: numbers)
		boolean varArgs = parameters.length == 1 && !parameters[0].isSingle();

		if ((!varArgs && arguments.length > parameters.length)
				|| arguments.length < signature.getMinParameters()) {
			if (warnedCalls.add(resolvedName + ' '))
				Skript.warning("Wrong number of arguments for the function '%s'. Call: %s"
						.formatted(resolvedName, toString(null, false)));
			return null;
		}

		SequencedMap<String, Object> args = new LinkedHashMap<>();
		for (int i = 0; i < arguments.length; i++) {
			Argument<Expression<?>> argument = arguments[i];

			Parameter<?> parameter;
			if (varArgs) {
				parameter = parameters[0];
			} else if (argument.type() == ArgumentType.NAMED) {
				parameter = signature.parameters().get(argument.name());
				if (parameter == null) {
					if (warnedCalls.add(resolvedName + argument.name()))
						Skript.warning("'%s' is not a parameter of the function '%s'. Call: %s"
								.formatted(argument.name(), resolvedName, toString(null, false)));
					return null;
				}
			} else {
				parameter = parameters[i];
			}

			Object[] values = argument.value().getArray(event);
			int expected = values.length;
			values = Converters.convert(values, (Class<Object>) Utils.getComponentType(parameter.type()));
			if (values.length < expected && warnedCalls.add(resolvedName + i))
				Skript.warning("Could not convert all values for parameter '%s' of the function '%s'. Call: %s"
						.formatted(parameter.name(), resolvedName, toString(null, false)));

			if (parameter.isSingle() && values.length > 1) {
				if (warnedCalls.add(resolvedName + ' ' + i))
					Skript.warning("Expected a single value for parameter '%s' of the function '%s', but got %d. Call: %s"
							.formatted(parameter.name(), resolvedName, values.length, toString(null, false)));
				return null;
			}

			// Don't allow mutating across function boundary; same hack is applied to variables
			for (int j = 0; j < values.length; j++)
				values[j] = Classes.clone(values[j]);

			if (varArgs) {
				Object existing = args.get(parameter.name());
				if (existing instanceof Object[] existingArray) {
					Object[] merged = Arrays.copyOf(existingArray, existingArray.length + values.length);
					System.arraycopy(values, 0, merged, existingArray.length, values.length);
					values = merged;
				}
				args.put(parameter.name(), values);
			} else {
				args.put(parameter.name(), parameter.isSingle()
						? (values.length == 0 ? null : values[0])
						: values);
			}
		}

		Function<T> function = (Function<T>) resolved;
		lastResolvedFunction = function;
		FunctionEvent<?> fnEvent = new FunctionEvent<>(function);

		if (Functions.callFunctionEvents)
			Bukkit.getPluginManager().callEvent(fnEvent);

		return function.execute(fnEvent, new FunctionArgumentsImpl(args));
	}

	private KeyedValue<?>[] evaluateKeyed(Expression<?> expression, Event event) {
		if (expression instanceof ExpressionList<?> list) {
			return evaluateSingleListParameter(list.getExpressions(), event);
		}
		return evaluateParameter(expression, event);
	}

	private KeyedValue<?>[] evaluateSingleListParameter(Expression<?>[] arguments, Event event) {
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

	private KeyedValue<?>[] evaluateParameter(Expression<?> argument, Event event) {
		if (argument == null)
			return null;

		Object[] values = argument.getArray(event);

		// Don't allow mutating across function boundary; same hack is applied to variables
		for (int i = 0; i < values.length; i++)
			values[i] = Classes.clone(values[i]);

		if (!(argument instanceof KeyProviderExpression<?> kpe))
			return KeyedValue.zip(values, null);

		String[] keys = KeyProviderExpression.areKeysRecommended(argument)
				? kpe.getArrayKeys(event)
				: null;
		return KeyedValue.zip(values, keys);
	}

	/**
	 * @return The function referred to by this reference.
	 */
	public @Nullable Function<T> function() {
		if (isDynamic())
			return lastResolvedFunction;

		if (cachedFunction == null) {
			Class<?>[] parameters = Arrays.stream(cachedSignature.parameters().all())
					.map(Parameter::type)
					.toArray(Class[]::new);

			Retrieval<ch.njol.skript.lang.function.Function<?>> retrieval = FunctionRegistry.getRegistry().getFunction(namespace, name, parameters);

			if (retrieval.result() == RetrievalResult.EXACT) {
				//noinspection unchecked
				cachedFunction = (Function<T>) retrieval.retrieved();
			}
		}

		return cachedFunction;
	}

	/**
	 * @return The signature belonging to this reference.
	 */
	public Signature<T> signature() {
		return cachedSignature;
	}

	/**
	 * @return The namespace that this reference is in.
	 */
	public String namespace() {
		return namespace;
	}

	/**
	 * @return The name of the function being referenced.
	 */
	public @NotNull String name() {
		return name;
	}

	/**
	 * @return The passed arguments.
	 */
	public @NotNull Argument<Expression<?>>[] arguments() {
		return arguments;
	}

	/**
	 * @return Whether this reference returns a single or multiple values.
	 */
	public boolean isSingle() {
		if (cachedSignature.contract() != null) {
			Expression<?>[] args = Arrays.stream(arguments)
					.map(it -> it.value)
					.toArray(Expression[]::new);

			return cachedSignature.contract().isSingle(args);
		} else {
			return cachedSignature.isSingle();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder builder = new StringBuilder();

		builder.append(name);
		builder.append("(");

		StringJoiner args = new StringJoiner(", ");
		for (Argument<Expression<?>> argument : arguments) {
			if (argument.name != null) {
				args.add("%s: %s".formatted(argument.name, argument.value.toString(event, debug)));
			} else {
				args.add(argument.value.toString(event, debug));
			}
		}

		builder.append(args);
		builder.append(")");
		return builder.toString();
	}

	/**
	 * An argument.
	 *
	 * @param type  The type of the argument.
	 * @param name  The name of the argument, possibly null.
	 * @param value The value of the argument.
	 * @param raw   The raw full string of this argument.
	 */
	public record Argument<T>(
			ArgumentType type,
			String name,
			T value,
			@Nullable String raw
	) {

		/**
		 * Secondary constructor where raw is null.
		 *
		 * @param type  The type of the argument.
		 * @param name  The name of the argument, possibly null.
		 * @param value The value of the argument.
		 */
		public Argument(ArgumentType type, String name, T value) {
			this(type, name, value, null);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Argument<?> argument)) {
				return false;
			}

			return Objects.equals(value, argument.value) && Objects.equals(name, argument.name) && type == argument.type;
		}

		@Override
		public int hashCode() {
			int result = Objects.hashCode(type);
			result = 31 * result + Objects.hashCode(name);
			result = 31 * result + Objects.hashCode(value);
			return result;
		}

	}

	/**
	 * The type of argument.
	 */
	public enum ArgumentType {
		/**
		 * Whether this argument has a name.
		 */
		NAMED,

		/**
		 * Whether this argument does not have a name.
		 */
		UNNAMED
	}

}
