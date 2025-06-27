package org.skriptlang.skript.common.colors.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Color;
import org.jetbrains.annotations.Nullable;

@Name("Hex Code")
@Description("Returns the hexadecimal code representing a given color(s).")
@Examples(
		"send formatted \"<%hex code of shade(red, 50)%>slightly darker red\" to all players"
)
@Since("INSERT VERSION")
public class ExprHex extends SimplePropertyExpression<Color, String> {

	static {
		register(ExprHex.class, String.class, "hex [code]", "colors");
	}

	@Override
	public @Nullable String convert(Color from) {
		return from.getHex();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "hex code";
	}

}
