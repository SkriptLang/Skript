package ch.njol.skript.lang.function;

import ch.njol.skript.variables.LocalVariableHoldingEvent;
import org.bukkit.event.HandlerList;

public final class FunctionEvent<T> extends LocalVariableHoldingEvent {
	
	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();
	
	private final Function<? extends T> function;
	
	public FunctionEvent(Function<? extends T> function) {
		this.function = function;
	}

	public FunctionEvent(org.skriptlang.skript.common.function.Function<? extends T> function) {
		this.function = (Function<? extends T>) function;
	}

	public Function<? extends T> getFunction() {
		return function;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
