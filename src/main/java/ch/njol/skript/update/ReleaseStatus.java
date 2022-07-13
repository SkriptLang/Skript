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
package ch.njol.skript.update;


/**
 * Status of currently installed release.
 */
public enum ReleaseStatus {
	
	/**
	 * Latest release in channel. This is a good thing.
	 */
	LATEST,
	
	/**
	 * Old, probably unsupported release.
	 */
	OUTDATED,
	
	/**
	 * Updates have not been checked, so it not known if any exist.
	 */
	UNKNOWN,
	
	/**
	 * Updates have been checked, but this release was not found at all.
	 * It might be not yet published.
	 */
	CUSTOM,

	/**
	 * Running a developer/nightly build, updates will not be checked.
	 */
	DEVELOPMENT
}
