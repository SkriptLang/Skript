package ch.njol.skript.doc;

import org.skriptlang.skript.lang.properties.Property;

import java.lang.annotation.*;

/**
 * Provides a {@link Property} to be used in documentation.
 * Only for classes which use a {@link Property}.
 * <br>
 * The name of the property should be supplied as the value of this annotation.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RelatedProperty {
	String value();
}
