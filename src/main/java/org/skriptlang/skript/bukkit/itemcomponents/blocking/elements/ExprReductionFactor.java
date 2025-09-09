package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.DamageReductionWrapper;

public class ExprReductionFactor extends SimplePropertyExpression<DamageReductionWrapper, Float> implements BlockingExperimentalSyntax {

	static {
		registerDefault(ExprReductionFactor.class, Float.class, "[damage] reduction factor amount", "damagereductions");
	}

	@Override
	public @Nullable Float convert(DamageReductionWrapper wrapper) {
		return wrapper.getDamageReduction().factor();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD, RESET -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		float provided = delta == null ? 0f : ((Number) delta[0]).floatValue();

		getExpr().stream(event).forEach(wrapper -> {
			float current = wrapper.getDamageReduction().factor();
			switch (mode) {
				case SET, RESET -> current = provided;
				case ADD -> current += provided;
				case REMOVE -> current -= provided;
			}
			float newFactor = current;
			wrapper.modify(builder -> builder.factor(newFactor));
		});
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "damage reduction factor amount";
	}

}
