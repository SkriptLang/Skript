package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.ParticleModule;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleInfo;

public class ExprParticleWithData extends SimpleExpression<ParticleEffect> {

	private static final Patterns<ParticleInfo<Object>> PATTERNS;

	static {
		// create Patterns object
		Object[][] patterns = new Object[ParticleModule.DATA_PARTICLE_INFOS.size()][2];
		int i = 0;
		for (var particleInfo : ParticleModule.DATA_PARTICLE_INFOS) {
			patterns[i][0] = particleInfo.pattern();
			patterns[i][1] = particleInfo;
			i++;
		}
		PATTERNS = new Patterns<>(patterns);

		Skript.registerExpression(ExprParticleWithData.class, ParticleEffect.class, ExpressionType.COMBINED, PATTERNS.getPatterns());
	}

	private ParseResult parseResult;
	private Expression<?>[] expressions;
	private ParticleInfo<Object> particleInfo;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.parseResult = parseResult;
		this.expressions = expressions;
		particleInfo = PATTERNS.getInfo(matchedPattern);
		return particleInfo != null;
	}

	@Override
	protected ParticleEffect @Nullable [] get(Event event) {
		Object data = particleInfo.dataSupplier().getData(event, expressions, parseResult);
		if (data == null) {
			error("Could not obtain required data for particle " + particleInfo.particle().name());
			return null;
		}
		ParticleEffect effect = ParticleEffect.of(particleInfo.particle());
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
		Object data = particleInfo.dataSupplier().getData(event, expressions, parseResult);
		return particleInfo.toStringFunction().toString(data);
	}

}
