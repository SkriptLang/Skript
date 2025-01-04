package org.skriptlang.skript.bukkit.spawner.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import org.bukkit.block.Block;
import org.bukkit.block.TrialSpawner;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerConfig;

public class CondIsOminous extends PropertyCondition<Object> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, CondIsOminous.class,
			"ominous", "trialspawnerconfigs/blocks/blockdatas");
	}

	@Override
	public boolean check(Object object) {
		if (object instanceof TrialSpawnerConfig config) {
			return config.ominous();
		} else if (object instanceof Block block && block.getState() instanceof TrialSpawner spawner) {
			return spawner.isOminous();
		} else if (object instanceof org.bukkit.block.data.type.TrialSpawner spawner) {
			return spawner.isOminous();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "trial spawner is " + (isNegated() ? "not " : "") + "ominous";
	}

}
