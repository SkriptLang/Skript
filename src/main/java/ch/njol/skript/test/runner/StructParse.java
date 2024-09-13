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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import com.google.common.collect.Iterables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.lang.structure.Structure;

import static ch.njol.skript.lang.SkriptParser.ParseResult;


@Name("Parse Structure")
@Description("Parses the code inside this structure as a structure and use 'parse logs' to grab any logs from it.")
@NoDoc
public class StructParse extends Structure {

	static {
		Skript.registerStructure(StructParse.class, "parse");
	}

	private static EntryValidator validator = EntryValidator.builder()
		.addEntryData(new ExpressionEntryData<>("results", null, false, Object.class))
		.addSection("code", false)
		.build();

	private SectionNode structureSectionNodeToParse;
	private String[] logs;
	private Expression<?> resultsExpression;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		SectionNode parseStructureSectionNode = entryContainer.getSource();

		Class<? extends Event>[] originalEvents = getParser().getCurrentEvents();
		getParser().setCurrentEvent("parse", ContextlessEvent.class);
		EntryContainer validatedEntries = validator.validate(parseStructureSectionNode);
		getParser().setCurrentEvents(originalEvents);
		if (validatedEntries == null) {
			Skript.error("A parse structure must have a result entry and a code section");
			return false;
		}
		Expression<?> maybeResultsExpression = (Expression<?>) validatedEntries.get("results", false);
		if (!ChangerUtils.acceptsChange(maybeResultsExpression, ChangeMode.SET, String[].class)) {
			Skript.error(maybeResultsExpression.toString(null, false) + " cannot be set to strings");
		}
		SectionNode codeSectionNode = (SectionNode) validatedEntries.get("code", false);
		Node maybeStructureSectionNodeToParse = Iterables.getFirst(codeSectionNode, null);
		if (Iterables.size(codeSectionNode) != 1 || !(maybeStructureSectionNodeToParse instanceof SectionNode)) {
			Skript.error("The code section must contain a single section to parse as a structure");
		}
		resultsExpression = maybeResultsExpression;
		structureSectionNodeToParse = (SectionNode) maybeStructureSectionNodeToParse;
		return true;
	}

	@Override
	public boolean postLoad() {
		resultsExpression.change(ContextlessEvent.get(), logs, ChangeMode.SET);
		return true;
	}

	@Override
	public boolean load() {
		try (RetainingLogHandler handler = SkriptLogger.startRetainingLog()) {
			String structureSectionNodeKey = ScriptLoader.replaceOptions(structureSectionNodeToParse.getKey());
			String error = "Can't understand this structure: " + structureSectionNodeKey;
			Structure.parse(structureSectionNodeKey, structureSectionNodeToParse, error);
			logs = handler.getLog().stream()
				.map(LogEntry::getMessage)
				.toArray(String[]::new);
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "parse";
	}

}
