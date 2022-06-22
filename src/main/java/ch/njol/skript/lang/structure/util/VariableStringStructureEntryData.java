package ch.njol.skript.lang.structure.util;

import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.structure.KeyValueStructureEntryData;
import ch.njol.skript.util.StringMode;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A type of {@link KeyValueStructureEntryData} designed to parse its value as a {@link VariableString}.
 * The {@link StringMode} may be specified during construction.
 * Constructors without a StringMode parameter assume {@link StringMode#MESSAGE}.
 */
public class VariableStringStructureEntryData extends KeyValueStructureEntryData<VariableString> {

	private final StringMode stringMode;

	@Nullable
	private final Class<? extends Event>[] events;

	/**
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public VariableStringStructureEntryData(String key, @Nullable Class<? extends Event>... events) {
		super(key);
		this.stringMode = StringMode.MESSAGE;
		this.events = events;
	}

	/**
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public VariableStringStructureEntryData(String key, @Nullable VariableString defaultValue, @Nullable Class<? extends Event>... events) {
		super(key, defaultValue);
		this.stringMode = StringMode.MESSAGE;
		this.events = events;
	}

	/**
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public VariableStringStructureEntryData(String key, boolean optional, @Nullable Class<? extends Event>... events) {
		super(key, optional);
		this.stringMode = StringMode.MESSAGE;
		this.events = events;
	}

	/**
	 * @param stringMode Sets <i>how</i> to parse the string (e.g. as a variable, message, etc.).
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public VariableStringStructureEntryData(String key, StringMode stringMode, @Nullable Class<? extends Event>... events) {
		super(key);
		this.stringMode = stringMode;
		this.events = events;
	}

	/**
	 * @param stringMode Sets <i>how</i> to parse the string (e.g. as a variable, message, etc.).
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public VariableStringStructureEntryData(String key, @Nullable VariableString defaultValue, StringMode stringMode, @Nullable Class<? extends Event>... events) {
		super(key, defaultValue);
		this.stringMode = stringMode;
		this.events = events;
	}

	/**
	 * @param stringMode Sets <i>how</i> to parse the string (e.g. as a variable, message, etc.).
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public VariableStringStructureEntryData(String key, boolean optional, StringMode stringMode, @Nullable Class<? extends Event>... events) {
		super(key, optional);
		this.stringMode = stringMode;
		this.events = events;
	}

	@Override
	@Nullable
	protected VariableString getValue(String value) {
		ParserInstance parser = ParserInstance.get();

		Class<? extends Event>[] oldEvents = parser.getCurrentEvents();
		Kleenean oldHasDelayBefore = parser.getHasDelayBefore();

		parser.setCurrentEvents(events);
		parser.setHasDelayBefore(Kleenean.FALSE);

		// Double up quotations
		value = value.replace("\"", "\"\"");

		VariableString variableString = VariableString.newInstance(value, stringMode);

		parser.setCurrentEvents(oldEvents);
		parser.setHasDelayBefore(oldHasDelayBefore);

		return variableString;
	}

}
