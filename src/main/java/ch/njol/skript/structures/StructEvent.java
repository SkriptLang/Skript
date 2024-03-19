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
package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.Locale;

@NoDoc
public class StructEvent extends Structure {

	static {
		Skript.registerStructure(StructEvent.class,
				"[on] <.+> [with priority (:(lowest|low|normal|high|highest|monitor))]");
	}

	private SkriptEvent event;

	@Override
	@SuppressWarnings("ConstantConditions")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		String expr = parseResult.regexes.get(0).group();

		EventData data = getParser().getData(EventData.class);
		// ensure there's no leftover data from previous parses
		data.clear();

		if (!parseResult.tags.isEmpty())
			data.priority = EventPriority.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));

		event = SkriptEvent.parse(expr, entryContainer.getSource(), null);

		// cleanup after ourselves
		data.clear();
		return event != null;
	}

	@Override
	public boolean preLoad() {
		getParser().setCurrentStructure(event);
		return event.preLoad();
	}

	@Override
	public boolean load() {
		getParser().setCurrentStructure(event);
		return event.load();
	}

	@Override
	public boolean postLoad() {
		getParser().setCurrentStructure(event);
		return event.postLoad();
	}

	@Override
	public void unload() {
		event.unload();
	}

	@Override
	public void postUnload() {
		event.postUnload();
	}

	@Override
	public Priority getPriority() {
		return event.getPriority();
	}

	public SkriptEvent getSkriptEvent() {
		return event;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return this.event.toString(event, debug);
	}

	static {
		ParserInstance.registerData(EventData.class, EventData::new);
	}

	public static class EventData extends ParserInstance.Data {

		@Nullable
		private EventPriority priority;

		public EventData(ParserInstance parserInstance) {
			super(parserInstance);
		}

		@Nullable
		public EventPriority getPriority() {
			return priority;
		}

		/**
		 * Clears all event-specific data from this instance.
		 */
		public void clear() {
			priority = null;
		}

	}

}
