package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.trial.TrialSpawnerReward;

public class ExprRewardWeight extends SimplePropertyExpression<TrialSpawnerReward, Integer> {

	static {
		register(ExprRewardWeight.class, Integer.class, "spawner reward weight", "trialspawnerrewards");
	}

	@Override
	public @Nullable Integer convert(TrialSpawnerReward reward) {
		return reward.getWeight();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int weight = delta != null ? ((int) delta[0]) : 0;

		for (TrialSpawnerReward reward : getExpr().getArray(event)) {
			switch (mode) {
				case SET -> reward.setWeight(weight);
				case ADD -> reward.setWeight(reward.getWeight() + weight);
				case REMOVE -> reward.setWeight(reward.getWeight() - weight);
				case RESET -> reward.setWeight(1); // default value
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner reward weight";
	}

}
