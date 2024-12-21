package org.skriptlang.skript.bukkit.displays.generic;

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
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		long ticks = (delta == null ? 0 : ((Timespan) delta[0]).getAs(TimePeriod.TICK));
		switch (mode) {
			case REMOVE:
				ticks = -ticks;
				//$FALL-THROUGH$
			case ADD:
				for (Display display : displays) {
					int value = (int) Math2.fit(0, display.getTeleportDuration() + ticks, 59);
					display.setTeleportDuration(value);
				}
				break;
			case RESET:
			case SET:
				ticks = Math2.fit(0, ticks, 59);
				for (Display display : displays)
					display.setTeleportDuration((int) ticks);
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
