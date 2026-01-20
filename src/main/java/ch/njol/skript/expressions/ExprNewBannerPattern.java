package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

@Name("Banner Pattern")
@Description("Creates a new banner pattern.")
@Example("set {_pattern} to a creeper banner pattern colored red")
@Example("add {_pattern} to banner patterns of {_banneritem}")
@Example("remove {_pattern} from banner patterns of {_banneritem}")
@Example("set the 1st banner pattern of block at location(0,0,0) to {_pattern}")
@Example("clear the 1st banner pattern of block at location(0,0,0)")
@Since("2.10")
public class ExprNewBannerPattern extends SimpleExpression<Pattern> {

	static {
		Skript.registerExpression(ExprNewBannerPattern.class, Pattern.class, ExpressionType.COMBINED,
			"[a] %bannerpatterntype% colo[u]red %color%");
	}

	private Expression<PatternType> selectedPattern;
	private Expression<Color> selectedColor;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedPattern = (Expression<PatternType>) exprs[0];
		selectedColor = (Expression<Color>) exprs[1];
		return true;
	}

	@Override
	protected Pattern @Nullable [] get(Event event) {
		Color color = selectedColor.getSingle(event);
		PatternType patternType = selectedPattern.getSingle(event);
		if (color == null || color.asDyeColor() == null || patternType == null)
			return null;

		return new Pattern[]{new Pattern(color.asDyeColor(), patternType)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Pattern> getReturnType() {
		return Pattern.class;
	}

	@Override
	public Expression<? extends Pattern> simplify() {
		if (selectedPattern instanceof Literal<PatternType> && selectedColor instanceof Literal<Color>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a " + selectedPattern.toString(event, debug) + " colored " + selectedColor.toString(event, debug);
	}

}
