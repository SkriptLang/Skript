package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Entity AI")
@Description(
	"Change whether an entity has AI. Entities without AI will not try to attack " +
	"other entities and will not be able to move at all. This includes knockback " +
	"from getting hit, falling due to gravity, getting pushed by entities/water, " +
	"changing velocity with commands and any other form of movement. Accumulated " +
	"knockback and velocity changes will be applied when ai is enabled again."
)
@Example("enable artificial intelligence of target entity")
@Example("disable ai of last spawned entity")
@Since("INSERT VERSION")
public class EffAI extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffAI.class)
				.addPatterns(
					"(enable|:disable) (ai|artificial intelligence) (of|for) %livingentities%",
					"(enable|:disable) %livingentities%'s (ai|artificial intelligence)"
				)
				.supplier(EffAI::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private boolean negated;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) expressions[0];
		negated = parseResult.hasTag("disable");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			entity.setAI(!negated);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(negated ? "disable" : "enable")
			.append("ai of", entities)
			.toString();
	}

}
