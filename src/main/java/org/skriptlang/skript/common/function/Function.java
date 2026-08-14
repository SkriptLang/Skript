package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.function.FunctionEvent;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.event.EventRegistry;

/**
 * Represents a function implementation.
 *
 * <h2>This interface should only be extended by {@link DefaultFunction} and {@link ch.njol.skript.lang.function.Function}</h2>
 * <p>It will contain methods when Function has been properly reworked.</p>
 */
@NonExtendable
@Internal
@Experimental
public interface Function<T> {

	/**
	 * Executes this function with the given parameters.
	 *
	 * @param event     The event that is associated with this function execution.
	 * @param arguments The arguments to execute the function with.
	 * @return The return value.
	 */
	T execute(@NotNull FunctionEvent<?> event, @NotNull FunctionArguments arguments);

	/**
	 * @return The signature belonging to this function.
	 */
	@NotNull Signature<T> signature();

	/**
	 * Resets the return value.
	 */
	@Experimental
	boolean resetReturnValue();

	/**
	 * @return The returned keys.
	 */
	@Experimental
	@NotNull String @Nullable [] returnedKeys();

	//<editor-fold desc="Function events">

	/**
	 * Used for listening to function events.
	 *
	 * @see #eventRegistry()
	 */
	interface Event extends org.skriptlang.skript.util.event.Event {
	}

	/**
	 * Called when a function is called.
	 */
	@FunctionalInterface
	interface CallEvent extends Event {

		/**
		 * The method that is called when this event triggers.
		 */
		void onCall(Function<?> function);

	}

	/**
	 * @return An event registry for function events.
	 */
	static EventRegistry<Event> eventRegistry() {
		return Data.EVENT_REGISTRY;
	}

	class Data {
		private static final EventRegistry<Event> EVENT_REGISTRY = new EventRegistry<>();
	}
	//</editor-fold>

}
