package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.expressions.ExprCaughtErrors;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.RuntimeError;
import org.skriptlang.skript.log.runtime.RuntimeErrorCatcher;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Catch Runtime Errors")
@Description("Catch any runtime errors produced by code within the section.")
@Example("""
	catch runtime errors:
		set worldborder center of {_border} to location(0, 0, NaN value)
	if last caught runtime errors contains "Your location can't have a NaN value as one of its components":
		set worldborder center of {_border} to location(0, 0, 0)
	""")
@Since("INSERT VERSION")
public class SecCatchErrors extends Section {

	static {
		Skript.registerSection(SecCatchErrors.class, "catch [run[ ]time] error[s]");
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
		trigger = loadCode(sectionNode, "runtime", afterLoading, Event.class);
		if (delayed.get() && TestMode.ENABLED) {
			Skript.error("Delays can't be used within a testing environment.");
			return false;
		}
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		RuntimeErrorCatcher catcher = new RuntimeErrorCatcher().start();
		TriggerItem.walk(trigger, event);
        ExprCaughtErrors.lastErrors = catcher.getCachedErrors().stream().map(RuntimeError::error).toArray(String[]::new);
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
