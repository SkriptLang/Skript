package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

/**
 * A test-only structure for checking in an annotation is visible or not.
 * Used for checking whether the parser can correctly see & dispose of an annotation.
 * This exists only for testing {@link ch.njol.skript.structures.StructAnnotate} and has no other purpose.
 */
public class StructHasAnnotations extends Structure {

	static {
		Skript.registerSimpleStructure(StructHasAnnotations.class, "test has an annotation", "test does not have an annotation");
	}

	private boolean not;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		this.not = matchedPattern == 1;
		boolean hasNone = ParserInstance.get().copyAnnotations().isEmpty();
		return not == hasNone;
	}

	@Override
	public boolean load() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return !not ? "test has an annotation": "test does not have an annotation";
	}

}
