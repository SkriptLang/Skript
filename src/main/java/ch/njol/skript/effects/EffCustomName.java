package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffCustomName extends Effect {

	static {
		Skript.registerEffect(EffCustomName.class,
			"(:show|hide) [the] (custom|display)[ ]name of %entities%",
					  "(:show|hide) %entities%'[s] (custom|display)[ ]name");
	}

	Boolean showCustomName;
	Expression<Entity> entities;

	@Override
	public boolean init(Expression<?>[] expr, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		showCustomName = parseResult.hasTag("show");
		entities = (Expression<Entity>) expr[0];
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (Entity entity : entities.getArray(e)) {
			entity.setCustomNameVisible(showCustomName);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return showCustomName ? "show" : "hide" + " the custom name of " + entities.toString(event, debug);
	}

}
