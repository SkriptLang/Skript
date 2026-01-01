package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprCaughtErrors;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.ExperimentalSyntax;
import org.skriptlang.skript.log.runtime.RuntimeError;
import org.skriptlang.skript.log.runtime.RuntimeErrorCatcher;

import java.util.List;

@Name("Catch Runtime Errors")
@Description("Catch any runtime errors produced by code within the section. This is an in progress feature.")
@Example("""
	catch runtime errors:
		set worldborder center of {_border} to location(0, 0, NaN value)
	if last caught runtime errors contains "Your location can't have a NaN value as one of its components":
		set worldborder center of {_border} to location(0, 0, 0)
	""")
@Since("2.12")
public class SecCatchErrors extends Section implements ExperimentalSyntax {

	static {
		Skript.registerSection(SecCatchErrors.class, "catch [run[ ]time] error[s]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		if (sectionNode.isEmpty()) {
			Skript.error("A catch errors section must contain code.");
			return false;
		}
		ParserInstance parser = getParser();
		Kleenean previousDelay = parser.getHasDelayBefore();
		parser.setHasDelayBefore(Kleenean.FALSE);
		loadCode(sectionNode);
		if (parser.getHasDelayBefore().isTrue()) {
			Skript.error("Delays can't be used within a catch errors section.");
			return false;
		}
		parser.setHasDelayBefore(previousDelay);
		return true;
	}

	@Override
	public boolean isSatisfiedBy(ExperimentSet experimentSet) {
		return experimentSet.hasExperiment(Feature.CATCH_ERRORS);
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		// don't try to run the section if we are uncertain about its boundaries
		if (first != null && last != null) {
			try (RuntimeErrorCatcher catcher = new RuntimeErrorCatcher().start()) {
				last.setNext(null);
				TriggerItem.walk(first, event);
				ExprCaughtErrors.lastErrors = catcher.getCachedErrors().stream().map(RuntimeError::error).toArray(String[]::new);
			}
		}
		return walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "catch runtime errors";
	}

}
