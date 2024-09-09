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
	"Note: Players only take damage when outside of the world's world border",
	"Note: Damage buffer distance can not be less than 0"
})
@Examples("set world border damage buffer of {_worldborder} to 10")
@Since("INSERT VERSION")
public class ExprWorldBorderDamageBuffer extends SimplePropertyExpression<WorldBorder, Double> {

	static {
		register(ExprWorldBorderDamageBuffer.class, Double.class, "world[ ]border damage buffer", "worldborders");
	}

	@Override
	@Nullable
	public Double convert(WorldBorder worldBorder) {
		return worldBorder.getDamageBuffer();
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
		double input = mode == ChangeMode.RESET ? 5 : ((Number) delta[0]).doubleValue();
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET, RESET -> worldBorder.setDamageBuffer(input);
				case ADD -> worldBorder.setDamageBuffer(worldBorder.getDamageBuffer() + input);
				case REMOVE -> worldBorder.setDamageBuffer(worldBorder.getDamageBuffer() - input);
			}
			worldBorder.setDamageBuffer(Math.max(worldBorder.getDamageBuffer(), 0));
		}
	}

	@Override
	protected String getPropertyName() {
		return "border damage buffer";
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

}
