package ch.njol.skript.doc;

import org.bukkit.event.Event;

import java.lang.annotation.*;

// FIXME Adjust the link if necessary
/**
 * Provides a {@link org.bukkit.event.Event} that the syntax element can be used in.
 * This annotation can be stacked multiple times
 * <p>
 * This annotation should only include a single event class, for multiple, use {@link ch.njol.skript.doc.AvailableEvents}
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AvailableEvent.AvailableEvents.class)
@Documented
@Events("WOw!")
public @interface AvailableEvent {

	Class<? extends Event> value();


	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface AvailableEvents {

		AvailableEvent[] value();

	}

}
