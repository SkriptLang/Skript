package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.block.spawner.SpawnRule;

public class SpawnRuleWrapper extends SpawnRule {

	public SpawnRuleWrapper(int minBlockLight, int maxBlockLight, int minSkyLight, int maxSkyLight) {
		super(minBlockLight, maxBlockLight, minSkyLight, maxSkyLight);
	}

	@Override
	public void setMinBlockLight(int minBlockLight) {
		if (getMaxBlockLight() >= minBlockLight && minBlockLight >= 0 && minBlockLight <= 15) {
			super.setMinBlockLight(minBlockLight);
		}
	}

	@Override
	public void setMaxBlockLight(int maxBlockLight) {
		if (getMinBlockLight() <= maxBlockLight && maxBlockLight >= 0 && maxBlockLight <= 15) {
			super.setMaxBlockLight(maxBlockLight);
		}
	}

	@Override
	public void setMinSkyLight(int minSkyLight) {
		if (getMaxSkyLight() >= minSkyLight && minSkyLight >= 0 && minSkyLight <= 15) {
			super.setMinSkyLight(minSkyLight);
		}
	}

	@Override
	public void setMaxSkyLight(int maxSkyLight) {
		if (getMinSkyLight() <= maxSkyLight && maxSkyLight >= 0 && maxSkyLight <= 15) {
			super.setMaxSkyLight(maxSkyLight);
		}
	}

}
