package org.skriptlang.skript.bukkit.misc.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.bukkit.misc.rotation.DisplayRotator;
import org.skriptlang.skript.bukkit.misc.rotation.QuaternionRotator;
import org.skriptlang.skript.bukkit.misc.rotation.Rotator;
import org.skriptlang.skript.bukkit.misc.rotation.Rotator.Axis;
import org.skriptlang.skript.bukkit.misc.rotation.VectorRotator;

import java.util.Locale;

@Name("Rotate")
@Description({
	"Rotates displays, quaternions, or vectors around an axis a set amount of degrees, or around all 3 axes at once.",
	"Vectors can only be rotated around the global X/Y/Z axes, or an arbitrary vector axis.",
	"Quaternions are more flexible, allowing rotation around the global or local X/Y/Z axes, arbitrary vectors, or all 3 local axes at once.",
	"Global axes are the ones in the Minecraft world. Local axes are relative to how the quaternion is already oriented.",
	"",
	"Rotating a display is a shortcut for rotating its left rotation. If the right rotation needs to be modified, it should be acquired, rotated, and re-set.",
	"",
	"Note that rotating a quaternion/display around a vector results in a rotation around the local vector, so results may not be what you expect. " +
	"For example, rotating quaternions/displays around vector(1, 0, 0) is the same as rotating around the local X axis.",
	"The same applies to rotations by all three axes at once. " +
	"In addition, rotating around all three axes of a quaternion/display at once will rotate in ZYX order, meaning the Z rotation will be applied first and the X rotation last."
})
@Examples({
	"rotate {_quaternion} around x axis by 10 degrees",
	"rotate last spawned block display around y axis by 10 degrees",
	"rotate {_vector} around vector(1, 1, 1) by 45",
	"rotate {_quaternion} by x 45, y 90, z 135"
})
@Since("2.2-dev28, 2.10 (quaternions, displays)")
public class EffRotate extends Effect {

	static {
		Skript.registerEffect(EffRotate.class,
			"rotate %vectors/quaternions/displays% around [the] [global] (:x|:y|:z)(-| )axis by %number%",
			"rotate %quaternions/displays% around [the|its|their] local (:x|:y|:z)(-| )ax(i|e)s by %number%",
			"rotate %vectors/quaternions/displays% around [the] %vector% by %number%",
			"rotate %quaternions/displays% by x %number%, y %number%(, [and]| and) z %number%"
		);
	}

	private Expression<?> toRotate;

	private @UnknownNullability Expression<Number> angle;
	private @UnknownNullability Expression<Vector> vector;
	private @UnknownNullability Axis axis;

	private @UnknownNullability Expression<Number> x, y, z;

	private int matchedPattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		toRotate = exprs[0];
		this.matchedPattern = matchedPattern;
		switch (matchedPattern) {
			case 0, 1 -> {
				String axisString = parseResult.tags.get(0).toUpperCase(Locale.ENGLISH);
				if (matchedPattern == 1)
					axisString = "LOCAL_" + axisString;
				angle = (Expression<Number>) exprs[1];
				axis = Axis.valueOf(axisString);
			}
			case 2 -> {
				vector = (Expression<Vector>) exprs[1];
				angle = (Expression<Number>) exprs[2];
				axis = Axis.ARBITRARY;
			}
			case 3 -> {
				x = (Expression<Number>) exprs[1];
				y = (Expression<Number>) exprs[2];
				z = (Expression<Number>) exprs[3];
			}
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (matchedPattern == 3) {
			Number x = this.x.getSingle(event);
			Number y = this.y.getSingle(event);
			Number z = this.z.getSingle(event);
			if (x == null || y == null || z == null)
				return;

			float radX = (float) (x.floatValue() * Math.PI / 180);
			float radY = (float) (y.floatValue() * Math.PI / 180);
			float radZ = (float) (z.floatValue() * Math.PI / 180);

			for (Object object : toRotate.getArray(event)) {
				if (object instanceof Quaternionf quaternion) {
					quaternion.rotateZYX(radZ, radY, radX);
				} else if (object instanceof Display display) {
					Transformation transformation = display.getTransformation();
					Quaternionf leftRotation = transformation.getLeftRotation();
					display.setTransformation(
						new Transformation(
							transformation.getTranslation(),
							leftRotation.rotateZYX(radZ, radY, radX),
							transformation.getScale(),
							transformation.getRightRotation()
						)
					);
				}
			}
			return;
		}

		// rotate around axis
		Number angle = this.angle.getSingle(event);
		if (angle == null)
			return;
		double radAngle = (angle.doubleValue() * Math.PI / 180);
		if (Double.isInfinite(radAngle) || Double.isNaN(radAngle))
			return;

		Rotator<Vector> vectorRotator;
		Rotator<Quaternionf> quaternionRotator;
		Rotator<Display> displayRotator;

		if (axis == Axis.ARBITRARY) {
			// rotate around arbitrary axis
			Vector axis = vector.getSingle(event);
			if (axis == null || axis.isZero())
				return;
			axis.normalize();
			Vector3f jomlAxis = axis.toVector3f();
			vectorRotator = new VectorRotator(Axis.ARBITRARY, axis, radAngle);
			quaternionRotator = new QuaternionRotator(Axis.LOCAL_ARBITRARY, jomlAxis, (float) radAngle);
			displayRotator = new DisplayRotator(Axis.LOCAL_ARBITRARY, jomlAxis, (float) radAngle);
		} else {
			vectorRotator = new VectorRotator(axis, radAngle);
			quaternionRotator = new QuaternionRotator(axis, (float) radAngle);
			displayRotator = new DisplayRotator(axis, (float) radAngle);
		}

		for (Object object : toRotate.getArray(event)) {
			if (object instanceof Vector vectorToRotate) {
				vectorRotator.rotate(vectorToRotate);
			} else if (object instanceof Quaternionf quaternion) {
				quaternionRotator.rotate(quaternion);
			} else if (object instanceof Display display) {
				displayRotator.rotate(display);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (matchedPattern) {
			case 0, 1 -> "rotate " + toRotate.toString(event, debug) +
					" around the " + axis + "-axis " +
					"by " + angle.toString(event, debug) + " degrees";
			case 2 -> "rotate " + toRotate.toString(event, debug) +
					" around " + vector.toString(event, debug) + "-axis " +
					"by " + angle.toString(event, debug) + " degrees";
			case 3 -> "rotate " + toRotate.toString(event, debug) +
					" by x " + x.toString(event, debug) + ", " +
					"y " + y.toString(event, debug) + ", " +
					"and z " + z.toString(event, debug);
			default -> "invalid";
		};
	}

}
