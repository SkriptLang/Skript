/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.Locale;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * Ported by Sashie from skript-vectors with bi0qaw's permission.
 * @author bi0qaw
 */
@Name("Vector/Quaternion/AxisAngle - XYZ Component")
@Description({
	"Gets or changes the x, y or z component of <a href='classes.html#vector'>vectors</a>/<a href='classes.html#quaternion'>quaternions</a>/<a href='classes.html#axisangle'>axis angles</a>.",
	"You cannot use w of vector. W/ANGLE is for quaternions/axis angles only."
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
@RequiredPlugins("Spigot 1.19.4+ for Quaternions")
@Since("2.2-dev28, INSERT VERSION (Quaternions)")
public class ExprXYZComponent extends SimplePropertyExpression<Object, Number> {

	static {
		String types = "vectors";
		if (Skript.isRunningMinecraft(1, 19, 4))
			types += "/quaternions/axisangle";
		register(ExprXYZComponent.class, Number.class, "[vector|quaternion] (w:(w|angle)|:x|:y|:z) [component[s]]", types);
	}

	private enum AXIS {
		W,
		X,
		Y,
		Z;
	}

	private AXIS axis;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		axis = AXIS.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Number convert(Object object) {
		if (object instanceof Vector) {
			if (axis == AXIS.W)
				return null;
			Vector vector = (Vector) object;
			return axis == AXIS.X ? vector.getX() : (axis == AXIS.Y ? vector.getY() : vector.getZ());
		} else if (object instanceof Quaternionf) {
			Quaternionf quaternion = (Quaternionf) object;
			switch (axis) {
				case W:
					return quaternion.w();
				case X:
					return quaternion.x();
				case Y:
					return quaternion.y();
				case Z:
					return quaternion.z();
				default:
					return null;
			}
		} else if (object instanceof AxisAngle4f) {
			AxisAngle4f axisAngle = (AxisAngle4f) object;
			switch (axis) {
				case W:
					return axisAngle.angle;
				case X:
					return axisAngle.x;
				case Y:
					return axisAngle.y;
				case Z:
					return axisAngle.z;
				default:
					return null;
			}
		}
		return null;
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getExpr().getReturnType().equals(Quaternionf.class) || getExpr().getReturnType().equals(AxisAngle4f.class)) {
			if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE))
				return new Class[] {Number.class};
		}
		if ((mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET)
				&& getExpr().isSingle() && Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Vector.class))
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert delta != null;
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Vector) {
				if (axis == AXIS.W)
					continue;
				Vector vector = (Vector) object;
				double value = ((Number) delta[0]).doubleValue();
				switch (mode) {
					case REMOVE:
						value = -value;
						//$FALL-THROUGH$
					case ADD:
						if (axis == AXIS.X) {
							vector.setX(vector.getX() + value);
						} else if (axis == AXIS.Y) {
							vector.setY(vector.getY() + value);
						} else {
							vector.setZ(vector.getZ() + value);
						}
						getExpr().change(event, new Vector[] {vector}, ChangeMode.SET);
						break;
					case SET:
						if (axis == AXIS.X) {
							vector.setX(value);
						} else if (axis == AXIS.Y) {
							vector.setY(value);
						} else {
							vector.setZ(value);
						}
						getExpr().change(event, new Vector[] {vector}, ChangeMode.SET);
						break;
					default:
						assert false;
				}
			} else if (object instanceof Quaternionf) {
				float value = ((Number) delta[0]).floatValue();
				Quaternionf quaternion = (Quaternionf) object;
				switch (mode) {
					case REMOVE:
						value = -value;
						//$FALL-THROUGH$
					case ADD:
						if (axis == AXIS.W) {
							quaternion.set(quaternion.w() + value, quaternion.x(), quaternion.y(), quaternion.z());
						} else if (axis == AXIS.X) {
							quaternion.set(quaternion.w(), quaternion.x() + value, quaternion.y(), quaternion.z());
						} else if (axis == AXIS.Y) {
							quaternion.set(quaternion.w(), quaternion.x(), quaternion.y() + value, quaternion.z());
						} else if (axis == AXIS.Z) {
							quaternion.set(quaternion.w(), quaternion.x(), quaternion.y(), quaternion.z() + value);
						}
						if (ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Quaternionf.class))
							getExpr().change(event, new Quaternionf[] {quaternion}, ChangeMode.SET);
						break;
					case SET:
						if (axis == AXIS.W) {
							quaternion.set(value, quaternion.x(), quaternion.y(), quaternion.z());
						} else if (axis == AXIS.X) {
							quaternion.set(quaternion.w(), value, quaternion.y(), quaternion.z());
						} else if (axis == AXIS.Y) {
							quaternion.set(quaternion.w(), quaternion.x(), value, quaternion.z());
						} else if (axis == AXIS.Z) {
							quaternion.set(quaternion.w(), quaternion.x(), quaternion.y(), value);
						}
						if (ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Quaternionf.class))
							getExpr().change(event, new Quaternionf[] {quaternion}, ChangeMode.SET);
						break;
					case DELETE:
					case REMOVE_ALL:
					case RESET:
						assert false;
				}
			} else if (object instanceof AxisAngle4f) {
				float value = ((Number) delta[0]).floatValue();
				AxisAngle4f axisAngle = (AxisAngle4f) object;
				switch (mode) {
					case REMOVE:
						value = -value;
						//$FALL-THROUGH$
					case ADD:
						if (axis == AXIS.W) {
							axisAngle.set(axisAngle.angle + value, axisAngle.x, axisAngle.y, axisAngle.z);
						} else if (axis == AXIS.X) {
							axisAngle.set(axisAngle.angle, axisAngle.x + value, axisAngle.y, axisAngle.z);
						} else if (axis == AXIS.Y) {
							axisAngle.set(axisAngle.angle, axisAngle.x, axisAngle.y + value, axisAngle.z);
						} else if (axis == AXIS.Z) {
							axisAngle.set(axisAngle.angle, axisAngle.x, axisAngle.y, axisAngle.z + value);
						}
						if (ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, AxisAngle4f.class))
							getExpr().change(event, new AxisAngle4f[] {axisAngle}, ChangeMode.SET);
						break;
					case SET:
						if (axis == AXIS.W) {
							axisAngle.set(value, axisAngle.x, axisAngle.y, axisAngle.z);
						} else if (axis == AXIS.X) {
							axisAngle.set(axisAngle.angle, value, axisAngle.y, axisAngle.z);
						} else if (axis == AXIS.Y) {
							axisAngle.set(axisAngle.angle, axisAngle.x, value, axisAngle.z);
						} else if (axis == AXIS.Z) {
							axisAngle.set(axisAngle.angle, axisAngle.x, axisAngle.y, value);
						}
						if (ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, AxisAngle4f.class))
							getExpr().change(event, new AxisAngle4f[] {axisAngle}, ChangeMode.SET);
						break;
					case DELETE:
					case REMOVE_ALL:
					case RESET:
						assert false;
				}
			}
		}
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
