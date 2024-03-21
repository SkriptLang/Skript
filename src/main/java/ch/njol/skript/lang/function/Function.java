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
import java.util.Arrays;
import java.util.List;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.registrations.Classes;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.util.coll.CollectionUtils;

/**
 * Functions can be called using arguments.
 */
public abstract class Function<T> {
	
	/**
	 * Execute functions even when some parameters are not present.
	 * Field is updated by SkriptConfig in case of reloads.
	 */
	public static boolean executeWithNulls = SkriptConfig.executeFunctionsWithMissingParams.value();

	private final Signature<T> sign;
	
	public Function(Signature<T> sign) {
		this.sign = sign;
	}
	
	/**
	 * Gets signature of this function that contains all metadata about it.
	 * @return A function signature.
	 */
	public Signature<T> getSignature() {
		return sign;
	}
	
	public String getName() {
		return sign.getName();
	}
	
	public Parameter<?>[] getParameters() {
		return sign.getParameters();
	}
	
	@SuppressWarnings("null")
	public Parameter<?> getParameter(int index) {
		return getParameters()[index];
	}
	
	public boolean isSingle() {
		return sign.isSingle();
	}
	
	@Nullable
	public ClassInfo<T> getReturnType() {
		return sign.getReturnType();
	}

	/**
	 * Executes the function with the given event and parameters.
	 * @param event The event for the function execution.
	 * @param rawParameters The parameters for the function execution.
	 * @return The result(s) of the function execution.
	 * Returns null if the parameters are invalid or cannot be processed.
	 */
	public final T @Nullable [] execute(Event event, @Nullable Expression<?> rawParameters) {
		Expression<?>[] parameters;
		if (rawParameters instanceof ExpressionList<?>) {
			ExpressionList<?> list = (ExpressionList<?>) rawParameters;
			Expression<?>[] expressions = list.getExpressions();
			parameters = list.getAnd() ? expressions : new Expression[] {CollectionUtils.getRandom(expressions)};
		} else {
			parameters = rawParameters != null ? new Expression[] {rawParameters} : new Expression[0];
		}
		return execute(event, parameters);
	}

	/**
	 * Executes the function with the given event and parameters.
	 * @param event The event for the function execution.
	 * @param parameters The parameters for the function execution.
	 * @return The result(s) of the function execution.
	 * Returns null if the parameters are invalid or cannot be processed.
	 */
	public final T @Nullable [] execute(Event event, Expression<?>[] parameters) {
		if (!validateParameters(parameters))
			return null;
		Object[][] params = processParameters(event, parameters);
		if (params == null)
			return null;
		return execute(params);
	}

	@SuppressWarnings("unchecked")
	private boolean validateParameters(Expression<?>[] parameters) {
		if (parameters.length < sign.getMinParameters())
			return false;

		boolean singleListParam = sign.getMaxParameters() == 1 && !getParameter(0).single;
		if (!singleListParam && parameters.length > sign.getMaxParameters())
			return false;

		for (int i = 0; i < parameters.length; i++) {
			Parameter<?> parameter = sign.parameters[singleListParam ? 0 : i];
			Expression<?> expression = parameters[i].getConvertedExpression(parameter.type.getC());
			if (expression == null || (parameter.single && !expression.isSingle()))
				return false;
			parameters[i] = expression;
		}
		return true;
	}

	private Object @Nullable [][] processParameters(Event event, Expression<?>[] parameters) {
		boolean singleListParam = sign.getMaxParameters() == 1 && !getParameter(0).single;

		Object[][] params = new Object[singleListParam ? 1 : parameters.length][];
		if (singleListParam && parameters.length > 1) { // All parameters to one list
			List<Object> l = new ArrayList<>();
			for (Expression<?> parameter : parameters)
				l.addAll(Arrays.asList(parameter.getArray(event)));
			params[0] = l.toArray();
		} else { // Use parameters in normal way

			if (params.length > sign.getMaxParameters())
				return null;

			for (int i = 0; i < parameters.length; i++) {
				Object[] array = parameters[i].getArray(event);
				params[i] = Arrays.copyOf(array, array.length);
			}
		}

		// Don't allow mutating across function boundary; same hack is applied to variables
		for (Object[] param : params) {
			for (int i = 0; i < param.length; i++) {
				param[i] = Classes.clone(param[i]);
			}
		}

		return params;
	}

	// FIXME what happens with a delay in a function?
	
	/**
	 * Executes this function with given parameter.
	 * @param params Function parameters. Must contain at least
	 * {@link Signature#getMinParameters()} elements and at most
	 * {@link Signature#getMaxParameters()} elements.
	 * @return The result(s) of this function
	 */
	@SuppressWarnings("null")
	@Nullable
	public final T[] execute(Object[][] params) {
		FunctionEvent<? extends T> e = new FunctionEvent<>(this);
		
		// Call function event only if requested by addon
		// Functions may be called VERY often, so this might have performance impact
		if (Functions.callFunctionEvents)
			Bukkit.getPluginManager().callEvent(e);
		
		// Parameters taken by the function.
		Parameter<?>[] parameters = sign.getParameters();
		
		if (params.length > parameters.length) {
			// Too many parameters, should have failed to parse
			assert false : params.length;
			return null;
		}
		
		// If given less that max amount of parameters, pad remaining with nulls
		Object[][] ps = params.length < parameters.length ? Arrays.copyOf(params, parameters.length) : params;
		
		// Execute parameters or default value expressions
		for (int i = 0; i < parameters.length; i++) {
			Parameter<?> p = parameters[i];
			Object[] val = ps[i];
			if (val == null) { // Go for default value
				assert p.def != null; // Should've been parse error
				val = p.def.getArray(e);
			}
			
			/*
			 * Cancel execution of function if one of parameters produces null.
			 * This used to be the default behavior, but since scripts don't
			 * really have a concept of nulls, it was changed. The config
			 * option may be removed in future.
			 */
			if (!executeWithNulls && val.length == 0)
				return null;
			ps[i] = val;
		}
		
		// Execute function contents
		T[] r = execute(e, ps);
		// Assert that return value type makes sense
		assert sign.getReturnType() == null ? r == null : r == null
			|| (r.length <= 1 || !sign.isSingle()) && !CollectionUtils.contains(r, null)
			&& sign.getReturnType().getC().isAssignableFrom(r.getClass().getComponentType())
			: this + "; " + Arrays.toString(r);
				
		// If return value is empty array, return null
		// Otherwise, return the value (nullable)
		return r == null || r.length > 0 ? r : null;
	}
	
	/**
	 * Executes this function with given parameters. Usually, using
	 * {@link #execute(Object[][])} is better; it handles optional arguments
	 * and function event creation automatically.
	 * @param e Associated function event. This is usually created by Skript.
	 * @param params Function parameters.
	 * There must be {@link Signature#getMaxParameters()} amount of them, and
	 * you need to manually handle default values.
	 * @return Function return value(s).
	 */
	@Nullable
	public abstract T[] execute(FunctionEvent<?> e, Object[][] params);

	/**
	 * Resets the return value of the {@code Function}.
	 * Should be called right after execution.
	 *
	 * @return Whether or not the return value was successfully reset
	 */
	public abstract boolean resetReturnValue();

	@Override
	public String toString() {
		return (sign.local ? "local " : "") + "function " + sign.getName();
	}

}
