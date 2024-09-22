package org.skriptlang.skript.bukkit.misc.rotation;

import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Function;

/**
 * Rotates {@link Quaternionf}s around the X, Y, and Z axes, as well as any arbitrary axis.
 * Supports all local axes.
 */
public class QuaternionRotator implements Rotator<Quaternionf> {

	private final Function<Quaternionf, Quaternionf> rotator;

	/*
	 * NOTE: the apparent mismatch between the axis and methods for local/non-local is intentional.
	 * Rotating quaternions via rotateLocal results in a visual rotation around the global axis.
	 */

	public QuaternionRotator(Axis axis, float angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.rotateLocalX(angle);
			case Y -> (input) -> input.rotateLocalY(angle);
			case Z -> (input) -> input.rotateLocalZ(angle);
			case LOCAL_X -> (input) -> input.rotateX(angle);
			case LOCAL_Y -> (input) -> input.rotateY(angle);
			case LOCAL_Z -> (input) -> input.rotateZ(angle);
			case LOCAL_ARBITRARY -> throw new UnsupportedOperationException("Rotation around the " + axis + " axis requires additional data. Use a different constructor.");
			case ARBITRARY -> (input) -> input;
		};
	}

	public QuaternionRotator(Axis axis, Vector3f vector, float angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.rotateLocalX(angle);
			case Y -> (input) -> input.rotateLocalY(angle);
			case Z -> (input) -> input.rotateLocalZ(angle);
			case LOCAL_X -> (input) -> input.rotateX(angle);
			case LOCAL_Y -> (input) -> input.rotateY(angle);
			case LOCAL_Z -> (input) -> input.rotateZ(angle);
			case LOCAL_ARBITRARY -> (input) -> input.rotateAxis(angle, vector);
			case ARBITRARY -> (input) -> input;
		};
	}

	@Override
	@Contract("_ -> param1")
	public Quaternionf rotate(Quaternionf input) {
		return rotator.apply(input);
	}

}
