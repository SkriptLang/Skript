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
package ch.njol.skript.util;

import ch.njol.skript.Skript;

public class ReflectConst {

	// Spigot removed the mapping for this method in 1.18, so it's back to the
	// obfuscated method which is found in `net.minecraft.server.MinecraftServer`
	public static String MINECRAFT_SERVER_IS_RUNNING = get("isRunning", "v", "u");

	private static String get(String def, String v118, String v119) {
		if (Skript.isRunningMinecraft(1, 19)) {
			return v119;
		} else if (Skript.isRunningMinecraft(1, 18)) {
			return v118;
		}
		return def;
	}

}
