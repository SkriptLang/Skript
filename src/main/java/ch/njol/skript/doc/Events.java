package ch.njol.skript.doc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;

/**
 * @author Peter Güttinger
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Events {
	/**
	 * A list of {@link SkriptEventInfo#getName() name(s)} of {@link SkriptEvent events} this expression is useful for.
	 */
	String[] value();
}
