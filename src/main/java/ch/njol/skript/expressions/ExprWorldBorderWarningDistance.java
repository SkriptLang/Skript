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
	"The warning distance of a world border. The player's screen will be tinted red when they are within this distance of the border",
	"Note: Distance can not be less than 0"
})
@Examples("set world border warning distance of {_worldborder} to 1")
@Since("INSERT VERSION")
public class ExprWorldBorderWarningDistance extends SimplePropertyExpression<WorldBorder, Integer> {

	static {
		register(ExprWorldBorderWarningDistance.class, Integer.class, "world[ ]border warning distance", "worldborders");
	}

	@Override
	@Nullable
	public Integer convert(WorldBorder worldBorder) {
		return worldBorder.getWarningDistance();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int input = mode == ChangeMode.RESET ? 5 : ((Number) delta[0]).intValue();
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET, RESET -> worldBorder.setWarningDistance(input);
				case ADD -> worldBorder.setWarningDistance(worldBorder.getWarningDistance() + input);
				case REMOVE -> worldBorder.setWarningDistance(worldBorder.getWarningDistance() - input);
			}
			worldBorder.setWarningDistance(Math.max(worldBorder.getWarningDistance(), 0));
		}
	}

	@Override
	protected String getPropertyName() {
		return "border warning distance";
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

}
