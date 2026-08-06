package ch.njol.skript.structures;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated See {@link org.skriptlang.skript.bukkit.command.elements.structures.StructCommand}.
 * Note that this replacement class is considered internal API.
 */
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class StructCommand extends Structure {

	/**
	 * @deprecated This field is no longer used internally and should not be relied upon for any behavior.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public static final Priority PRIORITY = new Priority(500);

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		return false;
	}

	@Override
	public boolean load() {
		return false;
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "command";
	}

}
