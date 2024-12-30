package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialspawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.TrialSpawner.State;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;

public class ExprTrialSpawnerState extends SimplePropertyExpression<Block, State> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprTrialSpawnerState.class, State.class,
			"[trial] spawner state", "blocks");
	}

	@Override
	public @Nullable State convert(Block block) {
		if (block.getState() instanceof org.bukkit.block.TrialSpawner spawner)
			if (spawner.getBlockData() instanceof TrialSpawner trialSpawner)
				return trialSpawner.getTrialSpawnerState();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(State.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		State state = delta != null ? (State) delta[0] : null;

		for (Block block : getExpr().getArray(event)) {
			if (!(block.getState() instanceof org.bukkit.block.TrialSpawner spawner))
				continue;

			if (spawner.getBlockData() instanceof TrialSpawner trialSpawner) {
				switch (mode) {
					case SET -> {
						// if no players nearby and the spawner is set to active, console gets spammed
						if (state == State.ACTIVE) {
							long count = spawner.getLocation().getNearbyEntitiesByType(Player.class, spawner.getRequiredPlayerRange()).stream()
								.filter(player -> player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
								.count();
							if (count == 0)
								return;
						}
						trialSpawner.setTrialSpawnerState(state);
					}
					case DELETE, RESET -> trialSpawner.setTrialSpawnerState(State.INACTIVE);
				}
				spawner.setBlockData(trialSpawner);
			}
		}
	}

	@Override
	public Class<? extends TrialSpawner.State> getReturnType() {
		return TrialSpawner.State.class;
	}

	@Override
	protected String getPropertyName() {
		return "trial spawner state";
	}

}
