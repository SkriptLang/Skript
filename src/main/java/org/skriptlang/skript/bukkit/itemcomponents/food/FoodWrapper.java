package org.skriptlang.skript.bukkit.itemcomponents.food;

import ch.njol.skript.util.ItemSource;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.FoodProperties.Builder;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;

/**
 * A {@link ComponentWrapper} for getting and setting data on a {@link FoodProperties} component.
 */
@SuppressWarnings("UnstableApiUsage")
public class FoodWrapper extends ComponentWrapper<FoodProperties, Builder> {

	public FoodWrapper(ItemStack itemStack) {
		super(itemStack);
	}

	public FoodWrapper(ItemSource<?> itemSource) {
		super(itemSource);
	}

	public FoodWrapper(FoodProperties component) {
		super(component);
	}

	public FoodWrapper(Builder builder) {
		super(builder);
	}

	@Override
	public Valued<FoodProperties> getDataComponentType() {
		return DataComponentTypes.FOOD;
	}

	@Override
	protected FoodProperties getComponent(ItemStack itemStack) {
		FoodProperties food = itemStack.getData(DataComponentTypes.FOOD);
		if (food != null)
			return food;
		return FoodProperties.food().build();
	}

	@Override
	protected Builder getBuilder(ItemStack itemStack) {
		FoodProperties food = itemStack.getData(DataComponentTypes.FOOD);
		if (food != null)
			return food.toBuilder();
		return FoodProperties.food();
	}

	@Override
	protected void setComponent(ItemStack itemStack, FoodProperties component) {
		itemStack.setData(DataComponentTypes.FOOD, component);
	}

	@Override
	protected Builder getBuilder(FoodProperties component) {
		return component.toBuilder();
	}

	@Override
	public FoodWrapper clone() {
		FoodProperties base = getComponent();
		FoodWrapper clone = newWrapper();
		clone.editBuilder(builder -> {
			builder.canAlwaysEat(base.canAlwaysEat());
			builder.nutrition(base.nutrition());
			builder.saturation(base.saturation());
		});
		return clone;
	}

	@Override
	public FoodProperties newComponent() {
		return newBuilder().build();
	}

	@Override
	public Builder newBuilder() {
		return FoodProperties.food();
	}

	@Override
	public FoodWrapper newWrapper() {
		return newInstance();
	}

	public static FoodWrapper newInstance() {
		return new FoodWrapper(FoodProperties.food().build());
	}

}
