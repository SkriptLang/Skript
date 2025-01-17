package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Task;

// todo doc
public class EffCancelTask extends Effect {

	static {
		Skript.registerEffect(EffCancelTask.class, "cancel %task%");
	}

	private Expression<Task> task;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!this.getParser().hasExperiment(Feature.TASKS))
			return false;
		//noinspection unchecked
		this.task = (Expression<Task>) expressions[0];
		return true;
	}

	@Override
	public void execute(Event event) {
		Task task = this.task.getSingle(event);
		if (task == null)
			return;
		task.cancel();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cancel " + task.toString(event, debug);
	}

}
