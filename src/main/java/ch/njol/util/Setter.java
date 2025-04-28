package ch.njol.util;

import java.util.function.Consumer;

/**
 * @deprecated use {@link Consumer} instead.
 */
@Deprecated(since = "2.10.0", forRemoval = true)
@FunctionalInterface
public interface Setter<T> extends Consumer<T> {

	void set(T t);

	@Override
	default void accept(T t) {
		this.set(t);
	}
}
