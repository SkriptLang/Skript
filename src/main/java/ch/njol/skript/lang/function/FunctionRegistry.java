package ch.njol.skript.lang.function;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.Registry;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @deprecated Use {@link org.skriptlang.skript.common.function.FunctionRegistry} instead.
 */
@Deprecated(forRemoval = true, since = "INSERT VERSION")
public final class FunctionRegistry implements Registry<Function<?>> {

	private static org.skriptlang.skript.common.function.FunctionRegistry newRegistry;

	@ApiStatus.Internal
	public static void setNewRegistry(org.skriptlang.skript.common.function.FunctionRegistry newRegistry) {
		FunctionRegistry.newRegistry = newRegistry;
	}

	private static FunctionRegistry registry;

	/**
	 * @deprecated Use {@link org.skriptlang.skript.addon.SkriptAddon#registry(Class)}
	 * and {@link org.skriptlang.skript.common.function.FunctionRegistry} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public static FunctionRegistry getRegistry() {
		if (registry == null) {
			registry = new FunctionRegistry();
		}
		return registry;
	}

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.FunctionRegistry#elements()} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	@Override
	public @Unmodifiable @NotNull Collection<Function<?>> elements() {
		return newRegistry.elements().stream()
				.map(it -> (Function<?>) it)
				.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.FunctionRegistry#register(String, org.skriptlang.skript.common.function.Signature)} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public void register(@Nullable String namespace, @NotNull Signature<?> signature) {
		if (namespace == null) {
			newRegistry.register(signature);
		} else {
			newRegistry.register(namespace, signature);
		}
	}

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.FunctionRegistry#register(String, org.skriptlang.skript.common.function.Function)} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public void register(@Nullable String namespace, @NotNull Function<?> function) {
		if (namespace == null) {
			newRegistry.register(function);
		} else {
			newRegistry.register(namespace, function);
		}
	}

	/**
	 * The result of attempting to retrieve a function.
	 * Depending on the type, a {@link Retrieval} will feature different data.
	 */
	public enum RetrievalResult {

		/**
		 * The specified function or signature has not been registered.
		 */
		NOT_REGISTERED,

		/**
		 * There are multiple functions or signatures that may fit the provided name and argument types.
		 */
		AMBIGUOUS,

		/**
		 * A single function or signature has been found which matches the name and argument types.
		 */
		EXACT

	}

	/**
	 * The result of trying to retrieve a function or signature.
	 * <p>
	 * When getting a function or signature, the following situations may occur.
	 * These are specified by {@code type}.
	 * <ul>
	 *     <li>
	 *         {@code NOT_REGISTERED}. The specified function or signature is not registered.
	 *         Both {@code retrieved} and {@code conflictingArgs} will be null.
	 *     </li>
	 *     <li>
	 *         {@code AMBIGUOUS}. There are multiple functions or signatures that
	 *         may fit the provided name and argument types.
	 *           {@code retrieved} will be null, and {@code conflictingArgs}
	 * 		   will contain the conflicting function or signature parameters.
	 *     </li>
	 *     <li>
	 *         {@code EXACT}. A single function or signature has been found which matches the name and argument types.
	 *         {@code retrieved} will contain the function or signature, and {@code conflictingArgs} will be null.
	 *     </li>
	 * </ul>
	 * </p>
	 *
	 * @param result          The result of the function or signature retrieval.
	 * @param retrieved       The function or signature that was found if {@code result} is {@code EXACT}.
	 * @param conflictingArgs The conflicting arguments if {@code result} is {@code AMBIGUOUS}.
	 */
	public record Retrieval<T>(
			@NotNull RetrievalResult result,
			T retrieved,
			Class<?>[][] conflictingArgs
	) {
	}

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.FunctionRegistry#getFunction(String, String, Class[])} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public @NotNull Retrieval<Function<?>> getFunction(
			@Nullable String namespace,
			@NotNull String name,
			@NotNull Class<?>... args
	) {
		org.skriptlang.skript.common.function.FunctionRegistry.Retrieval<org.skriptlang.skript.common.function.Function<?>> retrieval;

		if (namespace == null) {
			retrieval = newRegistry.getFunction(name, args);
		} else {
			retrieval = newRegistry.getFunction(namespace, name, args);
		}

		return new Retrieval<>(RetrievalResult.valueOf(retrieval.result().name()),
				(Function<?>) retrieval.retrieved(),
				retrieval.conflictingArgs().stream()
						.map(it -> it.toArray(Class<?>[]::new))
						.toArray(Class<?>[][]::new));
	}

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.FunctionRegistry#getSignature(String, String, Class[])} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public Retrieval<Signature<?>> getSignature(
			@Nullable String namespace,
			@NotNull String name,
			@NotNull Class<?>... args
	) {
		org.skriptlang.skript.common.function.FunctionRegistry.Retrieval<org.skriptlang.skript.common.function.Signature<?>> retrieval;

		if (namespace == null) {
			retrieval = newRegistry.getSignature(name, args);
		} else {
			retrieval = newRegistry.getSignature(namespace, name, args);
		}

		return new Retrieval<>(RetrievalResult.valueOf(retrieval.result().name()),
				(Signature<?>) retrieval.retrieved(),
				retrieval.conflictingArgs().stream()
						.map(it -> it.toArray(Class<?>[]::new))
						.toArray(Class<?>[][]::new));
	}

	/**
	 * Gets the signature for a function with the given name and arguments. If no local function is found,
	 * checks for global functions. If {@code namespace} is null, only global signatures will be checked.
	 * <p>
	 * This function checks performs no argument conversions, and is only used for determining whether a
	 * signature already exists with the exact specified arguments. In almost all cases, {@link #getSignature(String, String, Class[])}
	 * should be used.
	 * </p>
	 *
	 * @param namespace The namespace to get the function from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param name      The name of the function.
	 * @param args      The types of the arguments of the function.
	 * @return The signature for the function with the given name and argument types, or null if no such function exists.
	 */
	Retrieval<Signature<?>> getExactSignature(
			@Nullable String namespace,
			@NotNull String name,
			@NotNull Class<?>... args
	) {
		org.skriptlang.skript.common.function.FunctionRegistry.Retrieval<org.skriptlang.skript.common.function.Signature<?>> retrieval;

		if (namespace == null) {
			retrieval = newRegistry.getSignature(name, args);
		} else {
			retrieval = newRegistry.getSignature(namespace, name, args);
		}

		return new Retrieval<>(RetrievalResult.valueOf(retrieval.result().name()),
				(Signature<?>) retrieval.retrieved(),
				retrieval.conflictingArgs().stream()
						.map(it -> it.toArray(Class<?>[]::new))
						.toArray(Class<?>[][]::new));
	}

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.FunctionRegistry#getSignatures(String, String)} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public @Unmodifiable @NotNull Set<Signature<?>> getSignatures(@Nullable String namespace, @NotNull String name) {
		Set<org.skriptlang.skript.common.function.Signature<?>> signatures;

		if (namespace == null) {
			signatures = newRegistry.getSignatures(name);
		} else {
			signatures = newRegistry.getSignatures(namespace, name);
		}

		return signatures.stream()
				.map(it -> (Signature<?>) it)
				.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.FunctionRegistry#remove(org.skriptlang.skript.common.function.Signature)} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public void remove(@NotNull Signature<?> signature) {
		newRegistry.remove(signature);
	}

}
