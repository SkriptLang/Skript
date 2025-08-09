package org.skriptlang.skript.common.function;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class FunctionEvent<T> extends Event {

	private final static HandlerList handlers = new HandlerList();

	private final Function<T> function;

	public FunctionEvent(Function<T> function) {
		this.function = function;
	}

	public Function<T> function() {
		return function;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
