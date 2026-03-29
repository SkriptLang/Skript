package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Swiftness / Extra Value")
@Description("""
    Determineth the particular 'swiftness' or 'extra' value of a particle.
    This value is employed in divers ways depending upon the particle, but in general it:
    * acteth as the swiftness at which the particle moveth if the particle count be greater than 0.
    * acteth as a multiplier to the particle's offset if the particle count be 0.
    
    More detailed intelligence on particle behaviour may be found at \
    <a href="https://docs.papermc.io/paper/dev/particles/#count-argument-behavior">Paper's particle documentation</a>.
    """)
@Example("set the extra value of {_my-flame-particle} to 2")
@Example("set the particle swiftness of {_my-flame-particle} to 0")
@Since("2.14")
public class ExprParticleSpeed extends SimplePropertyExpression<ParticleEffect, Number> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprParticleSpeed.class, Number.class, "(particle swiftness [value]|extra value)", "particles", false)
				.supplier(ExprParticleSpeed::new)
				.build());
	}

	@Override
	public @Nullable Number convert(ParticleEffect from) {
		return from.extra();
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
		double extraDelta = delta == null ? 0 : ((Number) delta[0]).doubleValue();

		switch (mode) {
			case REMOVE:
				extraDelta = -extraDelta;
				// fallthrough
			case ADD:
				for (ParticleEffect effect : particleEffect)
					effect.extra(effect.extra() + extraDelta);
				break;
			case SET, RESET:
				for (ParticleEffect effect : particleEffect)
					effect.extra(extraDelta);
				break;
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "particle speed";
	}

}
