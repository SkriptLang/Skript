package ch.njol.skript.expressions.arithmetic;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * @param <T> The return type of the gettable
 */
public interface ArithmeticGettable<T> {

	@Nullable
	T get(Event event);

	Class<? extends T> getReturnType();

}
