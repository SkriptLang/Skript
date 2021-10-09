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
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Fishing Hook Wait Time")
@Description({"Returns the minimum and/or maximum waiting time of the fishing hook. Default minimum value is 5 seconds and maximum is 30 seconds.",
			"NOTE: Changing the values in a non sense results such as setting max time less than min time will set both min and max waiting time to the same provided value."})
@Examples({"on fish:",
			"\tset max waiting time of fishing hook to 1 second # Will also force setting the minimum to 1 second"})
@Events("fishing")
@Since("INSERT VERSION")
public class ExprFishingHookWaitTime extends SimplePropertyExpression<FishHook, Timespan> {

	static {
		register(ExprFishingHookWaitTime.class, Timespan.class, "(1¦min[imum]|max[imum]) wait[ing] time", "fishinghook");
	}

	private boolean isMin;

	@Override
	@SuppressWarnings({"null", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<FishHook>) exprs[0]);
		isMin = parseResult.mark == 1;
		return true;
	}

	@Override
	protected String getPropertyName() {
		return "waiting time of fishing hook";
	}

	@Override
	public @Nullable Timespan convert(FishHook fishHook) {
		return Timespan.fromTicks_i(isMin ? fishHook.getMinWaitTime() : fishHook.getMaxWaitTime());
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case RESET:
			case DELETE:
			case REMOVE_ALL:
				return null;
			default:
				return CollectionUtils.array(Timespan.class);
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null || delta[0] == null) {
			return;
		}

		long ticks = ((Timespan) delta[0]).getTicks_i();
		switch (mode) {
			case ADD:
				setWaitingTime(e, (int) ticks, false);
				break;
			case REMOVE:
				setWaitingTime(e, (int) (ticks * -1), false);
				break;
			case SET:
				setWaitingTime(e, (int) ticks, true);
				break;
		}
	}

	private void setWaitingTime(Event e, int value, boolean isSet) {
		FishHook hook = getExpr().getSingle(e);
		if (hook == null)
			return;

		int newValue;
		if (isMin) {
			newValue = Math.max((isSet ? 0 : hook.getMinWaitTime()) + value, 0);

			if (hook.getMaxWaitTime() < newValue)
				hook.setMaxWaitTime(newValue);

			hook.setMinWaitTime(newValue);
		} else {
			newValue = Math.max((isSet ? 0 : hook.getMaxWaitTime()) + value, 0);

			if (hook.getMinWaitTime() > newValue)
				hook.setMinWaitTime(newValue);

			hook.setMaxWaitTime(newValue);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (isMin ? "minimum" : "maximum") + " waiting time of " + getExpr().toString(e, debug);
	}
}
