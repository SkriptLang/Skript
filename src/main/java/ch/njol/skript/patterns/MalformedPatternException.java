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
package ch.njol.skript.patterns;

import org.eclipse.jdt.annotation.Nullable;

public class MalformedPatternException extends IllegalArgumentException {

	public MalformedPatternException(String pattern, String message) {
		this(pattern, message, null);
	}

	public MalformedPatternException(String pattern, String message, @Nullable Throwable cause) {
		super(message + " [pattern: " + pattern + "]", cause);
	}

}
