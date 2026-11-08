package org.skriptlang.skript.test.elements.structures;

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
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Task;
import com.google.common.collect.Iterables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Name("Parse Structure")
@Description("Parses the code inside this structure as a structure and use 'parse logs' to grab any logs from it.")
@NoDoc
public class StructParse extends Structure {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.STRUCTURE, SyntaxInfo.Structure.simple(StructParse.class, StructParse::new,
			"parse"));
	}

	private static final EntryValidator validator = EntryValidator.builder()
		.addEntryData(new ExpressionEntryData<>("results", null, false, Object.class))
		.addSection("code", false)
		.build();

	private final List<String> logs = new ArrayList<>();
	private Expression<?> resultsExpression;

	private @Nullable Structure structure;

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
		if (Iterables.size(codeSectionNode) != 1) {
			Skript.error("The code section must contain a single node to parse as a structure");
			return false;
		}

		try (RetainingLogHandler handler = SkriptLogger.startRetainingLog()) {
			Node structureNodeToParse = Iterables.getFirst(codeSectionNode, null);
			assert structureNodeToParse != null;
			String structureSectionNodeKey = ScriptLoader.replaceOptions(structureNodeToParse.getKey());
			String error = "Can't understand this structure: " + structureSectionNodeKey;
			structure = Structure.parse(structureSectionNodeKey, structureNodeToParse, error);
			extractLogs(handler);
		} finally {
			getParser().setCurrentStructure(this);
		}

		resultsExpression = maybeResultsExpression;
		return true;
	}

	@Override
	public boolean preLoad() {
		executeLoadMethod(Structure::preLoad);
		return true;
	}

	@Override
	public boolean load() {
		executeLoadMethod(Structure::load);
		return true;
	}

	@Override
	public boolean postLoad() {
		executeLoadMethod(Structure::postLoad);
		Task.callSync(() -> {
			resultsExpression.change(ContextlessEvent.get(), logs.toArray(new String[0]), ChangeMode.SET);
			return null;
		});
		return true;
	}

	@Override
	public void unload() {
		if (structure != null) {
			structure.unload();
		}
	}

	@Override
	public void postUnload() {
		if (structure != null) {
			structure.postUnload();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "parse";
	}

	private void executeLoadMethod(Function<Structure, Boolean> loadMethod) {
		if (structure == null) {
			return;
		}
		try (RetainingLogHandler handler = SkriptLogger.startRetainingLog()) {
			getParser().setCurrentStructure(structure);
			if (!loadMethod.apply(structure)) {
				structure = null;
			}
			extractLogs(handler);
		} finally {
			getParser().setCurrentStructure(this);
		}
	}

	private void extractLogs(RetainingLogHandler handler) {
		handler.getLog().stream()
			.filter(entry -> entry.getLevel() != SkriptLogger.DEBUG)
			.map(LogEntry::getMessage)
			.forEach(logs::add);
	}

}
