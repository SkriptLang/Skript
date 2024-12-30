package org.skriptlang.skript.bukkit.spawner.util;

import org.bukkit.block.BlockState;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.NotNull;

public record TrialSpawnerConfig(@NotNull TrialSpawnerConfiguration config, @NotNull BlockState state,
                                 boolean ominous) {

}
