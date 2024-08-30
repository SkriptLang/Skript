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
@Examples("set border center of {_worldborder} to location(10, 0, 20)")
@Since("INSERT VERSION")
public class ExprWorldBorderCenter extends SimplePropertyExpression<WorldBorder, Location> {

	static {
		register(ExprWorldBorderCenter.class, Location.class, "[world[ ]]border (center|middle)", "worldborders");
	}

	@Override
	@Nullable
	public Location convert(WorldBorder worldBorder) {
		return worldBorder.getCenter();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET -> CollectionUtils.array(Location.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Location location = mode == ChangeMode.SET ? (Location) delta[0] : null;
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET -> worldBorder.setCenter(location);
				case RESET -> worldBorder.setCenter(0, 0);
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "border center";
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}
}
