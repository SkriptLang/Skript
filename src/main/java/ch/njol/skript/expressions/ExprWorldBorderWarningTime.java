package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Name("Warning Time of World Border")
@Description("The warning time of a world border. If the border is shrinking, the player's screen will be tinted red once the border will catch the player within this time period.")
@Example("set world border warning time of {_worldborder} to 1 second")
@Since("2.11")
public class ExprWorldBorderWarningTime extends SimplePropertyExpression<WorldBorder, Timespan> {

	static {
		registerDefault(ExprWorldBorderWarningTime.class, Timespan.class, "world[ ]border warning time", "worldborders");
	}

	@Override
	public @Nullable Timespan convert(WorldBorder worldBorder) {
		return new Timespan(TimePeriod.SECOND, worldBorder.getWarningTimeTicks());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		long input = delta == null ? 15 : (((Timespan) delta[0]).getAs(TimePeriod.SECOND));
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			long warnTime = Objects.equals(Bukkit.getBukkitVersion().split("-")[0], "1.21.11")
				? worldBorder.getWarningTimeTicks()
				: worldBorder.getWarningTime();
			long warningTime = switch (mode) {
				case SET, RESET -> input;
				case ADD -> Math2.addClamped(warnTime, input);
				case REMOVE -> Math2.addClamped(warnTime, -input);
				default -> throw new IllegalStateException();
			};
			setWarningTime(worldBorder, warningTime);
		}
	}

	private static void setWarningTime(WorldBorder worldBorder, long inputTime) {
		// make sure this won't cause an overflow, as internal value is in ticks
		long time = Math2.multiplyClamped(inputTime, 20);
		// fit and convert back to seconds
		int warningTime = ((int) Math2.fit(0, time, Integer.MAX_VALUE)) / 20;
		if (Objects.equals(Bukkit.getBukkitVersion().split("-")[0], "1.21.11")) {
			worldBorder.setWarningTimeTicks(warningTime);
		} else {
			worldBorder.setWarningTime(warningTime);

		}

	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "world border warning time";
	}

}
