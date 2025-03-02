package org.skriptlang.skript.lang.entry.util;

import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.StringMode;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;

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
	protected @Nullable VariableString getValue(String value) {
		// Double up quotations outside of expressions
		if (stringMode != StringMode.VARIABLE_NAME)
			value = VariableString.quote(value);
		return VariableString.newInstance(value, stringMode);
	}

}
