package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.RuntimeError;
import org.skriptlang.skript.log.runtime.RuntimeErrorCatcher;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@NoDoc
public class SecRuntime extends Section {

	static {
		if (TestMode.ENABLED)
			Skript.registerSection(SecRuntime.class, "catch [run[ ]time] error[s]");
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		if (Iterables.size(sectionNode) == 0) {
			Skript.error("A runtime section must contain code.");
			return false;
		}

		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		trigger = loadCode(sectionNode, "runtime", afterLoading, SkriptTestEvent.class);
		if (delayed.get()) {
			Skript.error("Delays can't be used within a testing environment.");
			return false;
		}
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		SkriptTestEvent testEvent = new SkriptTestEvent();
		RuntimeErrorCatcher catcher = new RuntimeErrorCatcher().start();
		Variables.withLocalVariables(event, testEvent, () -> TriggerItem.walk(trigger, testEvent));
        ExprRuntimeErrors.lastErrors = catcher.getCachedErrors().stream().map(RuntimeError::error).toArray(String[]::new);
		catcher.clearCachedErrors()
			.clearCachedFrames()
			.stop();
		return walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "catch runtime errors";
	}

}
