package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

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
import io.papermc.paper.datacomponent.item.Consumable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Consumable Component")
@Description("""
	The consumable component of an item. Any changes made to the consumable component will be present on the item.
	NOTE: Consumable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("clear the consumable component of {_item}")
@Example("reset the consumable component of {_item}")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprConsumableComponent extends SimplePropertyExpression<Object, ConsumableWrapper> implements ConsumableExperimentSyntax {

	static {
		register(ExprConsumableComponent.class, ConsumableWrapper.class, "consumable component[s]", "slots/itemtypes");
	}

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprConsumableComponent.class, ConsumableWrapper.class, "consumable component[s]", "slots/itemtypes", false)
				.supplier(ExprConsumableComponent::new)
				.build()
		);
	}

	@Override
	public @Nullable ConsumableWrapper convert(Object object) {
		ItemSource<?> itemSource = null;
		if (object instanceof ItemType itemType) {
			itemSource = new ItemSource<>(itemType);
		} else if (object instanceof Slot slot) {
			itemSource = ItemSource.fromSlot(slot);
		}
		return itemSource == null ? null : new ConsumableWrapper(itemSource);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(ConsumableWrapper.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Consumable component = null;
		if (delta != null)
			component = ((ConsumableWrapper) delta[0]).getComponent();

		for (Object object : getExpr().getArray(event)) {
			if (object instanceof ItemType itemType) {
				changeItemType(itemType, mode, component);
			} else if (object instanceof Slot slot) {
				changeSlot(slot, mode, component);
			}
		}
	}

	public void changeItemType(ItemType itemType, ChangeMode mode, Consumable component) {
		for (ItemData itemData : itemType) {
			ItemStack dataStack = itemData.getStack();
			if (dataStack == null)
				continue;
			changeItemStack(dataStack, mode, component);
		}
	}

	public void changeSlot(Slot slot, ChangeMode mode, Consumable component) {
		ItemStack itemStack = slot.getItem();
		if (itemStack == null)
			return;
		itemStack = changeItemStack(itemStack, mode, component);
		slot.setItem(itemStack);
	}

	@SuppressWarnings("UnstableApiUsage")
	public ItemStack changeItemStack(ItemStack itemStack, ChangeMode mode, Consumable component) {
		switch (mode) {
			case SET -> itemStack.setData(DataComponentTypes.CONSUMABLE, component);
			case DELETE -> itemStack.unsetData(DataComponentTypes.CONSUMABLE);
			case RESET -> itemStack.resetData(DataComponentTypes.CONSUMABLE);
		}
		return itemStack;
	}

	@Override
	public Class<ConsumableWrapper> getReturnType() {
		return ConsumableWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "consumable component";
	}

}
