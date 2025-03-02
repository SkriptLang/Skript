package ch.njol.skript.doc;

import org.bukkit.event.Event;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface AvailableEvents {

	Class<? extends Event>[] value();

}
