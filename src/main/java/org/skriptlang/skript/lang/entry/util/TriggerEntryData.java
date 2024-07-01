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
 * This data will <b>NEVER</b> return null.
 * @see SectionEntryData
 */
public class TriggerEntryData extends EntryData<Trigger> {

	public TriggerEntryData(String key, @Nullable Trigger defaultValue, boolean optional) {
		super(key, defaultValue, optional);
	}

	@Nullable
	@Override
	public Trigger getValue(Node node) {
		assert node instanceof SectionNode;
		return new Trigger(
			ParserInstance.get().getCurrentScript(),
			"entry with key: " + getKey(),
			new SimpleEvent(),
			ScriptLoader.loadItems((SectionNode) node)
		);
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
