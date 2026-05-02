package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Entity Gravity")
@Description("Change whether an entity is affected by gravity.")
@Example("enable gravity of target entity")
@Example("disable last spawned entity's gravity")
@Since("INSERT VERSION")
public class EffGravity extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffGravity.class)
				.addPatterns(
					"(enable|:disable) (gravity) of %entities%",
					"(enable|:disable) %entities%'s (gravity)"
				)
				.supplier(EffGravity::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private boolean negated;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) expressions[0];
		negated = parseResult.hasTag("disable");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			entity.setGravity(!negated);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(!negated, "enable")
			.appendIf(negated, "disable")
			.append("gravity of", entities)
			.toString();
	}

}
