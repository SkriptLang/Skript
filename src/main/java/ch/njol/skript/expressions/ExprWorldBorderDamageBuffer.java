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

@Name("Damage Buffer of World Border")
@Description({
	"The amount of blocks a player may safely be outside the border before taking damage.",
	"Players only take damage when outside of the world's world border, and the damage buffer distance cannot be less than 0."
})
@Examples("set world border damage buffer of {_worldborder} to 10")
@Since("INSERT VERSION")
public class ExprWorldBorderDamageBuffer extends SimplePropertyExpression<WorldBorder, Double> {

	static {
		registerDefault(ExprWorldBorderDamageBuffer.class, Double.class, "world[ ]border damage buffer", "worldborders");
	}

	@Override
	public @Nullable Double convert(WorldBorder worldBorder) {
		return worldBorder.getDamageBuffer();
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
		double input = mode == ChangeMode.RESET ? 5 : ((Number) delta[0]).doubleValue();
		if (Double.isNaN(input)) {
			error("NaN is not a valid world border damage buffer");
			return;
		}
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET, RESET -> worldBorder.setDamageBuffer(Math.max(input, 0));
				case ADD -> worldBorder.setDamageBuffer(Math.max(worldBorder.getDamageBuffer() + input, 0));
				case REMOVE -> worldBorder.setDamageBuffer(Math.max(worldBorder.getDamageBuffer() - input, 0));
			}
		}
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

	@Override
	protected String getPropertyName() {
		return "world border damage buffer";
	}

}
