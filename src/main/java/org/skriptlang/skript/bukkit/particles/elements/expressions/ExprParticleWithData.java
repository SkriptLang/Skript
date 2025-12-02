package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.bukkit.particles.registration.DataParticles;
import org.skriptlang.skript.bukkit.particles.registration.EffectInfo;

public class ExprParticleWithData extends SimpleExpression<ParticleEffect> {

	private static final Patterns<EffectInfo<Particle, Object>> PATTERNS;

	static {
		// create Patterns object
		Object[][] patterns = new Object[DataParticles.getParticleInfos().size()][2];
		int i = 0;
		for (var particleInfo : DataParticles.getParticleInfos()) {
			patterns[i][0] = particleInfo.pattern();
			patterns[i][1] = particleInfo;
			i++;
		}
		PATTERNS = new Patterns<>(patterns);

		Skript.registerExpression(ExprParticleWithData.class, ParticleEffect.class, ExpressionType.COMBINED, PATTERNS.getPatterns());
	}

	private ParseResult parseResult;
	private Expression<?>[] expressions;
	private EffectInfo<Particle, Object> effectInfo;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.parseResult = parseResult;
		this.expressions = expressions;
		effectInfo = PATTERNS.getInfo(matchedPattern);
		return effectInfo != null;
	}

	@Override
	protected ParticleEffect @Nullable [] get(Event event) {
		Object data = effectInfo.dataSupplier().getData(event, expressions, parseResult);
		if (data == null) {
			error("Could not obtain required data for " + ParticleEffect.toString(effectInfo.effect(), 0));
			return null;
		}
		ParticleEffect effect = ParticleEffect.of(effectInfo.effect());
		effect.data(data);
		return new ParticleEffect[] {effect};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends ParticleEffect> getReturnType() {
		return ParticleEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return effectInfo.toStringFunction().toString(expressions, parseResult, new SyntaxStringBuilder(event, debug)).toString();
	}

}
