package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.block.spawner.SpawnRule;

public class SpawnRuleWrapper extends SpawnRule {

	public SpawnRuleWrapper(int minBlockLight, int maxBlockLight, int minSkyLight, int maxSkyLight) {
		super(minBlockLight, maxBlockLight, minSkyLight, maxSkyLight);
	}

	@Override
	public void setMinBlockLight(int minBlockLight) {
		if (minBlockLight >= 0 && getMaxBlockLight() >= minBlockLight) {
			super.setMinBlockLight(minBlockLight);
		}
	}

	@Override
	public void setMaxBlockLight(int maxBlockLight) {
		if (maxBlockLight >= 0 && getMinBlockLight() <= maxBlockLight) {
			super.setMaxBlockLight(maxBlockLight);
		}
	}

	@Override
	public void setMinSkyLight(int minSkyLight) {
		if (minSkyLight >= 0 && getMaxSkyLight() >= minSkyLight) {
			super.setMinSkyLight(minSkyLight);
		}
	}

	@Override
	public void setMaxSkyLight(int maxSkyLight) {
		if (maxSkyLight >= 0 && getMinSkyLight() <= maxSkyLight) {
			super.setMaxSkyLight(maxSkyLight);
		}
	}

}
