package ch.njol.skript.doc;

import org.bukkit.event.Event;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AvailableEvents {

	public Class<? extends Event>[] value();

}
