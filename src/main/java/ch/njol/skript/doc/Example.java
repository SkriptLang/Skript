package ch.njol.skript.doc;

import java.lang.annotation.*;

/**
 * An example to be used in documentation for the annotated element.
 * Multiple example annotations can be stacked on a single syntax element.
 * <p>
 * Each annotation should include a single example.
 * This can be used instead of the existing {@link ch.njol.skript.doc.Examples} annotation.
 * <p>
 * <b>Multi-line examples</b> should use multi-line strings.
 * Note that whitespace and quotes do not need to be escaped in this mode.
 * The indentation will start from the least-indented line (and most IDEs provide a guideline to show this).
 * <pre>{@code
 * @Example("set player's health to 1")
 * @Example("""
 * 		if player's health is greater than 10:
 * 			send "Wow you're really healthy!"
 * 		""")
 * @Example("""
 * 		# sets the player's health to 1
 * 		set player's health to 1""")
 * public class MyExpression extends ... {
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Example.Examples.class)
@Documented
public @interface Example {

	String value();

	boolean inTrigger() default true; // todo needed?

	/**
	 * The internal container annotation for multiple examples.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface Examples {

		Example[] value();

	}

}
