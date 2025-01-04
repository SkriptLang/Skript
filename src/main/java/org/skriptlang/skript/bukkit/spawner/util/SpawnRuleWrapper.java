package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.block.spawner.SpawnRule;

public class SpawnRuleWrapper extends SpawnRule {

	public SpawnRuleWrapper(int minBlockLight, int maxBlockLight, int minSkyLight, int maxSkyLight) {
		super(minBlockLight, maxBlockLight, minSkyLight, maxSkyLight);
	}

	@Override
	public void setMinBlockLight(int minBlockLight) {
		if (getMaxBlockLight() >= minBlockLight) {
			super.setMinBlockLight(minBlockLight);
		}
	}

	@Override
	public void setMaxBlockLight(int maxBlockLight) {
		if (getMinBlockLight() <= maxBlockLight) {
			super.setMaxBlockLight(maxBlockLight);
		}
	}

	@Override
	public void setMinSkyLight(int minSkyLight) {
		if (getMaxSkyLight() >= minSkyLight) {
			super.setMinSkyLight(minSkyLight);
		}
	}

	@Override
	public void setMaxSkyLight(int maxSkyLight) {
		if (getMinSkyLight() <= maxSkyLight) {
			super.setMaxSkyLight(maxSkyLight);
		}
	}

}
