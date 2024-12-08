package org.skriptlang.skript.misc.colours;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Color;
import org.jetbrains.annotations.Nullable;

public class ExprHex extends SimplePropertyExpression<Color, String> {

	static {
		register(ExprHex.class, String.class, "hex [code]", "colors");
	}

	@Override
	public @Nullable String convert(Color from) {
		return ColourUtils.toHex(from);
	}

	@Override
	protected String getPropertyName() {
		return "hex code";
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
}
