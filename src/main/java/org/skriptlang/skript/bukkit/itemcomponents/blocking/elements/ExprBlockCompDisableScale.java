package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;

public class ExprBlockCompDisableScale extends SimplePropertyExpression<BlockingWrapper, Float> implements BlockingExperimentalSyntax {

	static {
		registerDefault(ExprBlockCompDisableScale.class, Float.class, "blocking disable scale", "blockingcomponents");
	}

	@Override
	public @Nullable Float convert(BlockingWrapper wrapper) {
		return wrapper.getComponent().disableCooldownScale();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, REMOVE, ADD -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		float provided = delta == null ? 0f : ((Number) delta[0]).floatValue();

		getExpr().stream(event).forEach(wrapper -> {
			float current = wrapper.getComponent().disableCooldownScale();
			switch (mode) {
				case SET, DELETE -> current = provided;
				case ADD -> current += provided;
				case REMOVE -> current -= provided;
			}
			float newScale = Math2.fit(0, current, Float.MAX_VALUE);
			wrapper.editBuilder(builder -> builder.disableCooldownScale(newScale));
		});
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "blocking disable scale";
	}

}
