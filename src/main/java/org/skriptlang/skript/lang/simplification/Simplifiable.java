package org.skriptlang.skript.lang.simplification;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that can be simplified.
 *
 * @param <S> the type of the simplified object
 */
public interface Simplifiable<S> {

	enum Step {
		/**
		 * A simplification step right after
		 * {@link ch.njol.skript.lang.SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}
		 * is called.
		 * Objects should call simplify on their children at this step, using the {@link #PARENT} step.
		 */
		IMMEDIATE,
		/**
		 * A simplification step run after a parent element, if applicable, simplifies.
		 * <br>
		 * Objects may receive this step multiple times, with varying sources.
		 */
		PARENT,
		/**
		 * A simplification step run after the enclosing script, if applicable, completes parsing.
		 */
		SCRIPT
	}

	/**
	 * Simplifies this object at a given step.
	 * If simplification is not possible, the object is returned as is.
	 * All simplification calls should be passed down to children objects, though {@link Step#IMMEDIATE} calls should
	 * become {@link Step#PARENT} calls on the children, with this object as the source.
	 * <br>
	 * References to the original object should be replaced with the simplified object.
	 * <br>
	 * Any returned object should attempt to maintain the original value of {@link ch.njol.skript.lang.Debuggable#toString(Event, boolean)}.
	 * An addition indicating that the value was simplified can be added in the debug string. See {@link SimplifiedLiteral}
	 * for an example.
	 *
	 * @param step the step at which simplify is called.
	 * @param source the source of this simplification. Non-null when step is {@link Step#PARENT}, null otherwise.
	 * @return the simplified object.
	 * @see SimplifiedLiteral
	 */
	S simplify(Step step, @Nullable Simplifiable<?> source);

	/**
	 * Helper method to simplify a child object.
	 * If the step is {@link Step#IMMEDIATE}, the child object will be simplified with {@link Step#PARENT} and
	 * this object as the source.
	 *
	 * @param child the child object to simplify
	 * @param step the step at which simplify is called
	 * @param source the source of this simplification.
	 * @return the simplified child object.
	 * @param <T> the type of the child object
	 */
	default <T> T simplifyChild(@Nullable Simplifiable<T> child, Step step, @Nullable Simplifiable<?> source) {
		if (child == null)
			return null;
		return switch (step) {
			case IMMEDIATE -> child.simplify(Step.PARENT, this);
			case PARENT, SCRIPT -> child.simplify(step, source);
		};
	}
}
