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
package ch.njol.util;

import ch.njol.skript.effects.EffVectorRotateAroundAnother;
import ch.njol.skript.effects.EffVectorRotateXYZ;
import ch.njol.skript.expressions.ExprVectorCylindrical;
import ch.njol.skript.expressions.ExprVectorFromYawAndPitch;
import ch.njol.skript.expressions.ExprVectorSpherical;
import ch.njol.skript.expressions.ExprYawPitch;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.ScheduledForRemoval
public final class VectorMath {

	public static final double PI = Math.PI;
	public static final double HALF_PI = PI / 2;
	public static final double DEG_TO_RAD = Math.PI / 180;
	public static final double RAD_TO_DEG =  180 / Math.PI;

	private VectorMath() {}

	public static Vector fromSphericalCoordinates(double radius, double theta, double phi) {
		return ExprVectorSpherical.fromSphericalCoordinates(radius, theta, phi);
	}

	public static Vector fromCylindricalCoordinates(double radius, double phi, double height) {
		return ExprVectorCylindrical.fromCylindricalCoordinates(radius, phi, height);
	}

	public static Vector fromYawAndPitch(float yaw, float pitch) {
		return ExprYawPitch.fromYawAndPitch(yaw, pitch);
	}

	public static float getYaw(Vector vector) {
		return ExprYawPitch.getYaw(vector);
	}

	public static float getPitch(Vector vector) {
		return ExprYawPitch.getPitch(vector);
	}

	public static Vector rotX(Vector vector, double angle) {
		return EffVectorRotateXYZ.rotX(vector, angle);
	}

	public static Vector rotY(Vector vector, double angle) {
		return EffVectorRotateXYZ.rotY(vector, angle);
	}

	public static Vector rotZ(Vector vector, double angle) {
		return EffVectorRotateXYZ.rotZ(vector, angle);
	}

	public static Vector rot(Vector vector, Vector axis, double angle) {
		return EffVectorRotateAroundAnother.rot(vector, axis, angle);
	}

	public static float skriptYaw(float yaw) {
		return ExprYawPitch.skriptYaw(yaw);
	}

	public static float skriptPitch(float pitch) {
		return ExprYawPitch.skriptPitch(pitch);
	}

	public static float fromSkriptYaw(float yaw) {
		return ExprVectorFromYawAndPitch.fromSkriptYaw(yaw);
	}

	public static float fromSkriptPitch(float pitch) {
		return ExprVectorFromYawAndPitch.fromSkriptPitch(pitch);
	}

	public static float wrapAngleDeg(float angle) {
		return ExprVectorFromYawAndPitch.wrapAngleDeg(angle);
	}

	public static void copyVector(Vector vector1, Vector vector2) {
		vector1.copy(vector2);
	}

}
