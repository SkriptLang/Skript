package org.skriptlang.skript.bukkit.command.elements.structures.util;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;

/**
 * Utility record for passing around an argument builder with additional data.
 * @param builder The builder for the argument.
 * @param data The data describing the argument.
 */
public record ScriptArgumentBuilder(ArgumentBuilder<CommandSourceStack, ?> builder, @Nullable ArgumentData<?> data) { }
