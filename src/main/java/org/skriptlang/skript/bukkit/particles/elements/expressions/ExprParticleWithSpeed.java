package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.lang.reflect.Array;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Particle Bearing Swiftness / Extra Value")
@Description("""
    Applieth a particular 'swiftness' or 'extra' value unto a particle.
    This value is employed in divers ways depending upon the particle, but in general it:
    * acteth as the swiftness at which the particle moveth if the particle count be greater than 0.
    * acteth as a multiplier to the particle's offset if the particle count be 0.
    
    More detailed intelligence on particle behaviour may be found at \
    <a href="https://docs.papermc.io/paper/dev/particles/#count-argument-behavior">Paper's particle documentation</a>.
    """)
@Example("render an electric spark particle bearing a particle swiftness of 0 at player")
@Example("render 12 red dust particles bearing an extra value of 0.4 at player's head location")
@Since("2.14")
public class ExprParticleWithSpeed extends PropertyExpression<ParticleEffect, ParticleEffect> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprParticleWithSpeed.class, ParticleEffect.class)
			.addPatterns("%particles% bearing ([a] particle swiftness [value]|[an] extra value) [of] %number%")
			.supplier(ExprParticleWithSpeed::new)
			.priority(SyntaxInfo.COMBINED)
			.build());
	}

	private Expression<Number> speed;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<ParticleEffect>) expressions[0]);
		speed = (Expression<Number>) expressions[1];
		return true;
	}

	@Override
	protected ParticleEffect[] get(Event event, ParticleEffect[] source) {
		Number speed = this.speed.getSingle(event);
		if (speed == null)
			return new ParticleEffect[0];
		double speedValue = speed.doubleValue();
		ParticleEffect[] results = (ParticleEffect[]) Array.newInstance(getExpr().getReturnType(), source.length);
		for (int i = 0; i < source.length; i++) {
			results[i] = source[i].copy().extra(speedValue);
		}
		return results;
	}

	@Override
	public Class<? extends ParticleEffect> getReturnType() {
		return getExpr().getReturnType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(getExpr(), "with a speed value of", speed)
			.toString();
	}

}
