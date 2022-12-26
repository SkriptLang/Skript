package ch.njol.skript.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks the annotated element as being subject to removal in the future.
 * <p>
 * The annotated element should also be annotated with {@link Deprecated}.
 * <p>
 * It is recommended to provide when the annotated element will be removed,
 * using the {@code version} element.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface MarkedForRemoval {

	/**
	 * When the annotated element is expected to be removed.
	 * <p>
	 * For example, this could be {@code after "2.6.4"},
	 * {@code "starting from 2.7"} or simply {@code "2.7"}.
	 */
	String version() default "";

}
