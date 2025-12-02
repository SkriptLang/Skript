package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.Effect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.GameEffect;
import org.skriptlang.skript.bukkit.particles.registration.DataGameEffects;
import org.skriptlang.skript.bukkit.particles.registration.EffectInfo;

public class ExprGameEffectWithData extends SimpleExpression<GameEffect> {

	private static final Patterns<EffectInfo<Effect, Object>> PATTERNS;

	static {
		// create Patterns object
		Object[][] patterns = new Object[DataGameEffects.getGameEffectInfos().size()][2];
		int i = 0;
		for (var gameEffectInfo : DataGameEffects.getGameEffectInfos()) {
			patterns[i][0] = gameEffectInfo.pattern();
			patterns[i][1] = gameEffectInfo;
			i++;
		}
		PATTERNS = new Patterns<>(patterns);

		Skript.registerExpression(ExprGameEffectWithData.class, GameEffect.class, ExpressionType.COMBINED, PATTERNS.getPatterns());
	}

	private EffectInfo<Effect, Object> gameEffectInfo;
	private Expression<?>[] expressions;
	private ParseResult parseResult;


	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		gameEffectInfo = PATTERNS.getInfo(matchedPattern);
		this.expressions = expressions;
		this.parseResult = parseResult;
		return true;
	}

	@Override
	protected GameEffect @Nullable [] get(Event event) {
		GameEffect gameEffect = new GameEffect(gameEffectInfo.effect());
		Object data = gameEffectInfo.dataSupplier().getData(event, expressions, parseResult);

		if (data == null && gameEffect.getEffect() != Effect.ELECTRIC_SPARK) { // electric spark doesn't require an axis
			error("Could not obtain required data for " + gameEffect);
			return new GameEffect[0];
		}
		boolean success = gameEffect.setData(data);
		if (!success) {
			error("Could not obtain required data for " + gameEffect);
			return new GameEffect[0];
		}
		return new GameEffect[]{gameEffect};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends GameEffect> getReturnType() {
		return GameEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return gameEffectInfo.toStringFunction()
				.toString(expressions, parseResult, new SyntaxStringBuilder(event, debug)).toString();
	}
	
}
