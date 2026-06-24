package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;

public record ArgumentData<T>(
	String name,
	boolean isAutomaticName,
	ClassInfo<T> type,
	@Nullable Expression<T> defaultValue
) { }
