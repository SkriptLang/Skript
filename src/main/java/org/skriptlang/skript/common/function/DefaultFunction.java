package org.skriptlang.skript.common.function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.Parameter.Modifier;
import org.skriptlang.skript.docs.Documentation;
import org.skriptlang.skript.docs.DocumentationAdapter;
import org.skriptlang.skript.docs.DocumentationDocumentable;

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
		extends Function<T>, DocumentationDocumentable, ch.njol.skript.doc.Documentable
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
		 * Sets this function's documentation.
		 * @param documentation Documentation describing this function.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> documentation(@NotNull Documentation documentation);

		/**
		 * Completes this builder with the code to execute on call of this function.
		 *
		 * @param execute The code to execute.
		 * @return The final function.
		 */
		DefaultFunction<T> build(@NotNull java.util.function.Function<FunctionArguments, T> execute);

		/**
		 * Sets this function builder's description.
		 *
		 * @param description The description.
		 * @return This builder.
		 * @deprecated Use {@link #documentation(Documentation)} with {@link Documentation#description()}.
		 */
		@Contract("_ -> this")
		@Deprecated(since = "INSERT VERSION", forRemoval = true)
		Builder<T> description(@NotNull String @NotNull ... description);

		/**
		 * Sets this function builder's version history.
		 *
		 * @param since The version information.
		 * @return This builder.
		 * @deprecated Use {@link #documentation(Documentation)} with {@link Documentation#since()}.
		 */
		@Contract("_ -> this")
		@Deprecated(since = "INSERT VERSION", forRemoval = true)
		Builder<T> since(@NotNull String @NotNull ... since);

		/**
		 * Sets this function builder's examples.
		 *
		 * @param examples The examples.
		 * @return This builder.
		 * @deprecated Use {@link #documentation(Documentation)} with {@link Documentation#examples()}.
		 */
		@Contract("_ -> this")
		@Deprecated(since = "INSERT VERSION", forRemoval = true)
		Builder<T> examples(@NotNull String @NotNull ... examples);

		/**
		 * Sets this function builder's keywords.
		 *
		 * @param keywords The keywords.
		 * @return This builder.
		 * @deprecated Use {@link #documentation(Documentation)} with {@link Documentation#keywords()}.
		 */
		@Contract("_ -> this")
		@Deprecated(since = "INSERT VERSION", forRemoval = true)
		Builder<T> keywords(@NotNull String @NotNull ... keywords);

		/**
		 * Sets this function builder's requires.
		 *
		 * @param requires The requirements.
		 * @return This builder.
		 * @deprecated Use {@link #documentation(Documentation)} with {@link Documentation#requirements()}.
		 */
		@Contract("_ -> this")
		@Deprecated(since = "INSERT VERSION", forRemoval = true)
		Builder<T> requires(@NotNull String @NotNull ... requires);

	}

	@Override
	default boolean canWrite(DocumentationAdapter adapter) {
		return DocumentationDocumentable.super.canWrite(adapter);
	}

	@Override
	default void write(DocumentationAdapter adapter) {
		DocumentationDocumentable.super.write(adapter);
		Function.super.write(adapter);
	}

}
