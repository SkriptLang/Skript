package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

// todo doc
public class Pass extends Effect {

	static {
		Skript.registerEffect(Pass.class, "pass", "do nothing");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		return this.getParser().hasExperiment(Feature.TASKS);
	}

	@Override
	public void execute(Event event) {
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "do nothing";
	}

}
