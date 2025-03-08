package ch.njol.skript.doc;

import org.bukkit.event.Event;

import java.lang.annotation.*;

/**
 * Provides a list of {@link org.bukkit.event.Event}s that this syntax element can be used in.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AvailableEvents {

	Class<? extends Event>[] value();

}
