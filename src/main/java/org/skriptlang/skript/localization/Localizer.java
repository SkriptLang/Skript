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
package org.skriptlang.skript.localization;

import ch.njol.skript.localization.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A Localizer is used for the localization of translatable strings.
 *
 * This API is highly experimental and will be subject to change due to pending localization reworks.
 * In its current state, it acts as a bridge between old and new API.
 *
 * @see ch.njol.skript.localization.Language
 */
@ApiStatus.Experimental
public interface Localizer {

	/**
	 * @return A Localizer with no default translations.
	 */
	static Localizer empty() {
		return new LocalizerImpl(null, null);
	}

	/**
	 * An option for creating a Localizer that represents a set of language files.
	 * @param languageFileDirectory {@link #languageFileDirectory()}.
	 * @param dataFileDirectory {@link #dataFileDirectory()}
	 * @return A Localizer populated from the provided directories.
	 */
	static Localizer of(String languageFileDirectory, @Nullable String dataFileDirectory) {
		return new LocalizerImpl(languageFileDirectory, dataFileDirectory);
	}

	default String localize(String key) {
		return Language.get(key);
	}

	/**
	 * The path to the directory containing language files for this Localizer.
	 * When searching for language files on the jar, this will be used as the path.
	 * When searching for language files on the disk, this will be used along with {@link #dataFileDirectory()}.
	 * That is, it is expected that the path <code>dataFileDirectory() + languageFileDirectory()</code> would
	 *  lead to this Localizer's language files on the disk.
	 * @return A string representing the path to the directory.
	 *  Null if this Localizer does not store any language files.
	 */
	@Nullable
	String languageFileDirectory();

	/**
	 * The path to the directory on disk containing data files for this Localizer.
	 *  For example, this may include language files that have been saved to enable user customization.
	 * @return A string representing the path to the directory.
	 *  Null if this Localizer does not store any data files.
	 */
	@Nullable
	String dataFileDirectory();

}
