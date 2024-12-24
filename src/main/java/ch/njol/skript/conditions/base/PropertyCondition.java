package ch.njol.skript.conditions.base;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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
		HAVE,

		/**
		 * Indicates that the condition is in a form of <code>something will/be something</code>,
		 * also possibly in the negated form
		 */
		WILL
	}

	private Expression<? extends T> expr;

	/**
	 * Registers a new property condition. The property type is set to {@link PropertyType#BE}.
	 *
	 * @param condition the class to register
	 * @param property the property name, for example <i>fly</i> in <i>players can fly</i>
	 * @param type must be plural, for example <i>players</i> in <i>players can fly</i>
	 */
	public static void register(Class<? extends Condition> condition, String property, String type) {
		register(condition, PropertyType.BE, property, type);
	}

	/**
	 * Registers a new property condition.
	 *
	 * @param condition the class to register
	 * @param propertyType the property type, see {@link PropertyType}
	 * @param property the property name, for example <i>fly</i> in <i>players can fly</i>
	 * @param type must be plural, for example <i>players</i> in <i>players can fly</i>
	 */
	public static void register(Class<? extends Condition> condition, PropertyType propertyType, String property, String type) {
		Skript.registerCondition(condition, ConditionType.PROPERTY,
				getPatterns(propertyType, property, type));
	}

	/**
	 * Returns the patterns for the given property type, property and type.
	 *
	 * @param propertyType the property type, see {@link PropertyType}
	 * @param property the property name, for example <i>fly</i> in <i>players can fly</i>
	 * @param type must be plural, for example <i>players</i> in <i>players can fly</i>
	 * @return patterns formmated for {@link Skript#registerCondition(Class, String...)}
	 */
	public static String[] getPatterns(PropertyType propertyType, String property, String type) {
		if (type.contains("%"))
			throw new SkriptAPIException("The type argument must not contain any '%'s");

		return switch (propertyType) {
			case BE -> new String[] {
				"%" + type + "% (is|are) " + property,
				"%" + type + "% (isn't|is not|aren't|are not) " + property
			};
			case CAN -> new String[] {
				"%" + type + "% can " + property,
				"%" + type + "% (can't|cannot|can not) " + property
			};
			case HAVE -> new String[] {
				"%" + type + "% (has|have) " + property,
				"%" + type + "% (doesn't|does not|do not|don't) have " + property
			};
			case WILL -> new String[] {
				"%" + type + "% will " + property,
				"%" + type + "% (will (not|neither)|won't) " + property
			};
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expr = (Expression<? extends T>) expressions[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public final boolean check(Event event) {
		return expr.check(event, this, isNegated());
	}

	@Override
	public abstract boolean check(T value);

	protected abstract String getPropertyName();

	protected PropertyType getPropertyType() {
		return PropertyType.BE;
	}

	/**
	 * Sets the expression this condition checks a property of. No reference to the expression should be kept.
	 *
	 * @param expr The expression property of this property condition.
	 */
	protected final void setExpr(Expression<? extends T> expr) {
		this.expr = expr;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return toString(this, getPropertyType(), event, debug, expr, getPropertyName());
	}

	public static String toString(Condition condition, PropertyType propertyType, @Nullable Event event,
								  boolean debug, Expression<?> expr, String property) {
		switch (propertyType) {
			case BE:
				return expr.toString(event, debug) + (expr.isSingle() ? " is " : " are ") + (condition.isNegated() ? "not " : "") + property;
			case CAN:
				return expr.toString(event, debug) + (condition.isNegated() ? " can't " : " can ") + property;
			case HAVE:
				if (expr.isSingle()) {
					return expr.toString(event, debug) + (condition.isNegated() ? " doesn't have " : " has ") + property;
				} else {
					return expr.toString(event, debug) + (condition.isNegated() ? " don't have " : " have ") + property;
				}
			case WILL:
				return expr.toString(event, debug) + (condition.isNegated() ? " won't " : " will ") + "be " + property;
			default:
				assert false;
				return null;
		}
	}

}
