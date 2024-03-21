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

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.localization.Language;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;

final class LocalizerImpl implements Localizer {

	private final SkriptAddon addon;

	LocalizerImpl(SkriptAddon addon) {
		this.addon = addon;
	}

	private Class<?> source;
	private String languageFileDirectory;
	private String dataFileDirectory;

	@Override
	public void setSourceDirectories(Class<?> source, String languageFileDirectory, @Nullable String dataFileDirectory) {
		if (this.source != null) {
			throw new SkriptAPIException("A localizer's source directories may only be set once.");
		}
		this.source = source;
		this.languageFileDirectory = languageFileDirectory;
		this.dataFileDirectory = dataFileDirectory;
		Language.loadDefault(addon);
	}

	@Override
	@Nullable
	public Class<?> source() {
		return source;
	}

	@Override
	@Nullable
	public String languageFileDirectory() {
		return languageFileDirectory;
	}

	@Override
	@Nullable
	public String dataFileDirectory() {
		return dataFileDirectory;
	}

	@Override
	@Nullable
	public String translate(String key) {
		return Language.get_(key);
	}

	static final class UnmodifiableLocalizer implements Localizer {

		private final Localizer localizer;

		UnmodifiableLocalizer(Localizer localizer) {
			this.localizer = localizer;
		}

		@Override
		public void setSourceDirectories(Class<?> source, String languageFileDirectory, @Nullable String dataFileDirectory) {
			throw new UnsupportedOperationException("An unmodifiable localizer cannot have translations added.");
		}

		@Override
		@Nullable
		public Class<?> source() {
			return localizer.source();
		}

		@Override
		@Nullable
		public String languageFileDirectory() {
			return localizer.languageFileDirectory();
		}

		@Override
		@Nullable
		public String dataFileDirectory() {
			return localizer.dataFileDirectory();
		}

		@Override
		@Nullable
		public String translate(String key) {
			return localizer.translate(key);
		}

	}

}
