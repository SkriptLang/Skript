package org.skriptlang.skript.bukkit.misc.rotation;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.function.Function;

/**
 * Rotates {@link Vector}s around the X, Y, and Z axes, as well as any arbitrary axis.
 * Does not support local axes.
 */
public class VectorRotator implements Rotator<Vector> {

	private final Function<Vector, Vector> rotator;

	public VectorRotator(Axis axis, double angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.rotateAroundX(angle);
			case Y -> (input) -> input.rotateAroundY(angle);
			case Z -> (input) -> input.rotateAroundZ(angle);
			case ARBITRARY -> throw new UnsupportedOperationException("Rotation around the " + axis + " axis requires additional data. Use a different constructor.");
			case LOCAL_ARBITRARY, LOCAL_X, LOCAL_Y, LOCAL_Z -> (input) -> input;
		};
	}

	public VectorRotator(Axis axis, Vector vector, double angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.rotateAroundX(angle);
			case Y -> (input) -> input.rotateAroundY(angle);
			case Z -> (input) -> input.rotateAroundZ(angle);
			case ARBITRARY -> (input) -> input.rotateAroundNonUnitAxis(vector, angle);
			case LOCAL_ARBITRARY, LOCAL_X, LOCAL_Y, LOCAL_Z -> (input) -> input;
		};
	}

	@Override
	@Contract("_ -> param1")
	public Vector rotate(Vector input) {
		return rotator.apply(input);
	}

}
