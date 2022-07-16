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

import org.bukkit.block.Biome;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains utility methods related to biomes
 */
public class BiomeUtils {

	private final static EnumUtils<Biome> util = new EnumUtils<>(Biome.class, "biomes");

	@Nullable
	public static Biome parse(String name) {
		return util.parse(name);
	}

	public static String toString(Biome biome, int flags) {
		return util.toString(biome, flags);
	}

	public static String getAllNames() {
		return util.getAllNames();
	}

}
