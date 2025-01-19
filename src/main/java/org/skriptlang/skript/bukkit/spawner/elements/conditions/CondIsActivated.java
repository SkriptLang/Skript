package org.skriptlang.skript.bukkit.spawner.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.TrialSpawner.State;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

@Name("Spawner - Is Active")
@Description(
	"Check if a spawner is active. Inactive spawners have no player in the activation range of the spawner, "
		+ "or the sky/block light spawn levels do not match the requirement, or, if there is no potential spawn "
		+ "assigned to the spawner."
)
@Examples({
	"if the block at player is an active spawner:",
		"\tsend \"The spawner is activated!\""
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class CondIsActivated extends PropertyCondition<Object> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, CondIsActivated.class,
			"[an] (activated|active) spawner", "blocks/entities/trialspawnerconfigs");
	}

	@Override
	public boolean check(Object object) {
		if (SpawnerUtils.isSpawner(object)) {
			return SpawnerUtils.getAsSpawner(object).isActivated();
		} else if (SpawnerUtils.isTrialSpawner(object)) {
			TrialSpawner data = ((TrialSpawner) SpawnerUtils.getAsTrialSpawner(object).getBlockData());
			return data.getTrialSpawnerState() == State.ACTIVE;
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "spawner" + (isNegated() ? " is not " : " is ") + "activated";
	}

}
