package ch.njol.skript.test.runner;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.List;

public class StructEntryContainerTest extends Structure {

	public static class TestEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerStructure(StructEntryContainerTest.class,
			EntryValidator.builder()
				.addSection("has entry", true)
				.build(),
			"test entry container");
	}

	private EntryContainer entryContainer;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		assert entryContainer != null;
		this.entryContainer = entryContainer;
		if (entryContainer.hasEntry("has entry")) {
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
		Trigger trigger = new Trigger(script, "entry container test", null, triggerItems);
		trigger.execute(new TestEvent());
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "test entry container";
	}

}
