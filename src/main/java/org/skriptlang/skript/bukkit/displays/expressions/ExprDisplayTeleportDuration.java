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
package org.skriptlang.skript.bukkit.displays.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Display Teleport Delay/Duration")
@Description({
	"The teleport duration of displays is the amount of time it takes to get between locations.",
	"0 means that updates are applied immediately.",
	"1 means that the display entity will move from current position to the updated one over one tick.",
	"Higher values spread the movement over multiple ticks. Max of 59 ticks."
})
@Examples({
	"set teleport delay of the last spawned text display to 2 ticks",
	"teleport last spawned text display to {_location}",
	"wait 2 ticks",
	"message \"display entity has arrived at %{_location}%\""
})
@RequiredPlugins("Spigot 1.20.4+")
@Since("INSERT VERSION")
public class ExprDisplayTeleportDuration extends SimplePropertyExpression<Display, Timespan> {

	static {
		if (Skript.isRunningMinecraft(1, 20, 4))
			registerDefault(ExprDisplayTeleportDuration.class, Timespan.class, "teleport[ation] duration[s]", "displays");
	}

	@Override
	@Nullable
	public Timespan convert(Display display) {
		return new Timespan(TimePeriod.TICK, display.getTeleportDuration());
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET -> CollectionUtils.array(Timespan.class);
			case RESET -> CollectionUtils.array();
			case DELETE, REMOVE_ALL -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		int ticks = (int) (delta == null ? 0 : ((Timespan) delta[0]).getTicks()); // TODO: use getAs once fixed
		switch (mode) {
			case REMOVE:
				ticks = -ticks;
			case ADD:
				for (Display display : displays) {
					int value = Math2.fit(0, display.getTeleportDuration() + ticks, 59);
					display.setTeleportDuration(value);
				}
				break;
			case RESET:
			case SET:
				ticks = Math2.fit(0, ticks, 59);
				for (Display display : displays)
					display.setTeleportDuration(ticks);
				break;
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "teleport duration";
	}

}
