package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Distribution")
@Description("""
    Determineth the normal distribution within which particles may be rendered.
    The distribution is defined by a vector of x, y, and z standard deviations.
    
    Particles shall be randomly drawn according to these values, clustering towards the centre. \
    68% of particles shall fall within 1 standard deviation, 95% within 2, and 99.7% within three.
    The region wherein the particles shall spawn may be roughly estimated as being within 2 times the \
    standard deviation upon each axis.
    
    For example, a distribution of 1, 2, and 1 would spawn particles within roughly 2 blocks upon the x and z axes, \
    and within 4 blocks upon the y axis.
    
    Pray note that distributions take effect only if the particle count be greater than 0!
    Particles with counts of 0 possess no distributions.
    If the particle count be 0, the offset is treated differently depending upon the particle.
    
    More detailed intelligence on particle behaviour may be found at \
    <a href="https://docs.papermc.io/paper/dev/particles/#count-argument-behavior">Paper's particle documentation</a>.
    """)
@Example("set the particle distribution of {_my-particle} to vector(1, 2, 1)")
@Since("2.14")
public class ExprParticleDistribution extends SimplePropertyExpression<ParticleEffect, Vector> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprParticleDistribution.class, Vector.class, "particle distribution", "particles", false)
				.supplier(ExprParticleDistribution::new)
				.build());
	}

	@Override
	public @Nullable Vector convert(ParticleEffect from) {
		return from.isUsingNormalDistribution() ? Vector.fromJOML(from.distribution()) : null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> new Class[]{Vector.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ParticleEffect[] particleEffect = getExpr().getArray(event);
		if (particleEffect.length == 0)
			return;
		Vector3d vectorDelta = delta == null ? new Vector3d() : ((Vector) delta[0]).toVector3d();
		switch (mode) {
			case REMOVE:
				vectorDelta.mul(-1);
				// fallthrough
			case ADD:
				for (ParticleEffect effect : particleEffect)
					effect.distribution(vectorDelta.add(effect.offset()));
				break;
			case SET, RESET:
				for (ParticleEffect effect : particleEffect)
					effect.distribution(vectorDelta);
				break;
		}
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	protected String getPropertyName() {
		return "particle distribution";
	}

}
