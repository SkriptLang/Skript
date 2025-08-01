package org.skriptlang.skript.lang.function;

import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Signature;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.function.Parameter.Modifier;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A function that has been implemented in Java, instead of in Skript.
 * <p>
 * An example implementation is stated below.
 * <pre><code>
 * DefaultFunction.builder(addon, "floor", Long.class)
 * 	.description("Rounds a number down.")
 * 	.examples("floor(2.34) = 2")
 * 	.since("3.0")
 * 	.parameter("n", Number.class)
 * 	.build(args -> {
 * 		Object value = args.get("n");
 *
 * 		if (value instanceof Long l)
 * 			return l;
 *
 * 		return Math2.floor(((Number) value).doubleValue());
 *    })
 *    .register();
 * </code></pre>
 * </p>
 *
 * @param <T> The return type.
 * @see #builder(SkriptAddon, String, Class)
 */
public final class DefaultFunction<T> extends ch.njol.skript.lang.function.Function<T> {

	/**
	 * Creates a new builder for a function.
	 *
	 * @param name       The name of the function.
	 * @param returnType The type of the function.
	 * @param <T>        The return type.
	 * @return The builder for a function.
	 */
	public static <T> Builder<T> builder(@NotNull SkriptAddon source, @NotNull String name, @NotNull Class<T> returnType) {
		return new Builder<>(source, name, returnType);
	}

	private final Parameter<?>[] parameters;
	private final Function<FunctionArguments, T> execute;

	private final String[] description;
	private final String[] since;
	private final String[] examples;
	private final String[] keywords;

	private DefaultFunction(
		String name, Parameter<?>[] parameters,
		Class<T> returnType, boolean single,
		@Nullable ch.njol.skript.util.Contract contract, Function<FunctionArguments, T> execute,
		String[] description, String[] since, String[] examples, String[] keywords
	) {
		super(new Signature<>(null, name, parameters, returnType, single, contract));

		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(parameters, "parameters cannot be null");
		Preconditions.checkNotNull(returnType, "return type cannot be null");
		Preconditions.checkNotNull(execute, "execute cannot be null");

		this.parameters = parameters;
		this.execute = execute;
		this.description = description;
		this.since = since;
		this.examples = examples;
		this.keywords = keywords;
	}

	@Override
	public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
		Map<String, Object> args = new LinkedHashMap<>();

		int length = Math.min(parameters.length, params.length);
		for (int i = 0; i < length; i++) {
			Object[] arg = params[i];
			org.skriptlang.skript.lang.function.Parameter<?> parameter = parameters[i];

			if (arg == null || arg.length == 0) {
				if (parameter.modifiers().contains(Modifier.OPTIONAL)) {
					continue;
				} else {
					return null;
				}
			}

			if (arg.length == 1 || parameter.single()) {
				assert parameter.type().isAssignableFrom(arg[0].getClass())
					: "argument type %s does not match parameter type %s".formatted(parameter.type().getSimpleName(),
					arg[0].getClass().getSimpleName());

				args.put(parameter.name(), arg[0]);
			} else {
				assert parameter.type().isAssignableFrom(arg.getClass())
					: "argument type %s does not match parameter type %s".formatted(parameter.type().getSimpleName(),
					arg.getClass().getSimpleName());

				args.put(parameter.name(), arg);
			}
		}

		FunctionArguments arguments = new FunctionArguments(args);
		T result = execute.apply(arguments);

		if (result == null) {
			return null;
		} else if (result.getClass().isArray()) {
			//noinspection unchecked
			return (T[]) result;
		} else {
			//noinspection unchecked
			T[] array = (T[]) Array.newInstance(result.getClass(), 1);
			array[0] = result;
			return array;
		}
	}

	@Override
	public boolean resetReturnValue() {
		return true;
	}

	/**
	 * Returns this function's description.
	 *
	 * @return The description.
	 */
	public @NotNull String @NotNull [] description() {
		return description;
	}

	/**
	 * Returns this function's version history.
	 *
	 * @return The version history.
	 */
	public @NotNull String @NotNull [] since() {
		return since;
	}

	/**
	 * Returns this function's examples.
	 *
	 * @return The examples.
	 */
	public @NotNull String @NotNull [] examples() {
		return examples;
	}

	/**
	 * Returns this function's keywords.
	 *
	 * @return The keywords.
	 */
	public @NotNull String @NotNull [] keywords() {
		return keywords;
	}

	/**
	 * Registers this function.
	 *
	 * @return This function.
	 */
	@Contract(" -> this")
	public DefaultFunction<T> register() {
		Functions.register(this);

		return this;
	}

	public static class Builder<T> {

		private final SkriptAddon source;
		private final String name;
		private final Class<T> returnType;
		private final Map<String, DefaultParameter<?>> parameters = new LinkedHashMap<>();

		private ch.njol.skript.util.Contract contract = null;

		private String[] description, since, examples, keywords;

		private Builder(@NotNull SkriptAddon source, @NotNull String name, @NotNull Class<T> returnType) {
			Preconditions.checkNotNull(source, "source cannot be null");
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(returnType, "return type cannot be null");

			this.source = source;
			this.name = name;
			this.returnType = returnType;
		}

		/**
		 * Sets this function builder's {@link Contract}.
		 *
		 * @param contract The contract.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder<T> contract(@NotNull ch.njol.skript.util.Contract contract) {
			Preconditions.checkNotNull(contract, "contract cannot be null");

			this.contract = contract;
			return this;
		}

		/**
		 * Sets this function builder's description.
		 *
		 * @param description The description.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder<T> description(@NotNull String @NotNull ... description) {
			Preconditions.checkNotNull(description, "description cannot be null");
			checkNotNull(description, "description contents cannot be null");

			this.description = description;
			return this;
		}

		/**
		 * Sets this function builder's version history.
		 *
		 * @param since The version information.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder<T> since(@NotNull String @NotNull ... since) {
			Preconditions.checkNotNull(since, "since cannot be null");
			checkNotNull(since, "since contents cannot be null");

			this.since = since;
			return this;
		}

		/**
		 * Sets this function builder's examples.
		 *
		 * @param examples The examples.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder<T> examples(@NotNull String @NotNull ... examples) {
			Preconditions.checkNotNull(examples, "examples cannot be null");
			checkNotNull(examples, "examples contents cannot be null");

			this.examples = examples;
			return this;
		}

		/**
		 * Sets this function builder's keywords.
		 *
		 * @param keywords The keywords.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder<T> keywords(@NotNull String @NotNull ... keywords) {
			Preconditions.checkNotNull(keywords, "keywords cannot be null");
			checkNotNull(keywords, "keywords contents cannot be null");

			this.keywords = keywords;
			return this;
		}

		/**
		 * Checks whether the elements in a {@link String} array are null.
		 * @param strings The strings.
		 */
		private static void checkNotNull(@NotNull String[] strings, @NotNull String message) {
			for (String string : strings) {
				Preconditions.checkNotNull(string, message);
			}
		}

		/**
		 * Adds a parameter to this function builder.
		 *
		 * @param name The parameter name.
		 * @param type The type of the parameter.
		 * @param modifiers The {@link Modifier}s to apply to this parameter.
		 * @return This builder.
		 */
		@Contract("_, _, _ -> this")
		public Builder<T> parameter(@NotNull String name, @NotNull Class<?> type, Modifier @NotNull ... modifiers) {
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(type, "type cannot be null");

			parameters.put(name, new DefaultParameter<>(name, type, modifiers));
			return this;
		}

		/**
		 * Completes this builder with the code to execute on call of this function.
		 *
		 * @param execute The code to execute.
		 * @return The final function.
		 */
		public DefaultFunction<T> build(@NotNull Function<FunctionArguments, T> execute) {
			Preconditions.checkNotNull(execute, "execute cannot be null");

			return new DefaultFunction<>(name, parameters.values().toArray(new Parameter[0]), returnType,
				!returnType.isArray(), contract, execute, description, since, examples, keywords);
		}

	}

}
