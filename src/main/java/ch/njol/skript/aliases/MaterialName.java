/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.aliases;

import org.bukkit.Material;

final class MaterialName {
	String singular;
	String plural;
	int gender = 0;
	Material id;

	public MaterialName(final Material id, final String singular, final String plural, final int gender) {
		this.id = id;
		this.singular = singular;
		this.plural = plural;
		this.gender = gender;
	}

	public String toString(boolean p) {
		return p ? plural : singular;
	}

	public String getDebugName(boolean p) {
		// TODO more useful debug name wouldn't hurt
		return p ? plural : singular;
	}
}
