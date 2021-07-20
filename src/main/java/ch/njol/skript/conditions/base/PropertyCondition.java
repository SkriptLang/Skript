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
package ch.njol.skript.conditions.base;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

/**
 * This class can be used for an easier writing of conditions that contain only one type in the pattern,
 * and are in one of the following forms:
 * <ul>
 *     <li>something is something</li>
 *     <li>something can something</li>
 *     <li>something has something</li>
 * </ul>
 * The plural and negated forms are also supported.
 *
 * The gains of using this class:
 * <ul>
 *     <li>The {@link ch.njol.skript.lang.Debuggable#toString(Event, boolean)} method is already implemented,
 *     and it works well with the plural and negated forms</li>
 *     <li>You can use the {@link PropertyCondition#register(Class, PropertyType, String, String)}
 *     method for an easy registration</li>
 * </ul>
 *
 * <b>Note:</b> if you choose to register this class in any other way than by calling
 * {@link PropertyCondition#register(Class, PropertyType, String, String)} or
 * {@link PropertyCondition#register(Class, String, String)}, be aware that there can only be two patterns -
 * the first one needs to be a non-negated one and a negated one.
 */
public abstract class PropertyCondition<T> extends Condition implements Checker<T> {
	
	/**
	 * See {@link PropertyCondition} for more info
	 */
	public enum PropertyType {
		/**
		 * Indicates that the condition is in a form of <code>something is/are something</code>,
		 * also possibly in the negated form
		 */
		BE,
		
		/**
		 * Indicates that the condition is in a form of <code>something can something</code>,
		 * also possibly in the negated form
		 */
		CAN,
		
		/**
		 * Indicates that the condition is in a form of <code>something has/have something</code>,
		 * also possibly in the negated form
		 */
		HAVE
	}
	
	@SuppressWarnings("null")
	private Expression<? extends T> expr;
	
	/**
	 * @param c the class to register
	 * @param property the property name, for example <i>fly</i> in <i>players can fly</i>
	 * @param type must be plural, for example <i>players</i> in <i>players can fly</i>
	 */
	public static void register(final Class<? extends Condition> c, final String property, final String type) {
		register(c, PropertyType.BE, property, type);
	}
	
	/**
	 * @param c the class to register
	 * @param propertyType the property type, see {@link PropertyType}
	 * @param property the property name, for example <i>fly</i> in <i>players can fly</i>
	 * @param type must be plural, for example <i>players</i> in <i>players can fly</i>
	 */
	public static void register(final Class<? extends Condition> c, final PropertyType propertyType, final String property, final String type) {
		if (type.contains("%")) {
			throw new SkriptAPIException("The type argument must not contain any '%'s");
		}
		switch (propertyType) {
			case BE:
				Skript.registerCondition(c,
						"%" + type + "% (is|are) " + property,
						"%" + type + "% (isn't|is not|aren't|are not) " + property);
				break;
			case CAN:
				Skript.registerCondition(c,
						"%" + type + "% can " + property,
						"%" + type + "% (can't|cannot|can not) " + property);
				break;
			case HAVE:
				Skript.registerCondition(c,
						"%" + type + "% (has|have) " + property,
						"%" + type + "% (doesn't|does not|do not|don't) have " + property);
				break;
			default:
				assert false;
		}
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		expr = (Expression<? extends T>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public final boolean check(final Event e) {
		return expr.check(e, this, isNegated());
	}
	
	@Override
	public abstract boolean check(T t);
	
	protected abstract String getPropertyName();
	
	protected PropertyType getPropertyType() {
		return PropertyType.BE;
	}
	
	/**
	 * Sets the expression this condition checks a property of. No reference to the expression should be kept.
	 *
	 * @param expr
	 */
	protected final void setExpr(final Expression<? extends T> expr) {
		this.expr = expr;
	}
	
	/**
	 * @return The expression this condition checks the property of
	 */
	public final Expression<? extends T> getExpr() {
		return expr;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return toString(this, getPropertyType(), e, debug, expr, getPropertyName());
	}
	
	public static String toString(Condition condition, PropertyType propertyType, @Nullable Event e,
								  boolean debug, Expression<?> expr, String property) {
		switch (propertyType) {
			case BE:
				return expr.toString(e, debug) + (expr.isSingle() ? " is " : " are ") + (condition.isNegated() ? "not " : "") + property;
			case CAN:
				return expr.toString(e, debug) + (condition.isNegated() ? " can't " : " can ") + property;
			case HAVE:
				if (expr.isSingle())
					return expr.toString(e, debug) + (condition.isNegated() ? " doesn't have " : " has ") + property;
				else
					return expr.toString(e, debug) + (condition.isNegated() ? " don't have " : " have ") + property;
			default:
				assert false;
				throw new AssertionError();
		}
	}
}
