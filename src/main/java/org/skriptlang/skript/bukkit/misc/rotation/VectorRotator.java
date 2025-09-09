package org.skriptlang.skript.bukkit.misc.rotation;

import org.jetbrains.annotations.Contract;
import org.joml.Vector3d;

import java.util.function.Function;

/**
 * Rotates {@link Vector3d}s around the X, Y, and Z axes, as well as any arbitrary axis.
 * Does not support local axes.
 */
public class VectorRotator implements Rotator<Vector3d> {

	private final Function<Vector3d, Vector3d> rotator;

	public VectorRotator(Axis axis, double angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.rotateX(angle);
			case Y -> (input) -> input.rotateY(angle);
			case Z -> (input) -> input.rotateZ(angle);
			case ARBITRARY -> throw new UnsupportedOperationException("Rotation around the " + axis + " axis requires additional data. Use a different constructor.");
			case LOCAL_ARBITRARY, LOCAL_X, LOCAL_Y, LOCAL_Z -> (input) -> input;
		};
	}

	public VectorRotator(Axis axis, Vector3d vector, double angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.rotateX(angle);
			case Y -> (input) -> input.rotateY(angle);
			case Z -> (input) -> input.rotateZ(angle);
			case ARBITRARY -> (input) -> input.rotateAxis(angle, vector.x, vector.y, vector.z);
			case LOCAL_ARBITRARY, LOCAL_X, LOCAL_Y, LOCAL_Z -> (input) -> input;
		};
	}

	@Override
	@Contract("_ -> param1")
	public Vector3d rotate(Vector3d input) {
		return rotator.apply(input);
	}

}
