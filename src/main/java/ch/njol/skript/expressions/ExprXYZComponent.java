package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;

import java.util.Locale;

@Name("Vector/Quaternion - WXYZ Component")
@Description({
	"Gets or changes the W, X, Y or Z component of <a href='classes.html#vector'>vectors</a>/<a href='classes.html#quaternion'>quaternions</a>.",
	"You cannot use the W component with vectors; it is for quaternions only."
})
@Examples({
	"set {_v} to vector 1, 2, 3",
	"send \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"",
	"add 1 to x of {_v}",
	"add 2 to y of {_v}",
	"add 3 to z of {_v}",
	"send \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"",
	"set x component of {_v} to 1",
	"set y component of {_v} to 2",
	"set z component of {_v} to 3",
	"send \"%x component of {_v}%, %y component of {_v}%, %z component of {_v}%\""
})
@Since("2.2-dev28, INSERT VERSION (quaternions)")
public class ExprXYZComponent extends SimplePropertyExpression<Object, Number> {

	private static final boolean IS_RUNNING_1194 = Skript.isRunningMinecraft(1, 19, 4);

	static {
		String types = "vectors";
		if (IS_RUNNING_1194)
			types += "/quaternions";
		register(ExprXYZComponent.class, Number.class, "[vector|quaternion] (:w|:x|:y|:z) [component[s]]", types);
	}

	private enum Axis {
		W,
		X,
		Y,
		Z;
	}

	private ExprXYZComponent.@UnknownNullability Axis axis;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		axis = Axis.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Number convert(Object object) {
		if (object instanceof Vector vector) {
			return switch (axis) {
				case W -> null;
				case X -> vector.getX();
				case Y -> vector.getY();
				case Z -> vector.getZ();
			};
		} else if (object instanceof Quaternionf quaternion) {
			return switch (axis) {
				case W -> quaternion.w();
				case X -> quaternion.x();
				case Y -> quaternion.y();
				case Z -> quaternion.z();
			};
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if ((mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET)) {
			boolean acceptsChange;
			if (IS_RUNNING_1194) {
				acceptsChange = Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Vector.class, Quaternionf.class);
			} else {
				acceptsChange = Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Vector.class);
			}
			if (acceptsChange)
				return CollectionUtils.array(Number.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null; // reset/delete not supported
		Object[] objects = getExpr().getArray(event);
		double value = ((Number) delta[0]).doubleValue();

		boolean hasVectors = false;
		boolean hasQuaternions = false;
		boolean hasInvalidInput = false;

		for (Object object : objects) {
			if (object instanceof Vector vector) {
				changeVector(vector, axis, value, mode);
				hasVectors = true;
			} else if (object instanceof Quaternionf quaternion) {
				changeQuaternion(quaternion, axis, (float) value, mode);
				hasQuaternions = true;
			} else {
				hasInvalidInput = true;
			}
		}

		// don't SET the expression if there were invalid inputs
		if (hasInvalidInput)
			return;

		// covers the edge case where an expression can be set to Vector but returns Quaternions, or similar.
		if (hasVectors && !Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Vector.class))
			return;
		if (hasQuaternions && !Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Quaternionf.class))
			return;
		getExpr().change(event, objects, ChangeMode.SET);
	}

	/**
	 * Helper method to modify a single vector's component. Does not call .change().
	 *
	 * @param vector the vector to modify
	 * @param value the value to modify by
	 * @param mode the change mode to determine the modification type
	 */
	private static void changeVector(Vector vector, Axis axis, double value, ChangeMode mode) {
		if (axis == Axis.W)
			return;
		switch (mode) {
			case REMOVE:
				value = -value;
				//$FALL-THROUGH$
			case ADD:
				switch (axis) {
					case X -> vector.setX(vector.getX() + value);
					case Y -> vector.setY(vector.getY() + value);
					case Z -> vector.setZ(vector.getZ() + value);
				}
				break;
			case SET:
				switch (axis) {
					case X -> vector.setX(value);
					case Y -> vector.setY(value);
					case Z -> vector.setZ(value);
				}
				break;
			default:
				assert false;
		}
	}

	/**
	 * Helper method to modify a single quaternion's component. Does not call .change().
	 *
	 * @param quaternion the vector to modify
	 * @param value the value to modify by
	 * @param mode the change mode to determine the modification type
	 */
	private static void changeQuaternion(Quaternionf quaternion, Axis axis, float value, ChangeMode mode) {
		float x = quaternion.x();
		float y = quaternion.y();
		float z = quaternion.z();
		float w = quaternion.w();
		switch (mode) {
			case REMOVE:
				value = -value;
				//$FALL-THROUGH$
			case ADD:
				switch (axis) {
					case W -> w += value;
					case X -> x += value;
					case Y -> y += value;
					case Z -> z += value;
				}
				break;
			case SET:
				switch (axis) {
					case W -> w = value;
					case X -> x = value;
					case Y -> y = value;
					case Z -> z = value;
				}
				break;
		}
		quaternion.set(x, y, z, w);
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return axis.name().toLowerCase(Locale.ENGLISH) + " component";
	}

}
