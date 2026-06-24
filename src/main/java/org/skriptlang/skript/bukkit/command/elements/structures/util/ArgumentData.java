package org.skriptlang.skript.bukkit.command.elements.structures.util;

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
record ArgumentData<T>(
	String name,
	boolean isAutomaticName,
	ClassInfo<T> type,
	@Nullable Expression<T> defaultValue
) { }
