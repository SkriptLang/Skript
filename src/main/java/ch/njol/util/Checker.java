package ch.njol.util;

import java.util.function.Predicate;

/**
 * @deprecated use {@link Predicate}
 */
@Deprecated(forRemoval = true)
@FunctionalInterface
public interface Checker<T> extends Predicate<T> {

	boolean check(T o);

	@Override
	default boolean test(T t) {
		return this.check(t);
	}

}
