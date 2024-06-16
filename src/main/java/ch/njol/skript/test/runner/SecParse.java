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
package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterables;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

@Name("Parse Section")
@Description("Parse code inside this section and use 'parse logs' to grab any logs from it.")
@NoDoc
public class SecParse extends Section {

	static {
		Skript.registerSection(SecParse.class, "parse");
	}

	@Nullable
	public static String[] lastLogs;
	private String[] logs;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		if (Iterables.size(sectionNode) == 0) {
			Skript.error("A parse section must contain code");
			return false;
		}

		RetainingLogHandler handler = SkriptLogger.startRetainingLog();
		// we need to do this before loadCode because loadCode will add this section to the current sections
		boolean inParseSection = getParser().isCurrentSection(SecParse.class);
		loadCode(sectionNode);
		if (!inParseSection) {
			// only store logs if we're not in another parse section.
			// this way you can access the parse logs of the outermost parse section
			logs = handler.getLog().stream()
					.map(LogEntry::getMessage)
					.toArray(String[]::new);
		}
		handler.stop();
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		lastLogs = logs;
		return walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "parse";
	}

}
