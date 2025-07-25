package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SectionEvent<T> extends Event {

	private final T object;

	public SectionEvent(T object) {
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
