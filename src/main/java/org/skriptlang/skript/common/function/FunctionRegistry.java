package org.skriptlang.skript.common.function;

import ch.njol.skript.SkriptAPIException;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

import java.util.Set;

/**
 * A registry for functions.
 * <p>
 * Obtain an instance using {@code SkriptAddon#registry(FunctionRegistry.class)}.
 * <br>
 * Or an unmodifiable view using {@code Skript.instance().registry(FunctionRegistry.class)}.
 */
@NonExtendable
@Internal
@Experimental
public interface FunctionRegistry extends Registry<Function<?>>, ViewProvider<FunctionRegistry> {

	/**
	 * Creates an empty function registry.
	 *
	 * @param skript the Skript instance
	 * @return a new empty function registry
	 */
	@Contract("_ -> new")
	static FunctionRegistry empty(Skript skript) {
		return new ch.njol.skript.lang.function.FunctionRegistry(skript);
	}

	/**
	 * Registers a global signature.
	 * <p>
	 * Attempting to register a local signature in the global namespace, or a global signature in
	 * a local namespace, will throw an {@link IllegalArgumentException}.
	 * </p>
	 *
	 * @param signature The signature to register.
	 * @throws SkriptAPIException       if a signature with the same name and parameters is already registered
	 *                                  in this namespace.
	 * @throws IllegalArgumentException if the signature is local.
	 */
	void register(@NotNull Signature<?> signature);

	/**
	 * Registers a local signature.
	 * <p>
	 * Attempting to register a local signature in the global namespace, or a global signature in
	 * a local namespace, will throw an {@link IllegalArgumentException}.
	 * </p>
	 *
	 * @param namespace The namespace to register the signature in.
	 *                  Usually represents the path of the script this signature is registered in.
	 * @param signature The signature to register.
	 * @throws SkriptAPIException       if a signature with the same name and parameters is already registered
	 *                                  in this namespace.
	 * @throws IllegalArgumentException if the signature is global and namespace is not null.
	 */
	void register(@NotNull String namespace, @NotNull Signature<?> signature);

	/**
	 * Registers a global function.
	 *
	 * @param function The function to register.
	 * @throws SkriptAPIException if a signature with the same name and parameters is already registered
	 *                            in this namespace.
	 */
	void register(@NotNull Function<?> function);

	/**
	 * Registers a local function.
	 *
	 * @param function  The function to register.
	 * @param namespace The namespace to register the function in.
	 *                  Usually represents the path of the script this signature is registered in.
	 * @throws SkriptAPIException if a signature with the same name and parameters is already registered
	 *                            in this namespace.
	 */
	void register(@NotNull String namespace, @NotNull Function<?> function);

	/**
	 * The result of attempting to retrieve a function.
	 * Depending on the type, a {@link Retrieval} will feature different data.
	 */
	enum RetrievalResult {

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
	record Retrieval<T>(
			@NotNull RetrievalResult result,
			T retrieved,
			Class<?>[][] conflictingArgs
	) {
	}

	/**
	 * Gets a function.
	 *
	 * @param name The name of the function.
	 * @param args The types of the arguments of the function.
	 * @return Information related to the attempt to get the specified function, stored in a {@link Retrieval} object.
	 */
	@NotNull Retrieval<Function<?>> getFunction(
			@NotNull String name,
			@NotNull Class<?>... args
	);

	/**
	 * Gets a function. If no local function is found, checks for global functions.
	 *
	 * @param namespace The namespace to get the function from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param name      The name of the function.
	 * @param args      The types of the arguments of the function.
	 * @return Information related to the attempt to get the specified function, stored in a {@link Retrieval} object.
	 */
	@NotNull Retrieval<Function<?>> getFunction(
			@NotNull String namespace,
			@NotNull String name,
			@NotNull Class<?>... args
	);

	/**
	 * Gets the signature for a global function with the given name and arguments.
	 *
	 * @param name The name of the function.
	 * @param args The types of the arguments of the function.
	 * @return Information related to the attempt to get the specified signature, stored in a {@link Retrieval} object.
	 */
	@NotNull Retrieval<Signature<?>> getSignature(
			@NotNull String name,
			@NotNull Class<?>... args
	);

	/**
	 * Gets the signature for a function with the given name and arguments.
	 * If no local function is found, checks for global functions.
	 *
	 * @param namespace The namespace to get the function from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param name      The name of the function.
	 * @param args      The types of the arguments of the function.
	 * @return Information related to the attempt to get the specified signature, stored in a {@link Retrieval} object.
	 */
	@NotNull Retrieval<Signature<?>> getSignature(
			@NotNull String namespace,
			@NotNull String name,
			@NotNull Class<?>... args
	);

	/**
	 * Gets every signature with the name {@code name} in the global namespace.
	 *
	 * @param name The name of the signature(s) to obtain.
	 * @return A list of all signatures named {@code name}.
	 */
	@Unmodifiable
	@NotNull Set<Signature<?>> getSignatures(@NotNull String name);

	/**
	 * Gets every signature with the name {@code name}.
	 * This includes global functions and functions registered in the namespace (if valid).
	 *
	 * @param namespace The additional namespace to obtain signatures from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param name      The name of the signature(s) to obtain.
	 * @return A list of all signatures named {@code name}.
	 */
	@Unmodifiable
	@NotNull Set<Signature<?>> getSignatures(@NotNull String namespace, @NotNull String name);

	/**
	 * Removes a function's signature from the registry.
	 *
	 * @param signature The signature to remove.
	 */
	void remove(@NotNull Signature<?> signature);

}
