package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Detonate Entities")
@Description("Immediately detonates an entity. Accepted entities are fireworks, TNT minecarts, primed TNT, wind charges and creepers.")
@Example("detonate last launched firework")
@Since("2.10")
public class EffDetonate extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffDetonate.class)
				.addPattern("detonate %entities%")
				.supplier(EffDetonate::new)
				.build()
		);
	}

	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
 		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof Firework firework) {
				firework.detonate();
			} else if (entity instanceof WindCharge windCharge) {
				windCharge.explode();
			} else if (entity instanceof ExplosiveMinecart explosiveMinecart) {
				explosiveMinecart.explode();
			} else if (entity instanceof Creeper creeper) {
				creeper.explode();
			} else if (entity instanceof TNTPrimed tntPrimed) {
				tntPrimed.setFuseTicks(0);
			}
		}
	}

	public String toString(@Nullable Event event, boolean debug) {
		return "detonate " + entities.toString(event, debug);
	}

}
