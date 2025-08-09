package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprBlockSound.SoundType;
import ch.njol.skript.expressions.ExprKeyed;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.*;
import ch.njol.skript.lang.function.FunctionRegistry.Retrieval;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

			// mixing arguments is only allowed when the order of arguments matches param order
			boolean mix = Arrays.stream(arguments)
				.map(it -> it.type)
				.collect(Collectors.toSet()).size() == ArgumentType.values().length;

			// get the target params of the function
			LinkedHashMap<String, Parameter<?>> targetParameters = signature.parameters();

			for (Argument<Expression<?>> argument : arguments) {
				Parameter<?> target;
				if (argument.type == ArgumentType.NAMED) {
					target = targetParameters.get(argument.name);
				} else {
					Entry<String, Parameter<?>> first = targetParameters.entrySet().iterator().next();

					if (first == null) {
						return false;
					}

					target = first.getValue();
				}

				if (target == null) {
					return false;
				}

				// try to parse value in the argument
				Class<?> conversionTarget;
				if (target.type().isArray()) {
					conversionTarget = target.type().componentType();
				} else {
					conversionTarget = target.type();
				}

				//noinspection unchecked
				Expression<?> converted = argument.value.getConvertedExpression(conversionTarget);

				// failed to parse value
				if (!validateArgument(target, argument.value, converted)) {
					return false;
				}

				if (mix && !targetParameters.entrySet().iterator().next().getKey().equals(target.name())) {
					Skript.error(Language.get("functions.mixing named and unnamed arguments"));
					return false;
				}

				// all good
				cachedArguments.put(target.name(), new ArgInfo(converted, target.type(), target.modifiers()));
				targetParameters.remove(target.name());
			}
		}

		signature.calls().add(this);

		return true;
	}

	private boolean validateArgument(Parameter<?> target, Expression<?> original, Expression<?> converted) {
		if (converted == null) {
			if (LiteralUtils.hasUnparsedLiteral(original)) {
				Skript.error("Can't understand this expression: %s", original);
			} else {
				Skript.error("Expected type %s for argument '%s', but %s is of type %s.",
					getName(target.type(), target.single()), target.name(), original, getName(original.getReturnType(), original.isSingle()));
			}
			return false;
		}

		if (target.single() && !converted.isSingle()) {
			Skript.error("Expected type %s for argument '%s', but %s is of type %s.",
				getName(target.type(), target.single()), target.name(), converted, getName(converted.getReturnType(), converted.isSingle()));
			return false;
		}

		return true;
	}

	private String getName(Class<?> clazz, boolean single) {
		if (single) {
			return Classes.getSuperClassInfo(clazz).getName().getSingular();
		} else {
			if (clazz.isArray()) {
				return Classes.getSuperClassInfo(clazz.componentType()).getName().getPlural();
			}
			return Classes.getSuperClassInfo(clazz).getName().getPlural();
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
		FunctionEvent<T> fnEvent = new FunctionEvent<>(function);

		if (Functions.callFunctionEvents)
			Bukkit.getPluginManager().callEvent(fnEvent);

		return function.execute(fnEvent, new FunctionArguments(args));
	}

	private KeyedValue<?>[] evaluateKeyed(Expression<?> expression, Event event) {
		if (expression instanceof ExpressionList<?> list) {
			return evaluateSingleListParameter(list.getExpressions(), event);
		}
		return evaluateParameter(expression, event);
	}

	private KeyedValue<?>[] evaluateSingleListParameter(Expression<?>[] parameters, Event event) {
		List<Object> values = new ArrayList<>();
		Set<String> keys = new LinkedHashSet<>();
		int keyIndex = 1;
		for (Expression<?> parameter : parameters) {
			Object[] valuesArray = parameter.getArray(event);
			String[] keysArray = KeyProviderExpression.areKeysRecommended(parameter)
				? ((KeyProviderExpression<?>) parameter).getArrayKeys(event)
				: null;

			// Don't allow mutating across function boundary; same hack is applied to variables
			for (Object value : valuesArray)
				values.add(Classes.clone(value));

			if (keysArray != null) {
				keys.addAll(Arrays.asList(keysArray));
				continue;
			}

			for (int i = 0; i < valuesArray.length; i++) {
				while (keys.contains(String.valueOf(keyIndex)))
					keyIndex++;
				keys.add(String.valueOf(keyIndex++));
			}
		}
		return KeyedValue.zip(values.toArray(), keys.toArray(new String[0]));
	}

	private KeyedValue<?>[] evaluateParameter(Expression<?> parameter, Event event) {
		Object[] values = parameter.getArray(event);

		// Don't allow mutating across function boundary; same hack is applied to variables
		for (int i = 0; i < values.length; i++)
			values[i] = Classes.clone(values[i]);

		String[] keys = KeyProviderExpression.areKeysRecommended(parameter)
			? ((KeyProviderExpression<?>) parameter).getArrayKeys(event)
			: null;
		return KeyedValue.zip(values, keys);
	}

	/**
	 * @return The function referred to by this reference.
	 */
	public Function<T> function() {
		if (cachedFunction == null) {
			Class<?>[] parameters = signature.parameters().values().stream().map(Parameter::type).toArray(Class[]::new);

			Retrieval<Function<?>> retrieval = FunctionRegistry.getRegistry().getFunction(namespace, name, parameters);

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
	public boolean single() {
		if (signature.getContract() != null) {
			Expression<?>[] args = Arrays.stream(arguments)
				.map(it -> it.value)
				.toArray(Expression[]::new);

			return signature.getContract().isSingle(args);
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
	 */
	public record Argument<T>(
		ArgumentType type,
		String name,
		T value
	) {

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
