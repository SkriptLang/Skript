package org.skriptlang.skript.bukkit.misc.rotation;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.function.Function;

/**
 * Rotates {@link Vector}s around the X, Y, and Z axes, as well as any arbitrary axis.
 * Does not support local axes.
 * Returns new vector objects rather than mutating the input vector.
 */
public class NonMutatingVectorRotator implements Rotator<Vector> {

	private final Function<Vector, Vector> rotator;

	public NonMutatingVectorRotator(Axis axis, double angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.clone().rotateAroundX(angle);
			case Y -> (input) -> input.clone().rotateAroundY(angle);
			case Z -> (input) -> input.clone().rotateAroundZ(angle);
			case ARBITRARY -> throw new UnsupportedOperationException("Rotation around the " + axis + " axis requires additional data. Use a different constructor.");
			case LOCAL_ARBITRARY, LOCAL_X, LOCAL_Y, LOCAL_Z -> (input) -> input;
		};
	}

	public NonMutatingVectorRotator(Axis axis, Vector vector, double angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.clone().rotateAroundX(angle);
			case Y -> (input) -> input.clone().rotateAroundY(angle);
			case Z -> (input) -> input.clone().rotateAroundZ(angle);
			case ARBITRARY -> (input) -> input.clone().rotateAroundNonUnitAxis(vector, angle);
			case LOCAL_ARBITRARY, LOCAL_X, LOCAL_Y, LOCAL_Z -> (input) -> input;
		};
	}

	@Override
	@Contract("_ -> new")
	public Vector rotate(Vector input) {
		return rotator.apply(input);
	}

}
