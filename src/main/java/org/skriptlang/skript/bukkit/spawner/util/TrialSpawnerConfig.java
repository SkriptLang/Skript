package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.block.BlockState;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.NotNull;

public final class TrialSpawnerConfig {

	private final @NotNull TrialSpawnerConfiguration config;
	private final @NotNull BlockState state;
	private final boolean ominous;

	public TrialSpawnerConfig(@NotNull TrialSpawnerConfiguration config, @NotNull BlockState state, boolean ominous) {
		this.config = config;
		this.state = state;
		this.ominous = ominous;
	}

	public @NotNull TrialSpawnerConfiguration getConfig() {
		return config;
	}

	public boolean isOminous() {
		return ominous;
	}

	public @NotNull BlockState getState() {
		return state;
	}

}
