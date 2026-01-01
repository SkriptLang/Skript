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

@Name("Particle Speed/Extra Value")
@Description("""
	Determines the specific 'speed' or 'extra' value of a particle.
	This value is used in different ways depending on the particle, but in general it:
	* acts as the speed at which the particle moves if the particle count is greater than 0.
	* acts as a multiplier to the particle's offset if the particle count is 0.
	
	More detailed information on particle behavior can be found at \
	<a href="https://docs.papermc.io/paper/dev/particles/#count-argument-behavior">Paper's particle documentation</a>.
	""")
@Example("set the extra value of {_my-flame-particle} to 2")
@Example("set the particle speed of {_my-flame-particle} to 0")
@Since("INSERT VERSION")
public class ExprParticleSpeed extends SimplePropertyExpression<ParticleEffect, Number> {

	public static void register(@NotNull SyntaxRegistry registry, @NotNull Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprParticleSpeed.class, Number.class, "(particle speed [value]|extra value)", "particles", false)
				.supplier(ExprParticleSpeed::new)
				.origin(origin)
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
