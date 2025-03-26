package ch.njol.skript.lang.util;

import ch.njol.skript.lang.parser.ParserInstance;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This class is intended for usage in places of Skript that require an Event.
 * Of course, not everything is always context/event dependent.
 * For example, if one were to load a SectionNode or parse something into a {@link ch.njol.skript.lang.SyntaxElement},
 *  and {@link ParserInstance#getCurrentEvents()} was null or empty, the resulting elements
 *  would not be dependent upon a specific Event. Thus, there would be no reason for an Event to be required.
 * So, this classes exists to avoid dangerously passing null in these places.
 * @see #get()
 */
public final class ContextlessEvent extends Event {

	private ContextlessEvent() { }

	/**
	 * @return A new ContextlessEvent instance to be used for context-less {@link ch.njol.skript.lang.SyntaxElement}s.
	 */
	public static ContextlessEvent get() {
		return new ContextlessEvent();
	}

	/**
	 * This method should never be called.
	 */
	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

}
