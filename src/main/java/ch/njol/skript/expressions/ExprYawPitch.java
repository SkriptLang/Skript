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
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;

@Name("Yaw / Pitch")
@Description("The yaw or pitch of a location or vector.")
@Examples({"log \"%player%: %location of player%, %player's yaw%, %player's pitch%\" to \"playerlocs.log\"",
	"set {_yaw} to yaw of player",
	"set {_p} to pitch of target entity"})
@Since("2.0, 2.2-dev28 (vector yaw/pitch)")
public class ExprYawPitch extends SimplePropertyExpression<Object, Number> {

	private static final double DEG_TO_RAD = Math.PI / 180;
	private static final double RAD_TO_DEG =  180 / Math.PI;

	static {
		register(ExprYawPitch.class, Number.class, "(0¦yaw|1¦pitch)", "locations/vectors");
	}

	private boolean usesYaw;

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		usesYaw = parseResult.mark == 0;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Number convert(final Object object) {
		if (object instanceof Location) {
			Location l = ((Location) object);
			return usesYaw ? convertToPositive(l.getYaw()) : l.getPitch();
		} else if (object instanceof Vector) {
			Vector vector = ((Vector) object);
			if (usesYaw)
				return skriptYaw(getYaw(vector));
			return skriptPitch(getPitch(vector));
		}
		return null;
	}

	@SuppressWarnings({"null"})
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;
		float value = ((Number) delta[0]).floatValue();
		for (Object single : getExpr().getArray(e)) {
			if (single instanceof Location) {
				changeLocation(((Location) single), value, mode);
			} else if (single instanceof Vector) {
				changeVector(((Vector) single), value, mode);
			}
		}
	}

	private void changeLocation(Location l, float value, ChangeMode mode) {
		switch (mode) {
			case SET:
				if (usesYaw)
					l.setYaw(convertToPositive(value));
				else
					l.setPitch(value);
				break;
			case ADD:
				if (usesYaw)
					l.setYaw(convertToPositive(l.getYaw()) + value);
				else
					l.setPitch(l.getPitch() + value);
				break;
			case REMOVE:
				if (usesYaw)
					l.setYaw(convertToPositive(l.getYaw()) - value);
				else
					l.setPitch(l.getPitch() - value);
				break;
			default:
				break;
		}
	}

	private void changeVector(Vector vector, float n, ChangeMode mode) {
		float yaw = getYaw(vector);
		float pitch = getPitch(vector);
		switch (mode) {
			case REMOVE:
				n = -n;
				//$FALL-THROUGH$
			case ADD:
				if (usesYaw)
					yaw += n;
				else
					pitch -= n; // Negative because of Minecraft's / Skript's upside down pitch
				Vector newVector = fromYawAndPitch(yaw, pitch).multiply(vector.length());
				vector.copy(newVector);
				break;
			case SET:
				if (usesYaw)
					yaw = fromSkriptYaw(n);
				else
					pitch = fromSkriptPitch(n);
				newVector = fromYawAndPitch(yaw, pitch).multiply(vector.length());
				vector.copy(newVector);
		}
	}


	//Some random method decided to use for converting to positive values.
	public float convertToPositive(float f) {
		if (f != 0 && f * -1 == Math.abs(f))
			return 360 + f;
		return f;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return usesYaw ? "yaw" : "pitch";
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static Vector fromYawAndPitch(float yaw, float pitch) {
		double y = Math.sin(pitch * DEG_TO_RAD);
		double div = Math.cos(pitch * DEG_TO_RAD);
		double x = Math.cos(yaw * DEG_TO_RAD);
		double z = Math.sin(yaw * DEG_TO_RAD);
		x *= div;
		z *= div;
		return new Vector(x,y,z);
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static float getYaw(Vector vector) {
		if (((Double) vector.getX()).equals((double) 0) && ((Double) vector.getZ()).equals((double) 0)){
			return 0;
		}
		return (float) (Math.atan2(vector.getZ(), vector.getX()) * RAD_TO_DEG);
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static float getPitch(Vector vector) {
		double xy = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
		return (float) (Math.atan(vector.getY() / xy) * RAD_TO_DEG);
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static float skriptYaw(float yaw) {
		return yaw < 90
			? yaw + 270
			: yaw - 90;
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static float skriptPitch(float pitch) {
		return -pitch;
	}

	private static float fromSkriptYaw(float yaw) {
		return yaw > 270
			? yaw - 270
			: yaw + 90;
	}

	private static float fromSkriptPitch(float pitch) {
		return -pitch;
	}

}
