package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class EffTame extends Effect {

	static {
		Skript.registerEffect(EffTame.class, "([un:(un|de)]tame|domesticate) %entities%");
	}

	private boolean tame;
	private Expression<Entity> entities;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		tame = !parseResult.hasTag("un");
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof Tameable)
				((Tameable) entity).setTamed(tame);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (tame ? "tame " : "untame ") + entities.toString(event, debug);
	}
}
