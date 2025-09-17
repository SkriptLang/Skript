package org.skriptlang.skript.bukkit.spawners.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.TrialSpawner.State;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Active Spawner")
@Description("""
	Checks whether a spawner is active. An active spawner must have a player in its activation range, the sky and \
	block light spawn levels must match the requirements, and there must be an entity type, snapshot or spawner entry \
	assigned to the spawner.
	""")
@Example("""
	if the block at player is an active spawner:
		send "The spawner is activated!" to player
	""")
@Since("INSERT VERSION")
public class CondIsActivated extends PropertyCondition<Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsActivated.class, PropertyType.BE,
			"[an] (activated|active) spawner", "blocks/entities")
				.supplier(CondIsActivated::new)
				.build()
		);
	}

	@Override
	public boolean check(Object object) {
		if (SpawnerUtils.isMobSpawner(object)) {
			return SpawnerUtils.getMobSpawner(object).isActivated();
		} else if (SpawnerUtils.isTrialSpawner(object)) {
			TrialSpawner trialSpawner = (TrialSpawner) SpawnerUtils.getTrialSpawner(object).getBlockData();
			return trialSpawner.getTrialSpawnerState() == State.ACTIVE;
		}

		return false;
	}

	@Override
	protected String getPropertyName() {
		return "an activated spawner";
	}

}
