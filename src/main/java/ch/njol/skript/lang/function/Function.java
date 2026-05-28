package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.FunctionArguments;
import org.skriptlang.skript.common.function.Parameter.Modifier;
import org.skriptlang.skript.common.function.Parameters;
import org.skriptlang.skript.common.function.ScriptParameter;

import java.util.Arrays;

/**
 * Functions can be called using arguments.
 */
public abstract class Function<T> implements org.skriptlang.skript.common.function.Function<T> {

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

	@Override
	public org.skriptlang.skript.common.function.@NotNull Signature<T> signature() {
		return sign;
	}

	public String getName() {
		return sign.getName();
	}

	/**
	 * @deprecated Use {@link Signature#parameters()} and {@link Parameters#all()} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.14")
	public Parameter<?>[] getParameters() {
		return Arrays.stream(sign.getParameters()).map(
				Signature::toOldParameter)
				.toArray(Parameter[]::new);
	}

	/**
	 * @deprecated Use {@link Signature#parameters()} and {@link Parameters#get(int)} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.14")
	public Parameter<?> getParameter(int index) {
		return Signature.toOldParameter(sign.getParameter(index));
	}

	public boolean isSingle() {
		return sign.isSingle();
	}

	public @Nullable ClassInfo<T> getReturnType() {
		return sign.getReturnType();
	}

	/**
	 * @return The return type of this signature. Returns null for no return type.
	 */
	public Class<T> type() {
		return sign.returnType();
	}

	/**
	 * @deprecated Use {@link #execute(FunctionEvent, FunctionArguments)} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.14")
	public final T @Nullable [] execute(Object[][] params) {
		FunctionEvent<? extends T> event = new FunctionEvent<>(this);

		// Call function event only if requested by addon
		// Functions may be called VERY often, so this might have performance impact
		if (Functions.callFunctionEvents)
			Bukkit.getPluginManager().callEvent(event);

		// Parameters taken by the function.
		Parameters parameters = sign.parameters();
		if (params.length > parameters.size()) {
			// Too many parameters, should have failed to parse
			assert false : params.length;
			return null;
		}

		// If given less that max amount of parameters, pad remaining with nulls
		Object[][] parameterValues = params.length < parameters.size() ? Arrays.copyOf(params, parameters.size()) : params;

		int i = 0;
		// Execute parameters or default value expressions
		for (org.skriptlang.skript.common.function.Parameter<?> parameter : parameters.all()) {
			Object[] parameterValue = parameter.hasModifier(Modifier.KEYED) ? convertToKeyed(parameterValues[i]) : parameterValues[i];

			Expression<?> defaultValueExpr;
			if (parameter instanceof Parameter<?> script) {
				defaultValueExpr = script.def;
			} else if (parameter instanceof ScriptParameter<?> script) {
				defaultValueExpr = script.defaultValue();
			} else {
				defaultValueExpr = null;
			}

			// see https://github.com/SkriptLang/Skript/pull/8135
			if ((parameterValues[i] == null || parameterValues[i].length == 0) && parameter.hasModifier(Modifier.KEYED) && defaultValueExpr != null) {
				Object[] defaultValue = defaultValueExpr.getArray(event);
				if (defaultValue.length == 1) {
					parameterValue = KeyedValue.zip(defaultValue, null);
				} else {
					parameterValue = defaultValue;
				}
			} else if (parameterValue == null && !(this instanceof DefaultFunction<?>)) { // Go for default value
				assert defaultValueExpr != null; // Should've been parse error
				//noinspection unchecked,rawtypes
				parameterValue = parameter.evaluate((Expression) defaultValueExpr, event);
			}

			/*
			 * Cancel execution of function if one of parameters produces null.
			 * This used to be the default behavior, but since scripts don't
			 * really have a concept of nulls, it was changed. The config
			 * option may be removed in future.
			 */
			if (!(this instanceof DefaultFunction<?>) && !executeWithNulls && parameterValue != null && parameterValue.length == 0)
				return null;
			parameterValues[i] = parameterValue;
			i++;
		}

		// Execute function contents
		T[] r = execute(event, parameterValues);
		// Assert that return value type makes sense
		assert sign.getReturnType() == null ? r == null : r == null
			|| (r.length <= 1 || !sign.isSingle()) && !CollectionUtils.contains(r, null)
			&& sign.getReturnType().getC().isAssignableFrom(r.getClass().getComponentType())
			: this + "; " + Arrays.toString(r);

		// If return value is empty array, return null
		// Otherwise, return the value (nullable)
		return r == null || r.length > 0 ? r : null;
	}

	private KeyedValue<Object> @Nullable [] convertToKeyed(Object[] values) {
		if (values == null)
			return null;

		if (values.length == 0)
			//noinspection unchecked
			return new KeyedValue[0];

		if (values instanceof KeyedValue[])
			//noinspection unchecked
			return (KeyedValue<Object>[]) values;

		return KeyedValue.zip(values, null);
	}

	/**
	 * @deprecated Use {@link #execute(FunctionEvent, FunctionArguments)} instead.
	 */
	@Deprecated(since = "2.14", forRemoval = true)
	public abstract T @Nullable [] execute(FunctionEvent<?> event, Object[][] params);

	/**
	 * @return The keys of the values returned by this function, or null if no keys are returned.
	 */
	public @NotNull String @Nullable [] returnedKeys() {
		return null;
	}

	/**
	 * Resets the return value of the {@code Function}.
	 * Should be called right after execution.
	 *
	 * @return Whether or not the return value was successfully reset
	 */
	public abstract boolean resetReturnValue();

	@Override
	public String toString() {
		return (sign.isLocal() ? "local " : "") + "function " + sign.getName();
	}

}
