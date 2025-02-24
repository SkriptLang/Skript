package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Warning Time of World Border")
@Description("The warning time of a world border. If the border is shrinking, the player's screen will be tinted red once the border will catch the player within this time period.")
@Examples("set world border warning time of {_worldborder} to 1 second")
@Since("INSERT VERSION")
public class ExprWorldBorderWarningTime extends SimplePropertyExpression<WorldBorder, Timespan> {

	static {
		registerDefault(ExprWorldBorderWarningTime.class, Timespan.class, "world[ ]border warning time", "worldborders");
	}

	@Override
	public @Nullable Timespan convert(WorldBorder worldBorder) {
		return new Timespan(TimePeriod.SECOND, worldBorder.getWarningTime());
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
		int input = mode == ChangeMode.RESET ? 15 : (int) (((Timespan) delta[0]).getAs(TimePeriod.SECOND));
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET, RESET -> worldBorder.setWarningTime(input);
				case ADD -> worldBorder.setWarningTime(worldBorder.getWarningTime() + input);
				case REMOVE -> worldBorder.setWarningTime(Math.max(worldBorder.getWarningTime() - input, 0));
			}
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
