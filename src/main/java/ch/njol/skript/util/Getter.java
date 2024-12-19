package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

/**
 * Used to get a specific value from instances of some type.
 *
 * @deprecated use {@link Converter} instead
 * @param <R> the returned value type
 * @param <A> the type which holds the value
 */
@Deprecated(forRemoval = true)
public abstract class Getter<R, A> implements ch.njol.skript.classes.Converter<A, R>, Converter<A, R> {

	/**
	 * Gets a value from the given object.
	 * 
	 * @param arg the object to get the value from
	 * @return the value
	 */
	@Nullable
	public abstract R get(A arg);
	
	/**
	 * Convenience method to make getter implement converter
	 */
	@Override
	@Nullable
	public final R convert(final A a) {
		return get(a);
	}
	
}
