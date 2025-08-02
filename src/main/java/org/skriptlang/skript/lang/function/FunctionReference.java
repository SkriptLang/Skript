package org.skriptlang.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.FunctionRegistry.Retrieval;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.LiteralUtils;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.StringJoiner;

public final class FunctionReference<T> implements Debuggable {

	private final String namespace;
	private final String name;
	private final Signature<T> signature;
	private final Argument<Expression<?>>[] arguments;

	private Function<T> cachedFunction;
	private LinkedHashMap<String, ArgInfo> cachedArguments;

	private record ArgInfo(Expression<?> expression, Class<?> type) {

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
		if (signature != null) {
			if (cachedArguments == null) {
				cachedArguments = new LinkedHashMap<>();

				// get the target params of the function
				LinkedHashMap<String, Parameter<?>> targetParameters = signature.parameters();

				for (Argument<Expression<?>> argument : arguments) {
					Parameter<?> target;
					if (argument.type == ArgumentType.NAMED) {
						target = targetParameters.get(argument.name);
					} else {
						Entry<String, Parameter<?>> first = targetParameters.firstEntry();

						if (first == null) {
							return false;
						}

						target = first.getValue();
					}

					// tried to find target, but it was already taken, so
					// the user is mixing named and positional arguments out of order
					if (target == null) {
						Skript.error("Mixing named and positional arguments is not allowed unless the order of the arguments matches the order of the parameters.");
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
		}

		return true;
	}

	public T execute(Event event) {
		if (!validate()) {
			Skript.error("Failed to verify function %s before execution.", name);
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

		Function<T> function = function();
		return function.execute(new FunctionEvent<>(function), new FunctionArguments(args));
	}

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