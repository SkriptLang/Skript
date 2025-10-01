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

@Name("Food Component - Nutritional Value")
@Description("""
	The amount of food points to be restored when the item is eaten.
	NOTE: Food component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set the nutritional value of {_item} to 20")
@Example("""
	set {_component} to the food component of {_item}
	add 30 to the nutritional value of {_component}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class ExprFoodCompNutrition extends SimplePropertyExpression<FoodWrapper, Integer> implements FoodExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprFoodCompNutrition.class,
				Integer.class,
				"[food] nutritional value",
				"foodcomponents",
				true
			)
				.supplier(ExprFoodCompNutrition::new)
				.build()
		);
	}

	@Override
	public @Nullable Integer convert(FoodWrapper wrapper) {
		return wrapper.getComponent().nutrition();
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
		int provided = delta == null ? 0 : ((Number) delta[0]).intValue();

		getExpr().stream(event).forEach(wrapper -> {
			int current = wrapper.getComponent().nutrition();
			int newNutrition = switch (mode) {
				case ADD -> Math2.fit(0, current + provided, Integer.MAX_VALUE);
				case REMOVE -> Math2.fit(0, current - provided, Integer.MAX_VALUE);
				default -> provided;
			};
			wrapper.editBuilder(builder -> builder.nutrition(newNutrition));
		});
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "nutritional value";
	}

}
