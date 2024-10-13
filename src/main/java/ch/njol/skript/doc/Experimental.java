package ch.njol.skript.doc;

import org.skriptlang.skript.lang.experiment.LifeCycle;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides information about an experiment which the annotated element requires.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Experimental {

	String featureName();
	LifeCycle phase();
	String feedbackLink() default "";

}
