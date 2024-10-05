package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExprTagKey extends SimplePropertyExpression<Tag<?>, String> {

	static {
		register(ExprTagKey.class, String.class, "[namespace[d]] key", "minecrafttags");
	}

	@Override
	public @Nullable String convert(@NotNull Tag<?> from) {
		return from.getKey().toString();
	}

	@Override
	protected String getPropertyName() {
		return "namespaced key";
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

}
