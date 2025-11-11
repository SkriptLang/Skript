package org.skriptlang.skript.bukkit.itemcomponents.food.elements;

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Food Component")
@Description("""
	The food component of an item. Any changes made to the food component will also change the item.
	NOTE: Food component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_component} to the food component of {_item}
	set the nutritional value of {_component} to 50
	""")
@Example("clear the food component of {_item}")
@Example("reset the food component of {_item}")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class ExprFoodComponent extends SimplePropertyExpression<Object, FoodWrapper> implements FoodExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprFoodComponent.class,
				FoodWrapper.class,
				"food component[s]",
				"slots/itemtypes",
				false
			)
				.supplier(ExprFoodComponent::new)
				.build()
		);
	}

	@Override
	public @Nullable FoodWrapper convert(Object object) {
		ItemSource<?> itemSource = null;
		if (object instanceof ItemType itemType) {
			itemSource = new ItemSource<>(itemType);
		} else if (object instanceof Slot slot) {
			itemSource = ItemSource.fromSlot(slot);
		}
		return itemSource == null ? null : new FoodWrapper(itemSource);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(FoodWrapper.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		FoodProperties component = null;
		if (delta != null)
			component = ((FoodWrapper) delta[0]).getComponent();

		for (Object object : getExpr().getArray(event)) {
			if (object instanceof ItemType itemType) {
				changeItemType(itemType, mode, component);
			} else if (object instanceof Slot slot) {
				changeSlot(slot, mode, component);
			}
		}
	}

	private void changeItemType(ItemType itemType, ChangeMode mode, FoodProperties component) {
		for (ItemData itemData : itemType) {
			ItemStack dataStack = itemData.getStack();
			if (dataStack == null)
				continue;
			changeItemStack(dataStack, mode, component);
		}
	}

	private void changeSlot(Slot slot, ChangeMode mode, FoodProperties component) {
		ItemStack itemStack = slot.getItem();
		if (itemStack == null)
			return;
		itemStack = changeItemStack(itemStack, mode, component);
		slot.setItem(itemStack);
	}

	@SuppressWarnings("UnstableApiUsage")
	private ItemStack changeItemStack(ItemStack itemStack, ChangeMode mode, FoodProperties component) {
		switch (mode) {
			case SET -> itemStack.setData(DataComponentTypes.FOOD, component);
			case DELETE -> itemStack.unsetData(DataComponentTypes.FOOD);
			case RESET -> itemStack.resetData(DataComponentTypes.FOOD);
		}
		return itemStack;
	}

	@Override
	public Class<? extends FoodWrapper> getReturnType() {
		return FoodWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "food component";
	}

}
