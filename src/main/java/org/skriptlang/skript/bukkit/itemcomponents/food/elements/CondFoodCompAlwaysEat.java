package org.skriptlang.skript.bukkit.itemcomponents.food.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Food Component - Can Always Be Eaten")
@Description("""
	Whether an item can be eaten when the player's hunger bar is full.
	NOTE: Food component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	if {_item} can not always be eaten:
		allow {_item} to always be eaten
	""")
@Example("""
	set {_component} to the food component of {_item}
	if {_component} can be eaten when full:
		prevent {_component} from being eaten when full
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class CondFoodCompAlwaysEat extends PropertyCondition<FoodWrapper> implements FoodExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondFoodCompAlwaysEat.class,
				PropertyType.CAN,
				"(always be eaten|be eaten when full)",
				"foodcomponents"
			)
				.supplier(CondFoodCompAlwaysEat::new)
				.build()
		);
	}

	@Override
	public boolean check(FoodWrapper wrapper) {
		return wrapper.getComponent().canAlwaysEat();
	}

	@Override
	protected String getPropertyName() {
		return "always be eaten";
	}

}
