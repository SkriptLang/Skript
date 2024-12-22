package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

@NoDoc
public class EffRunRunnable extends Effect {

	static {
		if (TestMode.ENABLED)
			Skript.registerEffect(EffRunRunnable.class, "run %object%");
	}

	private Expression<?> task;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		this.task = LiteralUtils.defendExpression(expressions[0]);
		return LiteralUtils.canInitSafely(task);
	}

	@Override
	protected void execute(Event event) {
		Object single = this.task.getSingle(event);
		if (single instanceof Runnable runnable) {
			runnable.run();
		} else {
			Bukkit.getLogger().severe("Tried to run a non-runnable " + single);
		}
	}

	@Override
	public String toString(Event event, boolean debug) {
		return "run " + task.toString(event, debug);
	}

}
