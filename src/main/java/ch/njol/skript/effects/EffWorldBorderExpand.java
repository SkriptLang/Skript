package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
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
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Expand/Shrink World Border")
@Description({
	"Expand or shrink the size of a world border.",
	"Using `by` adds/subtracts from the current size of the world border.",
	"Using `to` sets to the specified size."
})
@Examples({
	"expand world border of player by 100 in 5 seconds",
	"shrink world border of world \"world\" to 100 in 10 seconds"
})
@Since("INSERT VERSION")
public class EffWorldBorderExpand extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffWorldBorderExpand.class,
			"(expand|grow) [[the] (diameter|:radius) of] %worldborders% (by|:to) %number% [over [a period of] %-timespan%]",
			"(expand|grow) %worldborders%['s (diameter|:radius)] (by|:to) %number% [over [a period of] %-timespan%]",
			"(contract|shrink) [[the] (diameter|:radius) of] %worldborders% (by|:to) %number% [over [a period of] %-timespan%]",
			"(contract|shrink) %worldborders%['s (diameter|:radius)] (by|:to) %number% [over [a period of] %-timespan%]"
		);
	}

	private boolean shrink;
	private boolean radius;
	private boolean to;
	private Expression<WorldBorder> worldBorders;
	private Expression<Number> numberExpr;
	private @Nullable Expression<Timespan> timespan;
	private static final double MAX_WORLDBORDER_SIZE = 59999968;
	private Node node;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worldBorders = (Expression<WorldBorder>) exprs[0];
		numberExpr = (Expression<Number>) exprs[1];
		timespan = (Expression<Timespan>) exprs[2];
		shrink = matchedPattern > 1;
		radius = parseResult.hasTag("radius");
		to = parseResult.hasTag("to");
		node = getParser().getNode();
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
	public Node getNode() {
		return node;
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
