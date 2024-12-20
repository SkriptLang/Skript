package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an element that can print details involving an event.
 */
public interface Debuggable {

	/**
	 * @param event The event to get information from. This is always null if debug == false.
	 * @param debug If true this should print more information, if false this should print what is shown to the end user
	 * @return String representation of this object
	 */
	String toString(@Nullable Event event, boolean debug);

	/**
	 * Should return <tt>{@link #toString(Event, boolean) toString}(null, false)</tt>
	 */
	@Override
	String toString();

}
