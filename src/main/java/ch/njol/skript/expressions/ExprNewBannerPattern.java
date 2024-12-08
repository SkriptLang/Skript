package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Banner Pattern")
@Description("Create a new banner pattern.")
@Examples({
	"set {_pattern} to a new creeper banner pattern colored red",
	"add {_pattern} to banner patterns of {_banneritem}",
	"remove {_pattern} from banner patterns of {_banneritem}",
	"set the 1st banner pattern of block at location(0,0,0) to {_pattern}",
	"clear the 1st banner pattern of block at location(0,0,0)",
	"",
	"set {_pattern} to a red mojang banner pattern"
})
@Since("INSERT VERSION")
public class ExprNewBannerPattern extends SimpleExpression<Pattern> {

	static {
		Skript.registerExpression(ExprNewBannerPattern.class, Pattern.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
			"[a] %bannerpatterntype% colo[u]red %color%",
			"[a] %*color% %bannerpatterntype%");
	}

	private Expression<PatternType> selectedPattern;
	private Expression<Color> selectedColor;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			//noinspection unchecked
			selectedPattern = (Expression<PatternType>) exprs[0];
			//noinspection unchecked
			selectedColor = (Expression<Color>) exprs[1];
		} else {
			//noinspection unchecked
			selectedPattern = (Expression<PatternType>) exprs[1];
			//noinspection unchecked
			selectedColor = (Expression<Color>) exprs[0];
		}
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
	public String toString(@Nullable Event event, boolean debug) {
		return "a new " + selectedPattern.toString(event, debug) + " colored " + selectedColor;
	}

}
