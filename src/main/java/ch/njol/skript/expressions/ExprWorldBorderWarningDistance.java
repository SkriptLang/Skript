package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Warning Distance of World Border")
@Description({
	"The warning distance of a world border. The player's screen will be tinted red when they are within this distance of the border.",
	"Players only see a red tint when approaching a world's worldborder and the warning distance has to be an integer greater than 0."
})
@Examples("set world border warning distance of {_worldborder} to 1")
@Since("INSERT VERSION")
public class ExprWorldBorderWarningDistance extends SimplePropertyExpression<WorldBorder, Integer> {

	static {
		registerDefault(ExprWorldBorderWarningDistance.class, Integer.class, "world[ ]border warning distance", "worldborders");
	}

	@Override
	public @Nullable Integer convert(WorldBorder worldBorder) {
		return worldBorder.getWarningDistance();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int input = mode == ChangeMode.RESET ? 5 : ((Number) delta[0]).intValue();
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET, RESET -> worldBorder.setWarningDistance(Math.max(input, 0));
				case ADD -> worldBorder.setWarningDistance(Math.max(worldBorder.getWarningDistance() + input, 0));
				case REMOVE -> worldBorder.setWarningDistance(Math.max(worldBorder.getWarningDistance() - input, 0));
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "world border warning distance";
	}

}
