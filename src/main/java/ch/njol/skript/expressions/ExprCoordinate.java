package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

/**
 * @author Peter Güttinger
 */
@Name("Coordinate")
@Description("Represents a given coordinate of a location. ")
@Examples({"player's y-coordinate is smaller than 40:",
		"	message \"Watch out for lava!\""})
@Since("1.4.3")
public class ExprCoordinate extends SimplePropertyExpression<Location, Number> {
	
	static {
		register(ExprCoordinate.class, Number.class, "(0¦x|1¦y|2¦z)(-| )(coord[inate]|pos[ition]|loc[ation])[s]", "locations");
	}
	
	private final static char[] axes = {'x', 'y', 'z'};
	
	private int axis;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		axis = parseResult.mark;
		return true;
	}
	
	@Override
	public Number convert(final Location l) {
		return axis == 0 ? l.getX() : axis == 1 ? l.getY() : l.getZ();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
			&& getExpr().isSingle() && Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Location.class))
			return new Class[] {Number.class};
		return null;
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		assert delta != null;
		final Location l = getExpr().getSingle(e);
		if (l == null)
			return;
		double n = ((Number) delta[0]).doubleValue();
		switch (mode) {
			case REMOVE:
				n = -n;
				//$FALL-THROUGH$
			case ADD:
				if (axis == 0) {
					l.setX(l.getX() + n);
				} else if (axis == 1) {
					l.setY(l.getY() + n);
				} else {
					l.setZ(l.getZ() + n);
				}
				getExpr().change(e, new Location[] {l}, ChangeMode.SET);
				break;
			case SET:
				if (axis == 0) {
					l.setX(n);
				} else if (axis == 1) {
					l.setY(n);
				} else {
					l.setZ(n);
				}
				getExpr().change(e, new Location[] {l}, ChangeMode.SET);
				break;
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public Expression<? extends Number> simplify() {
		if (getExpr() instanceof Literal<? extends Location>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	protected String getPropertyName() {
		return "the " + axes[axis] + "-coordinate";
	}

	
}
