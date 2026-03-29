package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Expand or Diminish the World's Border")
@Description({
	"Expand or diminish the breadth of a world border.",
	"Employing `by` doth add or subtract from the present size of the world border.",
	"Employing `to` doth set it to the specified measure."
})
@Example("expand world border of player by 100 over 5 seconds")
@Example("diminish world border of world \"world\" to 100 over 10 seconds")
@Since("2.11")
public class EffWorldBorderExpand extends Effect {

	static {
		Skript.registerEffect(EffWorldBorderExpand.class,
			"(expand|extend) [[the] (diameter|:radius) of] %worldborders% (by|:to) %number% [over [a span of] %-timespan%]",
			"(expand|extend) %worldborders%['s (diameter|:radius)] (by|:to) %number% [over [a span of] %-timespan%]",
			"(contract|diminish) [[the] (diameter|:radius) of] %worldborders% (by|:to) %number% [over [a span of] %-timespan%]",
			"(contract|diminish) %worldborders%['s (diameter|:radius)] (by|:to) %number% [over [a span of] %-timespan%]"
		);
	}

	private boolean shrink;
	private boolean radius;
	private boolean to;
	private Expression<WorldBorder> worldBorders;
	private Expression<Number> numberExpr;
	private @Nullable Expression<Timespan> timespan;
	private static final double MAX_WORLDBORDER_SIZE = 59999968;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worldBorders = (Expression<WorldBorder>) exprs[0];
		numberExpr = (Expression<Number>) exprs[1];
		timespan = (Expression<Timespan>) exprs[2];
		shrink = matchedPattern > 1;
		radius = parseResult.hasTag("radius");
		to = parseResult.hasTag("to");
		return true;
	}

	@Override
	protected void execute(Event event) {
		Number number = numberExpr.getSingle(event);
		if (number == null)
			return;
		double input = number.doubleValue();
		if (Double.isNaN(input)) {
			error("You can't " + (shrink ? "shrink" : "grow") + " a world border " + (to ? "to" : "by") + " NaN.");
			return;
		}
		if (radius)
			input *= 2;
		long speed = 0;
		if (timespan != null) {
			Timespan timespan = this.timespan.getSingle(event);
			if (timespan != null)
				speed = timespan.getAs(TimePeriod.SECOND);
		}
		WorldBorder[] worldBorders = this.worldBorders.getArray(event);
		if (to) {
			input = Math2.fit(1, input, MAX_WORLDBORDER_SIZE);
			for (WorldBorder worldBorder : worldBorders)
				worldBorder.setSize(input, speed);
		} else {
			if (shrink)
				input = -input;
			for (WorldBorder worldBorder : worldBorders) {
				double size = worldBorder.getSize();
				size = Math2.fit(1, size + input, MAX_WORLDBORDER_SIZE);
				worldBorder.setSize(size, speed);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(shrink ? "shrink" : "expand");
		builder.append(radius ? "radius" : "diameter");
		builder.append("of", worldBorders);
		builder.append(to ? "to" : "by");
		builder.append(numberExpr);
		if (timespan != null)
			builder.append("over", timespan);
		return builder.toString();
	}

}
