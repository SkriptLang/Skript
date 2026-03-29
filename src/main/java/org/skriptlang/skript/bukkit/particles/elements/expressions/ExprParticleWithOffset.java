package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.bukkit.particles.particleeffects.DirectionalEffect;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.lang.reflect.Array;
import java.util.function.BiFunction;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Particle Bearing Offset, Distribution, or Velocity")
@Description("""
    Applieth a particular offset unto a particle.
    Offsets art treated as distributions if particle count be greater than 0.
    Offsets art treated as velocity or some other special behaviour if particle count be 0.
    Setting distribution or velocity by this method may alter the particle count to 1 or 0 respectively.
    
    More detailed intelligence on particle behaviour may be found at \
    <a href="https://docs.papermc.io/paper/dev/particles/#count-argument-behavior">Paper's particle documentation</a>.
    """)
@Example("render an electric spark particle bearing a velocity of vector(1,2,3) at player")
@Example("render 12 red dust particles bearing a distribution of vector(1,2,1) at player's head location")
@Since("2.14")
public class ExprParticleWithOffset extends PropertyExpression<ParticleEffect, ParticleEffect> {

	enum Mode {
		OFFSET(ParticleEffect::offset),
		DISTRIBUTION(ParticleEffect::distribution),
		VELOCITY((particle, offset) -> ((DirectionalEffect) particle).velocity(offset));

		private final BiFunction<ParticleEffect, Vector3d, ParticleEffect> apply;

		Mode(BiFunction<ParticleEffect, Vector3d, ParticleEffect> apply) {
			this.apply = apply;
		}

		public ParticleEffect apply(ParticleEffect particle, Vector3d offset) {
			return this.apply.apply(particle, offset);
		}
	}

	private static final Patterns<Mode> patterns = new Patterns<>(new Object[][]{
		{"%particles% bearing [an] offset [of] %vector%", Mode.OFFSET},
		{"%particles% with [a] distribution [of] %vector%", Mode.DISTRIBUTION},
		{"%directionalparticles% with [a] velocity [of] %vector%", Mode.VELOCITY}
	});

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprParticleWithOffset.class, ParticleEffect.class)
			.addPatterns(patterns.getPatterns())
			.supplier(ExprParticleWithOffset::new)
			.priority(SyntaxInfo.COMBINED)
			.build());
	}

	private Mode mode;
	private Expression<Vector> offset;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		mode = patterns.getInfo(matchedPattern);
		setExpr((Expression<? extends ParticleEffect>) expressions[0]);
		offset = (Expression<Vector>) expressions[1];
		return true;
	}

	@Override
	protected ParticleEffect[] get(Event event, ParticleEffect[] source) {
		Vector offset = this.offset.getSingle(event);
		if (offset == null)
			return new ParticleEffect[0];
		Vector3d offsetJoml = offset.toVector3d();
		ParticleEffect[] results = (ParticleEffect[]) Array.newInstance(getExpr().getReturnType(), source.length);
		for (int i = 0; i < source.length; i++) {
			results[i] = mode.apply(source[i].copy(), offsetJoml);
		}
		return results;
	}

	@Override
	public Class<? extends ParticleEffect> getReturnType() {
		return getExpr().getReturnType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder ssb = new SyntaxStringBuilder(event, debug);
		ssb.append(getExpr(), "with");
		switch (mode) {
			case OFFSET -> ssb.append("an offset");
			case DISTRIBUTION -> ssb.append("a distribution");
			case VELOCITY -> ssb.append("a velocity");
		}
		ssb.append("of", offset);
		return ssb.toString();
	}

}
