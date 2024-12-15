package org.skriptlang.skript.bukkit.misc.rotation;

import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Rotates {@link Display}s around the X, Y, and Z axes, as well as any arbitrary axis.
 * Supports all local axes.
 * Modifies the left rotation of the display.
 */
public class DisplayRotator implements Rotator<Display> {

	private final QuaternionRotator qRotator;

	public DisplayRotator(Axis axis, float angle) {
		qRotator = new QuaternionRotator(axis, angle);
	}

	public DisplayRotator(Axis axis, Vector3f vector, float angle) {
		qRotator = new QuaternionRotator(axis, vector, angle);
	}

	@Override
	@Contract("_ -> param1")
	public Display rotate(Display input) {
		Transformation transformation = input.getTransformation();
		Quaternionf leftRotation = transformation.getLeftRotation();
		input.setTransformation(
			new Transformation(
				transformation.getTranslation(),
				qRotator.rotate(leftRotation),
				transformation.getScale(),
				transformation.getRightRotation()
			)
		);
		return input;
	}

}
