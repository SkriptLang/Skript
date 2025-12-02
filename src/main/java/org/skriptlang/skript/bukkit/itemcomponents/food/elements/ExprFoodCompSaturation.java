package org.skriptlang.skript.bukkit.itemcomponents.food.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Food Component - Saturation Value")
@Description("""
	The amount of saturation to be restored when the item is eaten.
	NOTE: Food component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set the saturation value of {_item} to 20")
@Example("""
	set {_component} to the food component of {_item}
	add 30 to the saturation value of {_component}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class ExprFoodCompSaturation extends SimplePropertyExpression<FoodWrapper, Float> implements FoodExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprFoodCompSaturation.class,
				Float.class,
				"saturation value",
				"foodcomponents",
				true
			)
				.supplier(ExprFoodCompSaturation::new)
				.build()
		);
	}

	@Override
	public @Nullable Float convert(FoodWrapper wrapper) {
		return wrapper.getComponent().saturation();
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
		float provided = delta == null ? 0 : ((Number) delta[0]).floatValue();

		getExpr().stream(event).forEach(wrapper -> {
			float current = wrapper.getComponent().saturation();
			float newSaturation = switch (mode) {
				case ADD -> Math2.fit(0, current + provided, Float.MAX_VALUE);
				case REMOVE -> Math2.fit(0, current - provided, Float.MAX_VALUE);
				default -> provided;
			};
			wrapper.editBuilder(builder -> builder.saturation(newSaturation));
		});
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "saturation value";
	}

}
