package org.skriptlang.skript.lang.function;

import ch.njol.skript.lang.Expression;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The 'contract' of a function or another callable.
 * This is a non-exhaustive helper for type hints, singularity, etc. that may change based on the arguments
 * passed to a callable, in order for it to make better judgements on correct use at parse time.
 */
public interface Contract {

	/**
	 * @return Whether, all other things being unknown, this will return a single value
	 * @see Expression#isSingle()
	 */
	boolean isSingle();

	/**
	 * @return Whether, given these parameters, this will return a single value
	 * @see Expression#isSingle()
	 */
	boolean isSingle(Expression<?>... arguments);

	/**
	 * @return What this will return, all other things being unknown
	 * @see Expression#getReturnType()
	 */
	@Nullable
	Class<?> getReturnType();

	/**
	 * @return What this will return, given these parameters
	 * @see Expression#getReturnType()
	 */
	@Nullable
	Class<?> getReturnType(Expression<?>... arguments);

}
