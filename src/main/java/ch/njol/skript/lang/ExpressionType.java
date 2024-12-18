package ch.njol.skript.lang;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;

/**
 * Used to define in which order to parse expressions.
 */
public enum ExpressionType {

	/**
	 * Expressions that only match simple text, e.g. "[the] player"
	 */
	SIMPLE,

	/**
	 * Expressions that are related to the Event that are typically simple.
	 * 
	 * @see EventValueExpression
	 */
	EVENT,

	/**
	 * Expressions that contain other expressions, e.g. "[the] distance between %location% and %location%"
	 * 
	 * @see #PROPERTY
	 */
	COMBINED,

	/**
	 * Property expressions, e.g. "[the] data value[s] of %items%"/"%items%'[s] data value[s]"
	 * 
	 * @see PropertyExpression
	 */
	PROPERTY,

	/**
	 * Expressions whose pattern matches (almost) everything. Typically when using regex. Example: "[the] [loop-]<.+>"
	 */
	PATTERN_MATCHES_EVERYTHING;

}
