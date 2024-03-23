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
package org.skriptlang.skript.lang.util;

import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class JomlBukkitUtils {

	/**
	 * Converts Joml {@link Vector3f} to Bukkit {@link Vector}
	 * 
	 * @param vector {@link Vector3f}
	 * @return converted vector as Bukkit {@link Vector}
	 */
	public static Vector toBukkitVector(Vector3f vector) {
		return new Vector(vector.x, vector.y, vector.z);
	}

	/**
	 * Converts Bukkit {@link Vector} to Joml {@link Vector3f}
	 * 
	 * @param vector {@link Vector}
	 * @return converted vector as Joml {@link Vector3f}
	 */
	public static Vector3f toVector(Vector vector) {
		return new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
	}

}
