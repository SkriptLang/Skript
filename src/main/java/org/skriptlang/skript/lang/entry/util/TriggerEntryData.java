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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.parser.ParserInstance;
import org.skriptlang.skript.lang.entry.EntryData;
import org.skriptlang.skript.lang.entry.SectionEntryData;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * An entry data class designed to take a {@link SectionNode} and parse it into a Trigger.
 * Events specified during construction *should* be used when the Trigger is executed.
 * This data will <b>NEVER</b> return null.
 * @see SectionEntryData
 */
public class TriggerEntryData extends EntryData<Trigger> {

	private final Class<? extends Event>[] events;

	/**
	 * @param events Events to be present during parsing and Trigger execution.
	 *               This allows the usage of event-restricted syntax and event-values.
	 * @see ParserInstance#setCurrentEvents(Class[])
	 */
	@SafeVarargs
	public TriggerEntryData(
		String key, @Nullable Trigger defaultValue, boolean optional,
		Class<? extends Event>... events
	) {
		super(key, defaultValue, optional);
		this.events = events;
	}

	@Nullable
	@Override
	public Trigger getValue(Node node) {
		assert node instanceof SectionNode;

		ParserInstance parser = ParserInstance.get();

		Class<? extends Event>[] oldEvents = parser.getCurrentEvents();
		Kleenean oldHasDelayBefore = parser.getHasDelayBefore();

		parser.setCurrentEvents(events);
		parser.setHasDelayBefore(Kleenean.FALSE);

		Trigger trigger = new Trigger(
			parser.getCurrentScript(), "entry with key: " + getKey(), new SimpleEvent(), ScriptLoader.loadItems((SectionNode) node)
		);

		parser.setCurrentEvents(oldEvents);
		parser.setHasDelayBefore(oldHasDelayBefore);

		return trigger;
	}

	@Override
	public boolean canCreateWith(Node node) {
		if (!(node instanceof SectionNode))
			return false;
		String key = node.getKey();
		if (key == null)
			return false;
		key = ScriptLoader.replaceOptions(key);
		return getKey().equalsIgnoreCase(key);
	}

}
