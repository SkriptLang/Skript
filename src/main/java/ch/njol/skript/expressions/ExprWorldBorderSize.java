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
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Size of World Border")
@Description({
	"The size of a world border.",
	"The size can not be smaller than 1."
})
@Examples("set world border radius of {_worldborder} to 10")
@Since("INSERT VERSION")
public class ExprWorldBorderSize extends SimplePropertyExpression<WorldBorder, Double> {

	static {
		registerDefault(ExprWorldBorderSize.class, Double.class, "world[ ]border (size|diameter|:radius)", "worldborders");
	}

	private boolean radius;
	private static final double MAX_WORLDBORDER_SIZE = 59999968;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		radius = parseResult.hasTag("radius");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
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
		double input = mode == ChangeMode.RESET ? MAX_WORLDBORDER_SIZE : ((Number) delta[0]).doubleValue() * (radius ? 2 : 1);
		if (Double.isNaN(input)) {
			error("NaN is not a valid world border size");
			return;
		}
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET, RESET -> worldBorder.setSize(Math2.fit(1, input, MAX_WORLDBORDER_SIZE));
				case ADD -> worldBorder.setSize(Math2.fit(1, worldBorder.getSize() + input, MAX_WORLDBORDER_SIZE));
				case REMOVE -> worldBorder.setSize(Math2.fit(1, worldBorder.getSize() - input, MAX_WORLDBORDER_SIZE));
			}
		}
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

	@Override
	protected String getPropertyName() {
		return "world border size";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "world border " + (radius ? "radius" : "diameter") + " of " + getExpr().toString(event, debug);
	}

}
