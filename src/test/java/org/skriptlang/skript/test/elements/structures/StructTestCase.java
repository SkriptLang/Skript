package org.skriptlang.skript.test.elements.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.lang.util.SimpleEvent;
import org.skriptlang.skript.test.SkriptTestEvent;
import ch.njol.skript.test.runner.TestTracker;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Test Case")
@Description("Contents represent one test case.")
@Since("2.5")
@NoDoc
public class StructTestCase extends Structure {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.STRUCTURE, SyntaxInfo.Structure.simple(StructTestCase.class, StructTestCase::new,
			"test %*string% [when <.+>]"));
	}

	private SectionNode source;
	private Literal<String> name;
	private @Nullable Condition condition;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		this.source = entryContainer.getSource();
		//noinspection unchecked
		name = (Literal<String>) args[0];
		if (!parseResult.regexes.isEmpty()) { // Do not parse or run unless condition is met
			String cond = parseResult.regexes.getFirst().group();
			condition = Condition.parse(cond, "Can't understand this condition: " + cond);
		}
		return true;
	}

	@Override
	public boolean load() {
		if (condition != null && !condition.check(ContextlessEvent.get())) {
			// condition did not pass, do not load or register test
			return true;
		}

		Trigger trigger;
		try {
			getParser().setCurrentEvent("test case", SkriptTestEvent.class);

			List<TriggerItem> items = ScriptLoader.loadItems(source);
			Script script = getParser().getCurrentScript();

			String name = toString(null, false);
			trigger = new Trigger(script, name, new SimpleEvent(name), items);
			int lineNumber = source.getLine();
			trigger.setLineNumber(lineNumber); // set line number for debugging
			trigger.setDebugLabel(script + ": line " + lineNumber);
		} finally {
			getParser().deleteCurrentEvent();
		}

		TestTracker.registerTest(name.getSingle(), () -> trigger.execute(new SkriptTestEvent()));

		return true;
	}

	@Override
	public void unload() {
		TestTracker.unregisterTest(name.getSingle());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "test " + name.getSingle();
	}

}
