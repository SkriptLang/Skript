/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
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
import ch.njol.util.Kleenean;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Expand/Shrink World Border")
@Description({
	"Expand or shrink the size of a world border.",
	"Note: Using <code>by</code> adds/subtracts from the current size of the world border. Using <code>to</code> sets to the specified size."
})
@Examples({
	"expand world border of player by 100 in 5 seconds",
	"shrink world border of world \"world\" to 100 in 10 seconds"
})
@Since("INSERT VERSION")
public class EffWorldBorderExpand extends Effect {

	static {
		Skript.registerEffect(EffWorldBorderExpand.class,
			"(expand|:shrink) %worldborders% by %number% [over [a [(time|period) of]] %-timespan%]",
			"(expand|:shrink) %worldborders% to %number% [over [a [(time|period) of]] %-timespan%]");
	}

	private boolean shrink;
	private int pattern;
	private Expression<WorldBorder> worldBorders;
	private Expression<Number> numberExpr;
	@Nullable
	private Expression<Timespan> timespanExpr;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worldBorders = (Expression<WorldBorder>) exprs[0];
		numberExpr = (Expression<Number>) exprs[1];
		timespanExpr = (Expression<Timespan>) exprs[2];
		shrink = parseResult.hasTag("shrink");
		pattern = matchedPattern;
		return true;
	}

	@Override
	protected void execute(Event event) {
		double input = numberExpr.getOptionalSingle(event).orElse(0).doubleValue();
		long speed = 0;
		if (timespanExpr != null) {
			Timespan timespan = timespanExpr.getSingle(event);
			if (timespan != null)
				speed = timespan.getMilliSeconds() / 1000;
		}
		WorldBorder[] worldBorders = this.worldBorders.getAll(event);
		if (pattern == 0) {
			if (shrink)
				input = -input;
			for (WorldBorder worldBorder : worldBorders) {
				double size = worldBorder.getSize();
				size = Math.max(1, Math.min(size + input, 6.0E7));
				worldBorder.setSize(size, speed);
			}
		} else {
			for (WorldBorder worldBorder : worldBorders)
				worldBorder.setSize(Math.max(1, Math.min(input, 6.0E7)), speed);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (shrink ? "shrink " : "expand ") + worldBorders.toString(event, debug)
			+ (pattern == 0 ? " by " : " to ") + numberExpr.toString(event, debug)
			+ (timespanExpr == null ? "" : " over " + timespanExpr.toString(event, debug));
	}

}
