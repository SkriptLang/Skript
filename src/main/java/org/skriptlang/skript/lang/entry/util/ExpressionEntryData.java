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

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A type of {@link KeyValueEntryData} designed to parse its value as an {@link Expression}.
 * This data <b>CAN</b> return null if expression parsing fails.
 */
public class ExpressionEntryData<T> extends KeyValueEntryData<Expression<? extends T>> {

	private final Class<T> returnType;

	private final int flags;
	
	private final Class<? extends Event>[] events;

	/**
	 * @param returnType The expected return type of the matched expression.
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public ExpressionEntryData(
		String key, @Nullable Expression<T> defaultValue, boolean optional,
		Class<T> returnType, Class<? extends Event>... events
	) {
		this(key, defaultValue, optional, returnType, SkriptParser.ALL_FLAGS, events);
	}

	/**
	 * @param returnType The expected return type of the matched expression.
	 * @param flags Parsing flags. See {@link SkriptParser#SkriptParser(String, int, ParseContext)}
	 *              javadoc for more details.
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public ExpressionEntryData(
		String key, @Nullable Expression<T> defaultValue, boolean optional,
		Class<T> returnType, int flags, Class<? extends Event>... events
	) {
		super(key, defaultValue, optional);
		this.returnType = returnType;
		this.flags = flags;
		this.events = events;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	protected Expression<? extends T> getValue(String value) {
		ParserInstance parser = ParserInstance.get();

		Class<? extends Event>[] oldEvents = parser.getCurrentEvents();
		Kleenean oldHasDelayBefore = parser.getHasDelayBefore();

		parser.setCurrentEvents(events);
		parser.setHasDelayBefore(Kleenean.FALSE);

		Expression<? extends T> expression = new SkriptParser(value, flags, ParseContext.DEFAULT).parseExpression(returnType);

		parser.setCurrentEvents(oldEvents);
		parser.setHasDelayBefore(oldHasDelayBefore);

		return expression;
	}

}
