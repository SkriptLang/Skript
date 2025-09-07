package org.skriptlang.skript.bukkit.itemcomponents.consumable;

import ch.njol.skript.util.ItemSource;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.Consumable.Builder;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;

@SuppressWarnings("UnstableApiUsage")
public class ConsumableWrapper extends ComponentWrapper<Consumable, Builder> {

	public ConsumableWrapper(ItemStack itemStack) {
		super(itemStack);
	}

	public ConsumableWrapper(ItemSource<?> itemSource) {
		super(itemSource);
	}

	public ConsumableWrapper(Consumable component) {
		super(component);
	}

	public ConsumableWrapper(Builder builder) {
		super(builder);
	}

	@Override
	public Valued<Consumable> getDataComponentType() {
		return DataComponentTypes.CONSUMABLE;
	}

	@Override
	protected Consumable getComponent(ItemStack itemStack) {
		Consumable consumable = itemStack.getData(DataComponentTypes.CONSUMABLE);
		if (consumable != null)
			return consumable;
		return Consumable.consumable().build();
	}

	@Override
	protected Builder getBuilder(ItemStack itemStack) {
		Consumable consumable = itemStack.getData(DataComponentTypes.CONSUMABLE);
		if (consumable != null)
			return consumable.toBuilder();
		return Consumable.consumable();
	}

	@Override
	protected void setComponent(ItemStack itemStack, Consumable component) {
		itemStack.setData(DataComponentTypes.CONSUMABLE, component);
	}

	@Override
	protected Builder toBuilder(Consumable component) {
		return component.toBuilder();
	}

	@Override
	public ConsumableWrapper clone() {
		ConsumableWrapper clone = newWrapper();
		Consumable base = getComponent();
		return clone;
	}

	@Override
	public Consumable newComponent() {
		return newBuilder().build();
	}

	@Override
	public Builder newBuilder() {
		return Consumable.consumable();
	}

	@Override
	public ConsumableWrapper newWrapper() {
		return newInstance();
	}

	public static ConsumableWrapper newInstance() {
		return new ConsumableWrapper(Consumable.consumable().build());
	}

}
