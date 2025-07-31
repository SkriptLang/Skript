package org.skriptlang.skript.lang.function;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A class containing all arguments in a function call.
 */
public final class FunctionArguments {

	private final @Unmodifiable @NotNull Map<String, Object> arguments;

	public FunctionArguments(@NotNull Map<String, Object> arguments) {
		Preconditions.checkNotNull(arguments, "arguments cannot be null");

		this.arguments = Collections.unmodifiableMap(arguments);
	}

	/**
	 * Gets a specific argument by name.
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
	public <T> T get(@NotNull String name) {
		Preconditions.checkNotNull(name, "name cannot be null");

		//noinspection unchecked
		return (T) arguments.get(name);
	}

	/**
	 * Gets a specific argument by name, or a default value if no value is found.
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
	public <T> T getOrDefault(@NotNull String name, T defaultValue) {
		Preconditions.checkNotNull(name, "name cannot be null");

		//noinspection unchecked
		return (T) arguments.getOrDefault(name, defaultValue);
	}

	/**
	 * Gets a specific argument by name, or calculates the default value if no value is found.
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
	public <T> T getOrDefault(@NotNull String name, Supplier<T> defaultValue) {
		Preconditions.checkNotNull(name, "name cannot be null");

		Object existing = arguments.get(name);
		if (existing == null) {
			return defaultValue.get();
		} else {
			//noinspection unchecked
			return (T) existing;
		}
	}

	/**
	 * Returns whether this method call contained the following argument.
	 *
	 * @param name The argument.
	 * @return True if the argument is present.
	 */
	public boolean has(@NotNull String name) {
		Preconditions.checkNotNull(name, "name cannot be null");

		return arguments.containsKey(name);
	}

}
