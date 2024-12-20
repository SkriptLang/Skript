package ch.njol.skript.lang;

import org.bukkit.event.Event;

/**
 * A {@link Section} implementing this interface can execute a task when
 * it is exited by an {@link ch.njol.skript.effects.EffExit 'exit'} or
 * {@link ch.njol.skript.effects.EffReturn 'return'} effect.
 */
public interface SectionExitHandler {

	/**
	 * Exits the section
	 * @param event The involved event
	 */
	void exit(Event event);

}
