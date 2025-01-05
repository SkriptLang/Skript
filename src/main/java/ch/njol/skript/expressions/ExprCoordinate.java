package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Coordinate")
@Description("Represents a given coordinate of a location or entity.")
@Examples({
	"player's y-coordinate is smaller than 40:",
		"	message \"Watch out for lava!\""
})
@Since("1.4.3")
public class ExprCoordinate extends SimplePropertyExpression<Object, Number> {

	static {
		registerDefault(ExprCoordinate.class, Number.class, "(0¦x|1¦y|2¦z)(-| )(coord[inate]|pos[ition]|loc[ation])[s]", "entities/locations");
	}

	private final static char[] axes = {'x', 'y', 'z'};
	private Changer<?> changer;
	private int axis;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		axis = parseResult.mark;
		return true;
	}

	@Override
	public Number convert(Object object) {
		if (object instanceof Entity entity) {
			return axis == 0 ? entity.getX() : axis == 1 ? entity.getY() : entity.getZ();
		} else if (object instanceof Location location) {
			return axis == 0 ? location.getX() : axis == 1 ? location.getY() : location.getZ();
		}
		assert false;
		return null;
	}

	@Override
	protected String getPropertyName() {
		return axes[axis] + "-coordinate";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		changer = Classes.getSuperClassInfo(getExpr().getReturnType()).getChanger();
		return switch (mode) {
			case SET, ADD, REMOVE -> {
				if (getExpr().isSingle() && ChangerUtils.acceptsChange(getExpr(), CollectionUtils.array(ChangeMode.INTERNAL, ChangeMode.SET), Location.class)) {
					yield new Class[] {Number.class};
				}
				yield null;
			}
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
		assert delta != null;
		Object object = getExpr().getSingle(event);
		Location location = object instanceof Entity entity ? entity.getLocation() : (Location) object;
		if (location == null)
			return;
		double n = ((Number) delta[0]).doubleValue();
		if (mode == ChangeMode.REMOVE) {
			n = -n;
			mode = ChangeMode.ADD;
		}
		if (mode == ChangeMode.ADD) {
			if (axis == 0) location.setX(location.getX() + n);
			else if (axis == 1) location.setY(location.getY() + n);
			else location.setZ(location.getZ() + n);
		} else if (mode == ChangeMode.SET) {
			if (axis == 0) location.setX(n);
			else if (axis == 1) location.setY(n);
			else location.setZ(n);
		}
		ChangerUtils.change(getExpr(), event, new Location[] {location}, ChangeMode.INTERNAL, ChangeMode.SET);
	}

}
