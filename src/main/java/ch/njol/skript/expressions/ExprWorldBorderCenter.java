package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Center of World Border")
@Description("The center of a world border.")
@Examples("set world border center of {_worldborder} to location(10, 0, 20)")
@Since("INSERT VERSION")
public class ExprWorldBorderCenter extends SimplePropertyExpression<WorldBorder, Location> {

	static {
		registerDefault(ExprWorldBorderCenter.class, Location.class, "world[ ]border (center|middle)", "worldborders");
	}

	@Override
	public @Nullable Location convert(WorldBorder worldBorder) {
		return worldBorder.getCenter();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET -> CollectionUtils.array(Location.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Location location = mode == ChangeMode.SET ? (Location) delta[0] : null;
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET:
					if (Math.abs(location.getX()) > worldBorder.getMaxCenterCoordinate()) {
						location.setX(location.getX() > 0 ? worldBorder.getMaxCenterCoordinate() : -1 * worldBorder.getMaxCenterCoordinate());
					}
					if (Math.abs(location.getZ()) > worldBorder.getMaxCenterCoordinate()) {
						location.setZ(location.getZ() > 0 ? worldBorder.getMaxCenterCoordinate() : -1 * worldBorder.getMaxCenterCoordinate());
					}
					worldBorder.setCenter(location);
					break;
				case RESET:
					worldBorder.setCenter(0, 0);
			}
		}
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	protected String getPropertyName() {
		return "world border center";
	}

}
