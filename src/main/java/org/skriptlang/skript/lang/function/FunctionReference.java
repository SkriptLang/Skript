package org.skriptlang.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.function.*;
import ch.njol.skript.util.LiteralUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.StringJoiner;

public final class FunctionReference<T> implements Debuggable {

	private final String namespace;
	private final String name;
	private final Argument<Expression<?>>[] arguments;

	private Function<T> cachedFunction;
	private Signature<T> cachedSignature;
	private LinkedHashMap<String, ArgInfo> cachedArguments;

	private record ArgInfo(Expression<?> expression, Class<?> type) {

	}

	public FunctionReference(String namespace, String name, Argument<Expression<?>>[] arguments) {
		this.namespace = namespace;
		this.name = name;
		this.arguments = arguments;
	}

	public boolean validate() {
		if (cachedSignature == null) {
			//noinspection unchecked
			cachedSignature = (Signature<T>) Functions.getSignature(name, namespace);
		}

		if (cachedFunction == null) {
			//noinspection unchecked
			cachedFunction = (Function<T>) Functions.getFunction(name, namespace);
		}

		if (cachedArguments == null && cachedSignature != null) {
			cachedArguments = new LinkedHashMap<>();

			// get the target params of the function
			LinkedHashMap<String, Parameter<?>> targetParameters = new LinkedHashMap<>();
			for (Parameter<?> parameter : cachedSignature.parameters().values()) {
				targetParameters.put(parameter.name(), parameter);
			}

			for (Argument<Expression<?>> argument : arguments) {
				Parameter<?> target;
				if (argument.type == ArgumentType.NAMED) {
					target = targetParameters.get(argument.name);
				} else {
					Entry<String, Parameter<?>> first = targetParameters.firstEntry();

					// argument mismatch or array
					if (first == null) {

					}

					target = first.getValue();
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
				if (converted == null) {
					if (LiteralUtils.hasUnparsedLiteral(argument.value)) {
						Skript.error("Can't understand this expression: %s".formatted(argument.value));
					} else {
						Skript.error("Type mismatch for argument '%s' in function '%s'. Expected: %s, got %s."
							.formatted(target.name(), name, argument.value.getReturnType(), target.type()));
					}
					return false;
				}

				// all good
				cachedArguments.put(target.name(), new ArgInfo(converted, target.type()));
				targetParameters.remove(target.name());
			}
		}

		return true;
	}

	public T execute(Event event) {
		if (!validate()) {
			Skript.error("Epic function fail");
			return null;
		}

		LinkedHashMap<String, Object> args = new LinkedHashMap<>();
		cachedArguments.forEach((k, v) -> {
			if (!v.type().isArray()) {
				args.put(k, v.expression.getSingle(event));
			} else {
				args.put(k, v.expression.getArray(event));
			}
		});

		Function<? extends T> function = function();
		return function.execute(new FunctionEvent<>(function), new FunctionArguments(args));
	}

	public Function<T> function() {
		if (cachedFunction == null) {
			//noinspection unchecked
			cachedFunction = (Function<T>) Functions.getFunction(name, namespace);
		}

		return cachedFunction;
	}

	public Signature<T> signature() {
		if (cachedFunction == null) {
			//noinspection unchecked
			cachedSignature = (Signature<T>) Functions.getSignature(name, namespace);
		}

		return cachedSignature;
	}

	public String namespace() {
		return namespace;
	}

	public String name() {
		return name;
	}

	public Argument<Expression<?>>[] arguments() {
		return arguments;
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