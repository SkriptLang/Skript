package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.util.Utils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.common.function.Parameter;
import org.skriptlang.skript.common.function.Parameter.Modifier;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.Registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A registry for functions.
 */
@ApiStatus.Internal
public final class FunctionRegistry implements Registry<Function<?>> {

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
	final static Pattern FUNCTION_NAME_PATTERN = Pattern.compile(Functions.functionNamePattern);

	/**
	 * The namespace for registered global functions.
	 */
	private final NamespaceIdentifier GLOBAL_NAMESPACE = new NamespaceIdentifier(null);

	/**
	 * All registered namespaces.
	 */
	private final Map<NamespaceIdentifier, Namespace> namespaces = new ConcurrentHashMap<>();

	@Override
	public @Unmodifiable @NotNull Collection<Function<?>> elements() {
		Set<Function<?>> functions = new HashSet<>();

		for (Namespace namespace : namespaces.values()) {
			functions.addAll(namespace.functions.values());
		}

		return Collections.unmodifiableSet(functions);
	}

	/**
	 * Registers a signature.
	 * <p>
	 * Attempting to register a local signature in the global namespace, or a global signature in
	 * a local namespace, will throw an {@link IllegalArgumentException}.
	 * If {@code namespace} is null, will register this signature globally,
	 * only if the signature is global.
	 * </p>
	 *
	 * @param namespace The namespace to register the signature in.
	 *                  Usually represents the path of the script this signature is registered in.
	 * @param signature The signature to register.
	 * @throws SkriptAPIException       if a signature with the same name and parameters is already registered
	 *                                  in this namespace.
	 * @throws IllegalArgumentException if the signature is global and namespace is not null, or
	 *                                  if the signature is local and namespace is null.
	 */
	public void register(@Nullable String namespace, @NotNull Signature<?> signature) {
		Preconditions.checkNotNull(signature, "signature cannot be null");
		if (signature.isLocal() && namespace == null) {
			throw new IllegalArgumentException("Cannot register a local signature in the global namespace");
		}
		if (!signature.isLocal() && namespace != null) {
			throw new IllegalArgumentException("Cannot register a global signature in a local namespace");
		}

		Skript.debug("Registering signature '%s'", signature.getName());

		// namespace
		NamespaceIdentifier namespaceId;
		if (namespace != null) {
			namespaceId = new NamespaceIdentifier(namespace);
		} else {
			namespaceId = GLOBAL_NAMESPACE;
		}

		Namespace ns = namespaces.computeIfAbsent(namespaceId, n -> new Namespace());
		FunctionIdentifier identifier = FunctionIdentifier.of(signature);

		// register
		// since we are getting a set and then updating it,
		// avoid race conditions by ensuring only one thread can access this namespace for this operation
		synchronized (ns) {
			Set<FunctionIdentifier> identifiersWithName = ns.identifiers.computeIfAbsent(identifier.name, s -> new HashSet<>());
			boolean exists = identifiersWithName.add(identifier);
			if (!exists) {
				alreadyRegisteredError(signature.getName(), identifier, namespaceId);
			}
		}

		Signature<?> existing = ns.signatures.putIfAbsent(identifier, signature);
		if (existing != null) {
			alreadyRegisteredError(signature.getName(), identifier, namespaceId);
		}
	}

	/**
	 * Registers a function.
	 * <p>
	 * Attempting to register a local function in the global namespace, or a global function in
	 * a local namespace, will throw an {@link IllegalArgumentException}.
	 * If {@code namespace} is null, will register this function globally,
	 * only if the function is global.
	 * </p>
	 *
	 * @param namespace The namespace to register the function in.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param function  The function to register.
	 * @throws SkriptAPIException       if the function name is invalid or if
	 *                                  a function with the same name and parameters is already registered
	 *                                  in this namespace.
	 * @throws IllegalArgumentException if the function is global and namespace is not null, or
	 *                                  if the function is local and namespace is null.
	 */
	public void register(@Nullable String namespace, @NotNull Function<?> function) {
		Preconditions.checkNotNull(function, "function cannot be null");
		if (function.getSignature().isLocal() && namespace == null) {
			throw new IllegalArgumentException("Cannot register a local function in the global namespace");
		}
		if (!function.getSignature().isLocal() && namespace != null) {
			throw new IllegalArgumentException("Cannot register a global function in a local namespace");
		}
		Skript.debug("Registering function '%s'", function.getName());

		String name = function.getName();
		if (!FUNCTION_NAME_PATTERN.matcher(name).matches()) {
			throw new SkriptAPIException("Invalid function name '" + name + "'");
		}

		// namespace
		NamespaceIdentifier namespaceId;
		if (namespace != null) {
			namespaceId = new NamespaceIdentifier(namespace);
		} else {
			namespaceId = GLOBAL_NAMESPACE;
		}

		FunctionIdentifier identifier = FunctionIdentifier.of(function.getSignature());
		if (!signatureExists(namespaceId, identifier)) {
			register(namespace, function.getSignature());
		}

		Namespace ns = namespaces.computeIfAbsent(namespaceId, n -> new Namespace());

		Function<?> existing = ns.functions.putIfAbsent(identifier, function);
		if (existing != null) {
			alreadyRegisteredError(name, identifier, namespaceId);
		}
	}

	private static void alreadyRegisteredError(String name, FunctionIdentifier identifier, NamespaceIdentifier namespace) {
		throw new SkriptAPIException("Function '%s' with parameters %s is already registered in %s"
			.formatted(name, Arrays.toString(Arrays.stream(identifier.args).map(Class::getSimpleName).toArray()),
				namespace));
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

		Namespace ns = namespaces.get(namespace);
		if (ns == null) {
			return false;
		}

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
	 * Gets a function from a script. If no local function is found, checks for global functions.
	 * If {@code namespace} is null, only global functions will be checked.
	 *
	 * @param namespace The namespace to get the function from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param name      The name of the function.
	 * @param args      The types of the arguments of the function.
	 * @return Information related to the attempt to get the specified function,
	 * stored in a {@link Retrieval} object.
	 */
	public @NotNull Retrieval<Function<?>> getFunction(
		@Nullable String namespace,
		@NotNull String name,
		@NotNull Class<?>... args
	) {
		Retrieval<Function<?>> attempt = null;
		if (namespace != null) {
			attempt = getFunction(new NamespaceIdentifier(namespace),
				FunctionIdentifier.of(name, true, args));
		}
		if (attempt == null || attempt.result() == RetrievalResult.NOT_REGISTERED) {
			attempt = getFunction(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, false, args));
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
	 * stored in a {@link Retrieval} object.
	 */
	private @NotNull Retrieval<Function<?>> getFunction(@NotNull NamespaceIdentifier namespace, @NotNull FunctionIdentifier provided) {
		Preconditions.checkNotNull(namespace, "namespace cannot be null");
		Preconditions.checkNotNull(provided, "provided cannot be null");

		Namespace ns = namespaces.getOrDefault(namespace, new Namespace());
		Set<FunctionIdentifier> existing = ns.identifiers.get(provided.name);
		if (existing == null) {
			Skript.debug("No functions named '%s' exist in the '%s' namespace", provided.name, namespace.name);
			return new Retrieval<>(RetrievalResult.NOT_REGISTERED, null, null);
		}

		Set<FunctionIdentifier> candidates = candidates(provided, existing, false);
		if (candidates.isEmpty()) {
			Skript.debug("Failed to find a function for '%s'", provided.name);
			return new Retrieval<>(RetrievalResult.NOT_REGISTERED, null, null);
		} else if (candidates.size() == 1) {
			if (Skript.debug()) {
				Skript.debug("Matched function for '%s': %s", provided.name, candidates.stream().findAny().orElse(null));
			}
			return new Retrieval<>(RetrievalResult.EXACT,
				ns.functions.get(candidates.stream().findAny().orElse(null)),
				null);
		} else {
			if (Skript.debug()) {
				String options = candidates.stream().map(Record::toString).collect(Collectors.joining(", "));
				Skript.debug("Failed to match an exact function for '%s'", provided.name);
				Skript.debug("Identifier: %s", provided);
				Skript.debug("Options: %s", options);
			}
			return new Retrieval<>(RetrievalResult.AMBIGUOUS,
				null,
				candidates.stream()
					.map(FunctionIdentifier::args)
					.toArray(Class<?>[][]::new));
		}
	}

	/**
	 * Gets the signature for a function with the given name and arguments. If no local function is found,
	 * checks for global functions. If {@code namespace} is null, only global signatures will be checked.
	 *
	 * @param namespace The namespace to get the function from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param name      The name of the function.
	 * @param args      The types of the arguments of the function.
	 * @return The signature for the function with the given name and argument types, or null if no such function exists.
	 */
	public Retrieval<Signature<?>> getSignature(
		@Nullable String namespace,
		@NotNull String name,
		@NotNull Class<?>... args
	) {
		Retrieval<Signature<?>> attempt = null;
		if (namespace != null) {
			attempt = getSignature(new NamespaceIdentifier(namespace),
				FunctionIdentifier.of(name, true, args), false);
		}
		if (attempt == null || attempt.result() == RetrievalResult.NOT_REGISTERED) {
			attempt = getSignature(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, false, args), false);
		}
		return attempt;
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
		Retrieval<Signature<?>> attempt = null;
		if (namespace != null) {
			attempt = getSignature(new NamespaceIdentifier(namespace),
				FunctionIdentifier.of(name, true, args), true);
		}

		if (attempt == null || attempt.result() == RetrievalResult.NOT_REGISTERED) {
			attempt = getSignature(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, false, args), true);
		}
		return attempt;
	}

	/**
	 * Gets every signature with the name {@code name}.
	 * This includes global functions and, if {@code namespace} is not null, functions under that namespace (if valid).
	 * @param namespace The additional namespace to obtain signatures from.
	 *                  Usually represents the path of the script this function is registered in.
	 * @param name      The name of the signature(s) to obtain.
	 * @return A list of all signatures named {@code name}.
	 */
	public @Unmodifiable @NotNull Set<Signature<?>> getSignatures(@Nullable String namespace, @NotNull String name) {
		Preconditions.checkNotNull(name, "name cannot be null");

		Map<FunctionIdentifier, Signature<?>> total = new HashMap<>();

		// obtain all local functions of "name"
		if (namespace != null) {
			Namespace local = namespaces.getOrDefault(new NamespaceIdentifier(namespace), new Namespace());

			for (FunctionIdentifier identifier : local.identifiers.getOrDefault(name, Collections.emptySet())) {
				total.putIfAbsent(identifier, local.signatures.get(identifier));
			}
		}

		// obtain all global functions of "name"
		Namespace global = namespaces.getOrDefault(GLOBAL_NAMESPACE, new Namespace());
		for (FunctionIdentifier identifier : global.identifiers.getOrDefault(name, Collections.emptySet())) {
			total.putIfAbsent(identifier, global.signatures.get(identifier));
		}

		return Set.copyOf(total.values());
	}

	/**
	 * Gets the signature for a function with the given name and arguments.
	 *
	 * @param namespace The namespace to get the function from.
	 * @param provided  The provided identifier of the function.
	 * @param exact When false, will convert arguments to different types to attempt to find a match.
	 *              When true, will not convert arguments.
	 * @return The signature for the function with the given name and argument types, or null if no such signature exists
	 * in the specified namespace.
	 */
	private Retrieval<Signature<?>> getSignature(@NotNull NamespaceIdentifier namespace, @NotNull FunctionIdentifier provided, boolean exact) {
		Preconditions.checkNotNull(namespace, "namespace cannot be null");
		Preconditions.checkNotNull(provided, "provided cannot be null");

		Namespace ns = namespaces.getOrDefault(namespace, new Namespace());
		if (!ns.identifiers.containsKey(provided.name)) {
			Skript.debug("No signatures named '%s' exist in the '%s' namespace", provided.name, namespace.name);
			return new Retrieval<>(RetrievalResult.NOT_REGISTERED, null, null);
		}

		Set<FunctionIdentifier> candidates = candidates(provided, ns.identifiers.get(provided.name), exact);
		if (candidates.isEmpty()) {
			Skript.debug("Failed to find a signature for '%s'", provided.name);
			return new Retrieval<>(RetrievalResult.NOT_REGISTERED, null, null);
		} else if (candidates.size() == 1) {
			if (Skript.debug()) {
				Skript.debug("Matched signature for '%s': %s",
					provided.name, ns.signatures.get(candidates.stream().findAny().orElse(null)));
			}
			return new Retrieval<>(RetrievalResult.EXACT,
				ns.signatures.get(candidates.stream().findAny().orElse(null)),
				null);
		} else {
			if (Skript.debug()) {
				String options = candidates.stream().map(Record::toString).collect(Collectors.joining(", "));
				Skript.debug("Failed to match an exact signature for '%s'", provided.name);
				Skript.debug("Identifier: %s", provided);
				Skript.debug("Options: %s", options);
			}
			return new Retrieval<>(RetrievalResult.AMBIGUOUS,
				null,
				candidates.stream()
					.map(FunctionIdentifier::args)
					.toArray(Class<?>[][]::new));
		}
	}

	/**
	 * Returns an unmodifiable list of candidates for the provided function identifier.
	 *
	 * @param provided The provided function.
	 * @param existing The existing functions with the same name.
	 * @param exact When false, will convert arguments to different types to attempt to find a match.
	 *              When true, will not convert arguments.
	 * @return An unmodifiable list of candidates for the provided function.
	 */
	private static @Unmodifiable @NotNull Set<FunctionIdentifier> candidates(
		@NotNull FunctionIdentifier provided,
		Set<FunctionIdentifier> existing,
		boolean exact
	) {
		Set<FunctionIdentifier> candidates = new HashSet<>();

		candidates:
		for (FunctionIdentifier candidate : existing) {
			// by this point, all candidates have matching names

			if (Arrays.stream(candidate.args).filter(Class::isArray).count() == 1
				&& candidate.args.length == 1
				&& candidate.args[0].isArray()) {
				// if a function has single list value param, check all types
				// make sure all types in the passed array are valid for the array parameter
				Class<?> arrayType = candidate.args[0].componentType();
				for (Class<?> arrayArg : provided.args) {
					arrayArg = Utils.getComponentType(arrayArg);
          
					if (!Converters.converterExists(arrayArg, arrayType)) {
						continue candidates;
					}
				}

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
				// allows single passed values to still match array type in candidate (e.g. clamp)
				Class<?> providedType = Utils.getComponentType(provided.args[i]);

				Class<?> candidateType = Utils.getComponentType(candidate.args[i]);
				Class<?> providedArg = provided.args[i];
				if (exact) {
					if (providedArg != candidateType) {
						continue candidates;
					}
				} else {
					if (!Converters.converterExists(providedType, candidateType)) {
						continue candidates;
					}
				}
			}

			candidates.add(candidate);
		}

		if (candidates.size() <= 1) {
			// if there is only one candidate, then return without trying to convert
			return Collections.unmodifiableSet(candidates);
		}

		// let overloaded(Long, Long) and overloaded(String, String) be two functions.
		// the code below allows overloaded(1, {_x}) to match Long, Long and avoid String, String,
		// and allow overloaded({_x}, 1) to match Long, Long and avoid String, String
		// despite not being an exact match in all arguments,
		// since variables have an unknown type at runtime.
		Iterator<FunctionIdentifier> iterator = candidates.iterator();
		while (iterator.hasNext()) {
			FunctionIdentifier candidate = iterator.next();
			int argIndex = 0;

			while (argIndex < provided.args.length) {
				if (provided.args[argIndex] == Object.class) {
					argIndex++;
					continue;
				}

				if (provided.args[argIndex] != candidate.args[argIndex]) {
					iterator.remove();
					break;
				}

				argIndex++;
			}
		}

		return Collections.unmodifiableSet(candidates);
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

		Namespace namespace;
		if (signature.isLocal()) {
			namespace = namespaces.get(new NamespaceIdentifier(signature.namespace()));
		} else {
			namespace = namespaces.get(GLOBAL_NAMESPACE);
		}

		if (namespace == null) {
			return;
		}

		for (FunctionIdentifier other : namespace.identifiers.getOrDefault(name, Set.of())) {
			if (!identifier.equals(other)) {
				continue;
			}

			removeUpdateMaps(namespace, other, name);
			return;
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
	 * An identifier for a function namespace.
	 */
	private record NamespaceIdentifier(@Nullable String name) {

		/**
		 * Returns whether this identifier is for local namespaces.
		 *
		 * @return Whether this identifier is for local namespaces.
		 */
		public boolean local() {
			return name == null;
		}

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
	record FunctionIdentifier(@NotNull String name, boolean local, int minArgCount,
							  @NotNull Class<?>... args) {

		/**
		 * Returns the identifier for the given arguments.
		 *
		 * @param name The name of the function.
		 * @param args The types of the arguments.
		 * @return The identifier for the signature.
		 */
		static FunctionIdentifier of(@NotNull String name, boolean local, @NotNull Class<?>... args) {
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(args, "args cannot be null");

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

			Parameter<?>[] signatureParams = signature.parameters().all();
			Class<?>[] parameters = new Class[signatureParams.length];

			int optionalArgs = 0;
			for (int i = 0; i < signatureParams.length; i++) {
				Parameter<?> param = signatureParams[i];
				if (param.hasModifier(Modifier.OPTIONAL)) {
					optionalArgs++;
				}

				parameters[i] = param.type();
			}

			return new FunctionIdentifier(signature.getName(), signature.isLocal(),
				parameters.length - optionalArgs, parameters);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, Arrays.hashCode(args));
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

			for (int i = 0; i < args.length; i++) {
				if (args[i] != other.args[i]) {
					return false;
				}
			}

			return true;
		}

		@Override
		public @NotNull String toString() {
			return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("local", local)
				.add("minArgCount", minArgCount)
				.add("args", Arrays.stream(args).map(Class::getSimpleName).collect(Collectors.joining(", ")))
				.toString();
		}

	}

}
