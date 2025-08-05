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
public class SectionEvent<T> extends Event {

	private final Class<T> type;
	private T object;

	public SectionEvent(Class<T> type) {
		this.type = type;
	}

	public SectionEvent(Section section, T object) {
		this(section, object.getClass(), object);
	}

	public SectionEvent(Section section, Class<?> type, T object) {
		//noinspection unchecked
		this.type = (Class<T>) type;
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

}
