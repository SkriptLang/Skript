package org.skriptlang.skript.common.colors.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.colors.ColorUtils;

@Name("Complementary Colours")
@Description({
	"Returns the complementary colour of a given colour(s).",
	"Can optionally use a HSL-based approach if need be, but this is rarely required."
})
@Examples({
	"set {_bluesComplement} to complement of blue",
	"set {_allComplements} to complementary colour of all colours"
})
@Since("INSERT VERSION")
public class ExprComplementaryColor extends SimplePropertyExpression<Color, Color> {

	static {
		register(ExprComplementaryColor.class, Color.class, "[:hsl] complement[ary] colo[u]r[s]", "colors");
	}

	private boolean hsl;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.hsl = parseResult.hasTag("hsl");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Color convert(Color from) {
		return hsl ? ColorUtils.complementColorHSL(from) : ColorUtils.complementColor(from);
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	protected String getPropertyName() {
		return (hsl ? "hsl " : "") + "complementary color";
	}

}
