package org.skriptlang.skript.lang.properties.handlers;


import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

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
