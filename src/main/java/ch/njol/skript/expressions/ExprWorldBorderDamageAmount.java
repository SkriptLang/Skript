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
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Damage Amount of World Border")
@Description({
	"The amount of damage a player takes per second for each block they are outside the border plus the border buffer.",
	"Players only take damage when outside of the world's world border, and the damage value cannot be less than 0.",
})
@Examples("set world border damage amount of {_worldborder} to 1")
@Since("INSERT VERSION")
public class ExprWorldBorderDamageAmount extends SimplePropertyExpression<WorldBorder, Double> implements SyntaxRuntimeErrorProducer {

	static {
		registerDefault(ExprWorldBorderDamageAmount.class, Double.class, "world[ ]border damage amount", "worldborders");
	}

	@Override
	public @Nullable Double convert(WorldBorder worldBorder) {
		return worldBorder.getDamageAmount();
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
		double input = mode == ChangeMode.RESET ? 0.2 : ((Number) delta[0]).doubleValue();
		if (Double.isNaN(input)) {
			error("NaN is not a valid world border damage amount");
			return;
		}
		if (Double.isInfinite(input)) {
			error("World border damage amount cannot be infinite");
			return;
		}
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET, RESET -> worldBorder.setDamageAmount(Math.max(input, 0));
				case ADD -> worldBorder.setDamageAmount(Math.max(worldBorder.getDamageAmount() + input, 0));
				case REMOVE -> worldBorder.setDamageAmount(Math.max(worldBorder.getDamageAmount() - input, 0));
			}
		}
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

	@Override
	protected String getPropertyName() {
		return "world border damage amount";
	}

}
