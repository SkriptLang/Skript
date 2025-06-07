package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.Registry;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A registry for functions.
 */
@ApiStatus.Internal // for now
final class FunctionRegistry implements Registry<Function<?>> {

	private static FunctionRegistry registry;

	/**
	 * Gets the global function registry.
	 *
	 * @return The global function registry.
	 */
	public static FunctionRegistry getRegistry() {
		if (registry == null) {
			registry = new FunctionRegistry();
		}
		return registry;
	}

	/**
	 * The pattern for a valid function name.
	 * Functions must start with a letter or underscore and can only contain letters, numbers, and underscores.
	 */
	final static String FUNCTION_NAME_PATTERN = "[\\p{IsAlphabetic}_][\\p{IsAlphabetic}\\d_]*";

	/**
	 * The namespace for functions registered using Java.
	 */
	private final NamespaceIdentifier GLOBAL_NAMESPACE = new NamespaceIdentifier(Scope.GLOBAL, null);

	/**
	 * All registered namespaces.
	 */
	private final Map<NamespaceIdentifier, Namespace> namespaces = new ConcurrentHashMap<>();

	@Override
	@UnmodifiableView
	public @NotNull Collection<Function<?>> elements() {
		Set<Function<?>> functions = new HashSet<>();

		for (Namespace namespace : namespaces.values()) {
			functions.addAll(namespace.functions.values());
		}

		return Collections.unmodifiableSet(functions);
	}

	/**
	 * Registers a signature.
	 *
	 * @param namespace The namespace to register the signature in.
	 *                  If namespace is null, will register this signature globally.
	 *                  Usually represents the path of the script this signature is registered in.
	 * @param signature The signature to register.
	 * @throws SkriptAPIException if a signature with the same name and parameters is already registered
	 *                            in this namespace.
	 */
	public void register(@Nullable String namespace, @NotNull Signature<?> signature) {
		Preconditions.checkNotNull(signature, "signature cannot be null");
		Skript.debug("Registering signature '" + signature.getName() + "'");

		// namespace
		NamespaceIdentifier namespaceId = GLOBAL_NAMESPACE;
		if (namespace != null && signature.isLocal()) {
			namespaceId = new NamespaceIdentifier(Scope.LOCAL, namespace);
		}

		// since we are getting a namespace and then updating it by putting it back in the map,
		// avoid race conditions by ensuring only one thread can access the namespaces map at a time.
		synchronized (namespaces) {
			Namespace ns = namespaces.getOrDefault(namespaceId, new Namespace());

			FunctionIdentifier identifier = FunctionIdentifier.of(signature);

			// register
			Set<FunctionIdentifier> identifiersWithName = ns.identifiers.getOrDefault(identifier.name, new HashSet<>());
			boolean exists = identifiersWithName.add(identifier);
			if (!exists) {
				alreadyRegisteredError(signature.getName(), identifier, namespaceId);
			}
			ns.identifiers.put(identifier.name, identifiersWithName);

			ns.signatures.put(identifier, signature);

			namespaces.put(namespaceId, ns);
		}
	}

	/**
	 * Registers a global function.
	 *
	 * @param function The function to register.
	 * @throws SkriptAPIException if the function name is invalid or if
	 *                            a function with the same name and parameters is already registered
	 *                            in this namespace.
	 */
	public void register(@NotNull Function<?> function) {
		register(null, function);
	}

	/**
	 * Registers a function.
	 *
	 * @param namespace The namespace to register the function in.
	 *                  If namespace is null, will register this function globally.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param function  The function to register.
	 * @throws SkriptAPIException if the function name is invalid or if
	 *                            a function with the same name and parameters is already registered
	 *                            in this namespace.
	 */
	public void register(@Nullable String namespace, @NotNull Function<?> function) {
		Preconditions.checkNotNull(function, "function cannot be null");
		Skript.debug("Registering function '" + function.getName() + "'");

		String name = function.getName();
		if (!name.matches(FUNCTION_NAME_PATTERN)) {
			throw new SkriptAPIException("Invalid function name '" + name + "'");
		}

		// namespace
		NamespaceIdentifier namespaceId = GLOBAL_NAMESPACE;
		if (namespace != null && function.getSignature().isLocal()) {
			namespaceId = new NamespaceIdentifier(Scope.LOCAL, namespace);
		}

		FunctionIdentifier identifier = FunctionIdentifier.of(function.getSignature());
		if (!signatureExists(namespaceId, identifier)) {
			register(namespace, function.getSignature());
		}

		// since we are getting a namespace and then updating it by putting it back in the map,
		// avoid race conditions by ensuring only one thread can access the namespaces map at a time.
		synchronized (namespaces) {
			Namespace ns = namespaces.getOrDefault(namespaceId, new Namespace());
			Function<?> existing = ns.functions.put(identifier, function);
			if (existing != null) {
				alreadyRegisteredError(name, identifier, namespaceId);
			}

			namespaces.put(namespaceId, ns);
		}
	}

	private static void alreadyRegisteredError(String name, FunctionIdentifier identifier, NamespaceIdentifier namespace) {
		throw new SkriptAPIException("Function '%s' with parameters %s is already registered in %s"
			.formatted(name, Arrays.toString(Arrays.stream(identifier.args).map(Class::getSimpleName).toArray()),
				namespace));
	}

	/**
	 * Checks if a signature with the given name and arguments exists in the given namespace.
	 *
	 * <ul>
	 * <li>If {@code namespace} is null, only global signatures will be checked.</li>
	 * <li>If {@code args} is null or empty,
	 * the first function with the same name as the {@code name} param will be returned.</li>
	 * </ul>
	 *
	 * @param namespace The namespace to check in.
	 *                  Usually represents the path of the script a signature is registered in.
	 * @param name      The name of the function.
	 * @param args      The types of the arguments of the function.
	 * @return True if a signature with the given name and argument types exists in the script, false otherwise.
	 */
	public boolean signatureExists(@Nullable String namespace, @NotNull String name, Class<?>... args) {
		if (namespace == null) {
			return signatureExists(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, false, args));
		}

		return signatureExists(new NamespaceIdentifier(Scope.LOCAL, namespace.toLowerCase()),
			FunctionIdentifier.of(name, true, args));
	}

	/**
	 * Checks if a function with the given name and arguments exists in the namespace.
	 *
	 * @param namespace  The namespace to check in.
	 * @param identifier The identifier of the function.
	 * @return True if a function with the given name and arguments exists in the namespace, false otherwise.
	 */
	private boolean signatureExists(@NotNull NamespaceIdentifier namespace, @NotNull FunctionIdentifier identifier) {
		Preconditions.checkNotNull(namespace, "namespace cannot be null");
		Preconditions.checkNotNull(identifier, "identifier cannot be null");

		Namespace ns = namespaces.getOrDefault(namespace, new Namespace());
		if (!ns.identifiers.containsKey(identifier.name)) {
			return false;
		}

		for (FunctionIdentifier other : ns.identifiers.get(identifier.name)) {
			if (identifier.equals(other)) {
				return true;
			}
		}

		return false;
	}


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
	 * The result of trying to retrieve a function.
	 * <p>
	 * When getting a function, the following situations may occur.
	 * These are specified by {@code result}.
	 * <ul>
	 *     <li>
	 *         {@code NOT_REGISTERED}. The specified function is not registered.
	 *         Both {@code function} and {@code conflictingArgs} will be null.
	 *     </li>
	 *     <li>
	 *         {@code AMBIGUOUS}. There are multiple functions that
	 *         may fit the provided name and argument types.
	 *           {@code function} will be null, and {@code conflictingArgs}
	 * 		   will contain the conflicting function parameters.
	 *     </li>
	 *     <li>
	 *         {@code EXACT}. A single function has been found which matches the name and argument types.
	 *         {@code function} will contain the function, and {@code conflictingArgs} will be null.
	 *     </li>
	 * </ul>
	 * </p>
	 *
	 * @param result          The result of the function retrieval.
	 * @param function        The function that was found if {@code result} is {@code EXACT}.
	 * @param conflictingArgs The conflicting arguments if {@code result} is {@code AMBIGUOUS}.
	 */
	public record FunctionRetrieval(
		@NotNull RetrievalResult result,
		Function<?> function,
		Class<?>[][] conflictingArgs
	) { }

	/**
	 * Gets a function from a script. If no local function is found, checks for global functions.
	 *
	 * <ul>
	 * <li>If {@code namespace} is null, only global functions will be checked.</li>
	 * <li>If {@code args} is null or empty,
	 * the first function with the same name as the {@code name} param will be returned.</li>
	 * </ul>
	 *
	 * @param namespace The namespace to get the function from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param name      The name of the function.
	 * @param args      The types of the arguments of the function.
	 * @return Information related to the attempt to get the specified function,
	 * stored in a {@link FunctionRetrieval} object.
	 */
	public @NotNull FunctionRetrieval getFunction(
		@Nullable String namespace,
		@NotNull String name, Class<?>... args
	) {
		if (namespace == null) {
			return getFunction(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, false, args));
		}

		FunctionRetrieval attempt = getFunction(new NamespaceIdentifier(Scope.LOCAL, namespace),
			FunctionIdentifier.of(name, true, args));
		if (attempt.result != RetrievalResult.EXACT) {
			return getFunction(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, false, args));
		}
		return attempt;
	}

	/**
	 * Gets a function from a namespace.
	 *
	 * @param namespace The namespace to get the function from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param provided  The provided identifier of the function.
	 * @return Information related to the attempt to get the specified function,
	 * stored in a {@link FunctionRetrieval} object.
	 */
	private @NotNull FunctionRetrieval getFunction(@NotNull NamespaceIdentifier namespace, @NotNull FunctionIdentifier provided) {
		Preconditions.checkNotNull(namespace, "namespace cannot be null");
		Preconditions.checkNotNull(provided, "provided cannot be null");

		Namespace ns = namespaces.getOrDefault(namespace, new Namespace());
		Set<FunctionIdentifier> existing = ns.identifiers.get(provided.name);
		if (existing == null) {
			Skript.debug("No functions named '%s' exist in the '%s' namespace".formatted(provided.name, namespace.name));
			return new FunctionRetrieval(RetrievalResult.NOT_REGISTERED, null, null);
		}

		Set<FunctionIdentifier> candidates = candidates(provided, existing);
		if (candidates.isEmpty()) {
			Skript.debug("Failed to find a function for '%s'".formatted(provided.name));
			return new FunctionRetrieval(RetrievalResult.NOT_REGISTERED, null, null);
		} else if (candidates.size() == 1) {
			Skript.debug("Matched function for '%s': %s".formatted(provided.name, candidates.stream().findAny().orElse(null)));
			return new FunctionRetrieval(RetrievalResult.EXACT,
				ns.functions.get(candidates.stream().findAny().orElse(null)),
				null);
		} else {
			String options = candidates.stream().map(Record::toString).collect(Collectors.joining(", "));
			Skript.debug("Failed to match an exact function for '%s'".formatted(provided.name));
			Skript.debug("Identifier: %s".formatted(provided));
			Skript.debug("Options: %s".formatted(options));
			return new FunctionRetrieval(RetrievalResult.AMBIGUOUS,
				null,
				candidates.stream()
					.map(FunctionIdentifier::args)
					.toArray(Class<?>[][]::new));
		}
	}

	/**
	 * The result of trying to retrieve a signature.
	 * <p>
	 * When getting a signature, the following situations may occur.
	 * These are specified by {@code result}.
	 * <ul>
	 *     <li>
	 *         {@code NOT_REGISTERED}. The specified signature is not registered.
	 *         Both {@code signature} and {@code conflictingArgs} will be null.
	 *     </li>
	 *     <li>
	 *         {@code AMBIGUOUS}. There are multiple signatures that
	 *         may fit the provided name and argument types.
	 *         {@code signature} will be null, and {@code conflictingArgs}
	 * 		   will contain the conflicting signature parameters.
	 *     </li>
	 *     <li>
	 *         {@code EXACT}. A single signature has been found which matches the name and argument types.
	 *         {@code signature} will contain the signature, and {@code conflictingArgs} will be null.
	 *     </li>
	 * </ul>
	 * </p>
	 *
	 * @param result          The result of the function retrieval.
	 * @param signature       The signature that was found if {@code result} is {@code EXACT}.
	 * @param conflictingArgs The conflicting arguments if {@code result} is {@code AMBIGUOUS}.
	 */
	public record SignatureRetrieval(
		@NotNull RetrievalResult result,
		Signature<?> signature,
		Class<?>[][] conflictingArgs
	) {

	}

	/**
	 * Gets the signature for a function with the given name and arguments. If no local function is found,
	 * checks for global functions.
	 *
	 * <ul>
	 * <li>If {@code namespace} is null, only global signatures will be checked.</li>
	 * <li>If {@code args} is null or empty,
	 * the first function with the same name as the {@code name} param will be returned.</li>
	 * </ul>
	 *
	 * @param namespace The namespace to get the function from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param name      The name of the function.
	 * @param args      The types of the arguments of the function.
	 * @return The signature for the function with the given name and argument types, or null if no such function exists.
	 */
	public SignatureRetrieval getSignature(@Nullable String namespace, @NotNull String name, Class<?>... args) {
		if (namespace == null) {
			return getSignature(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, false, args));
		}

		SignatureRetrieval attempt = getSignature(new NamespaceIdentifier(Scope.LOCAL, namespace),
			FunctionIdentifier.of(name, true, args));
		if (attempt.result != RetrievalResult.EXACT) {
			return getSignature(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, false, args));
		}
		return attempt;
	}

	/**
	 * Gets the signature for a function with the given name and arguments.
	 *
	 * @param namespace The namespace to get the function from.
	 * @param provided  The provided identifier of the function.
	 * @return The signature for the function with the given name and argument types, or null if no such signature exists
	 * in the specified namespace.
	 */
	private SignatureRetrieval getSignature(@NotNull NamespaceIdentifier namespace, @NotNull FunctionIdentifier provided) {
		Preconditions.checkNotNull(namespace, "namespace cannot be null");
		Preconditions.checkNotNull(provided, "provided cannot be null");

		Namespace ns = namespaces.getOrDefault(namespace, new Namespace());
		if (!ns.identifiers.containsKey(provided.name)) {
			Skript.debug("No signatures named '%s' exist in the '%s' namespace", provided.name, namespace.name);
			return new SignatureRetrieval(RetrievalResult.NOT_REGISTERED, null, null);
		}

		Set<FunctionIdentifier> candidates = candidates(provided, ns.identifiers.get(provided.name));
		if (candidates.isEmpty()) {
			Skript.debug("Failed to find a signature for '%s'", provided.name);
			return new SignatureRetrieval(RetrievalResult.NOT_REGISTERED, null, null);
		} else if (candidates.size() == 1) {
			Skript.debug("Matched signature for '%s': %s",
				provided.name, ns.signatures.get(candidates.stream().findAny().orElse(null)));
			return new SignatureRetrieval(RetrievalResult.EXACT,
				ns.signatures.get(candidates.stream().findAny().orElse(null)),
				null);
		} else {
			String options = candidates.stream().map(Record::toString).collect(Collectors.joining(", "));
			Skript.debug("Failed to match an exact signature for '%s'", provided.name);
			Skript.debug("Identifier: %s", provided);
			Skript.debug("Options: %s", options);
			return new SignatureRetrieval(RetrievalResult.AMBIGUOUS,
				null,
				candidates.stream()
					.map(FunctionIdentifier::args)
					.toArray(Class<?>[][]::new));
		}
	}

	/**
	 * Returns a list of candidates for the provided function identifier.
	 *
	 * @param provided The provided function.
	 * @param existing The existing functions with the same name.
	 * @return A list of candidates for the provided function.
	 */
	private static @NotNull Set<FunctionIdentifier> candidates(
		@NotNull FunctionIdentifier provided,
		Set<FunctionIdentifier> existing
	) {
		Set<FunctionIdentifier> candidates = new HashSet<>();

		candidates:
		for (FunctionIdentifier candidate : existing) {
			// if the provided name does not match the candidate name, skip
			if (!candidate.name.equals(provided.name)) {
				continue;
			}

			// if we have no provided arguments, we can match any function
			// we can skip the rest of the checks
			if (provided.args == null || provided.args.length == 0) {
				candidates.add(candidate);
				continue;
			}

			// if argument counts are not possible, skip
			if (provided.args.length > candidate.args.length
				|| provided.args.length < candidate.minArgCount) {
				continue;
			}

			// if the types of the provided arguments do not match the candidate arguments, skip
			for (int i = 0; i < provided.args.length; i++) {
				if (!Converters.converterExists(provided.args[i], candidate.args[i])) {
					continue candidates;
				}
			}

			candidates.add(candidate);
		}

		if (candidates.isEmpty()) {
			return Set.of();
		} else if (candidates.size() == 1 || provided.args == null || provided.args.length == 0) {
			// if there is only one candidate,
			// or we should match any function if provided.args == null || provided.args.length == 0,
			// then return without trying to convert
			return candidates;
		}

		// let overloaded(Long, Long) and overloaded(String, String) be two functions.
		// the code below allows overloaded(1, {_x}) to match Long, Long and avoid String, String,
		// and allow overloaded({_x}, 1) to match Long, Long and avoid String, String
		// despite not being an exact match in all arguments,
		// since variables have an unknown type at runtime.
		for (FunctionIdentifier candidate : new HashSet<>(candidates)) {
			int argIndex = 0;

			while (argIndex < provided.args.length) {
				if (provided.args[argIndex] == Object.class) {
					argIndex++;
					continue;
				}

				if (provided.args[argIndex] != candidate.args[argIndex]) {
					candidates.remove(candidate);
					break;
				}

				argIndex++;
			}
		}

		return candidates;
	}

	/**
	 * Removes a function's signature from the registry.
	 *
	 * @param signature The signature to remove.
	 */
	public void remove(@NotNull Signature<?> signature) {
		Preconditions.checkNotNull(signature, "signature cannot be null");

		String name = signature.getName();
		FunctionIdentifier identifier = FunctionIdentifier.of(signature);

		synchronized (namespaces) {
			for (Entry<NamespaceIdentifier, Namespace> entry : namespaces.entrySet()) {
				NamespaceIdentifier namespaceId = entry.getKey();
				Namespace namespace = entry.getValue();

				if (!namespace.identifiers.containsKey(name)) {
					continue;
				}

				for (FunctionIdentifier other : namespace.identifiers.get(name)) {
					if (!identifier.equals(other)) {
						continue;
					}

					removeUpdateMaps(namespace, other, name);
					namespaces.put(namespaceId, namespace);

					return;
				}
			}
		}
	}

	/**
	 * Updates the maps by removing the provided function identifier from the maps.
	 *
	 * @param namespace The namespace
	 * @param toRemove  The identifier to remove
	 * @param name      The name of the function
	 */
	private void removeUpdateMaps(Namespace namespace, FunctionIdentifier toRemove, String name) {
		namespace.identifiers.computeIfPresent(name, (k, set) -> {
			if (set.remove(toRemove)) {
				Skript.debug("Removed identifier '%s' from %s", toRemove, namespace);
			}
			return set.isEmpty() ? null : set;
		});
		if (namespace.functions.remove(toRemove) != null) {
			Skript.debug("Removed function '%s' from %s", toRemove, namespace);
		}
		if (namespace.signatures.remove(toRemove) != null) {
			Skript.debug("Removed signature '%s' from %s", toRemove, namespace);
		}
	}

	/**
	 * Scope of functions in namespace.
	 */
	private enum Scope {
		GLOBAL,
		LOCAL
	}

	/**
	 * A namespace for a function.
	 */
	private record NamespaceIdentifier(@NotNull Scope scope, @Nullable String name) {
	}

	/**
	 * The data a namespace contains.
	 */
	private static final class Namespace {

		/**
		 * Map for all function names to their identifiers, allowing for quicker lookup.
		 */
		private final Map<String, Set<FunctionIdentifier>> identifiers = new HashMap<>();

		/**
		 * Map for all identifier to function combinations.
		 */
		private final Map<FunctionIdentifier, Function<?>> functions = new HashMap<>();

		/**
		 * Map for all identifier to signature combinations.
		 */
		private final Map<FunctionIdentifier, Signature<?>> signatures = new HashMap<>();

	}

	/**
	 * An identifier for a function.
	 * <p>Used to differentiate between functions with the same name but different parameters.</p>
	 *
	 * @param name The name of the function.
	 * @param args The arguments of the function.
	 */
	record FunctionIdentifier(@NotNull String name, boolean local, int minArgCount, Class<?>... args) {

		/**
		 * Returns the identifier for the given arguments.
		 *
		 * @param name The name of the function.
		 * @param args The types of the arguments.
		 * @return The identifier for the signature.
		 */
		static FunctionIdentifier of(@NotNull String name, boolean local, Class<?>... args) {
			Preconditions.checkNotNull(name, "name cannot be null");

			if (args == null) {
				return new FunctionIdentifier(name, local, 0);
			}
			return new FunctionIdentifier(name, local, args.length, args);
		}

		/**
		 * Returns the identifier for the given signature.
		 *
		 * @param signature The signature to get the identifier for.
		 * @return The identifier for the signature.
		 */
		static FunctionIdentifier of(@NotNull Signature<?> signature) {
			Preconditions.checkNotNull(signature, "signature cannot be null");

			Parameter<?>[] signatureParams = signature.parameters;
			Class<?>[] parameters = new Class[signatureParams.length];

			int optionalArgs = 0;
			for (int i = 0; i < signatureParams.length; i++) {
				Parameter<?> param = signatureParams[i];
				if (param.def != null) {
					optionalArgs++;
				}

				Class<?> type = param.getType().getC();
				if (param.isSingleValue()) {
					parameters[i] = type;
				} else {
					parameters[i] = type.arrayType();
				}
			}

			return new FunctionIdentifier(signature.getName(), signature.isLocal(),
				parameters.length - optionalArgs, parameters);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, local, Arrays.hashCode(args));
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FunctionIdentifier other)) {
				return false;
			}

			if (!name.equals(other.name)) {
				return false;
			}

			if (args.length != other.args.length) {
				return false;
			}

			if (local != other.local) {
				return false;
			}

			for (int i = 0; i < args.length; i++) {
				if (args[i] != other.args[i]) {
					return false;
				}
			}

			return true;
		}

		@Override
		public String toString() {
			return "FunctionIdentifier{name='%s', local=%s, minArgCount=%d, args=[%s]}".formatted(
				name, local, minArgCount,
				Arrays.stream(args).map(Class::getSimpleName).collect(Collectors.joining(", ")));
		}
	}

}
