package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.Effect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.GameEffect;
import org.skriptlang.skript.bukkit.particles.registration.DataGameEffects;
import org.skriptlang.skript.bukkit.particles.registration.EffectInfo;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Game Effects with Data")
@Description("""
	Creates game effects that require some extra information, such as colors, particle counts, or block data.
	Game effects consist of combinations particles and/or sounds that are used in Minecraft, such as \
	the bone meal particles, the sound of footsteps on a specific block, or the particles and sound of breaking a splash potion.
	Game effects not present here do not require data and can be found in the Game Effect type.
	Data requirements vary from version to version, so these docs are only accurate for the most recent Minecraft \
	version at time of release.
	""")
@Example("play compost success sound effect to player")
@Since("INSERT VERSION")
public class ExprGameEffectWithData extends SimpleExpression<GameEffect> {

	private static Patterns<EffectInfo<Effect, Object>> PATTERNS;

	public static void register(@NotNull SyntaxRegistry registry, @NotNull Origin origin) {
		// create Patterns object
		Object[][] patterns = new Object[DataGameEffects.getGameEffectInfos().size()][2];
		int i = 0;
		for (var gameEffectInfo : DataGameEffects.getGameEffectInfos()) {
			patterns[i][0] = gameEffectInfo.pattern();
			patterns[i][1] = gameEffectInfo;
			i++;
		}
		PATTERNS = new Patterns<>(patterns);

		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprGameEffectWithData.class, GameEffect.class)
				.addPatterns(PATTERNS.getPatterns())
				.supplier(ExprGameEffectWithData::new)
				.priority(SyntaxInfo.COMBINED)
				.origin(origin)
				.build());
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
