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

import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.Nullable;

public interface Color extends YggdrasilExtendedSerializable {

	/**
	 * Gets Bukkit color representing this color.
	 * @return Bukkit color.
	 */
	org.bukkit.Color asBukkitColor();

	/**
	 * @return The alpha component of this color.
	 */
	int getAlpha();

	/**
	 * @return The red component of this color.
	 */
	int getRed();

	/**
	 * @return The green component of this color.
	 */
	int getGreen();

	/**
	 * @return The blue component of this color.
	 */
	int getBlue();

	/**
	 * Gets Bukkit dye color representing this color, if one exists.
	 * @return Dye color or null.
	 */
	@Nullable
	DyeColor asDyeColor();

	/**
	 * @return Name of the color.
	 */
	String getName();

}
