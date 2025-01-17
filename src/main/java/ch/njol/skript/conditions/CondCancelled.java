package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Cancelled")
@Description("Checks whether or not the event or a task is cancelled.")
@Examples({"on click:",
		"\tif event is cancelled:",
		"\t\tbroadcast \"no clicks allowed!\""
})
@Since("2.2-dev36, INSERT VERSION (tasks: experimental)")
public class CondCancelled extends Condition {

	static {
		Skript.registerCondition(CondCancelled.class,
			"[the] event is cancel[l]ed",
			"[the] event (is not|isn't) cancel[l]ed",
			"%cancellable% is cancel[l]ed",
			"%cancellable% (is not|isn't) cancel[l]ed"
			);
	}

	private @Nullable Expression<Cancellable> cancellable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.setNegated(matchedPattern % 2 != 0);
		if (matchedPattern > 1 && !this.getParser().hasExperiment(Feature.TASKS))
			return false;
		if (matchedPattern > 1)
			this.cancellable = (Expression<Cancellable>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (cancellable != null) {
			Cancellable single = cancellable.getSingle(event);
			if (single != null)
				return single.isCancelled() ^ isNegated();
			return false;
		}
		return (event instanceof Cancellable cancellable && cancellable.isCancelled() ^ isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (cancellable != null)
			return cancellable.toString(event, debug) + (isNegated() ? " is not cancelled" : " is cancelled");
		return isNegated() ? "event is not cancelled" : "event is cancelled";
	}

}
