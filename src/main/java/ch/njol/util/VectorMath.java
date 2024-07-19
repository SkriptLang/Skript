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

import org.bukkit.util.Vector;

/**
 * @author bi0qaw
 */
public class VectorMath {

	public static final double PI = Math.PI;
	public static final double HALF_PI = PI / 2;
	public static final double DEG_TO_RAD = PI / 180;
	public static final double RAD_TO_DEG =  180 / PI;

	public static Vector fromSphericalCoordinates(double radius, double theta, double phi) {
		double r = Math.abs(radius);
		double t = theta * DEG_TO_RAD;
		double p = phi * DEG_TO_RAD;
		double sinp = Math.sin(p);
		double x = r * sinp * Math.cos(t);
		double y = r * Math.cos(p);
		double z = r * sinp * Math.sin(t);
		return new Vector(x, y, z);
	}

	public static Vector fromCylindricalCoordinates(double radius, double phi, double height) {
		double r = Math.abs(radius);
		double p = phi * DEG_TO_RAD;
		double x = r * Math.cos(p);
		double z = r * Math.sin(p);
		return new Vector(x, height, z);

	}

	public static Vector fromYawAndPitch(float yaw, float pitch) {
		double y = Math.sin(pitch * DEG_TO_RAD);
		double div = Math.cos(pitch * DEG_TO_RAD);
		double x = Math.cos(yaw * DEG_TO_RAD);
		double z = Math.sin(yaw * DEG_TO_RAD);
		x *= div;
		z *= div;
		return new Vector(x,y,z);
	}

	public static float getYaw(Vector vector) {
		if (((Double) vector.getX()).equals((double) 0) && ((Double) vector.getZ()).equals((double) 0)){
			return 0;
		}
		return (float) (Math.atan2(vector.getZ(), vector.getX()) * RAD_TO_DEG);
	}

	public static float getPitch(Vector vector) {
		double xy = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
		return (float) (Math.atan(vector.getY() / xy) * RAD_TO_DEG);
	}

	public static Vector setYaw(Vector vector, float yaw) {
		vector = fromYawAndPitch(yaw, getPitch(vector));
		return vector;
	}

	public static Vector setPitch(Vector vector, float pitch) {
		vector = fromYawAndPitch(getYaw(vector), pitch);
		return vector;
	}

	public static Vector rotX(Vector vector, double angle) {
		double sin = Math.sin(angle * DEG_TO_RAD);
		double cos = Math.cos(angle * DEG_TO_RAD);
		Vector vy = new Vector(0, cos, -sin);
		Vector vz = new Vector(0, sin, cos);
		Vector clone = vector.clone();
		vector.setY(clone.dot(vy));
		vector.setZ(clone.dot(vz));
		return vector;
	}

	public static Vector rotY(Vector vector, double angle) {
		double sin = Math.sin(angle * DEG_TO_RAD);
		double cos = Math.cos(angle * DEG_TO_RAD);
		Vector vx = new Vector(cos, 0, sin);
		Vector vz = new Vector(-sin, 0, cos);
		Vector clone = vector.clone();
		vector.setX(clone.dot(vx));
		vector.setZ(clone.dot(vz));
		return vector;
	}

	public static Vector rotZ(Vector vector, double angle) {
		double sin = Math.sin(angle * DEG_TO_RAD);
		double cos = Math.cos(angle * DEG_TO_RAD);
		Vector vx = new Vector(cos, -sin, 0);
		Vector vy = new Vector(sin, cos, 0);
		Vector clone = vector.clone();
		vector.setX(clone.dot(vx));
		vector.setY(clone.dot(vy));
		return vector;
	}

	public static Vector rot(Vector vector, Vector axis, double angle) {
		double sin = Math.sin(angle * DEG_TO_RAD);
		double cos = Math.cos(angle * DEG_TO_RAD);
		Vector a = axis.clone().normalize();
		double ax = a.getX();
		double ay = a.getY();
		double az = a.getZ();
		Vector rotx = new Vector(cos+ax*ax*(1-cos), ax*ay*(1-cos)-az*sin, ax*az*(1-cos)+ay*sin);
		Vector roty = new Vector(ay*ax*(1-cos)+az*sin, cos+ay*ay*(1-cos), ay*az*(1-cos)-ax*sin);
		Vector rotz = new Vector(az*ax*(1-cos)-ay*sin, az*ay*(1-cos)+ax*sin, cos+az*az*(1-cos));
		double x = rotx.dot(vector);
		double y = roty.dot(vector);
		double z = rotz.dot(vector);
		vector.setX(x).setY(y).setZ(z);
		return vector;
	}

	public static float notchYaw(float yaw){
		float y = yaw - 90;
		if (y < -180){
			y += 360;
		}
		return y;
	}

	public static float notchPitch(float pitch){
		return -pitch;
	}

	public static float fromNotchYaw(float notchYaw){
		float y = notchYaw + 90;
		if (y > 180){
			y -= 360;
		}
		return y;
	}

	public static float fromNotchPitch(float notchPitch){
		return -notchPitch;
	}

	public static float skriptYaw(float yaw){
		float y = yaw - 90;
		if (y < 0){
			y += 360;
		}
		return y;
	}

	public static float skriptPitch(float pitch){
		return -pitch;
	}

	public static float fromSkriptYaw(float yaw){
		float y = yaw + 90;
		if (y > 360){
			y -= 360;
		}
		return y;
	}

	public static float fromSkriptPitch(float pitch){
		return -pitch;
	}

	public static float wrapAngleDeg(float angle) {
		angle %= 360f;
		if (angle <= -180) {
			return angle + 360;
		} else if (angle > 180) {
			return angle - 360;
		} else {
			return angle;
		}
	}

	/**
	 * Copies vector components of {@code vector2} into {@code vector1}.
	 */
	public static void copyVector(Vector vector1, Vector vector2) {
		vector1.setX(vector2.getX()).setY(vector2.getY()).setZ(vector2.getZ());
	}

	/**
	 * Check whether or not each component of this vector is equal to 0.
	 * <br>Replaces {@code Vector#isZero()} since that method was added in spigot 1.19.3
	 * @return true if equal to zero, false if at least one component is non-zero
	 */
	public static boolean isZero(Vector vector) {
		return (vector.getX() == 0 && vector.getY() == 0 && vector.getZ() == 0);
	}

}
