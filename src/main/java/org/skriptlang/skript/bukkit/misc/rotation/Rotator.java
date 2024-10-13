package org.skriptlang.skript.bukkit.misc.rotation;

import java.util.Locale;

/**
 * A functional interface to support uniform rotation semantics between various types.
 * A rotator rotates objects around a specific {@link Axis} by a specific angle in radians.
 *
 * @param <T> The class on which this rotator acts.
 *
 * @see	Axis
 */
@FunctionalInterface
public interface Rotator<T> {

	/**
	 * Rotates the input around the rotator's axis by the rotator's angle.
	 * May modify the input.
	 */
	T rotate(T input);

	/**
	 * Represents an axis around which to rotate.
	 */
	enum Axis {
		/**
		 * The global X axis, relative to the world as a whole.
		 */
		X,

		/**
		 * The local X axis, relative to the object being rotated.
		 */
		LOCAL_X,

		/**
		 * The global Y axis, relative to the world as a whole.
		 */
		Y,

		/**
		 * The local Y axis, relative to the object being rotated.
		 */
		LOCAL_Y,

		/**
		 * The global Z axis, relative to the world as a whole.
		 */
		Z,

		/**
		 * The local Z axis, relative to the object being rotated.
		 */
		LOCAL_Z,

		/**
		 * An arbitrary global axis, relative to the world as a whole.
		 */
		ARBITRARY,

		/**
		 * An arbitrary local axis, relative to the object being rotated.
		 */
		LOCAL_ARBITRARY;

		@Override
		public String toString() {
			return super.toString().toLowerCase(Locale.ENGLISH).replace("_", " ");
		}

		/**
		 * A helper method for converting from Bukkit {@link org.bukkit.Axis}.
		 * @param axis the axis to convert from
		 * @return the converted axis
		 */
		public static Axis fromBukkit(org.bukkit.Axis axis) {
			return switch (axis) {
				case X -> Axis.X;
				case Y -> Axis.Y;
				case Z -> Axis.Z;
			};
		}
	}

}
