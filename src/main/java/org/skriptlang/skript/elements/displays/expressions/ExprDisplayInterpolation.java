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
package org.skriptlang.skript.elements.displays.expressions;

import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Display Interpolation Delay/Duration")
@Description({
	"Returns or changes the interpolation delay/duration of <a href='classes.html#display'>displays</a>.",
	"Interpolation delay is the amount of ticks before client-side interpolation will commence.",
	"Setting to 0 seconds will make it immediate."
})
@Examples("set interpolation delay of the last spawned text display to 2 ticks")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprDisplayInterpolation extends SimplePropertyExpression<Display, Timespan> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprDisplayInterpolation.class, Timespan.class, "interpolation (:delay|duration)[s]", "displays");
	}

	private boolean delay;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		delay = parseResult.hasTag("delay");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Timespan convert(Display display) {
		return Timespan.fromTicks(delay ? display.getInterpolationDelay() : display.getInterpolationDuration());
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(Timespan.class, Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		int ticks = (int) (delta == null ? 0 : (delta[0] instanceof Number ? ((Number) delta[0]).intValue() : ((Timespan) delta[0]).getTicks()));
		switch (mode) {
			case REMOVE_ALL:
			case REMOVE:
				ticks = -ticks;
			case ADD:
				for (Display display : displays) {
					if (delay) {
						int value = Math.max(0, display.getInterpolationDelay() + ticks);
						display.setInterpolationDelay(value);
					} else {
						int value = Math.max(0, display.getInterpolationDuration() + ticks);
						display.setInterpolationDuration(value);
					}
				}
				break;
			case DELETE:
			case RESET:
				for (Display display : displays) {
					if (delay) {
						display.setInterpolationDelay(0);
					} else {
						display.setInterpolationDuration(0);
					}
				}
				break;
			case SET:
				ticks = Math.max(0, ticks);
				for (Display display : displays) {
					if (delay) {
						display.setInterpolationDelay(ticks);
					} else {
						display.setInterpolationDuration(ticks);
					}
				}
				break;
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "interpolation " + (delay ? "delay" : "duration");
	}

}
