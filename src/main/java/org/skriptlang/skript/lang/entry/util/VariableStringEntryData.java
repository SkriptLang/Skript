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
package org.skriptlang.skript.lang.entry.util;

import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.parser.ParserInstance;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import ch.njol.skript.util.StringMode;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A type of {@link KeyValueEntryData} designed to parse its value as a {@link VariableString}.
 * The {@link StringMode} may be specified during construction.
 * Constructors without a StringMode parameter assume {@link StringMode#MESSAGE}.
 * This data <b>CAN</b> return null if string parsing fails (e.g. the user formatted their string wrong).
 */
public class VariableStringEntryData extends KeyValueEntryData<VariableString> {

	private final StringMode stringMode;

	/**
	 * Uses {@link StringMode#MESSAGE} as the default string mode.
	 * @see #VariableStringEntryData(String, VariableString, boolean, StringMode)
	 */
	public VariableStringEntryData(
		String key, @Nullable VariableString defaultValue, boolean optional
	) {
		this(key, defaultValue, optional, StringMode.MESSAGE);
	}

	/**
	 * @param stringMode Sets <i>how</i> to parse the string (e.g. as a variable, message, etc.).
	 */
	public VariableStringEntryData(
		String key, @Nullable VariableString defaultValue, boolean optional,
		StringMode stringMode
	) {
		super(key, defaultValue, optional);
		this.stringMode = stringMode;
	}

	@Override
	@Nullable
	protected VariableString getValue(String value) {
		// Double up quotations outside of expressions
		if (stringMode != StringMode.VARIABLE_NAME)
			value = VariableString.quote(value);
		return VariableString.newInstance(value, stringMode);
	}

}
