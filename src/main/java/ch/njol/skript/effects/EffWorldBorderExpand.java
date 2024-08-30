package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Expand/Shrink World Border")
@Description({
	"Expand or shrink the size of a world border.",
	"Note: Using <code>by</code> adds/subtracts from the current size of the world border.",
	"Using <code>to</code> sets to the specified size."
})
@Examples({
	"expand world border of player by 100 in 5 seconds",
	"shrink world border of world \"world\" to 100 in 10 seconds"
})
@Since("INSERT VERSION")
public class EffWorldBorderExpand extends Effect {

	static {
		Skript.registerEffect(EffWorldBorderExpand.class,
			"(expand|:shrink) (radius|:diameter) of %worldborders% (by|:to) %number% [over [[a ](time|period) of] %-timespan%]",
			"(expand|:shrink) %worldborders%'s (radius|:diameter) (by|:to) %number% [over [[a ](time|period) of] %-timespan%]"

		);
	}

	private boolean shrink;
	private boolean diameter;
	private boolean to;
	private Expression<WorldBorder> worldBorders;
	private Expression<Number> number;
	@Nullable
	private Expression<Timespan> timespan;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worldBorders = (Expression<WorldBorder>) exprs[0];
		number = (Expression<Number>) exprs[1];
		timespan = (Expression<Timespan>) exprs[2];
		shrink = parseResult.hasTag("shrink");
		diameter = parseResult.hasTag("diameter");
		to = parseResult.hasTag("to");
		return true;
	}

	@Override
	protected void execute(Event event) {
		double input = number.getOptionalSingle(event).orElse(0).doubleValue();
		if (!diameter)
			input *= 2;
		long speed = 0;
		if (timespan != null) {
			Timespan timespan = this.timespan.getSingle(event);
			if (timespan != null)
				speed = timespan.getAs(TimePeriod.SECOND);
		}
		WorldBorder[] worldBorders = this.worldBorders.getAll(event);
		if (to) {
			for (WorldBorder worldBorder : worldBorders)
				worldBorder.setSize(Math.max(1, Math.min(input, 6.0E7)), speed);
		} else {
			if (shrink)
				input = -input;
			for (WorldBorder worldBorder : worldBorders) {
				double size = worldBorder.getSize();
				size = Math.max(1, Math.min(size + input, 6.0E7));
				worldBorder.setSize(size, speed);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (shrink ? "shrink " : "expand ")
			+ (diameter ? "diameter " : "radius ") + "of " + worldBorders.toString(event, debug)
			+ (to ? " to " : " by ") + number.toString(event, debug)
			+ (timespan == null ? "" : " over " + timespan.toString(event, debug));
	}

}
