package ch.njol.skript.lang;

import ch.njol.skript.registrations.Classes;
import org.bukkit.event.Event;

/**
 * This interface provides methods for {@link Condition}s to provide expected and received values for {@link ch.njol.skript.test.runner.EffAssert}
 * or others to use to in debugging or testing scenarios. <br>
 * <br>
 * Expected values should be the value being compared against, the source of truth. <br>
 * Received values should be the value being tested, the value that may or may not be correct. 
 */
public interface VerboseAssert {
	
	/**
	 * This method is intended to be used directly after {@code "Expected "} and the grammar of the returned string should match.
	 * 
	 * @param event The event used to evaluate this condition.
	 * @return The expected value in this condition, formatted as a readable string.
	 */
	String getExpectedMessage(Event event);
	
	/**
	 * This method is intended to be used directly after {@code "Expected x, but got "} and the grammar of the returned string should match.
	 *
	 * @param event The event used to evaluate this condition.
	 * @return The received value in this condition, formatted as a readable string.
	 */
	String getReceivedMessage(Event event);
	
	/**
	 * Helper method to simplify stringify-ing the values of expressions.
	 * Evaluates the expression using {@link Expression#getAll(Event)} and stringifies using {@link Classes#toString(Object[], boolean)}.
	 * @param expression The expression to evaluate
	 * @param event The event used for evaluation
	 * @return The string representation of the expression's value.
	 */
	static String getExpressionValue(Expression<?> expression, Event event) {
		return Classes.toString(expression.getAll(event), expression.getAnd());
	}
	
}
