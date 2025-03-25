package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptAPIException;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

class FunctionRegistry {

	private FunctionRegistry() {
		throw new UnsupportedOperationException("Cannot instantiate utility class");
	}

	/**
	 * The pattern for a valid function name.
	 * Functions must start with a letter and can only contain letters, numbers, and underscores.
	 */
	final static String FUNCTION_NAME_PATTERN = "\\p{IsAlphabetic}[\\p{IsAlphabetic}\\d_]*";

	/**
	 * The namespace for functions registered using Java.
	 */
	private static final Namespace GLOBAL_NAMESPACE = new Namespace(Scope.GLOBAL, null);

	/**
	 * Map for all function names to their identifiers, allowing for quicker lookup.
	 */
	private static final Map<Namespace, Map<String, Set<FunctionIdentifier>>> identifiers = new HashMap<>();

	/**
	 * Map for all identifier to function combinations, belonging to a namespace.
	 */
	private static final Map<Namespace, Map<FunctionIdentifier, Function<?>>> functions = new HashMap<>();

	/**
	 * Map for all identifier to signature combinations, belonging to a namespace.
	 */
	private static final Map<Namespace, Map<FunctionIdentifier, Signature<?>>> signatures = new HashMap<>();

	public static void registerSignature(@Nullable String script, @NotNull Signature<?> signature) {
		Preconditions.checkNotNull(signature, "signature is null");

		Namespace namespace = GLOBAL_NAMESPACE;
		if (script != null) {
			namespace = new Namespace(signature.isLocal() ? Scope.LOCAL : Scope.GLOBAL, script);
		}

		FunctionIdentifier identifier = FunctionIdentifier.of(signature);

		Map<String, Set<FunctionIdentifier>> javaIdentifiers = identifiers.getOrDefault(namespace, new HashMap<>());
		Set<FunctionIdentifier> identifiersWithName = javaIdentifiers.getOrDefault(identifier.name, new HashSet<>());
		boolean exists = identifiersWithName.add(identifier);
		if (!exists) {
			alreadyRegisteredError(identifier.name, identifier);
		}
		javaIdentifiers.put(identifier.name, identifiersWithName);
		identifiers.put(namespace, javaIdentifiers);

		Map<FunctionIdentifier, Signature<?>> orDefault = signatures.getOrDefault(namespace, new HashMap<>());
		orDefault.put(identifier, signature);
		signatures.put(namespace, orDefault);
	}

	/**
	 * Registers a function.
	 *
	 * @param function The function to register.
	 * @throws SkriptAPIException if the function name is invalid or if
	 *                            a function with the same name and parameters is already registered
	 *                            in this namespace.
	 */
	public static void registerFunction(@NotNull Function<?> function) {
		registerFunction(null, function);
	}

	/**
	 * Registers a function.
	 *
	 * @param script The script to register the function in.
	 * @param function The function to register.
	 * @throws SkriptAPIException if the function name is invalid or if
	 *                            a function with the same name and parameters is already registered
	 *                            in this namespace.
	 */
	public static void registerFunction(@Nullable String script, @NotNull Function<?> function) {
		Preconditions.checkNotNull(function, "function is null");

		String name = function.getName();
		if (!name.matches(FUNCTION_NAME_PATTERN)) {
			throw new SkriptAPIException("Invalid function name '" + name + "'");
		}
		Namespace namespace = GLOBAL_NAMESPACE;
		if (script != null) {
			namespace = new Namespace(function.getSignature().isLocal() ? Scope.LOCAL : Scope.GLOBAL, script);
		}

		FunctionIdentifier identifier = FunctionIdentifier.of(function.getSignature());
		if (!signatureExists(namespace, identifier)) {
			registerSignature(script, function.getSignature());
		}

		Map<FunctionIdentifier, Function<?>> identifierToFunction = functions.getOrDefault(namespace, new HashMap<>());
		Function<?> existing = identifierToFunction.put(identifier, function);
		if (existing != null) {
			alreadyRegisteredError(name, identifier);
		}
		functions.put(namespace, identifierToFunction);
	}

	private static void alreadyRegisteredError(String name, FunctionIdentifier identifier) {
		throw new SkriptAPIException("Function '%s' with parameters %s is already registered"
			.formatted(name, Arrays.toString(Arrays.stream(identifier.args).map(Class::getSimpleName).toArray())));
	}

	/**
	 * Checks if a function with the given name and arguments exists in the given script.
	 * If no local function is found, checks for global functions.
	 *
	 * @param script The script to check in.
	 * @param name The name of the function.
	 * @param args The types of the arguments of the function.
	 * @return True if a function with the given name and argument types exists in the script or globally, false otherwise.
	 */
	public static boolean signatureExists(@Nullable String script, @NotNull String name, Class<?>... args) {
		if (script == null) {
			return signatureExists(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, args));
		}

		boolean local = signatureExists(new Namespace(Scope.LOCAL, script.toLowerCase()), FunctionIdentifier.of(name, args));
		if (!local) {
			return signatureExists(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, args));
		}
		return true;
	}

	/**
	 * Checks if a function with the given name and arguments exists in the namespace.
	 *
	 * @param namespace The namespace to check in.
	 * @param identifier The identifier of the function.
	 * @return True if a function with the given name and arguments exists in the namespace, false otherwise.
	 */
	private static boolean signatureExists(@NotNull Namespace namespace, @NotNull FunctionIdentifier identifier) {
		Preconditions.checkNotNull(namespace, "namespace is null");
		Preconditions.checkNotNull(identifier, "identifier is null");

		Map<String, Set<FunctionIdentifier>> javaIdentifiers = identifiers.getOrDefault(namespace, new HashMap<>());

		if (!javaIdentifiers.containsKey(identifier.name)) {
			return false;
		}

		for (FunctionIdentifier other : javaIdentifiers.get(identifier.name)) {
			if (Arrays.equals(identifier.args, other.args)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets a function from a script. If no local function is found, checks for global functions.
	 *
	 * @param script The script to get the function from.
	 * @param name The name of the function.
	 * @param args The types of the arguments of the function.
	 * @return The function with the given name and argument types, or null if no such function exists.
	 */
	public static Function<?> function(@Nullable String script, @NotNull String name, Class<?>... args) {
		if (script == null) {
			return function(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, args));
		}

		Function<?> function = function(new Namespace(Scope.LOCAL, script), FunctionIdentifier.of(name, args));
		if (function == null) {
			return function(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, args));
		}
		return function;
	}

	/**
	 * Gets a function from a namespace.
	 * <p>
	 * To match the arguments, we check if a converter between the candidate type and the provided type exists
	 * for each argument in the function signature.
	 * </p>
	 *
	 * @param namespace The namespace to get the function from.
	 * @param provided The provided of the function.
	 * @return The function with the given name and argument types, or null if no such function exists.
	 */
	private static Function<?> function(@NotNull Namespace namespace, @NotNull FunctionIdentifier provided) {
		Preconditions.checkNotNull(namespace, "namespace is null");
		Preconditions.checkNotNull(provided, "provided is null");

		Map<FunctionIdentifier, Function<?>> identifierFunctionMap = functions.get(namespace);
		Map<String, Set<FunctionIdentifier>> namespaceIdentifiers = identifiers.get(namespace);
		if (identifierFunctionMap == null || namespaceIdentifiers == null) {
			return null;
		}

		Set<FunctionIdentifier> existing = namespaceIdentifiers.get(provided.name);
		if (existing == null) {
			return null;
		}

		if (existing.size() == 1) {
			FunctionIdentifier identifier = existing.stream().findAny().orElse(null);
			return identifierFunctionMap.get(identifier);
		}

		List<FunctionIdentifier> candidates = candidates(provided, existing);
		if (candidates.isEmpty()) {
			return null;
		} else if (candidates.size() == 1) {
			return identifierFunctionMap.get(candidates.get(0));
		} else {
			throw new SkriptAPIException("Ambiguous function call: " + provided.name);
		}
	}

	/**
	 * Returns a list of candidates for the provided function.
	 *
	 * @param provided The provided function.
	 * @param existing The existing functions with the same name.
	 * @return A list of candidates for the provided function.
	 */
	private static @NotNull List<FunctionIdentifier> candidates(@NotNull FunctionIdentifier provided,
																Set<FunctionIdentifier> existing) {
		List<FunctionIdentifier> candidates = new ArrayList<>();
		for (FunctionIdentifier candidate : existing) {
			if (!candidate.name.equals(provided.name)) {
				continue;
			}

			if (provided.args.length > candidate.args.length
				|| provided.args.length < candidate.minArgCount) {
				continue;
			}

//			When runtime types are added, uncomment this for function overloading between types :)
//			for (int i = 0; i < provided.args.length; i++) {
//				if (!Converters.converterExists(provided.args[i], candidate.args[i])) {
//					continue candidates;
//				}
//			}

			candidates.add(candidate);
		}
		return candidates;
	}

	public static Signature<?> signature(@Nullable String script, @NotNull String name, Class<?>... args) {
		if (script == null) {
			return signature(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, args));
		}

		Signature<?> signature = signature(new Namespace(Scope.LOCAL, script), FunctionIdentifier.of(name, args));
		if (signature == null) {
			return signature(GLOBAL_NAMESPACE, FunctionIdentifier.of(name, args));
		}
		return signature;
	}

	private static Signature<?> signature(@NotNull Namespace namespace, @NotNull FunctionIdentifier identifier) {
		Preconditions.checkNotNull(namespace, "namespace is null");
		Preconditions.checkNotNull(identifier, "identifier is null");

		Map<String, Set<FunctionIdentifier>> javaIdentifiers = identifiers.getOrDefault(namespace, new HashMap<>());

		if (!javaIdentifiers.containsKey(identifier.name)) {
//			System.out.println(9);
			return null;
		}

		List<FunctionIdentifier> candidates = candidates(identifier, javaIdentifiers.get(identifier.name));
		if (candidates.isEmpty()) {
//			System.out.println(10);
			return null;
		} else if (candidates.size() == 1) {
//			System.out.println("11 " + signatures.get(namespace).get(candidates.get(0)));
			return signatures.get(namespace).get(candidates.get(0));
		} else {
			String options = candidates.stream().map(Record::toString).collect(Collectors.joining(", "));
			System.out.println(identifier);
			System.out.println(options);
			throw new SkriptAPIException("Ambiguous signature call for '%s'".formatted(identifier.name));
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
	 *
	 * @param scope
	 * @param name
	 */
	private record Namespace(@NotNull Scope scope, @Nullable String name) {

	}

	/**
	 * An identifier for a function.
	 * <p>Used to differentiate between functions with the same name but different parameters.</p>
	 *
	 * @param name The name of the function.
	 * @param args The arguments of the function.
	 */
	private record FunctionIdentifier(@NotNull String name, boolean local, int minArgCount, Class<?>... args) {

		/**
		 * Returns the identifier for the given arguments.
		 *
		 * @param name The name of the function.
		 * @param args The types of the arguments.
		 * @return The identifier for the signature.
		 */
		private static FunctionIdentifier of(@NotNull String name, Class<?>... args) {
			Preconditions.checkNotNull(name, "name is null");

			if (args == null) {
				return new FunctionIdentifier(name, false, 0);
			}
			return new FunctionIdentifier(name, false, args.length, args);
		}

		/**
		 * Returns the identifier for the given signature.
		 *
		 * @param signature The signature to get the identifier for.
		 * @return The identifier for the signature.
		 */
		private static FunctionIdentifier of(@NotNull Signature<?> signature) {
			Preconditions.checkNotNull(signature, "signature is null");

			Parameter<?>[] signatureParams = signature.parameters;
			Class<?>[] parameters = new Class[signatureParams.length];

			int optionalArgs = 0;
			for (int i = 0; i < signatureParams.length; i++) {
				Parameter<?> param = signatureParams[i];
				if (param.def != null) {
					optionalArgs++;
				}

				if (param.isSingleValue()) {
					parameters[i] = param.getType().getC();
				} else {
					parameters[i] = param.getType().getC().arrayType(); // for functions with Object[] arguments
				}
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
				if (args[i] != other.args[i])
					return false;
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
