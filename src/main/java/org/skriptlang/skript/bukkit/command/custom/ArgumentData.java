package org.skriptlang.skript.bukkit.command.custom;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;

/**
 * Data describing a command argument.
 * @param name The name of the argument.
 * @param isAutomaticName Whether {@link #name} was automatically generated.
 * @param type The type of the argument.
 * @param defaultValue The default value of the argument, if specified.
 * @param <T> The type of the argument.
 */
public record ArgumentData<T>(
	String name,
	boolean isAutomaticName,
	ClassInfo<T> type,
	boolean isSingle,
	@Nullable Expression<T> defaultValue,
	@Nullable T min,
	@Nullable T max
) { }
