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
package org.skriptlang.skript.bukkit.registration;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.registration.SyntaxOrigin;

/**
 * An origin for syntax created by Bukkit plugins.
 */
@ApiStatus.Experimental
public final class BukkitOrigin implements SyntaxOrigin {

	/**
	 * @param plugin The plugin the syntax originates from.
	 * @return An origin pointing to the provided plugin.
	 */
	@Contract("_ -> new")
	public static SyntaxOrigin of(Plugin plugin) {
		return new BukkitOrigin(plugin.getName());
	}

	private final String name;

	private BukkitOrigin(String name) {
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

}
