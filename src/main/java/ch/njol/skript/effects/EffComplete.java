package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.skriptlang.skript.util.Completable;

// todo doc
public class EffComplete extends Effect {

	static {
		Skript.registerEffect(EffComplete.class, "(complete|finish) %completable%");
	}

	private Expression<Completable> completable;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		if (!this.getParser().hasExperiment(Feature.TASKS))
			return false;
		//noinspection unchecked
		this.completable = (Expression<Completable>) expressions[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Completable single = completable.getSingle(event);
		if (single != null)
			single.complete();
	}

	@Override
	public String toString(Event event, boolean debug) {
		return "complete " + completable.toString(event, debug);
	}

}
