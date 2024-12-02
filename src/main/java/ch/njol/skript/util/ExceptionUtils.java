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

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;

public abstract class ExceptionUtils {
	private ExceptionUtils() {}
	
	private final static String IO_NODE = "io exceptions";
	
	@Nullable
	public static String toString(final IOException e) {
		if (Language.keyExists(IO_NODE + "." + e.getClass().getSimpleName())) {
			return Language.format(IO_NODE + "." + e.getClass().getSimpleName(), e.getLocalizedMessage());
		}
		if (Skript.testing())
			e.printStackTrace();
		return e.getLocalizedMessage();
	}
	
}
