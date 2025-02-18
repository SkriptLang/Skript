package ch.njol.skript.effects;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

@Name("Explode Creeper")
@Description("Starts the explosion process of a creeper or instantly explodes it.")
@Examples({"start explosion of the last spawned creeper",
			"stop ignition of the last spawned creeper"})
@Since("2.5")
@RequiredPlugins("Paper 1.13 or newer/Spigot 1.14 or newer. Ignition can be stopped on Paper 1.13 or newer.")
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

	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;

	private boolean instant;

	private boolean stop;

	/*
	 * setIgnited() was added in Paper 1.13
	 * ignite() was added in Spigot 1.14, so we can use setIgnited() 
	 * to offer this functionality to Paper 1.13 users.
	 */
	private final boolean paper = Skript.methodExists(Creeper.class, "setIgnited", boolean.class);

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
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
	protected void execute(final Event e) {
		for (final LivingEntity le : entities.getArray(e)) {
			if (le instanceof Creeper) {
				if (instant) {
					((Creeper) le).explode();
				} else if (stop) {
					((Creeper) le).setIgnited(false);
				} else {
					if (paper) {
						((Creeper) le).setIgnited(true);
					} else {
						((Creeper) le).ignite();
					}
				}
			}
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (instant == true ? "instantly explode " : "start the explosion process of ") + entities.toString(e, debug);
	}

}
