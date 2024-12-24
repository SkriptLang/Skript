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
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reference to a Skript function.
 */
public class FunctionReference<T> implements Contract {
	
	/**
	 * Name of function that is called, for logging purposes.
	 */
	final String functionName;
	
	/**
	 * Signature of referenced function. If {@link #validateFunction(boolean)}
	 * succeeds, this is not null.
	 */
	private @Nullable Signature<? extends T> signature;
	
	/**
	 * Actual function reference. Null before the function is called for first
	 * time.
	 */
	private @Nullable Function<? extends T> function;
	
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
	private final @Nullable Node node;
	
	/**
	 * Script in which this reference is found. Used for function unload
	 * safety checks.
	 */
	public final @Nullable String script;

	/**
	 * The contract for this function (typically the function reference itself).
	 * Used to determine input-based return types and simple behaviour.
	 */
	private Contract contract;

	public FunctionReference(
			String functionName, @Nullable Node node, @Nullable String script,
			@Nullable Class<? extends T>[] returnTypes, Expression<?>[] params
	) {
		this.functionName = functionName;
		this.node = node;
		this.script = script;
		this.returnTypes = returnTypes;
		this.parameters = params;
		this.contract = this;
	}

	public boolean validateParameterArity(boolean first) {
		if (!first && script == null)
			return false;
		Signature<?> sign = Functions.getSignature(functionName, script);
		if (sign == null)
			return false;
		// Not enough parameters
		return parameters.length >= sign.getMinParameters();
	}
	
	/**
	 * Validates this function reference. Prints errors if needed.
	 * @param first True if this is called while loading a script. False when
	 * this is called when the function signature changes.
	 * @return True if validation succeeded.
	 */
	public boolean validateFunction(boolean first) {
		if (!first && script == null)
			return false;
		Function<? extends T> previousFunction = function;
		function = null;
		SkriptLogger.setNode(node);
		Skript.debug("Validating function " + functionName);
		Signature<?> sign = Functions.getSignature(functionName, script);

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
				//noinspection unchecked
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

		//noinspection unchecked
		signature = (Signature<? extends T>) sign;
		sign.calls.add(this);

		Contract contract = sign.getContract();
		if (contract != null)
			this.contract = contract;
		
		return true;
	}

	public @Nullable Function<? extends T> getFunction() {
		return function;
	}

	public boolean resetReturnValue() {
		if (function != null)
			return function.resetReturnValue();
		return false;
	}

	protected T @Nullable [] execute(Event event) {
		// If needed, acquire the function reference
		if (function == null)
			//noinspection unchecked
			function = (Function<? extends T>) Functions.getFunction(functionName, script);

		if (function == null) { // It might be impossible to resolve functions in some cases!
			Skript.error("Couldn't resolve call for '" + functionName + "'.");
			return null; // Return nothing and hope it works
		}
		
		// Prepare parameter values for calling
		Object[][] params = new Object[singleListParam ? 1 : parameters.length][];
		if (singleListParam && parameters.length > 1) { // All parameters to one list
			List<Object> l = new ArrayList<>();
			for (Expression<?> parameter : parameters)
				l.addAll(Arrays.asList(parameter.getArray(event)));
			params[0] = l.toArray();
			
			// Don't allow mutating across function boundary; same hack is applied to variables
			for (int i = 0; i < params[0].length; i++) {
				params[0][i] = Classes.clone(params[0][i]);
			}
		} else { // Use parameters in normal way
			for (int i = 0; i < parameters.length; i++) {
				Object[] array = parameters[i].getArray(event);
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
		return contract.isSingle(parameters);
	}

	@Override
	public boolean isSingle(Expression<?>... arguments) {
		return single;
	}

	public @Nullable Class<? extends T> getReturnType() {
		//noinspection unchecked
		return (Class<? extends T>) contract.getReturnType(parameters);
	}

	@Override
	public @Nullable Class<?> getReturnType(Expression<?>... arguments) {
		if (signature == null)
			throw new SkriptAPIException("Signature of function is null when return type is asked!");

		ClassInfo<? extends T> ret = signature.returnType;
		return ret == null ? null : ret.getC();
	}

	/**
	 * The contract is used in preference to the function for determining return type, etc.
	 * @return The contract determining this function's parse-time hints, potentially this reference
	 */
	public Contract getContract() {
		return contract;
	}

	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder b = new StringBuilder(functionName + "(");
		for (int i = 0; i < parameters.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(parameters[i].toString(event, debug));
		}
		b.append(")");
		return b.toString();
	}
	
}
