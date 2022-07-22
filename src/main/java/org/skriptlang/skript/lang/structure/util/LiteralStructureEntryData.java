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
package org.skriptlang.skript.lang.structure.util;

import ch.njol.skript.lang.ParseContext;
import org.skriptlang.skript.lang.structure.KeyValueStructureEntryData;
import ch.njol.skript.registrations.Classes;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A specific {@link KeyValueStructureEntryData} type designed to parse the
 *  entry's value as a supported literal type.
 * This entry makes use of {@link Classes#parse(String, Class, ParseContext)}
 *  to parse the user's input using registered {@link ch.njol.skript.classes.ClassInfo}s
 *  and {@link ch.njol.skript.classes.Converter}s.
 */
public class LiteralStructureEntryData<T> extends KeyValueStructureEntryData<T> {

	private final Class<T> type;

	/**
	 * @param type The type to parse the value into.
	 */
	public LiteralStructureEntryData(String key, @Nullable T defaultValue, boolean optional, Class<T> type) {
		super(key, defaultValue, optional);
		this.type = type;
	}

	@Override
	@Nullable
	public T getValue(String value) {
		return Classes.parse(value, type, ParseContext.DEFAULT);
	}

}
