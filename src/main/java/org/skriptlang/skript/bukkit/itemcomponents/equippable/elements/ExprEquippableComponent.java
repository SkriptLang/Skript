package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

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
import io.papermc.paper.datacomponent.item.Equippable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component")
@Description("""
	The equippable component of an item. Any changes made to the equippable component will be present on the item.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_component} to the equippable component of {_item}
	set the equipment slot of {_component} to helmet slot
	""")
@Example("clear the equippable component of {_item}")
@Example("reset the equippable component of {_item}")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class ExprEquippableComponent extends SimplePropertyExpression<Object, EquippableWrapper> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEquippableComponent.class, EquippableWrapper.class, "equippable component[s]", "slots/itemtypes", false)
				.supplier(ExprEquippableComponent::new)
				.build()
		);
	}

	@Override
	public EquippableWrapper convert(Object object) {
		ItemSource<?> itemSource = null;
		if (object instanceof ItemType itemType) {
			itemSource = new ItemSource<>(itemType);
		} else if (object instanceof Slot slot) {
			itemSource = ItemSource.fromSlot(slot);
		}
		return itemSource == null ? null : new EquippableWrapper(itemSource);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(EquippableWrapper.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Equippable component = null;
		if (delta != null)
			component = ((EquippableWrapper) delta[0]).getComponent();

		for (Object object : getExpr().getArray(event)) {
			if (object instanceof ItemType itemType) {
				changeItemType(itemType, mode, component);
			} else if (object instanceof Slot slot) {
				changeSlot(slot, mode, component);
			}
		}
	}

	public void changeItemType(ItemType itemType, ChangeMode mode, Equippable component) {
		for (ItemData itemData : itemType) {
			ItemStack dataStack = itemData.getStack();
			if (dataStack == null)
				continue;
			changeItemStack(dataStack, mode, component);
		}
	}

	public void changeSlot(Slot slot, ChangeMode mode, Equippable component) {
		ItemStack itemStack = slot.getItem();
		if (itemStack == null)
			return;
		itemStack = changeItemStack(itemStack, mode, component);
		slot.setItem(itemStack);
	}

	@SuppressWarnings("UnstableApiUsage")
	public ItemStack changeItemStack(ItemStack itemStack, ChangeMode mode, Equippable component) {
		switch (mode) {
			case SET -> itemStack.setData(DataComponentTypes.EQUIPPABLE, component);
			case DELETE -> itemStack.unsetData(DataComponentTypes.EQUIPPABLE);
			case RESET -> itemStack.resetData(DataComponentTypes.EQUIPPABLE);
		}
		return itemStack;
	}

	@Override
	public Class<EquippableWrapper> getReturnType() {
		return EquippableWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "equippable component";
	}

}
