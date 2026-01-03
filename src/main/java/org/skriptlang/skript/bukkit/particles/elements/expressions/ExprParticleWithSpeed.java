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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.lang.reflect.Array;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Particle with Speed/Extra Value")
@Description("""
	Applies a specific 'speed' or 'extra' value to a particle.
	This value is used in different ways depending on the particle, but in general it:
	* acts as the speed at which the particle moves if the particle count is greater than 0.
	* acts as a multiplier to the particle's offset if the particle count is 0.
	
	More detailed information on particle behavior can be found at \
	<a href="https://docs.papermc.io/paper/dev/particles/#count-argument-behavior">Paper's particle documentation</a>.
	""")
@Example("draw an electric spark particle with a speed of 0 at player")
@Example("draw 12 red dust particles with an extra value of 0.4 at player's head location")
@Since("2.14")
public class ExprParticleWithSpeed extends PropertyExpression<ParticleEffect, ParticleEffect> {

	public static void register(@NotNull SyntaxRegistry registry, @NotNull Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprParticleWithSpeed.class, ParticleEffect.class)
			.addPatterns("%particles% with ([a] particle speed [value]|[an] extra value) [of] %number%")
			.supplier(ExprParticleWithSpeed::new)
			.priority(SyntaxInfo.COMBINED)
			.origin(origin)
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
