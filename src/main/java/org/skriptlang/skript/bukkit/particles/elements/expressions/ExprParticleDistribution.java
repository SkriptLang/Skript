package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Distribution")
@Description("""
	Determines the normal distribution that particles may be drawn within.
	The distribution is defined by a vector of x, y, and z standard deviations.
	
	Particles will be randomly drawn based on these values, clustering towards the center. \
	68% of particles will be within 1 standard deviation, 95% within 2, and 99.7% within three.
	The area the particles will spawn in can be roughly estimated as being within 2 times the \
	standard deviation in each axis.
	
	For example, a distribution of 1, 2, and 1 would spawn particles within roughly 2 blocks on the x and z axes, \
	and within 4 blocks on the y axis.
	
	Please note that distributions only take effect if the particle count is greater than 0!
	Particles with counts of 0 do not have distributions.
	If the particle count is 0, the offset is treated differently depending on the particle.
	
	More detailed information on particle behavior can be found at \
	<a href="https://docs.papermc.io/paper/dev/particles/#count-argument-behavior">Paper's particle documentation</a>.
	""")
@Example("set the particle distribution of {_my-particle} to vector(1, 2, 1)")
@Since("2.14")
public class ExprParticleDistribution extends SimplePropertyExpression<ParticleEffect, Vector> {

	public static void register(@NotNull SyntaxRegistry registry, @NotNull Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprParticleDistribution.class, Vector.class, "particle distribution", "particles", false)
				.supplier(ExprParticleDistribution::new)
				.origin(origin)
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
