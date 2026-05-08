package org.skriptlang.skript.bukkit.entity.creeper;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Explode Creeper")
@Description("Starts the explosion process of a creeper or instantly explodes it.")
@Example("start explosion of the last spawned creeper")
@Example("stop ignition of the last spawned creeper")
@Since("2.5")
public class EffExplodeCreeper extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffExplodeCreeper.class)
				.addPatterns(
					"instantly explode [creeper[s]] %livingentities%",
					"explode [creeper[s]] %livingentities% instantly",
					"ignite creeper[s] %livingentities%",
					"start (ignition|explosion) [process] of [creeper[s]] %livingentities%",
					"stop (ignition|explosion) [process] of [creeper[s]] %livingentities%"
				).supplier(EffExplodeCreeper::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private boolean instant;
	private boolean stop;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		instant = matchedPattern == 0 || matchedPattern == 1;
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
					creeper.setIgnited(true);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (instant ? "instantly explode " : "start the explosion process of ") + entities.toString(event, debug);
	}

}
