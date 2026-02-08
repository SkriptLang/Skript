package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Make Entity Scream")
@Description("Make a goat or enderman start or stop screaming.")
@Example("""
		make last spawned goat start screaming
		force last spawned goat to stop screaming
	"""
)
@Example("""
		make {_enderman} scream
		force {_enderman} to stop screaming
	"""
)
@Since("2.11")
public class EffScreaming extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffScreaming.class)
				.addPatterns(
					"make %livingentities% (start screaming|scream)",
					"force %livingentities% to (start screaming|scream)",
					"make %livingentities% stop screaming",
					"force %livingentities% to stop screaming"
				).supplier(EffScreaming::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private boolean scream;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		scream = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Goat goat) {
				goat.setScreaming(scream);
			} else if (entity instanceof Enderman enderman) {
				enderman.setScreaming(scream);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (scream ? " start " : " stop ") + "screaming";
	}

}
