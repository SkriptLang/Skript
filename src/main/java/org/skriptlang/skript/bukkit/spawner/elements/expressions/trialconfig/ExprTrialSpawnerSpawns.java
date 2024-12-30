package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;

public class ExprTrialSpawnerSpawns extends SimplePropertyExpression<TrialSpawnerConfiguration, Float> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprTrialSpawnerSpawns.class, Float.class,
			"(:additional|base) [trial] spawner spawn (amount|value)[s]", "trialspawnerconfigs");
	}

	private boolean additional;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		additional = parseResult.hasTag("additional");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Float convert(TrialSpawnerConfiguration config) {
		if (additional)
			return config.getAdditionalSpawnsBeforeCooldown();
		return config.getBaseSpawnsBeforeCooldown();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Float.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		float amount = (float) delta[0];

		for (TrialSpawnerConfiguration config : getExpr().getArray(event)) {
			if (additional) {
				switch (mode) {
					case SET -> config.setAdditionalSpawnsBeforeCooldown(amount);
					case ADD -> config.setAdditionalSpawnsBeforeCooldown(config.getAdditionalSpawnsBeforeCooldown() + amount);
					case REMOVE -> config.setAdditionalSpawnsBeforeCooldown(config.getAdditionalSpawnsBeforeCooldown() - amount);
				}
			} else {
				switch (mode) {
					case SET -> config.setBaseSpawnsBeforeCooldown(amount);
					case ADD -> config.setBaseSpawnsBeforeCooldown(config.getBaseSpawnsBeforeCooldown() + amount);
					case REMOVE -> config.setBaseSpawnsBeforeCooldown(config.getBaseSpawnsBeforeCooldown() - amount);
				}
			}
		}
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return (additional ? "additional " : "base ") + "spawns";
	}

}
