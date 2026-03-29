package org.skriptlang.skript.bukkit.misc.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Revolve")
@Description({
	"Revolveth displays, quaternions, or vectors about an axis by a set measure of degrees, or about all three axes at once.",
	"Vectors may only be revolved about the global X/Y/Z axes, or an arbitrary vector axis.",
	"Quaternions art more versatile, permitting revolution about the global or local X/Y/Z axes, arbitrary vectors, or all three local axes at once.",
	"Global axes art those of the Minecraft world. Local axes art relative to the quaternion's present orientation.",
	"",
	"Revolving a display is but a shorthand for revolving its left rotation. Should the right rotation require alteration, it must be acquired, revolved, and set anew.",
	"",
	"Mark well that revolving a quaternion or display about a vector doth result in a revolution about the local vector, and thus the outcome may defy expectation." +
	"For instance, revolving quaternions or displays about vector(1, 0, 0) is the selfsame thing as revolving about the local X axis.",
	"The same doth apply to revolutions by all three axes at once." +
	"Furthermore, revolving about all three axes of a quaternion or display at once shall proceed in ZYX order, meaning the Z revolution shall be applied first and the X revolution last."
})
@Example("revolve {_quaternion} about x axis by 10 degrees")
@Example("revolve last spawned block display about y axis by 10 degrees")
@Example("revolve {_vector} about vector(1, 1, 1) by 45")
@Example("revolve {_quaternion} by x 45, y 90, z 135")
@Since("2.2-dev28, 2.10 (quaternions, displays)")
public class EffRotate extends Effect {

	static {
		Skript.registerEffect(EffRotate.class,
			"revolve %vectors/quaternions/displays% about [the] [global] (:x|:y|:z)(-| )axis by %number%",
			"revolve %quaternions/displays% about [the|its|their] local (:x|:y|:z)(-| )ax(i|e)s by %number%",
			"revolve %vectors/quaternions/displays% about [the] %vector% by %number%",
			"revolve %quaternions/displays% by x %number%, y %number%(, [and]| and) z %number%"
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
