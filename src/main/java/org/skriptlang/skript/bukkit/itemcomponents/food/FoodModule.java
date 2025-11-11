package org.skriptlang.skript.bukkit.itemcomponents.food;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.datacomponent.item.FoodProperties;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.itemcomponents.food.elements.CondFoodCompAlwaysEat;
import org.skriptlang.skript.bukkit.itemcomponents.food.elements.EffFoodCompAlwaysEat;
import org.skriptlang.skript.bukkit.itemcomponents.food.elements.ExprFoodCompNutrition;
import org.skriptlang.skript.bukkit.itemcomponents.food.elements.ExprFoodCompSaturation;
import org.skriptlang.skript.bukkit.itemcomponents.food.elements.ExprFoodComponent;
import org.skriptlang.skript.bukkit.itemcomponents.food.elements.ExprSecBlankFoodComp;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.function.Consumer;

public class FoodModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("io.papermc.paper.datacomponent.item.FoodProperties");
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(FoodWrapper.class, "foodcomponent")
			.user("food ?components?")
			.name("Food Component")
			.description("""
				Represents a food component used for items.
				NOTE: Food component elements are experimental. Thus, they are subject to change and may not work as intended.
				""")
			.requiredPlugins("Minecraft 1.21.3+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(FoodWrapper.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(FoodWrapper wrapper, int flags) {
					return "food component";
				}

				@Override
				public String toVariableNameString(FoodWrapper wrapper) {
					return "food component#" + wrapper.hashCode();
				}
			})
			.after("itemstack", "itemtype", "slot")
		);

		Converters.registerConverter(FoodProperties.class, FoodWrapper.class, FoodWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemStack.class, FoodWrapper.class, FoodWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemType.class, FoodWrapper.class, itemType -> new FoodWrapper(new ItemSource<>(itemType)), Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Slot.class, FoodWrapper.class, slot -> {
			ItemSource<Slot> itemSource = ItemSource.fromSlot(slot);
			if (itemSource == null)
				return null;
			return new FoodWrapper(itemSource);
		}, Converter.NO_RIGHT_CHAINING);
	}

	@Override
	public void load(SkriptAddon addon) {
		register(addon.syntaxRegistry(),

			CondFoodCompAlwaysEat::register,

			EffFoodCompAlwaysEat::register,

			ExprFoodCompNutrition::register,
			ExprFoodComponent::register,
			ExprFoodCompSaturation::register,

			ExprSecBlankFoodComp::register
		);
	}

	private void register(SyntaxRegistry registry, Consumer<SyntaxRegistry>... consumers) {
		Arrays.stream(consumers).forEach(consumer -> consumer.accept(registry));
	}

}
