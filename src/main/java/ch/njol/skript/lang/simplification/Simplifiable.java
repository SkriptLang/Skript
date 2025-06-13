package ch.njol.skript.lang.simplification;

import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SyntaxElement;
import org.bukkit.event.Event;

/**
 * Represents an object that can be simplified to a simpler {@link SyntaxElement}. For example, a complex math equation
 * can be simplified to a single number {@link Literal} if all the inputs are
 * {@link Literal}s.
 * <br>
 * Simplification should never invalidate contracts. For example, any simplified expression should take care to return
 * the same or a more specific type than the original expression, never a more generic type. Likewise, be sure to
 * maintain the behavior of change() and acceptsChange(). Failure to do so can result in unexpected behavior and
 * tricky bugs.
 * @param <S> the type of the simplified object
 */
public interface Simplifiable<S extends SyntaxElement> {

	/**
	 * Simplifies this object. This should be called immediately after init() returns true.
	 * If simplification is not possible, the object is returned as is.
	 * <br>
	 * References to the original object should be replaced with the simplified object.
	 * <br>
	 * Any returned object should attempt to maintain the original value of {@link Debuggable#toString(Event, boolean)}.
	 * An addition indicating that the value was simplified can be added in the debug string. See {@link SimplifiedLiteral}
	 * for an example.
	 * <br>
	 * Simplification should never invalidate contracts. For example, any simplified expression should take care to return
	 * the same or a more specific type than the original expression, never a more generic type. Likewise, be sure to
	 * maintain the behavior of change() and acceptsChange(). Failure to do so can result in unexpected behavior and
	 * tricky bugs.
	 * <br>
	 * Finally, simplified results should update {@link Expression#getSource()} to point to the expression prior to
	 * simplification. This makes maintaining the above contracts easier.
	 *
	 * @return the simplified object.
	 * @see SimplifiedLiteral
	 */
	S simplify();

}
