package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Explode Creeper")
@Description("Starts the explosion process of a creeper or instantly explodes it.")
@Examples({
	"start explosion of the last spawned creeper",
	"stop ignition of the last spawned creeper"
})
@Since("2.5")
@RequiredPlugins("Paper 1.13+/Spigot 1.14+, Paper 1.13+ for stopping ignition")
public class EffExplodeCreeper extends Effect {

	static {
		if (Skript.methodExists(Creeper.class, "explode")) {
			Skript.registerEffect(EffExplodeCreeper.class, 
				"instantly explode [creeper[s]] %livingentities%",
				"explode [creeper[s]] %livingentities% instantly",
				"ignite creeper[s] %livingentities%",
				"start (ignition|explosion) [process] of [creeper[s]] %livingentities%",
				"stop (ignition|explosion) [process] of [creeper[s]] %livingentities%");
		}
	}

	private Expression<LivingEntity> entities;
	private boolean instant, stop;

	/*
	 * setIgnited() was added in Paper 1.13
	 * ignite() was added in Spigot 1.14, so we can use setIgnited() 
	 * to offer this functionality to Paper 1.13 users.
	 */
	private final boolean paper = Skript.methodExists(Creeper.class, "setIgnited", boolean.class);

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 4) {
			if (!paper) {
				Skript.error("Stopping the ignition process is only possible on Paper 1.13+", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		entities = (Expression<LivingEntity>) exprs[0];
		instant = matchedPattern == 0;
		stop = matchedPattern == 4;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Creeper creeper) {
				if (instant) {
					creeper.explode();
				} else if (stop) {
					creeper.setIgnited(false);
				} else {
					if (paper) {
						creeper.setIgnited(true);
					} else {
						creeper.ignite();
					}
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (instant ? "instantly explode " : "start the explosion process of ") + entities.toString(event, debug);
	}

}
