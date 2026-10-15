package org.skriptlang.skript.test.elements.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.test.runner.SkriptTestEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

public class StructTestEntryContainer extends Structure {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.STRUCTURE, SyntaxInfo.Structure.builder(StructTestEntryContainer.class)
			.supplier(StructTestEntryContainer::new)
			.entryValidator(EntryValidator.builder()
				.addSection("has entry", true)
				.addSection("has multiple entries", true, true)
				.build())
			.addPattern("test entry container")
			.build());
	}

	private EntryContainer entryContainer;

	@Override
	public boolean init(
		Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer
	) {
		assert entryContainer != null;
		this.entryContainer = entryContainer;
		if (entryContainer.hasEntry("has entry") && entryContainer.hasEntry("has multiple entries")) {
			return true;
		}
		assert false;
		return false;
	}

	@Override
	public boolean load() {
		SectionNode section = entryContainer.get("has entry", SectionNode.class, false);
		List<TriggerItem> triggerItems = ScriptLoader.loadItems(section);
		Script script = getParser().getCurrentScript();
		Trigger trigger = new Trigger(script, "entry container test", new SimpleEvent(), triggerItems);
		trigger.execute(new SkriptTestEvent());

		List<SectionNode> multipleSections = entryContainer.getAll(
			"has multiple entries", SectionNode.class, false
		);
		for (SectionNode multipleSection : multipleSections) {
			triggerItems = ScriptLoader.loadItems(multipleSection);
			trigger = new Trigger(script, "entry container test", new SimpleEvent(), triggerItems);
			trigger.execute(new SkriptTestEvent());
		}

		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "test entry container";
	}

}
