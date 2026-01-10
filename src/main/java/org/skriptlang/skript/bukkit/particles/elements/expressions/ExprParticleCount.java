package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Count")
@Description("""
	Sets how many particles to draw.
	Particle count has an influence on how the 'offset' and 'extra' values of a particle apply.
	Offsets are treated as distributions if particle count is greater than 0.
	Offsets are treated as velocity or some other special behavior if particle count is 0.
	
	This means that setting the particle count may change how your particle behaves. Be careful!
	
	More detailed information on particle behavior can be found at \
	<a href="https://docs.papermc.io/paper/dev/particles/#count-argument-behavior">Paper's particle documentation</a>.
	""")
@Example("draw 7 blue dust particles at player")
@Since("2.14")
public class ExprParticleCount extends SimplePropertyExpression<ParticleEffect, Number> {

	public static void register(@NotNull SyntaxRegistry registry, @NotNull Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprParticleCount.class, Number.class, "particle count", "particles", false)
				.supplier(ExprParticleCount::new)
				.origin(origin)
				.build());
	}

	@Override
	public Number convert(ParticleEffect from) {
		return from.count();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ParticleEffect[] particleEffect = getExpr().getArray(event);
		if (particleEffect.length == 0)
			return;
		int countDelta = delta == null ? 0 : ((Number) delta[0]).intValue();

		switch (mode) {
			case REMOVE:
				countDelta = -countDelta;
				// fallthrough
			case ADD:
				for (ParticleEffect effect : particleEffect)
					effect.count(Math.clamp(effect.count() + countDelta, 0, 1000));
				break;
			case SET, RESET:
				countDelta = Math.clamp(countDelta, 0, 1000);
				for (ParticleEffect effect : particleEffect)
					effect.count(countDelta); // Limit count to 1000 to prevent unintended crashing
				break;
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "particle count";
	}

}
