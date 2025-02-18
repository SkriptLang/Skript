package ch.njol.skript.lang;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

/**
 * Used to define in which order to parse expressions.
 */
public enum ExpressionType {

	/**
	 * Expressions that only match simple text, e.g. "[the] player"
	 */
	SIMPLE(SyntaxInfo.SIMPLE),

	/**
	 * Expressions that are related to the Event that are typically simple.
	 * 
	 * @see EventValueExpression
	 */
	EVENT(EventValueExpression.DEFAULT_PRIORITY),

	/**
	 * Expressions that contain other expressions, e.g. "[the] distance between %location% and %location%"
	 * 
	 * @see #PROPERTY
	 */
	COMBINED(SyntaxInfo.COMBINED),

	/**
	 * Property expressions, e.g. "[the] data value[s] of %items%"/"%items%'[s] data value[s]"
	 * 
	 * @see PropertyExpression
	 */
	PROPERTY(PropertyExpression.DEFAULT_PRIORITY),

	/**
	 * Expressions whose pattern matches (almost) everything. Typically when using regex. Example: "[the] [loop-]<.+>"
	 */
	PATTERN_MATCHES_EVERYTHING(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);

	@ApiStatus.Experimental
	private final Priority priority;

	@ApiStatus.Experimental
	ExpressionType(Priority priority) {
		this.priority = priority;
	}

	/**
	 * @return The Priority equivalent of this ExpressionType.
	 */
	@ApiStatus.Experimental
	public Priority priority() {
		return priority;
	}

	@ApiStatus.Experimental
	public static @Nullable ExpressionType fromModern(Priority priority) {
		if (priority == SyntaxInfo.SIMPLE)
			return ExpressionType.SIMPLE;
		if (priority == EventValueExpression.DEFAULT_PRIORITY)
			return ExpressionType.EVENT;
		if (priority == SyntaxInfo.COMBINED)
			return ExpressionType.COMBINED;
		if (priority == PropertyExpression.DEFAULT_PRIORITY)
			return ExpressionType.PROPERTY;
		if (priority == SyntaxInfo.PATTERN_MATCHES_EVERYTHING)
			return ExpressionType.PATTERN_MATCHES_EVERYTHING;
		return null;
	}

}
