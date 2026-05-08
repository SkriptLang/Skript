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

@Name("Make Entity Glide")
@Description("""
	Makes an entity start/stop gliding if they have an elytra or a chestplate with a glider component. If the entity has neither, the state of the entity will flicker for 1 tick.
	""")
@Example("make last spawned zombie start gliding")
@Since("INSERT VERSION")
public class EffGlide extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffGlide.class)
				.addPatterns(
					"make %livingentities% ((start|begin) gliding|glide)",
					"make %livingentities% (stop gliding|no longer glide)"
				)
				.supplier(EffGlide::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private boolean negated;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) expressions[0];
		negated = matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			entity.setGliding(!negated);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", entities)
			.appendIf(negated, "not")
			.append("glide")
			.toString();
	}

}

