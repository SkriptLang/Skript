package org.skriptlang.skript.common.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Color;
import org.jetbrains.annotations.Nullable;

@Name("Hex Code")
@Description("""
	Returns the hexadecimal value representing the given color(s).
	The hex value of a colour does not contain a leading #, just the RRGGBB value.
	For those looking for hex values of numbers, see the asBase and fromBase functions.
	""")
@Example("send formatted \"<#%hex code of rgb(100, 10, 10)%>darker red\" to all players")
@Since("INSERT VERSION")
public class ExprHexCode extends SimplePropertyExpression<Color, String> {

	static {
		register(ExprHexCode.class, String.class, "hex[adecimal] code", "colors");
	}

	@Override
	public @Nullable String convert(Color color) {
		return color.toHexString();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "hexadecimal code";
	}

	@Override
	public Expression<? extends String> simplify() {
		if (getExpr() instanceof Literal<?>) {
			return SimplifiedLiteral.fromExpression(this);
		}
		return this;
	}

}
