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

import ch.njol.skript.Skript;

public final class ArgsMessage extends Message {
	
	public ArgsMessage(String key) {
		super(key);
	}
	
	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}
	
	public String toString(Object... args) {
		try {
			String val = getValue();
			return val == null ? key : "" + String.format(val, args);
		} catch (IllegalFormatException e) {
			String m = "The formatted message '" + key + "' uses an illegal format: " + e.getLocalizedMessage();
			Skript.adminBroadcast("<red>" + m);
			System.err.println("[Skript] " + m);
			e.printStackTrace();
			return "[ERROR]";
		}
	}
	
}
