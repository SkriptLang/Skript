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

// todo doc
public class CondComplete extends Condition {

	static {
		Skript.registerCondition(CondComplete.class,
			"%completable% is (finished|complete[d])",
			"%completable% (is not|isn't) (finished|complete[d])"
		);
	}

	private Expression<Completable> completable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.setNegated(matchedPattern == 1);
		if (matchedPattern > 1 && !this.getParser().hasExperiment(Feature.TASKS))
			return false;
		//noinspection unchecked
		this.completable = (Expression<Completable>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		Completable task = completable.getSingle(event);
		if (task == null)
			return false;
		return task.isComplete() ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return completable.toString(event, debug) + (isNegated() ? " is not complete" : " is complete");
	}

}
