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
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reference to a Skript function.
 */
public class FunctionReference<T> {
	
	/**
	 * Name of function that is called, for logging purposes.
	 */
	final String functionName;
	
	/**
	 * Signature of referenced function. If {@link #validateFunction(boolean)}
	 * succeeds, this is not null.
	 */
	@Nullable
	private Signature<? extends T> signature;
	
	/**
	 * Actual function reference. Null before the function is called for first
	 * time.
	 */
	@Nullable
	private Function<? extends T> function;
	
	/**
	 * If all function parameters can be condensed to a single list.
	 */
	private boolean singleListParam;
	
	/**
	 * Definitions of function parameters.
	 */
	private final Expression<?>[] parameters;

	/**
	 * Indicates if the caller expects this function to return a single value.
	 * Used for verifying correctness of the function signature.
	 */
	private boolean single;
	
	/**
	 * Return types expected from this function. Used for verifying correctness
	 * of the function signature.
	 */
	@Nullable
	final Class<? extends T>[] returnTypes;
	
	/**
	 * Node for {@link #validateFunction(boolean)} to use for logging.
	 */
	@Nullable
	private final Node node;
	
	/**
	 * Script in which this reference is found. Used for function unload
	 * safety checks.
	 */
	@Nullable
	public final String script;
	
	public FunctionReference(
			String functionName, @Nullable Node node, @Nullable String script,
			@Nullable Class<? extends T>[] returnTypes, Expression<?>[] params
	) {
		this.functionName = functionName;
		this.node = node;
		this.script = script;
		this.returnTypes = returnTypes;
		parameters = params;
	}
	
	/**
	 * Validates this function reference. Prints errors if needed.
	 * @param first True if this is called while loading a script. False when
	 * this is called when the function signature changes.
	 * @return True if validation succeeded.
	 */
	@SuppressWarnings("unchecked")
	public boolean validateFunction(boolean first) {
		if (!first && script == null)
			return false;
		Function<? extends T> previousFunction = function;
		function = null;
		SkriptLogger.setNode(node);
		Skript.debug("Validating function " + functionName);
		Signature<?> sign = Functions.getLocalSignature(functionName, script);
		if (sign == null)
			sign = Functions.getGlobalSignature(functionName);
		
		// Check if the requested function exists
		if (sign == null) {
			if (first) {
				Skript.error("The function '" + functionName + "' does not exist.");
			} else {
				Skript.error("The function '" + functionName + "' was deleted or renamed, but is still used in other script(s)."
					+ " These will continue to use the old version of the function until Skript restarts.");
				function = previousFunction;
			}
			return false;
		}
		
		// Validate that return types are what caller expects they are
		Class<? extends T>[] returnTypes = this.returnTypes;
		if (returnTypes != null) {
			ClassInfo<?> rt = sign.returnType;
			if (rt == null) {
				if (first) {
					Skript.error("The function '" + functionName + "' doesn't return any value.");
				} else {
					Skript.error("The function '" + functionName + "' was redefined with no return value, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
					function = previousFunction;
				}
				return false;
			}
			if (!Converters.converterExists(rt.getC(), returnTypes)) {
				if (first) {
					Skript.error("The returned value of the function '" + functionName + "', " + sign.returnType + ", is " + SkriptParser.notOfType(returnTypes) + ".");
				} else {
					Skript.error("The function '" + functionName + "' was redefined with a different, incompatible return type, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
					function = previousFunction;
				}
				return false;
			}
			if (first) {
				single = sign.single;
			} else if (single && !sign.single) {
				Skript.error("The function '" + functionName + "' was redefined with a different, incompatible return type, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
				function = previousFunction;
				return false;
			}
		}
		
		// Validate parameter count
		singleListParam = sign.getMaxParameters() == 1 && !sign.getParameter(0).single;
		if (!singleListParam) { // Check that parameter count is within allowed range
			// Too many parameters
			if (parameters.length > sign.getMaxParameters()) {
				if (first) {
					if (sign.getMaxParameters() == 0) {
						Skript.error("The function '" + functionName + "' has no arguments, but " + parameters.length + " are given."
							+ " To call a function without parameters, just write the function name followed by '()', e.g. 'func()'.");
					} else {
						Skript.error("The function '" + functionName + "' has only " + sign.getMaxParameters() + " argument" + (sign.getMaxParameters() == 1 ? "" : "s") + ","
							+ " but " + parameters.length + " are given."
							+ " If you want to use lists in function calls, you have to use additional parentheses, e.g. 'give(player, (iron ore and gold ore))'");
					}
				} else {
					Skript.error("The function '" + functionName + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s)."
						+ " These will continue to use the old version of the function until Skript restarts.");
					function = previousFunction;
				}
				return false;
			}
		}
		
		// Not enough parameters
		if (parameters.length < sign.getMinParameters()) {
			if (first) {
				Skript.error("The function '" + functionName + "' requires at least " + sign.getMinParameters() + " argument" + (sign.getMinParameters() == 1 ? "" : "s") + ","
					+ " but only " + parameters.length + " " + (parameters.length == 1 ? "is" : "are") + " given.");
			} else {
				Skript.error("The function '" + functionName + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s)."
					+ " These will continue to use the old version of the function until Skript restarts.");
				function = previousFunction;
			}
			return false;
		}
		
		// Check parameter types
		for (int i = 0; i < parameters.length; i++) {
			Parameter<?> p = sign.parameters[singleListParam ? 0 : i];
			RetainingLogHandler log = SkriptLogger.startRetainingLog();
			try {
				Expression<?> e = parameters[i].getConvertedExpression(p.type.getC());
				if (e == null) {
					if (first) {
						if (LiteralUtils.hasUnparsedLiteral(parameters[i])) {
							Skript.error("Can't understand this expression: " + parameters[i].toString());
						} else {
							Skript.error("The " + StringUtils.fancyOrderNumber(i + 1) + " argument given to the function '" + functionName + "' is not of the required type " + p.type + "."
								+ " Check the correct order of the arguments and put lists into parentheses if appropriate (e.g. 'give(player, (iron ore and gold ore))')."
								+ " Please note that storing the value in a variable and then using that variable as parameter will suppress this error, but it still won't work.");
						}
					} else {
						Skript.error("The function '" + functionName + "' was redefined with different, incompatible arguments, but is still used in other script(s)."
							+ " These will continue to use the old version of the function until Skript restarts.");
						function = previousFunction;
					}
					return false;
				} else if (p.single && !e.isSingle()) {
					if (first) {
						Skript.error("The " + StringUtils.fancyOrderNumber(i + 1) + " argument given to the function '" + functionName + "' is plural, "
							+ "but a single argument was expected");
					} else {
						Skript.error("The function '" + functionName + "' was redefined with different, incompatible arguments, but is still used in other script(s)."
							+ " These will continue to use the old version of the function until Skript restarts.");
						function = previousFunction;
					}
					return false;
				}
				parameters[i] = e;
			} finally {
				log.printLog();
			}
		}
		
		signature = (Signature<? extends T>) sign;
		sign.calls.add(this);
		
		return true;
	}

	@Nullable
	public Function<? extends T> getFunction() {
		return function;
	}

	public boolean resetReturnValue() {
		if (function != null)
			return function.resetReturnValue();
		return false;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected T[] execute(Event e) {
		// If needed, acquire the function reference
		if (function == null)
			function = (Function<? extends T>) Functions.getLocalFunction(functionName, script);
		
		if (function == null)
			function = (Function<? extends T>) Functions.getGlobalFunction(functionName);

		if (function == null) { // It might be impossible to resolve functions in some cases!
			Skript.error("Couldn't resolve call for '" + functionName + "'.");
			return null; // Return nothing and hope it works
		}
		
		// Prepare parameter values for calling
		Object[][] params = new Object[singleListParam ? 1 : parameters.length][];
		if (singleListParam && parameters.length > 1) { // All parameters to one list
			List<Object> l = new ArrayList<>();
			for (Expression<?> parameter : parameters)
				l.addAll(Arrays.asList(parameter.getArray(e)));
			params[0] = l.toArray();
			
			// Don't allow mutating across function boundary; same hack is applied to variables
			for (int i = 0; i < params[0].length; i++) {
				params[0][i] = Classes.clone(params[0][i]);
			}
		} else { // Use parameters in normal way
			for (int i = 0; i < parameters.length; i++) {
				Object[] array = parameters[i].getArray(e);
				params[i] = Arrays.copyOf(array, array.length);
				// Don't allow mutating across function boundary; same hack is applied to variables
				for (int j = 0; j < params[i].length; j++) {
					params[i][j] = Classes.clone(params[i][j]);
				}
			}
		}
		
		// Execute the function
		return function.execute(params);
	}
	
	public boolean isSingle() {
		return single;
	}
	
	@Nullable
	public Class<? extends T> getReturnType() {
		if (signature == null)
			throw new SkriptAPIException("Signature of function is null when return type is asked!");
		
		@SuppressWarnings("ConstantConditions")
		ClassInfo<? extends T> ret = signature.returnType;
		return ret == null ? null : ret.getC();
	}
	
	public String toString(@Nullable Event e, boolean debug) {
		StringBuilder b = new StringBuilder(functionName + "(");
		for (int i = 0; i < parameters.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(parameters[i].toString(e, debug));
		}
		b.append(")");
		return b.toString();
	}
	
}
