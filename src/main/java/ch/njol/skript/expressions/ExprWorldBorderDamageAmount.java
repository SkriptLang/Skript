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

@Name("Damage Amount of World Border")
@Description({
	"The amount of damage a player takes per second for each block they are outside the border plus the border buffer.",
	"Note: Players only take damage when outside of the world's world border"
})
@Examples("set damage amount of {_worldborder} to 1")
@Since("INSERT VERSION")
public class ExprWorldBorderDamageAmount extends SimplePropertyExpression<WorldBorder, Double> {

	static {
		register(ExprWorldBorderDamageAmount.class, Double.class, "[[world[ ]]border] damage amount", "worldborders");
	}

	@Override
	@Nullable
	public Double convert(WorldBorder worldBorder) {
		return worldBorder.getDamageAmount();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case RESET:
				return CollectionUtils.array(Number.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		double input = mode == ChangeMode.RESET ? 0.2 : ((Number) delta[0]).doubleValue();
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET:
				case RESET:
					worldBorder.setDamageAmount(input);
					break;
				case ADD:
					worldBorder.setDamageAmount(worldBorder.getDamageAmount() + input);
					break;
				case REMOVE:
					worldBorder.setDamageAmount(worldBorder.getDamageAmount() - input);
					break;
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "border damage amount";
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

}
