package ch.njol.skript.lang;

import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Generic {@link Event} that can be used with {@link EffectSection}s and {@link SectionExpression}s that use
 * an {@link Event} with a singular object to be used for elements inside their section.
 * <p>
 *     The value of this section can only be accessed through and should never
 *     be used to register an event-value via {@code #registerEventValue} in {@link EventValues}.
 * </p>
 * @param <T> The typed object.
 */
public class SectionEvent<T extends SyntaxElement & SectionValueProvider> extends Event {

	private final T element;

	public SectionEvent(T element) {
		this.element = element;
	}

	public T getElement() {
		return element;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

}
