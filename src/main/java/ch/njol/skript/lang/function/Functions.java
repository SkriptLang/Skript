/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

	static boolean callFunctionEvents = false;

	/**
	 * Registers a function written in Java.
	 *
	 * @return The passed function
	 */
	public static JavaFunction<?> registerFunction(JavaFunction<?> function) {
		Skript.checkAcceptRegistrations();
		String name = function.getName();
		if (!name.matches(functionNamePattern))
			throw new SkriptAPIException("Invalid function name '" + name + "'");
		javaNamespace.addSignature(function.getSignature());
		javaNamespace.addFunction(function);
		globalFunctions.put(function.getName(), javaNamespace);

		return function;
	}

	public final static String functionNamePattern = "[\\p{IsAlphabetic}][\\p{IsAlphabetic}\\p{IsDigit}_]*";

	/**
	 * Loads a script function from given node.
	 * @param script The script the function is declared in
	 * @param node Section node.
	 * @param signature The signature of the function. Use {@link Functions#parseSignature(String, String, String, String, boolean)}
	 * to get a new signature instance and {@link Functions#registerSignature(Signature)} to register the signature
	 * @return Script function, or null if something went wrong.
	 */
	public static @Nullable Function<?> loadFunction(Script script, SectionNode node, Signature<?> signature) {
		String name = signature.name;
		Namespace namespace = getScriptNamespace(script.getConfig().getFileName());
		if (namespace == null) {
			namespace = globalFunctions.get(name);
			if (namespace == null)
				return null; // Probably duplicate signature; reported before
		}
		Parameter<?>[] params = signature.parameters;
		ClassInfo<?> c = signature.returnType;

		if (Skript.debug() || node.debug())
			Skript.debug((signature.local ? "local " : "") + "function " + name + "(" + StringUtils.join(params, ", ") + ")"
				+ (c != null ? " :: " + (signature.isSingle() ? c.getName().getSingular() : c.getName().getPlural()) : "") + ":");

		Function<?> f = new ScriptFunction<>(signature, node);

		// Register the function for signature
		namespace.addFunction(f);

		return f;
	}


	/**
	 * Parses the signature from the given arguments.
	 * @param script Script file name (<b>might</b> be used for some checks).
	 * @param name The name of the function.
	 * @param args The parameters of the function. See {@link Parameter#parse(String)}
	 * @param returnType The return type of the function
	 * @param local If the signature of function is local.
	 * @return Parsed signature or null if something went wrong.
	 * @see Functions#registerSignature(Signature)
	 */
	public static @Nullable Signature<?> parseSignature(String script, String name, String args, @Nullable String returnType, boolean local) {
		List<Parameter<?>> parameters = Parameter.parse(args);
		if (parameters == null)
			return null;

		// Parse return type if one exists
		ClassInfo<?> returnClass;
		boolean singleReturn;
		if (returnType == null) {
			returnClass = null;
			singleReturn = false; // Ignored, nothing is returned
		} else {
			returnClass = Classes.getClassInfoFromUserInput(returnType);
			NonNullPair<String, Boolean> p = Utils.getEnglishPlural(returnType);
			singleReturn = !p.getSecond();
			if (returnClass == null)
				returnClass = Classes.getClassInfoFromUserInput(p.getFirst());
			if (returnClass == null)
				return signError("Cannot recognise the type '" + returnType + "'");
		}
		//noinspection unchecked
		return new Signature<>(script, name, parameters.toArray(new Parameter[0]), local, (ClassInfo<Object>) returnClass, singleReturn, null);
	}

	/**
	 * Registers the signature.
	 * @param signature The signature to register.
	 * @return Signature of function, or null if something went wrong.
	 * @see Functions#parseSignature(String, String, String, String, boolean)
	 */
	public static @Nullable Signature<?> registerSignature(Signature<?> signature) {
		// Ensure there are no duplicate functions
		if (signature.local) {
			Namespace namespace = getScriptNamespace(signature.script);
			if (namespace != null && namespace.getSignature(signature.name, true) != null)
				return signError("A local function named '" + signature.name + "' already exists in the script");
		} else {
			if (globalFunctions.containsKey(signature.name)) {
				Namespace namespace = globalFunctions.get(signature.name);
				if (namespace == javaNamespace) { // Special messages for built-in functions
					return signError("Function name '" + signature.name + "' is reserved by Skript");
				} else {
					Signature<?> sign = namespace.getSignature(signature.name, false);
					assert sign != null : "globalFunctions points to a wrong namespace";
					return signError("A global function named '" + signature.name + "' already exists in script '" + sign.script + "'");
				}
			}
		}

		Namespace.Key namespaceKey = new Namespace.Key(Namespace.Origin.SCRIPT, signature.script);
		Namespace namespace = namespaces.computeIfAbsent(namespaceKey, k -> new Namespace());
		namespace.addSignature(signature);
		if (!signature.local)
			globalFunctions.put(signature.name, namespace);

		Skript.debug("Registered function signature: " + signature.name);

		return signature;
	}

	/**
	 * Creates an error and returns Function null.
	 * @param error Error message.
	 * @return Null.
	 */
	private static @Nullable Function<?> error(String error) {
		Skript.error(error);
		return null;
	}

	/**
	 * Creates an error and returns Signature null.
	 * @param error Error message.
	 * @return Null.
	 */
	private static @Nullable Signature<?> signError(String error) {
		Skript.error(error);
		return null;
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
	@Deprecated
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
	@Deprecated
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

	/**
	 * Remember to call {@link #validateFunctions()} after calling this
	 *
	 * @return How many functions were removed
	 */
	@Deprecated
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
			for (FunctionReference<?> ref : sign.calls) {
				if (!script.equals(ref.script)) {
					toValidate.add(ref);
				}
			}
		}
		return namespace.getSignatures().size();
	}

	public static void unregisterFunction(Signature<?> signature) {
		Iterator<Namespace> namespaceIterator = namespaces.values().iterator();
		while (namespaceIterator.hasNext()) {
			Namespace namespace = namespaceIterator.next();
			if (namespace.removeSignature(signature)) {
				if (!signature.local)
					globalFunctions.remove(signature.getName());

				// remove the namespace if it is empty
				if (namespace.getSignatures().isEmpty())
					namespaceIterator.remove();

				break;
			}
		}

		for (FunctionReference<?> ref : signature.calls) {
			if (!signature.script.equals(ref.script))
				toValidate.add(ref);
		}
	}

	public static void validateFunctions() {
		for (FunctionReference<?> c : toValidate)
			c.validateFunction(false);
		toValidate.clear();
	}

	/**
	 * Clears all function calls and removes script functions.
	 */
	@Deprecated
	public static void clearFunctions() {
		// Keep Java functions, remove everything else
		globalFunctions.values().removeIf(namespace -> namespace != javaNamespace);
		namespaces.clear();

		assert toValidate.isEmpty() : toValidate;
		toValidate.clear();
	}

	@SuppressWarnings({"unchecked"})
	public static Collection<JavaFunction<?>> getJavaFunctions() {
		// We know there are only Java functions in that namespace
		return (Collection<JavaFunction<?>>) (Object) javaNamespace.getFunctions();
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
