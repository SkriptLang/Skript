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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionReferenceParser.EmptyExpression;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.util.*;

/**
 * A reference to a {@link Function<T>} found in a script.
 *
 * @param <T> The return type of this reference.
 */
public final class FunctionReference<T> implements Debuggable {

	private final String namespace;
	private final String name;
	private final Signature<T> signature;
	private final Argument<Expression<?>>[] arguments;

	private Function<T> cachedFunction;
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
		this.signature = signature;
		this.arguments = arguments;
	}

	/**
	 * Validates this function reference.
	 *
	 * @return True if this is a valid function reference, false if not.
	 */
	public boolean validate() {
		if (signature == null) {
			return false;
		}

		if (cachedArguments == null) {
			cachedArguments = new LinkedHashMap<>();

			Parameter<?>[] targets = signature.parameters().all();
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

		signature.addCall(this);

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
		if (!validate()) {
			Skript.error("Failed to verify function %s before execution.", name);
			return null;
		}

		LinkedHashMap<String, Object> args = new LinkedHashMap<>();
		cachedArguments.forEach((k, v) -> {
			if (v.modifiers().contains(Modifier.KEYED)) {
				args.put(k, evaluateKeyed(v.expression(), event));
				return;
			}

			if (!v.type().isArray()) {
				args.put(k, v.expression().getSingle(event));
			} else {
				args.put(k, v.expression().getArray(event));
			}
		});

		Function<T> function = function();
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
	public Function<T> function() {
		if (cachedFunction == null) {
			Class<?>[] parameters = Arrays.stream(signature.parameters().all())
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
		return signature;
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
		if (signature.contract() != null) {
			Expression<?>[] args = Arrays.stream(arguments)
					.map(it -> it.value)
					.toArray(Expression[]::new);

			return signature.contract().isSingle(args);
		} else {
			return signature.isSingle();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder builder = new StringBuilder();

		builder.append(name);
		builder.append("(");

		StringJoiner args = new StringJoiner(", ");
		for (Argument<Expression<?>> argument : arguments) {
			args.add("%s: %s".formatted(argument.name, argument.value.toString(event, debug)));
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
