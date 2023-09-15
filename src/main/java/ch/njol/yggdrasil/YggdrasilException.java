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
package ch.njol.yggdrasil;

/**
 * Thrown if the object(s) that should be saved/loaded with Yggdrasil do not comply with its requirements, or if Yggdrasil is used incorrectly.
 * <p>
 * A detail message will always be supplied, so fixing these errors should be trivial.
 *
 * @author Peter Güttinger
 */
public class YggdrasilException extends RuntimeException {
	private static final long serialVersionUID = -6130660396780458226L;

	public YggdrasilException(final String message) {
		super(message);
	}

	public YggdrasilException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public YggdrasilException(final Throwable cause) {
		super(cause.getClass().getSimpleName() + (cause.getMessage() == null ? "" : ": " + cause.getMessage()), cause);
	}

}
