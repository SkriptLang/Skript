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
package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a section of a trigger, e.g. a conditional or a loop
 */
public abstract class TriggerSection extends TriggerItem {

	protected @Nullable TriggerItem first, last;

	/**
	 * Reserved for new Trigger(...)
	 */
	protected TriggerSection(List<TriggerItem> items) {
		setTriggerItems(items);
	}

	protected TriggerSection(SectionNode node) {
		ParserInstance parser = ParserInstance.get();
		List<TriggerSection> previousSections = parser.getCurrentSections();

		List<TriggerSection> sections = new ArrayList<>(previousSections);
		sections.add(this);
		parser.setCurrentSections(sections);

		try {
			setTriggerItems(ScriptLoader.loadItems(node));
		} finally {
			parser.setCurrentSections(previousSections);
		}
	}

	/**
	 * Important when using this constructor: set the items with {@link #setTriggerItems(List)}!
	 */
	protected TriggerSection() {}

	/**
	 * Remember to add this section to {@link ParserInstance#getCurrentSections()} before parsing child elements!
	 * 
	 * <pre>
	 * ScriptLoader.currentSections.add(this);
	 * setTriggerItems(ScriptLoader.loadItems(node));
	 * ScriptLoader.currentSections.remove(ScriptLoader.currentSections.size() - 1);
	 * </pre>
	 */
	protected void setTriggerItems(List<TriggerItem> items) {
		if (!items.isEmpty()) {
			first = items.get(0);
			last = items.get(items.size() - 1);
			last.setNext(getNext());

			for (TriggerItem item : items) {
				item.setParent(this);
			}
		}
	}

	@Override
	public TriggerSection setNext(@Nullable TriggerItem next) {
		super.setNext(next);
		if (last != null)
			last.setNext(next);
		return this;
	}

	@Override
	public TriggerSection setParent(@Nullable TriggerSection parent) {
		super.setParent(parent);
		return this;
	}

	@Override
	protected final boolean run(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected abstract @Nullable TriggerItem walk(Event event);

	protected final @Nullable TriggerItem walk(Event event, boolean run) {
		debug(event, run);
		if (run && first != null) {
			return first;
		} else {
			return getNext();
		}
	}

}
