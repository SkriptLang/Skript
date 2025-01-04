package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.util.common.AnyWeight;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprWeight extends SimplePropertyExpression<Object, Integer> {

	static {
		registerDefault(ExprWeight.class, Integer.class, "weight", "weighteds");
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (object instanceof AnyWeight weighted)
			return weighted.weight();
		assert false;
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		int weight = (int) delta[0];

		for (Object object : getExpr().getArray(event)) {
			if (!(object instanceof AnyWeight weighted))
				continue;

			if (!weighted.supportsWeightChange()) {
				error("The weight of this object cannot be changed.");
				continue;
			}

			switch (mode) {
				case SET -> weighted.setWeight(weight);
				case ADD -> weighted.setWeight(weighted.weight() + weight);
				case REMOVE -> weighted.setWeight(weighted.weight() - weight);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "the weight";
	}

}
