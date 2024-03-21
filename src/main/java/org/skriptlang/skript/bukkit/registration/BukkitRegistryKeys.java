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

import ch.njol.skript.lang.SkriptEvent;
import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.bukkit.registration.BukkitInfos.Event;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.registration.SyntaxRegistry.ChildKey;
import org.skriptlang.skript.registration.SyntaxRegistry.Key;

import static org.skriptlang.skript.registration.SyntaxRegistry.STRUCTURE;

/**
 * A class containing {@link SyntaxRegistry} keys for Bukkit-specific syntax elements.
 */
@ApiStatus.Experimental
public final class BukkitRegistryKeys {

	private BukkitRegistryKeys() { }

	/**
	 * A key representing the Bukkit-specific {@link SkriptEvent} syntax element.
	 */
	public static final Key<Event<?>> EVENT = ChildKey.of(STRUCTURE, "event");

}
