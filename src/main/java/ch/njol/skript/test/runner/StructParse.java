package ch.njol.skript.test.runner;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
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

@Name("Parse Structure")
@Description("Parses the code inside this structure as a structure and use 'parse logs' to grab any logs from it.")
@NoDoc
public class StructParse extends Structure {

	static {
		Skript.registerStructure(StructParse.class, "parse");
	}

	private static final EntryValidator validator = EntryValidator.builder()
		.addEntryData(new ExpressionEntryData<>("results", null, false, Object.class))
		.addSection("code", false)
		.build();

	private SectionNode structureSectionNodeToParse;
	private String[] logs;
	private Expression<?> resultsExpression;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern,
						ParseResult parseResult, EntryContainer entryContainer) {
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
			return false;
		}

		SectionNode codeSectionNode = (SectionNode) validatedEntries.get("code", false);
		Node maybeStructureSectionNodeToParse = Iterables.getFirst(codeSectionNode, null);
		if (Iterables.size(codeSectionNode) != 1 || !(maybeStructureSectionNodeToParse instanceof SectionNode)) {
			Skript.error("The code section must contain a single section to parse as a structure");
			return false;
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
			Structure structure = Structure.parse(structureSectionNodeKey, structureSectionNodeToParse, error);

			getParser().setCurrentStructure(structure);
			if (structure != null && structure.preLoad() && structure.load()) {
				structure.postLoad();
			}

			getParser().setCurrentStructure(null);

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
