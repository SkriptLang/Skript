package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.function.FunctionRegistry.Retrieval;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.FunctionParser;
import org.skriptlang.skript.common.function.FunctionReference;
import org.skriptlang.skript.common.function.Parameter;
import org.skriptlang.skript.lang.script.Script;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Static methods to work with functions.
 */
public abstract class Functions {

	private static final String INVALID_FUNCTION_DEFINITION =
		"Invalid function definition. Please check for " +
			"typos and make sure that the function's name " +
			"only contains letters and underscores. " +
			"Refer to the documentation for more information.";

	private Functions() {}

	public static @Nullable ScriptFunction<?> currentFunction = null;

	/**
	 * Function namespaces.
	 */
	private static final Map<Namespace.Key, Namespace> namespaces = new HashMap<>();

	/**
	 * Namespace of Java functions.
	 */
	private static final Namespace javaNamespace;

	static {
		javaNamespace = new Namespace();
		namespaces.put(new Namespace.Key(Namespace.Origin.JAVA, "unknown"), javaNamespace);
	}

	/**
	 * Namespaces of functions that are globally available.
	 */
	private static final Map<String, Namespace> globalFunctions = new HashMap<>();

	public static boolean callFunctionEvents = false;

	/**
	 * Registers a {@link DefaultFunction}.
	 *
	 * @param function The function to register.
	 * @return The registered function.
	 */
	public static DefaultFunction<?> register(DefaultFunction<?> function) {
		Skript.checkAcceptRegistrations();

		String name = function.name();
		if (!name.matches(functionNamePattern))
			throw new SkriptAPIException("Invalid function name '%s'".formatted(name));

		javaNamespace.addSignature((Signature<?>) function.signature());
		javaNamespace.addFunction((Function<?>) function);
		globalFunctions.put(function.name(), javaNamespace);

		FunctionRegistry.getRegistry().register(null, (Function<?>) function);

		return function;
	}

	/**
	 * @deprecated Use {@link #register(DefaultFunction)} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.13")
	public static JavaFunction<?> registerFunction(JavaFunction<?> function) {
		Skript.checkAcceptRegistrations();
		String name = function.getName();
		if (!name.matches(functionNamePattern))
			throw new SkriptAPIException("Invalid function name '" + name + "'");
		javaNamespace.addSignature(function.getSignature());
		javaNamespace.addFunction(function);
		globalFunctions.put(function.getName(), javaNamespace);

		FunctionRegistry.getRegistry().register(null, function);

		return function;
	}

	public final static String functionNamePattern = "[\\p{IsAlphabetic}_][\\p{IsAlphabetic}\\d_]*";

	/**
	 * Loads a script function from given node.
	 * @param script The script the function is declared in
	 * @param node Section node.
	 * @param signature The signature of the function. Use {@link Functions#parseSignature(String, String, String, String, boolean)}
	 * to get a new signature instance and {@link Functions#registerSignature(Signature)} to register the signature
	 * @return Script function, or null if something went wrong.
	 */
	public static @Nullable Function<?> loadFunction(Script script, SectionNode node, Signature<?> signature) {
		String name = signature.getName();
		Namespace namespace = getScriptNamespace(script.getConfig().getFileName());
		if (namespace == null) {
			namespace = globalFunctions.get(name);
			if (namespace == null)
				return null; // Probably duplicate signature; reported before
		}

		if (Skript.debug() || node.debug()) {
			Skript.debug(signature.toString());
		}

		Function<?> function;
		try {
			function = new ScriptFunction<>(signature, node);
		} catch (SkriptAPIException ex) {
			//noinspection ThrowableNotThrown
			Skript.exception(ex, "Error while trying to load a function");

			// avoid getting a "function is already registered" error when the function implementation is not known yet
			Functions.unregisterFunction(signature);
			return null;
		}

		if (namespace.getFunction(signature.getName()) == null) {
			namespace.addFunction(function);
		}

		if (function.getSignature().isLocal()) {
			FunctionRegistry.getRegistry().register(script.getConfig().getFileName(), function);
		} else {
			FunctionRegistry.getRegistry().register(null, function);
		}

		return function;
	}

	/**
	 * @deprecated Use {@link FunctionParser#parse(String, String, String, String, boolean)} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.14")
	public static @Nullable Signature<?> parseSignature(String script, String name, String args, @Nullable String returnType, boolean local) {
		return FunctionParser.parse(script, name, args, returnType, local);
	}

	/**
	 * Registers the signature.
	 * @param signature The signature to register.
	 * @return Signature of function, or null if something went wrong.
	 * @see Functions#parseSignature(String, String, String, String, boolean)
	 */
	public static @Nullable Signature<?> registerSignature(Signature<?> signature) {
		Retrieval<Signature<?>> existing;
		Parameter<?>[] parameters = signature.parameters().all();

		if (parameters.length == 1 && !parameters[0].isSingle()) {
			existing = FunctionRegistry.getRegistry().getExactSignature(signature.namespace(), signature.getName(), parameters[0].type().arrayType());
		} else {
			Class<?>[] types = new Class<?>[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				types[i] = parameters[i].type();
			}

			existing = FunctionRegistry.getRegistry().getExactSignature(signature.namespace(), signature.getName(), types);
		}

		// if this function has already been registered, only allow it if one function is local and one is global.
		// if both are global or both are local, disallow.
		if (existing.result() == RetrievalResult.EXACT && existing.retrieved().isLocal() == signature.isLocal()) {
			StringBuilder error = new StringBuilder();

			if (existing.retrieved().isLocal()) {
				error.append("Local function ");
			} else {
				error.append("Function ");
			}
			error.append("'%s' with the same argument types already exists".formatted(signature.getName()));
			if (existing.retrieved().namespace() != null) {
				error.append(" in script '%s'.".formatted(existing.retrieved().namespace()));
			} else {
				error.append(".");
			}

			Skript.error(error.toString());

			return null;
		}

		Namespace.Key namespaceKey = new Namespace.Key(Namespace.Origin.SCRIPT, signature.namespace());
		Namespace namespace = namespaces.computeIfAbsent(namespaceKey, k -> new Namespace());
		if (namespace.getSignature(signature.getName()) == null) {
			namespace.addSignature(signature);
		}
		if (!signature.isLocal())
			globalFunctions.put(signature.getName(), namespace);

		if (signature.isLocal()) {
			FunctionRegistry.getRegistry().register(signature.namespace(), signature);
		} else {
			FunctionRegistry.getRegistry().register(null, signature);
		}

		Skript.debug("Registered function signature: " + signature.getName());

		return signature;
	}

	/**
	 * Gets a function, if it exists. Note that even if function exists in scripts,
	 * it might not have been parsed yet. If you want to check for existence,
	 * then use {@link #getGlobalSignature(String)}.
	 *
	 * @deprecated in favour of {@link #getGlobalFunction(String)} for proper name.
	 * @param name Name of function.
	 * @return Function, or null if it does not exist.
	 */
	@Deprecated(since = "2.7.0", forRemoval = true)
	public static @Nullable Function<?> getFunction(String name) {
		return getGlobalFunction(name);
	}

	/**
	 * Gets a function, if it exists. Note that even if function exists in scripts,
	 * it might not have been parsed yet. If you want to check for existence,
	 * then use {@link #getGlobalSignature(String)}.
	 *
	 * @param name Name of function.
	 * @return Function, or null if it does not exist.
	 */
	public static @Nullable Function<?> getGlobalFunction(String name) {
		Namespace namespace = globalFunctions.get(name);
		if (namespace == null)
			return null;
		return namespace.getFunction(name, false);
	}

	/**
	 * Gets a function, if it exists. Note that even if function exists in scripts,
	 * it might not have been parsed yet. If you want to check for existence,
	 * then use {@link #getLocalSignature(String, String)}.
	 *
	 * @param name Name of function.
	 * @param script The script where the function is declared in. Used to get local functions.
	 * @return Function, or null if it does not exist.
	 */
	public static @Nullable Function<?> getLocalFunction(String name, String script) {
		Namespace namespace = null;
		Function<?> function = null;
		namespace = getScriptNamespace(script);
		if (namespace != null)
			function = namespace.getFunction(name);
		return function;
	}

	/**
	 * Gets a local function, if it doesn't exist it'll fall back to a global function,
	 * if it exists. Note that even if function exists in scripts,
	 * it might not have been parsed yet. If you want to check for existence,
	 * then use {@link #getSignature(String, String)}.
	 *
	 * @param name Name of function.
	 * @param script The script where the function is declared in. Used to get local functions.
	 * @return Function, or null if it does not exist.
	 */
	public static @Nullable Function<?> getFunction(String name, @Nullable String script) {
		if (script == null)
			return getGlobalFunction(name);
		Function<?> function = getLocalFunction(name, script);
		if (function == null)
			return getGlobalFunction(name);
		return function;
	}

	/**
	 * Gets a signature of function with given name.
	 *
	 * @deprecated in favour of {@link #getGlobalSignature(String)} for proper name.
	 * @param name Name of function.
	 * @return Signature, or null if function does not exist.
	 */
	@Deprecated(since = "2.7.0", forRemoval = true)
	public static @Nullable Signature<?> getSignature(String name) {
		return getGlobalSignature(name);
	}

	/**
	 * Gets a signature of function with given name.
	 *
	 * @param name Name of function.
	 * @return Signature, or null if function does not exist.
	 */
	public static @Nullable Signature<?> getGlobalSignature(String name) {
		Namespace namespace = globalFunctions.get(name);
		if (namespace == null)
			return null;
		return namespace.getSignature(name, false);
	}

	/**
	 * Gets a signature of function with given name.
	 *
	 * @param name Name of function.
	 * @param script The script where the function is declared in. Used to get local functions.
	 * @return Signature, or null if function does not exist.
	 */
	public static @Nullable Signature<?> getLocalSignature(String name, String script) {
		Namespace namespace = null;
		Signature<?> signature = null;
		namespace = getScriptNamespace(script);
		if (namespace != null)
			signature = namespace.getSignature(name);
		return signature;
	}

	/**
	 * Gets a signature of local function with the given name, if no signature was found,
	 * it will fall back to a global function.
	 *
	 * @param name Name of function.
	 * @param script The script where the function is declared in. Used to get local functions.
	 * @return Signature, or null if function does not exist.
	 */
	public static @Nullable Signature<?> getSignature(String name, @Nullable String script) {
		if (script == null)
			return getGlobalSignature(name);
		Signature<?> signature = getLocalSignature(name, script);
		if (signature == null)
			return getGlobalSignature(name);
		return signature;
	}

	public static @Nullable Namespace getScriptNamespace(String script) {
		return namespaces.get(new Namespace.Key(Namespace.Origin.SCRIPT, script));
	}

	private final static Collection<FunctionReference<?>> toValidate = new ArrayList<>();

	@Deprecated(since = "2.7.0", forRemoval = true)
	public static int clearFunctions(String script) {
		// Get and remove function namespace of script
		Namespace namespace = namespaces.remove(new Namespace.Key(Namespace.Origin.SCRIPT, script));
		if (namespace == null) { // No functions defined
			return 0;
		}

		// Remove references to this namespace from global functions
		globalFunctions.values().removeIf(loopedNamespaced -> loopedNamespaced == namespace);

		// Queue references to signatures we have for revalidation
		// Can't validate here, because other scripts might be loaded soon
		for (Signature<?> sign : namespace.getSignatures()) {
			for (FunctionReference<?> ref : sign.calls()) {
				if (!script.equals(ref.namespace())) {
					toValidate.add(ref);
				}
			}
		}
		return namespace.getSignatures().size();
	}

	public static void unregisterFunction(Signature<?> signature) {
		FunctionRegistry.getRegistry().remove(signature);

		Iterator<Namespace> namespaceIterator = namespaces.values().iterator();
		while (namespaceIterator.hasNext()) {
			Namespace namespace = namespaceIterator.next();
			if (namespace.removeSignature(signature)) {
				if (!signature.isLocal())
					globalFunctions.remove(signature.getName());

				// remove the namespace if it is empty
				if (namespace.getSignatures().isEmpty())
					namespaceIterator.remove();

				break;
			}
		}

		for (FunctionReference<?> ref : signature.calls()) {
			if (signature.namespace() != null && !signature.namespace().equals(ref.namespace()))
				toValidate.add(ref);
		}
	}

	public static void validateFunctions() {
		for (FunctionReference<?> c : toValidate)
			c.validate();
		toValidate.clear();
	}

	/**
	 * Clears all function calls and removes script functions.
	 */
	@Deprecated(since = "2.7.0", forRemoval = true)
	public static void clearFunctions() {
		// Keep Java functions, remove everything else
		globalFunctions.values().removeIf(namespace -> namespace != javaNamespace);
		namespaces.clear();

		assert toValidate.isEmpty() : toValidate;
		toValidate.clear();
	}

	/**
	 * @deprecated Use {@link #getFunctions()} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.13")
	public static Collection<JavaFunction<?>> getJavaFunctions() {
		// We know there are only Java functions in that namespace
		return javaNamespace.getFunctions().stream()
				.filter(it -> it instanceof JavaFunction<?>)
				.map(it -> (JavaFunction<?>) it)
				.collect(Collectors.toSet());
	}

	/**
	 * Returns all functions registered using Java.
	 *
	 * @return All {@link JavaFunction} or {@link DefaultFunction} functions.
	 */
	public static Collection<Function<?>> getFunctions() {
		return javaNamespace.getFunctions();
	}

	/**
	 * Normally, function calls do not cause actual Bukkit events to be
	 * called. If an addon requires such functionality, it should call this
	 * method. After doing so, the events will be called. Calling this method
	 * many times will not cause any additional changes.
	 * <p>
	 * Note that calling events is not free; performance might vary
	 * once you have enabled that.
	 *
	 * @param addon Addon instance.
	 */
	@SuppressWarnings({"null", "unused"})
	public static void enableFunctionEvents(SkriptAddon addon) {
		if (addon == null) {
			throw new SkriptAPIException("enabling function events requires addon instance");
		}

		callFunctionEvents = true;
	}
}
