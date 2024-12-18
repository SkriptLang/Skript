package ch.njol.skript.util;

import ch.njol.skript.classes.Converter;
import org.jetbrains.annotations.Nullable;

/**
 * Used to get a specific value from instances of some type.
 * 
 * @param <R> the returned value type
 * @param <A> the type which holds the value
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation") // for backwards compatibility
public abstract class Getter<R, A> implements Converter<A, R> {
	
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
