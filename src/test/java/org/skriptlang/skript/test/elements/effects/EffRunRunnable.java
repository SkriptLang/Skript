package org.skriptlang.skript.test.elements.effects;

import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@NoDoc
public class EffRunRunnable extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.simple(EffRunRunnable.class, EffRunRunnable::new,
			"run runnable %object%"));
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
			error("Tried to run a non-runnable: " + single);
		}
	}

	@Override
	public String toString(Event event, boolean debug) {
		return "run runnable " + task.toString(event, debug);
	}

}
