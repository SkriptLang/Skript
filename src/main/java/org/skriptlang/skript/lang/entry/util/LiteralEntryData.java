package org.skriptlang.skript.lang.entry.util;

import ch.njol.skript.lang.ParseContext;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;

/**
 * A specific {@link KeyValueEntryData} type designed to parse the
 *  entry's value as a supported literal type.
 * This entry makes use of {@link Classes#parse(String, Class, ParseContext)}
 *  to parse the user's input using registered {@link ch.njol.skript.classes.ClassInfo}s
 *  and {@link ch.njol.skript.classes.Converter}s.
 * This data <b>CAN</b> return null if the user's input is unable to be parsed as the expected type.
 */
public class LiteralEntryData<T> extends KeyValueEntryData<T> {

	private final Class<T> type;

	/**
	 * @param type The type to parse the value into.
	 */
	public LiteralEntryData(
		String key, @Nullable T defaultValue, boolean optional,
		Class<T> type
	) {
		super(key, defaultValue, optional);
		this.type = type;
	}

	@Override
	@Nullable
	public T getValue(String value) {
		return Classes.parse(value, type, ParseContext.DEFAULT);
	}

}
