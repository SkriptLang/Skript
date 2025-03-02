package ch.njol.skript.doc;

import org.bukkit.event.Event;

import java.lang.annotation.*;

/**
 * Provides a list of {@link org.bukkit.event.Event} that the syntax element can be used in.
 * <p>
 * This annotation can only be used once per element, for stacking, see {@link AvailableEvent}
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AvailableEvents {

	Class<? extends Event>[] value();

}
