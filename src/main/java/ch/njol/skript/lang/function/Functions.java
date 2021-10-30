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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;

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
	
	@Nullable
	public static ScriptFunction<?> currentFunction = null;
	
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
	
	@SuppressWarnings("null")
	private final static Pattern functionPattern = Pattern.compile("function (" + functionNamePattern + ")\\((.*)\\)(?: :: (.+))?", Pattern.CASE_INSENSITIVE),
			paramPattern = Pattern.compile("\\s*(.+?)\\s*:(?=[^:]*$)\\s*(.+?)(?:\\s*=\\s*(.+))?\\s*");
	
	/**
	 * Loads a script function from given node.
	 * @param node Section node.
	 * @return Script function, or null if something went wrong.
	 */
	@Nullable
	public static Function<?> loadFunction(SectionNode node) {
		SkriptLogger.setNode(node);
		String key = node.getKey();
		String definition = ScriptLoader.replaceOptions(key == null ? "" : key);
		assert definition != null;
		Matcher m = functionPattern.matcher(definition);
		if (!m.matches()) // We have checks when loading the signature, but matches() must be called anyway
			return error(INVALID_FUNCTION_DEFINITION);
		String name = "" + m.group(1);
		
		Namespace namespace = globalFunctions.get(name);
		if (namespace == null) {
			return null; // Probably duplicate signature; reported before
		}
		Signature<?> sign = namespace.getSignature(name);
		if (sign == null) // Signature parsing failed, probably: null signature
			return null; // This has been reported before...
		Parameter<?>[] params = sign.parameters;
		ClassInfo<?> c = sign.returnType;
		
		if (Skript.debug() || node.debug())
			Skript.debug("function " + name + "(" + StringUtils.join(params, ", ") + ")"
				+ (c != null ? " :: " + (sign.isSingle() ? c.getName().getSingular() : c.getName().getPlural()) : "") + ":");
		
		Function<?> f = new ScriptFunction<>(sign, node);
		
		// Register the function for signature
		namespace.addFunction(f);
		
		return f;
	}
	
	/**
	 * Loads the signature of function from given node.
	 * @param script Script file name (<b>might</b> be used for some checks).
	 * @param node Section node.
	 * @return Signature of function, or null if something went wrong.
	 */
	@Nullable
	public static Signature<?> loadSignature(String script, SectionNode node) {
		SkriptLogger.setNode(node);
		String key = node.getKey();
		String definition = ScriptLoader.replaceOptions(key == null ? "" : key);
		Matcher m = functionPattern.matcher(definition);
		if (!m.matches())
			return signError(INVALID_FUNCTION_DEFINITION);
		String name = "" + m.group(1);
		
		// Ensure there are no duplicate functions
		if (globalFunctions.containsKey(name)) {
			Namespace namespace = globalFunctions.get(name);
			if (namespace == javaNamespace) { // Special messages for built-in functions
				return signError("Function name '" + name + "' is reserved by Skript");
			} else {
				Signature<?> sign = namespace.getSignature(name);
				assert sign != null : "globalFunctions points to a wrong namespace";
				return signError("A function named '" + name + "' already exists in script '" + sign.script + "'");
			}
		}
		
		String args = m.group(2);
		String returnType = m.group(3);
		List<Parameter<?>> params = new ArrayList<>();
		int j = 0;
		for (int i = 0; i <= args.length(); i = SkriptParser.next(args, i, ParseContext.DEFAULT)) {
			if (i == -1)
				return signError("Invalid text/variables/parentheses in the arguments of this function");
			if (i == args.length() || args.charAt(i) == ',') {
				String arg = args.substring(j, i);
				
				if (arg.isEmpty()) // Zero-argument function
					break;
				
				// One ore more arguments for this function
				Matcher n = paramPattern.matcher(arg);
				if (!n.matches())
					return signError("The " + StringUtils.fancyOrderNumber(params.size() + 1) + " argument's definition is invalid. It should look like 'name: type' or 'name: type = default value'.");
				String paramName = "" + n.group(1);
				for (Parameter<?> p : params) {
					if (p.name.toLowerCase(Locale.ENGLISH).equals(paramName.toLowerCase(Locale.ENGLISH)))
						return signError("Each argument's name must be unique, but the name '" + paramName + "' occurs at least twice.");
				}
				ClassInfo<?> c;
				c = Classes.getClassInfoFromUserInput("" + n.group(2));
				NonNullPair<String, Boolean> pl = Utils.getEnglishPlural("" + n.group(2));
				if (c == null)
					c = Classes.getClassInfoFromUserInput(pl.getFirst());
				if (c == null)
					return signError("Cannot recognise the type '" + n.group(2) + "'");
				String rParamName = paramName.endsWith("*") ? paramName.substring(0, paramName.length() - 3) +
									(!pl.getSecond() ? "::1" : "") : paramName;
				Parameter<?> p = Parameter.newInstance(rParamName, c, !pl.getSecond(), n.group(3));
				if (p == null)
					return null;
				params.add(p);
				
				j = i + 1;
			}
			if (i == args.length())
				break;
		}
		
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
			if (returnClass == null) {
				return signError("Cannot recognise the type '" + returnType + "'");
			}
		}
		
		@SuppressWarnings({"unchecked", "null"})
		Signature<?> sign = new Signature<>(script, name,
			params.toArray(new Parameter[0]), (ClassInfo<Object>) returnClass, singleReturn);

		// Register this signature
		Namespace.Key namespaceKey = new Namespace.Key(Namespace.Origin.SCRIPT, script);
		Namespace namespace = namespaces.computeIfAbsent(namespaceKey, k -> new Namespace());
		namespace.addSignature(sign);
		globalFunctions.put(name, namespace);
		
		Skript.debug("Registered function signature: " + name);
		return sign;
	}
	
	/**
	 * Creates an error and returns Function null.
	 * @param error Error message.
	 * @return Null.
	 */
	@Nullable
	private static Function<?> error(String error) {
		Skript.error(error);
		return null;
	}
	
	/**
	 * Creates an error and returns Signature null.
	 * @param error Error message.
	 * @return Null.
	 */
	@Nullable
	private static Signature<?> signError(String error) {
		Skript.error(error);
		return null;
	}
	
	/**
	 * Gets a function, if it exists. Note that even if function exists in scripts,
	 * it might not have been parsed yet. If you want to check for existance,
	 * then use {@link #getSignature(String)}.
	 * @param name Name of function.
	 * @return Function, or null if it does not exist.
	 */
	@Nullable
	public static Function<?> getFunction(String name) {
		Namespace namespace = globalFunctions.get(name);
		if (namespace == null) {
			return null;
		}
		return namespace.getFunction(name);
	}
	
	/**
	 * Gets a signature of function with given name.
	 * @param name Name of function.
	 * @return Signature, or null if function does not exist.
	 */
	@Nullable
	public static Signature<?> getSignature(String name) {
		Namespace namespace = globalFunctions.get(name);
		if (namespace == null) {
			return null;
		}
		return namespace.getSignature(name);
	}
	
	private final static Collection<FunctionReference<?>> toValidate = new ArrayList<>();
	
	/**
	 * Remember to call {@link #validateFunctions()} after calling this
	 *
	 * @return How many functions were removed
	 */
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
	
	public static void validateFunctions() {
		for (FunctionReference<?> c : toValidate)
			c.validateFunction(false);
		toValidate.clear();
	}
	
	/**
	 * Clears all function calls and removes script functions.
	 */
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
