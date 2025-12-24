package org.skriptlang.skript.common.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A class containing all arguments in a function call.
 */
public sealed interface FunctionArguments
		permits FunctionArgumentsImpl {

	/**
	 * Gets a specific argument by name.
	 *
	 * <p>
	 * This method automatically conforms to your expected type,
	 * to avoid having to cast from Object. Use this method as follows.
	 * <pre><code>
	 * Number value = args.get("n");
	 * Boolean value = args.get("b");
	 * Number[] value = args.get("ns");
	 * args.<Boolean>get("b"); // inline
	 * </code></pre>
	 * </p>
	 *
	 * @param name The name of the parameter.
	 * @param <T>  The type to return.
	 * @return The value present, or null if no value is present.
	 */
	<T> T get(@NotNull String name);

	/**
	 * Gets a specific argument by name, or a default value if no value is found.
	 *
	 * <p>
	 * This method automatically conforms to your expected type,
	 * to avoid having to cast from Object. Use this method as follows.
	 * <pre><code>
	 * Number value = args.getOrDefault("n", 3.0);
	 * boolean value = args.getOrDefault("b", false);
	 * args.<Boolean>getOrDefault("b", () -> false); // inline
	 * </code></pre>
	 * </p>
	 *
	 * @param name         The name of the parameter.
	 * @param defaultValue The default value.
	 * @param <T>          The type to return.
	 * @return The value present, or the default value if no value is present.
	 */
	<T> T getOrDefault(@NotNull String name, T defaultValue);

	/**
	 * Gets a specific argument by name, or calculates the default value if no value is found.
	 *
	 * <p>
	 * This method automatically conforms to your expected type,
	 * to avoid having to cast from Object. Use this method as follows.
	 * <pre><code>
	 * Number value = args.getOrDefault("n", () -> 3.0);
	 * boolean value = args.getOrDefault("b", () -> false);
	 * args.<Boolean>getOrDefault("b", () -> false); // inline
	 * </code></pre>
	 * </p>
	 *
	 * @param name         The name of the parameter.
	 * @param defaultValue A supplier that calculates the default value if no existing value is found.
	 * @param <T>          The type to return.
	 * @return The value present, or the calculated default value if no value is present.
	 */
	<T> T getOrDefault(@NotNull String name, Supplier<T> defaultValue);

	/**
	 * Returns all the argument names available in this instance.
	 *
	 * @return All argument names.
	 */
	@Unmodifiable @NotNull Set<String> names();

}
