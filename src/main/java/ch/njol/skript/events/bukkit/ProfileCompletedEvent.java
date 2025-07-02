package ch.njol.skript.events.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a custom event triggered when a profiling section is completed.
 * This event holds information about the name of the profiling section and
 * the time it took to complete in milliseconds.
 */
public class ProfileCompletedEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final String sectionName;
	private final double durationMs;

	/**
	 * Constructs a new ProfileCompletedEvent.
	 * @param sectionName the name of the profiling section
	 * @param durationMs  the duration in milliseconds the section took to execute
	 */
	public ProfileCompletedEvent(String sectionName, double durationMs) {
		this.sectionName = sectionName;
		this.durationMs = durationMs;
	}

	/**
	 * Gets the name of the profiling section that was completed.
	 * @return the name of the completed profiling section
	 */
	public String getName() {
		return sectionName;
	}

	/**
	 * Gets the duration in milliseconds that the profiling section took to complete.
	 * @return the execution time of the section in milliseconds
	 */
	public double getDurationMs() {
		return durationMs;
	}

	/**
	 * Gets the list of handlers registered for this event.
	 * @return the list of handlers
	 */
	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * Gets the static handler list for this event class.
	 * @return the static handler list
	 */
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
