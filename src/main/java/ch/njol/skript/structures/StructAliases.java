package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ScriptAliases;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

@Name("Aliases")
@Description("Used for registering custom aliases for a script.")
@Examples({
	"aliases:",
	"\tblacklisted items = TNT, bedrock, obsidian, mob spawner, lava, lava bucket",
	"\tshiny swords = golden sword, iron sword, diamond sword"
})
@Since("1.0")
public class StructAliases extends Structure {

	public static final Priority PRIORITY = new Priority(200);

	static {
		Skript.registerStructure(StructAliases.class, "aliases");
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		// noinspection ConstantConditions - entry container cannot be null as this structure is not simple
		SectionNode node = entryContainer.getSource();
		node.convertToEntries(0, "=");

		// Initialize and load script aliases
		Script script = getParser().getCurrentScript();
		ScriptAliases scriptAliases = Aliases.getScriptAliases(script);
		if (scriptAliases == null)
			scriptAliases = Aliases.createScriptAliases(script);
		scriptAliases.parser.load(node);

		return true;
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "aliases";
	}

}
