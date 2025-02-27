package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.bukkit.misc.rotation.NonMutatingQuaternionRotator;
import org.skriptlang.skript.bukkit.misc.rotation.NonMutatingVectorRotator;
import org.skriptlang.skript.bukkit.misc.rotation.Rotator;
import org.skriptlang.skript.bukkit.misc.rotation.Rotator.Axis;

import java.util.Locale;
import java.util.Objects;

@Name("Rotated Quaternion/Vector")
@Description({
	"Rotates a quaternion or vector around an axis a set amount of degrees, or around all 3 axes at once.",
	"Vectors can only be rotated around the global X/Y/Z axes, or an arbitrary vector axis.",
	"Quaternions are more flexible, allowing rotation around the global or local X/Y/Z axes, arbitrary vectors, or all 3 local axes at once.",
	"Global axes are the ones in the Minecraft world. Local axes are relative to how the quaternion is already oriented.",
	"",
	"Note that rotating a quaternion around a vector results in a rotation around the local vector, so results may not be what you expect. " +
	"For example, rotating around vector(1, 0, 0) is the same as rotating around the local X axis.",
	"The same applies to rotations by all three axes at once. " +
	"In addition, rotating around all three axes of a quaternion/display at once will rotate in ZYX order, meaning the Z rotation will be applied first and the X rotation last."
})
@Examples({
	"set {_new} to {_quaternion} rotated around x axis by 10 degrees",
	"set {_new} to {_vector} rotated around vector(1, 1, 1) by 45",
	"set {_new} to {_quaternion} rotated by x 45, y 90, z 135"
})
@Since("2.10")
public class ExprRotate extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprRotate.class, Object.class, ExpressionType.SIMPLE,
				"%quaternions/vectors% rotated around [the] [global] (:x|:y|:z)(-| )axis by %number%",
				"%quaternions% rotated around [the|its|their] local (:x|:y|:z)(-| )ax(i|e)s by %number%",
				"%quaternions/vectors% rotated around [the] %vector% by %number%",
				"%quaternions% rotated by x %number%, y %number%(, [and]| and) z %number%");
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
	@Nullable
	protected Object[] get(Event event) {
		if (matchedPattern == 3) {
			Number x = this.x.getSingle(event);
			Number y = this.y.getSingle(event);
			Number z = this.z.getSingle(event);
			if (x == null || y == null || z == null)
				return new Quaternionf[0];

			float radX = (float) (x.floatValue() * Math.PI / 180);
			float radY = (float) (y.floatValue() * Math.PI / 180);
			float radZ = (float) (z.floatValue() * Math.PI / 180);

			//noinspection unchecked
			return ((Expression<Quaternionf>) toRotate).stream(event)
				.map(quaternion -> quaternion.rotateZYX(radZ, radY, radX))
				.toArray(Quaternionf[]::new);
		}

		// rotate around axis
		Number angle = this.angle.getSingle(event);
		if (angle == null)
			return new Object[0];
		double radAngle = (angle.doubleValue() * Math.PI / 180);
		if (Double.isInfinite(radAngle) || Double.isNaN(radAngle))
			return new Object[0];

		Rotator<Vector> vectorRotator;
		Rotator<Quaternionf> quaternionRotator;

		if (axis == Axis.ARBITRARY) {
			// rotate around arbitrary axis
			Vector axis = vector.getSingle(event);
			if (axis == null || axis.isZero())
				return new Object[0];
			axis.normalize();
			Vector3f jomlAxis = axis.toVector3f();
			vectorRotator = new NonMutatingVectorRotator(Axis.ARBITRARY, axis, radAngle);
			quaternionRotator = new NonMutatingQuaternionRotator(Axis.LOCAL_ARBITRARY, jomlAxis, (float) radAngle);
		} else {
			vectorRotator = new NonMutatingVectorRotator(axis, radAngle);
			quaternionRotator = new NonMutatingQuaternionRotator(axis, (float) radAngle);
		}

		return toRotate.stream(event)
			.map(object -> {
				if (object instanceof Vector vectorToRotate) {
					return vectorRotator.rotate(vectorToRotate);
				} else if (object instanceof Quaternionf quaternion) {
					return quaternionRotator.rotate(quaternion);
				}
				return null;
			})
			.filter(Objects::nonNull)
			.toArray();
	}

	@Override
	public boolean isSingle() {
		return toRotate.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		return (matchedPattern == 1 || matchedPattern == 3) ? Quaternionf.class : toRotate.getReturnType();
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return new Class<?>[]{Quaternionf.class, Vector.class};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (matchedPattern) {
			case 0, 1 -> toRotate.toString(event, debug) +
				" rotated around the " + axis + "-axis " +
				"by " + angle.toString(event, debug) + " degrees";
			case 2 -> toRotate.toString(event, debug) +
				" rotated around " + vector.toString(event, debug) + "-axis " +
				"by " + angle.toString(event, debug) + " degrees";
			case 3 -> toRotate.toString(event, debug) +
				" rotated by x " + x.toString(event, debug) + ", " +
				"y " + y.toString(event, debug) + ", " +
				"and z " + z.toString(event, debug);
			default -> "invalid";
		};
	}

}
