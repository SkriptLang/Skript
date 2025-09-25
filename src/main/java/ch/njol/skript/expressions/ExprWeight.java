package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.util.common.AnyWeighted;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Weight")
@Description("""
	Returns the weight of a weighted object. If supported, this weight can be modified.
	""")
@Example("""
	broadcast weight of {_spawner entry}
	set weight of {_spawner entry} to 5
	add 5 to weight of {_spawner entry}
	remove 2 from weight of {_spawner entry}
	""")
@Since("INSERT VERSION")
public class ExprWeight extends SimplePropertyExpression<AnyWeighted, Number> {

	static {
		register(ExprWeight.class, Number.class, "weight[s]", "weighteds");
	}

	@Override
	public @Nullable Number convert(AnyWeighted weighted) {
		return weighted.weight();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;

		Number deltaValue = (Number) delta[0];
		for (AnyWeighted weighted : getExpr().getArray(event)) {
			if (!weighted.supportsWeightChange())
				error("This object does not support weight modification.");

			Number newValue = switch (mode) {
				case SET -> deltaValue;
				case ADD -> weighted.weight().doubleValue() + deltaValue.doubleValue();
				case REMOVE -> weighted.weight().doubleValue() - deltaValue.doubleValue();
				default -> 0;
			};

			weighted.setWeight(newValue);
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "weight";
	}

}
