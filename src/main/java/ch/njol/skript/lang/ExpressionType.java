package ch.njol.skript.lang;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

/**
 * Used to define in which order to parse expressions.
 * @deprecated This has been replaced by {@link Priority}.
 * See the documentation of each element to determine their replacements.
 */
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public enum ExpressionType {

	/**
	 * Expressions that only match simple text, e.g. "[the] player"
	 * @deprecated Use {@link SyntaxInfo#SIMPLE}.
	 */
	SIMPLE(SyntaxInfo.SIMPLE),

	/**
	 * Expressions that are related to the Event that are typically simple.
	 * 
	 * @see EventValueExpression
	 * @deprecated Use {@link EventValueExpression#DEFAULT_PRIORITY}.
	 */
	EVENT(EventValueExpression.DEFAULT_PRIORITY),

	/**
	 * Expressions that contain other expressions, e.g. "[the] distance between %location% and %location%"
	 * 
	 * @see #PROPERTY
	 * @deprecated Use {@link SyntaxInfo#COMBINED}.
	 */
	COMBINED(SyntaxInfo.COMBINED),

	/**
	 * Property expressions, e.g. "[the] data value[s] of %items%"/"%items%'[s] data value[s]"
	 * 
	 * @see PropertyExpression
	 * @deprecated Use {@link PropertyExpression#DEFAULT_PRIORITY}.
	 */
	PROPERTY(PropertyExpression.DEFAULT_PRIORITY),

	/**
	 * Expressions whose pattern matches (almost) everything. Typically when using regex. Example: "[the] [loop-]<.+>"
	 * @deprecated Use {@link SyntaxInfo#PATTERN_MATCHES_EVERYTHING}.
	 */
	PATTERN_MATCHES_EVERYTHING(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);

	private final Priority priority;

	ExpressionType(Priority priority) {
		this.priority = priority;
	}

	/**
	 * @return The Priority equivalent of this ExpressionType.
	 */
	public Priority priority() {
		return priority;
	}

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
