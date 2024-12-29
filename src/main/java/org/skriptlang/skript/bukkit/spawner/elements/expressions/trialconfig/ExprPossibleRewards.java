package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.trial.TrialSpawnerConfig;
import org.skriptlang.skript.bukkit.spawner.trial.TrialSpawnerReward;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExprPossibleRewards extends PropertyExpression<TrialSpawnerConfig, TrialSpawnerReward> {

	static {
		Skript.registerExpression(ExprPossibleRewards.class, TrialSpawnerReward.class, ExpressionType.PROPERTY,
			"[the] possible [trial] spawner rewards (from|of) %trialspawnerconfigs%",
			"%trialspawnerconfigs%'[s] possible [trial] spawner rewards"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends TrialSpawnerConfig>) exprs[0]);
		return true;
	}

	@Override
	protected TrialSpawnerReward[] get(Event event, TrialSpawnerConfig[] source) {
		List<TrialSpawnerReward> rewards = new ArrayList<>();
		for (TrialSpawnerConfig config : source) {
			for (Map.Entry<LootTable, Integer> entrySet : config.getConfig().getPossibleRewards().entrySet()) {
				rewards.add(new TrialSpawnerReward(entrySet.getKey(), entrySet.getValue()));
			}
		}

		return rewards.toArray(new TrialSpawnerReward[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(TrialSpawnerReward[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;

		for (TrialSpawnerConfig trialConfig : getExpr().getArray(event)) {
			Map<LootTable, Integer> possibleRewards = null;
			if (mode == ChangeMode.SET)
				possibleRewards = new HashMap<>();

			TrialSpawnerConfiguration config = trialConfig.getConfig();

			for (Object object : delta) {
				TrialSpawnerReward reward = (TrialSpawnerReward) object;
				switch (mode) {
					case SET -> possibleRewards.put(reward.getLootTable(), reward.getWeight());
					case ADD -> config.addPossibleReward(reward.getLootTable(), reward.getWeight());
					case REMOVE -> config.removePossibleReward(reward.getLootTable());
				}
			}

			if (mode == ChangeMode.SET)
				config.setPossibleRewards(possibleRewards);

			SpawnerUtils.updateState(trialConfig.getState());
		}
	}

	@Override
	public Class<? extends TrialSpawnerReward> getReturnType() {
		return TrialSpawnerReward.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("possible trial spawner rewards of")
			.append(getExpr());
		return builder.toString();
	}

}
