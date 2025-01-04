package org.skriptlang.skript.bukkit.spawner.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.TrialSpawner.State;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

public class CondIsActivated extends PropertyCondition<Object> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, CondIsActivated.class,
			"[an] (activated|active) spawner", "blocks/entities");
	}

	@Override
	public boolean check(Object object) {
		if (SpawnerUtils.isSpawner(object)) {
			return SpawnerUtils.getAsSpawner(object).isActivated();
		} else if (object instanceof Block block && block.getBlockData() instanceof TrialSpawner spawner) {
			return spawner.getTrialSpawnerState() == State.ACTIVE;
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "spawner" + (isNegated() ? " is not " : " is ") + "activated";
	}

}
