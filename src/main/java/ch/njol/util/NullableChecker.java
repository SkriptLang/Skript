package ch.njol.util;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

@SuppressWarnings("removal")
public interface NullableChecker<T> extends ch.njol.util.Checker<T>, Predicate<T> {

	@Override
	boolean check(@Nullable T o);

	@Override
	default boolean test(@Nullable T t) {
		return this.check(t);
	}

	NullableChecker<Object> nullChecker = Objects::nonNull;

}
