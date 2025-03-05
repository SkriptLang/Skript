package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.ExperimentalSyntax;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Feature;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.structure.Structure;

@NoDoc
@Name("Example")
@Description({
	"Examples are structures that are parsed, but will never be run.",
	"They are used as miniature tutorials for demonstrating code snippets in the example files.",
	"Scripts containing an example are seen as 'examples' by the parser and may have special safety restrictions."
})
@Examples({"""
	example:
		broadcast "hello world"
		# this is never run"""
})
@Since("2.10")
public class StructExample extends Structure implements ExperimentalSyntax {

	public static final Priority PRIORITY = new Priority(550);

	static {
		Skript.registerStructure(StructExample.class,
			"example"
		);
	}

	private SectionNode source;

	@Override
	public boolean init(Literal<?>[] literals, int matchedPattern, ParseResult parseResult,
						@Nullable EntryContainer entryContainer) {
		assert entryContainer != null; // cannot be null for non-simple structures
		this.source = entryContainer.getSource();
		return true;
	}

	@Override
	public boolean isSatisfiedBy(ExperimentSet experimentSet) {
		return experimentSet.hasExperiment(Feature.EXAMPLES);
	}

	@Override
	public boolean load() {
		ParserInstance parser = this.getParser();
		// This acts like a 'function' except without some of the features (e.g. returns)
		// The code is parsed and loaded, but then discarded since it will never be run
		// This allows things like parse problems and errors to be detected.
		parser.setCurrentEvent("example", FunctionEvent.class);
		ScriptLoader.loadItems(source);
		parser.deleteCurrentEvent();
		return true;
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "example";
	}

}
