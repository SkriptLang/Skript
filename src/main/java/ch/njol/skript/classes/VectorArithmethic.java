package ch.njol.skript.classes;

import org.bukkit.util.Vector;

/**
 * @author bi0qaw
 */
@Deprecated
public class VectorArithmethic implements Arithmetic<Vector, Vector> {
	
	@Override
	public Vector difference(final Vector first, final Vector second) {
		return new Vector(Math.abs(first.getX() - second.getX()), Math.abs(first.getY() - second.getY()), Math.abs(first.getZ() - second.getZ()));
	}
	
	@Override
	public Vector add(final Vector value, final Vector difference) {
		return new Vector().add(value).add(difference);
	}
	
	@Override
	public Vector subtract(Vector value, Vector difference) {
		return new Vector().add(value).subtract(difference);
	}

	@Override
	public Vector multiply(Vector value, Vector multiplier) {
		return value.clone().multiply(multiplier);
	}

	@Override
	public Vector divide(Vector value, Vector divider) {
		return value.clone().divide(divider);
	}

	@Override
	public Vector power(Vector value, Vector exponent) {
		return new Vector(Math.pow(value.getX(), exponent.getX()), Math.pow(value.getY(), exponent.getY()), Math.pow(value.getZ(), exponent.getZ()));
	}
}
