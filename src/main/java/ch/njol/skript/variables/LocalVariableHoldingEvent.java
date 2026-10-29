package ch.njol.skript.variables;

import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An event which can hold local variables itself, rather than resorting to the global {@link java.util.concurrent.ConcurrentHashMap}
 * for storage. Speeds up access a lot when dealing with high-churn events like {@link ch.njol.skript.lang.function.FunctionEvent}.
 * <br>
 * Temporary solution until runtime context overhaul can provide proper local variable scopes.
 */
@ApiStatus.Internal
public abstract class LocalVariableHoldingEvent extends Event {

	private @Nullable VariablesMap map;

	/**
	 * @return The current local variable map for this event instance. May be null.
	 */
	@Nullable VariablesMap getLocals() {
		return map;
	}

	/**
	 * Sets the current local variable map for this event instance.
	 *
	 * @param map The new local variables. May be null to clear.
	 */
	void setLocals(@Nullable VariablesMap map) {
		this.map = map;
	}

}
