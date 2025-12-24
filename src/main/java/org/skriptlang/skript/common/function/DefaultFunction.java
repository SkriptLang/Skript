package org.skriptlang.skript.common.function;

import ch.njol.skript.doc.Documentable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.Parameter.Modifier;

/**
 * A function that has been implemented in Java, instead of in Skript.
 * <p>
 * An example implementation is stated below.
 * <pre><code>
 * DefaultFunction<Long> function = DefaultFunction.builder(addon, "floor", Long.class)
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
 *    });
 *
 * Functions.register(function);
 * </code></pre>
 * </p>
 *
 * @param <T> The return type.
 * @see #builder(SkriptAddon, String, Class)
 */
public sealed interface DefaultFunction<T>
		extends Function<T>, Documentable
		permits DefaultFunctionImpl {

	/**
	 * Creates a new builder for a function.
	 *
	 * @param name       The name of the function.
	 * @param returnType The type of the function.
	 * @param <T>        The return type.
	 * @return The builder for a function.
	 */
	@Contract("_, _, _ -> new")
	static <T> @NotNull Builder<T> builder(@NotNull SkriptAddon source, @NotNull String name, @NotNull Class<T> returnType) {
		return new DefaultFunctionImpl.BuilderImpl<>(source, name, returnType);
	}

	/**
	 * @return The addon this function was registered for.
	 */
	@NotNull SkriptAddon source();

	/**
	 * Represents a builder for {@link DefaultFunction DefaultFunctions}.
	 *
	 * @param <T> The return type of the function.
	 */
	interface Builder<T> {

		/**
		 * Sets this function builder's {@link ch.njol.skript.util.Contract}.
		 *
		 * @param contract The contract.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> contract(@NotNull ch.njol.skript.util.Contract contract);

		/**
		 * Sets this function builder's description.
		 *
		 * @param description The description.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> description(@NotNull String @NotNull ... description);

		/**
		 * Sets this function builder's version history.
		 *
		 * @param since The version information.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> since(@NotNull String @NotNull ... since);

		/**
		 * Sets this function builder's examples.
		 *
		 * @param examples The examples.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> examples(@NotNull String @NotNull ... examples);

		/**
		 * Sets this function builder's keywords.
		 *
		 * @param keywords The keywords.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> keywords(@NotNull String @NotNull ... keywords);

		/**
		 * Sets this function builder's requires.
		 *
		 * @param requires The requirements.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> requires(@NotNull String @NotNull ... requires);

		/**
		 * Adds a parameter to this function builder.
		 *
		 * @param name      The parameter name.
		 * @param type      The type of the parameter.
		 * @param modifiers The {@link Modifier}s to apply to this parameter.
		 * @return This builder.
		 */
		@Contract("_, _, _ -> this")
		Builder<T> parameter(@NotNull String name, @NotNull Class<?> type, Modifier @NotNull ... modifiers);

		/**
		 * Completes this builder with the code to execute on call of this function.
		 *
		 * @param execute The code to execute.
		 * @return The final function.
		 */
		DefaultFunction<T> build(@NotNull java.util.function.Function<FunctionArguments, T> execute);

	}

}
