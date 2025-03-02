package ch.njol.skript.doc;

import org.bukkit.event.Event;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AvailableEvent.AvailableEvents.class)
@Documented
public @interface AvailableEvent {

	public Class<? extends Event>[] value();


	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface AvailableEvents {

		AvailableEvent[] value();

	}

}
