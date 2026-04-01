package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Feature;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;
import org.skriptlang.skript.lang.structure.Structure;

@NoDoc
@Name("Exemplar")
@Description({
	"Exemplars art structures that are parsed, yet shall never be executed.",
	"They serve as miniature treatises for demonstrating passages of code within the example files.",
	"Scripts containing an exemplar are regarded as 'examples' by the parser and may bear special safeguards."
})
@Example("""
    exemplar:
    	broadcast "hello world"
    	# this is never run
    """)
@Since("2.10")
public class StructExample extends Structure implements SimpleExperimentalSyntax {

	private static final ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.EXAMPLES);

	public static final Priority PRIORITY = new Priority(550);

	static {
		Skript.registerStructure(StructExample.class,
			"exemplar"
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
	public ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

	@Override
	public boolean load() {
		ParserInstance parser = this.getParser();
		// This acts like a 'function' except without some of the features (e.g. returns)
		// The code is parsed and loaded, but then discarded since it will never be run
		// This allows things like parse problems and errors to be detected.
		parser.setCurrentEvent("exemplar", FunctionEvent.class);
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
		return "exemplar";
	}

}
