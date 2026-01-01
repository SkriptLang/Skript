package org.skriptlang.skript.common.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Color from Hex Code")
@Description("Returns a proper argb color from a hex code string. The hex code must contain RRGGBB values, but can also " +
	"contain a leading # or AARRGGBB format. Invalid codes will cause runtime errors.")
@Example("send color from hex code \"#FFBBA7\"")
@Example("send color from hex code \"FFBBA7\"")
@Example("send color from hex code \"#AAFFBBA7\"")
@Since("INSERT VERSION")
public class ExprColorFromHexCode extends SimplePropertyExpression<String, Color> {

	static {
		Skript.registerExpression(ExprColorFromHexCode.class, Color.class, ExpressionType.PROPERTY,
				"[the] colo[u]r[s] (from|of) hex[adecimal] code[s] %strings%");
	}

	@Override
	public @Nullable Color convert(String from) {
		if (from.startsWith("#")) // strip leading #
			from = from.substring(1);
		Color color = ColorRGB.fromHexString(from);
		if (color == null)
			error("Could not parse '" + from + "' as a hex code!");
		return color;
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	protected String getPropertyName() {
		return "ExprColorFromHexCode - UNUSED";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the color of hex code " + getExpr().toString(event, debug);
	}

	@Override
	public Expression<? extends Color> simplify() {
		if (getExpr() instanceof Literal<?>) {
			return SimplifiedLiteral.fromExpression(this);
		}
		return this;
	}

}
