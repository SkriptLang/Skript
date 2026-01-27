package org.skriptlang.skript.bukkit.entity.general.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.entity.Shearable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Shear")
@Description("""
	Shears or un-shears a shearable entity with drops by shearing and a 'sheared' sound. Using with 'force' will force this effect despite the entity's 'shear state'.
	Please note that..:
	- Force-shearing or un-shearing on a sheared mushroom cow is not possible
	""")
@Example("""
	on rightclick on a sheep holding a sword:
		shear the clicked sheep
		chance of 10%
		force shear the clicked sheep
	""")
@Since("2.0 (cows, sheep & snowmen), 2.8.0 (all shearable entities)")
public class EffShear extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffShear.class)
				.addPatterns(
					"[:force] shear %livingentities%",
					"un[-]shear %livingentities%"
				).supplier(EffShear::new)
				.build()
		);
	}

	private Expression<LivingEntity> entity;
	private boolean force;
	private boolean shear;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entity = (Expression<LivingEntity>) exprs[0];
		force = parseResult.hasTag("force");
		shear = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entity.getArray(event)) {
			if (shear) {
				if (!(entity instanceof Shearable shearable))
					continue;
				if (!force && !shearable.readyToBeSheared())
					continue;
				shearable.shear();
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (shear ? "" : "un") + "shear " + entity.toString(event, debug);
	}
	
}
