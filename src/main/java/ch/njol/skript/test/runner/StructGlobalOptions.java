package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.OptionRegistry;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

@NoDoc
public class StructGlobalOptions extends Structure {

	static {
		Skript.registerStructure(StructGlobalOptions.class, "global options");
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @UnknownNullability EntryContainer entryContainer) {
		SectionNode node = entryContainer.getSource();
		node.convertToEntries(-1);
		OptionRegistry optionRegistry = Skript.instance().registry(OptionRegistry.class);
		optionRegistry.loadGlobalOptions(node);
		return true;
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "global options";
	}

}
