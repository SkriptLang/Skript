package ch.njol.skript.lang.function;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * A class containing all arguments in a function call.
 */
public final class FunctionArguments {

	private final Map<String, Object> arguments;

	public FunctionArguments(@NotNull SequencedMap<String, Object> arguments) {
		Preconditions.checkNotNull(arguments, "arguments cannot be null");

		this.arguments = arguments;
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
	 * Returns whether this method call contained the following argument.
	 *
	 * @param name The argument.
	 * @return True if the argument is present.
	 */
	public boolean has(@NotNull String name) {
		Preconditions.checkNotNull(name, "name cannot be null");

		return arguments.containsKey(name);
	}

	/**
	 * Returns an unmodifiable set of all names.
	 *
	 * @return All names.
	 */
	@Unmodifiable
	@NotNull Set<String> getNames() {
		return Collections.unmodifiableSet(arguments.keySet());
	}

}
