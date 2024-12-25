package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Size of World Border")
@Description("The size of a world border.")
@Examples("set world border radius of {_worldborder} to 10")
@Since("INSERT VERSION")
public class ExprWorldBorderSize extends SimplePropertyExpression<WorldBorder, Double> {
	static {
		register(ExprWorldBorderSize.class, Double.class, "world[ ]border (size|diameter|:radius)", "worldborders");
	}

	private boolean radius;

	@Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = parseResult.hasTag("radius");
        setExpr((Expression<? extends WorldBorder>) exprs[0]);
        return true;
    }

	@Override
	public @Nullable Double convert(WorldBorder worldBorder) {
		return worldBorder.getSize() * (radius ? 0.5 : 1);
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
		double input = mode == ChangeMode.RESET ? 6.0E7 : Math.max(1, Math.min(((Number) delta[0]).doubleValue() * (radius ? 2 : 1), 6.0E7));
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET, RESET -> worldBorder.setSize(input);
				case ADD -> worldBorder.setSize(worldBorder.getSize() + input);
				case REMOVE -> worldBorder.setSize(worldBorder.getSize() - input);
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "world border size";
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "border " + (radius ? "radius" : "diameter") + " of " + getExpr().toString(event, debug);
	}

}
