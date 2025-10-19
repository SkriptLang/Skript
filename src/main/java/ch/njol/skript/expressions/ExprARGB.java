package ch.njol.skript.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

import java.util.Locale;
import java.util.function.Function;

@Name("Alpha/Red/Green/Blue Color Value")
@Description({
	"The alpha, red, green, or blue value of colors. Ranges from 0 to 255.",
	"Alpha represents opacity."
})
@Example("broadcast red value of rgb(100, 0, 50) # sends '100'")
@Example("set {_red} to red's red value + 10")
@Keywords({"ARGB", "RGB", "color", "colour"})
@Since("2.10")
public class ExprARGB extends SimplePropertyExpression<Color, Integer> {

	static {
		register(ExprARGB.class, Integer.class, "(:alpha|:red|:green|:blue) (value|component)", "colors");
	}

	private RGB color;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		color = RGB.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Integer convert(Color from) {
		return color.getValue(from);
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return color.name().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public Expression<? extends Integer> simplify() {
		if (getExpr() instanceof Literal<? extends Color>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	/**
	 * helper enum for getting argb values of {@link Color}s.
	 */
	private enum RGB {
		ALPHA(Color::getAlpha),
		RED(Color::getRed),
		GREEN(Color::getGreen),
		BLUE(Color::getBlue);

		private final Function<Color, Integer> get;

		RGB(Function<Color, Integer> get) {
			this.get = get;
		}

		public int getValue(Color from) {
			return get.apply(from);
		}

	}

}
