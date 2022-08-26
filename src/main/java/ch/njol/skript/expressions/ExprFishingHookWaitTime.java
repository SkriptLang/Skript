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
package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Fishing Hook Wait Time")
@Description({
	"Returns the minimum and/or maximum waiting time of the fishing hook. Default minimum value is 5 seconds and maximum is 30 seconds.",
	"NOTE: Changing the values in a non sense results such as setting max time less than min time will set both min and max " +
		"waiting time to the same provided value."
})
@Examples({
	"on fish:",
	"\tset max waiting time of fishing hook to 1 second # Will also force setting the minimum to 1 second"
})
@Events("fishing")
@Since("INSERT VERSION")
public class ExprFishingHookWaitTime extends SimplePropertyExpression<FishHook, Timespan> {

	static {
		register(ExprFishingHookWaitTime.class, Timespan.class, "(1:min[imum]|max[imum]) wait[ing] time", "fishinghooks");
	}

	private static final int DEFAULT_MINIMUM_TIME = 100;
	private static final int DEFAULT_MAXIMUM_TIME = 600;
	private boolean isMin;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMin = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Timespan convert(FishHook fishHook) {
		return Timespan.fromTicks_i(isMin ? fishHook.getMinWaitTime() : fishHook.getMaxWaitTime());
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return (isMin ? "minimum" : "maximum") + " waiting time";
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case DELETE:
			case REMOVE_ALL:
				return null;
			default:
				return CollectionUtils.array(Timespan.class);
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode != ChangeMode.RESET && delta == null)
			return;

		int ticks = mode == ChangeMode.RESET ? (isMin ? DEFAULT_MINIMUM_TIME : DEFAULT_MAXIMUM_TIME) : (int) ((Timespan) delta[0]).getTicks_i();
		switch (mode) {
			case ADD:
				setWaitingTime(event, ticks, false);
				break;
			case REMOVE:
				setWaitingTime(event, (ticks * -1), false);
				break;
			case SET:
			case RESET:
				setWaitingTime(event, ticks, true);
				break;
		}
	}

	private void setWaitingTime(Event event, int value, boolean isSet) {
		for (FishHook hook : getExpr().getArray(event)) {
			if (isMin) {
				int newValue = Math.max((isSet ? 0 : hook.getMinWaitTime()) + value, 0);
				if (hook.getMaxWaitTime() < newValue)
					hook.setMaxWaitTime(newValue);

				hook.setMinWaitTime(newValue);
			} else {
				int newValue = Math.max((isSet ? 0 : hook.getMaxWaitTime()) + value, 0);
				if (hook.getMinWaitTime() > newValue)
					hook.setMinWaitTime(newValue);

				hook.setMaxWaitTime(newValue);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isMin ? "minimum" : "maximum") + " waiting time of " + getExpr().toString(event, debug);
	}

}
