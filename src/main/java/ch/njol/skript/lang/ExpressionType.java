/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

/**
 * Used to define in which order to parse expressions.
 * @deprecated Use {@link org.skriptlang.skript.util.Priority}.
 */
@Deprecated
public enum ExpressionType {

	/**
	 * Expressions that only match simple text, e.g. "[the] player"
	 * @deprecated Use {@link SyntaxInfo#SIMPLE}.
	 */
	@Deprecated
	SIMPLE(SyntaxInfo.SIMPLE),

	/**
	 * Expressions that are related to the Event that are typically simple.
	 * 
	 * @see EventValueExpression
	 * @deprecated Use {@link EventValueExpression#DEFAULT_PRIORITY} when not using the built-in register methods.
	 */
	@Deprecated
	EVENT(EventValueExpression.DEFAULT_PRIORITY),

	/**
	 * Expressions that contain other expressions, e.g. "[the] distance between %location% and %location%"
	 * 
	 * @see #PROPERTY
	 * @deprecated Use {@link SyntaxInfo#COMBINED}.
	 */
	@Deprecated
	COMBINED(SyntaxInfo.COMBINED),

	/**
	 * Property expressions, e.g. "[the] data value[s] of %items%"/"%items%'[s] data value[s]"
	 * 
	 * @see PropertyExpression
	 * @deprecated Use {@link PropertyExpression#DEFAULT_PRIORITY} when not using the built-in register methods.
	 */
	@Deprecated
	PROPERTY(PropertyExpression.DEFAULT_PRIORITY),

	/**
	 * Expressions whose pattern matches (almost) everything. Typically when using regex. Example: "[the] [loop-]<.+>"
	 * @deprecated Use {@link SyntaxInfo#PATTERN_MATCHES_EVERYTHING}.
	 */
	@Deprecated
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

	@Nullable
	public static ExpressionType fromModern(Priority priority) {
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
