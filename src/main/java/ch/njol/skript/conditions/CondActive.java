package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Completable;
import org.skriptlang.skript.util.Task;

// todo doc
public class CondActive extends Condition {

	static {
		Skript.registerCondition(CondActive.class,
			"%completable% is (running|active|:incomplete)",
			"%completable% (is not|isn't) (running|active|:incomplete)",
			"%completable% is inactive"
		);
	}

	private Expression<Completable> completable;
	private boolean incomplete;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.setNegated(matchedPattern > 0);
		if (matchedPattern > 1 && !this.getParser().hasExperiment(Feature.TASKS))
			return false;
		//noinspection unchecked
		this.completable = (Expression<Completable>) exprs[0];
		this.incomplete = parseResult.hasTag("incomplete");
		return true;
	}

	@Override
	public boolean check(Event event) {
		Completable thing = completable.getSingle(event);
		if (thing == null)
			return false;
		// If it's a task, running/active also means not cancelled
		if (thing instanceof Task task && !incomplete)
			return (task.isCancelled() || thing.isComplete()) ^ isNegated();
		return thing.isComplete() ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (incomplete)
			return completable.toString(event, debug) + (isNegated() ? " is not incomplete" : " is incomplete");
		return completable.toString(event, debug) + (isNegated() ? " is not active" : " is active");
	}

}
