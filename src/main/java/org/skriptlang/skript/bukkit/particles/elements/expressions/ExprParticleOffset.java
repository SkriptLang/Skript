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

@Name("Particle Offset")
@Description("""
	Determines the offset value for a particle.
	Offsets are treated as distributions if particle count is greater than 0.
	Offsets are treated as velocity or some other special behavior if particle count is 0.
	Setting distribution/velocity with this method may change the particle count to 1/0 respectively.
	
	More detailed information on particle behavior can be found at \
	<a href="https://docs.papermc.io/paper/dev/particles/#count-argument-behavior">Paper's particle documentation</a>.
	""")
@Example("set the particle offset of {_my-particle} to vector(1, 2, 1)")
@Since("INSERT VERSION")
public class ExprParticleOffset extends SimplePropertyExpression<ParticleEffect, Vector> {

	public static void register(@NotNull SyntaxRegistry registry, @NotNull Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprParticleOffset.class, Vector.class, "particle offset", "particles", false)
				.supplier(ExprParticleOffset::new)
				.origin(origin)
				.build());
	}

	@Override
	public @Nullable Vector convert(ParticleEffect from) {
		return Vector.fromJOML(from.offset());
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
					effect.offset(vectorDelta.add(effect.offset()));
				break;
			case SET, RESET:
				for (ParticleEffect effect : particleEffect)
					effect.offset(vectorDelta);
				break;
		}
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	protected String getPropertyName() {
		return "particle offset";
	}

}
