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
package ch.njol.skript.lang.structure.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.structure.KeyValueStructureEntryData;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A type of {@link KeyValueStructureEntryData} designed to parse its value as an {@link Expression}.
 */
public class ExpressionStructureEntryData<T> extends KeyValueStructureEntryData<Expression<? extends T>> {

	private final Class<T> returnType;
	
	private final Class<? extends Event>[] events;

	/**
	 * @param returnType The expected return type of the matched expression.
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public ExpressionStructureEntryData(
		String key, @Nullable Expression<T> defaultValue, boolean optional,
		Class<T> returnType, Class<? extends Event>... events)
	{
		super(key, defaultValue, optional);
		this.returnType = returnType;
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

		Expression<? extends T> expression = new SkriptParser(value, SkriptParser.PARSE_EXPRESSIONS, ParseContext.DEFAULT).parseExpression(returnType);

		parser.setCurrentEvents(oldEvents);
		parser.setHasDelayBefore(oldHasDelayBefore);

		return expression;
	}

}
