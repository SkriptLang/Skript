package org.skriptlang.skript.lang.properties.handlers;


import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

/**
 * A handler for types that have W, X, Y, or Z axes.
 * Since this handler contains state information (see the axis field), the newInstance method
 * must be implemented to return a new instance of the handler with the same axis set.
 *
 * @param <Type>      The type of the property holder
 * @param <ValueType> The type of the value returned
 * @see org.skriptlang.skript.bukkit.base.types.LocationClassInfo.LocationWXYZHandler
 */
public abstract class WXYZHandler<Type, ValueType> implements ExpressionPropertyHandler<Type, ValueType> {

	public enum Axis {W, X, Y, Z}

	protected Axis axis;

	@Override
	abstract public PropertyHandler<Type> newInstance();

	/**
	 * @return Whether this handler supports the given axis
	 */
	public abstract boolean supportsAxis(Axis axis);

	/**
	 * Sets the specific axis for this handler to use.
	 *
	 * @param axis The axis to set
	 */
	public void axis(Axis axis) {
		this.axis = axis;
	}

	/**
	 * @return The axis this handler is using
	 */
	public Axis axis() {
		return axis;
	}

}
