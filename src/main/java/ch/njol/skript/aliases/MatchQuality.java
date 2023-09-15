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

/**
 * How well two items match each other.
 */
public enum MatchQuality {

	/**
	 * Everything matches.
	 */
	EXACT,

	/**
	 * The matched item has all metadata and block states that matcher has set
	 * to same values that matcher has. It also has additional metadata or
	 * block states.
	 */
	SAME_ITEM,

	/**
	 * The matched and matcher item share a material.
	 */
	SAME_MATERIAL,

	/**
	 * The items share nothing in common.
	 */
	DIFFERENT;

	public boolean isBetter(MatchQuality another) {
		return ordinal() < another.ordinal();
	}

	public boolean isAtLeast(MatchQuality another) {
		return ordinal() <= another.ordinal();
	}
}
