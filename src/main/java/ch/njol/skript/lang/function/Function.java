package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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

	public @Nullable ClassInfo<T> getReturnType() {
		return sign.getReturnType();
	}
	
	// FIXME what happens with a delay in a function?
	
	/**
	 * Executes this function with given parameter.
	 * @param params Function parameters. Must contain at least
	 * {@link Signature#getMinParameters()} elements and at most
	 * {@link Signature#getMaxParameters()} elements.
	 * @return The result(s) of this function
	 */
	public final T @Nullable [] execute(Object[][] params) {
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
	 * @param event Associated function event. This is usually created by Skript.
	 * @param params Function parameters.
	 * There must be {@link Signature#getMaxParameters()} amount of them, and
	 * you need to manually handle default values.
	 * @return Function return value(s).
	 */
	public abstract T @Nullable [] execute(FunctionEvent<?> event, Object[][] params);

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
