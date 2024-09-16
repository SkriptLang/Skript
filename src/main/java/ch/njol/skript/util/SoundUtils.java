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

import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class SoundUtils {
	private SoundUtils() {}
	
	static {
		assert false;
	}
	
	private final static EnumUtils<Sound> util = new EnumUtils<>(Sound.class, "sounds");
	
	@Nullable
	public static Sound parse(final String s) {
		return util.parse(s);
	}
	
	public static String toString(final Sound s, final int flags) {
		return util.toString(s, flags);
	}
	
	public static String getAllNames() {
		return util.getAllNames();
	}
	
}
