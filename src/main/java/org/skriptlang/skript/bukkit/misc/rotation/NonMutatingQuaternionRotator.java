package org.skriptlang.skript.bukkit.misc.rotation;

import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Function;

public class NonMutatingQuaternionRotator implements Rotator<Quaternionf> {

	private final Function<Quaternionf, Quaternionf> rotator;

	/*
	 * NOTE: the apparent mismatch between the axis and methods for local/non-local is intentional.
	 * Rotating quaternions via rotateLocal results in a visual rotation around the global axis.
	 */

	public NonMutatingQuaternionRotator(Axis axis, float angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.rotateLocalX(angle, new Quaternionf());
			case Y -> (input) -> input.rotateLocalY(angle, new Quaternionf());
			case Z -> (input) -> input.rotateLocalZ(angle, new Quaternionf());
			case LOCAL_X -> (input) -> input.rotateX(angle, new Quaternionf());
			case LOCAL_Y -> (input) -> input.rotateY(angle, new Quaternionf());
			case LOCAL_Z -> (input) -> input.rotateZ(angle, new Quaternionf());
			case LOCAL_ARBITRARY -> throw new UnsupportedOperationException("Rotation around the " + axis + " axis requires addition data. Use a different constructor.");
			case ARBITRARY -> throw new UnsupportedOperationException("Rotation around the " + axis + " axis is not supported.");
		};
	}

	public NonMutatingQuaternionRotator(Axis axis, Vector3f vector, float angle) {
		this.rotator = switch (axis) {
			case X -> (input) -> input.rotateLocalX(angle, new Quaternionf());
			case Y -> (input) -> input.rotateLocalY(angle, new Quaternionf());
			case Z -> (input) -> input.rotateLocalZ(angle, new Quaternionf());
			case LOCAL_X -> (input) -> input.rotateX(angle, new Quaternionf());
			case LOCAL_Y -> (input) -> input.rotateY(angle, new Quaternionf());
			case LOCAL_Z -> (input) -> input.rotateZ(angle, new Quaternionf());
			case LOCAL_ARBITRARY -> (input) -> input.rotateAxis(angle, vector, new Quaternionf());
			case ARBITRARY -> throw new UnsupportedOperationException("Rotation around the " + axis + " axis is not supported.");
		};
	}

	@Override
	@Contract("_ -> new")
	public Quaternionf rotate(Quaternionf input) {
		return rotator.apply(input);
	}
}
