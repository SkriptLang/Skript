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
package ch.njol.skript.localization;

import java.util.IllegalFormatException;
import java.util.concurrent.atomic.AtomicReference;

import ch.njol.skript.Skript;

public final class FormattedMessage extends Message {
	
	private final Object[] args;
	
	/**
	 * @param key
	 * @param args An array of Objects to replace into the format message, e.g. {@link AtomicReference}s.
	 */
	public FormattedMessage(final String key, final Object... args) {
		super(key);
		assert args.length > 0;
		this.args = args;
	}
	
	@Override
	public String toString() {
		try {
			String val = getValue();
			return val == null ? key : "" + String.format(val, args);
		} catch (final IllegalFormatException e) {
			String m = "The formatted message '" + key + "' uses an illegal format: " + e.getLocalizedMessage();
			Skript.adminBroadcast("<red>" + m);
			System.err.println("[Skript] " + m);
			e.printStackTrace();
			return "[ERROR]";
		}
	}
	
}
