package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;

public class ExprBlockingComponent extends SimplePropertyExpression<Object, BlockingWrapper> implements BlockingExperimentalSyntax {

	static {
		register(ExprBlockingComponent.class, BlockingWrapper.class,
			"blocking component[s]", "slots/itemtypes");
	}

	@Override
	public BlockingWrapper convert(Object object) {
		ItemSource<?> itemSource = null;
		if (object instanceof ItemType itemType) {
			itemSource = new ItemSource<>(itemType);
		} else if (object instanceof Slot slot) {
			itemSource = ItemSource.fromSlot(slot);
		}
		return itemSource == null ? null : new BlockingWrapper(itemSource);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(BlockingWrapper.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		BlocksAttacks component = null;
		if (delta != null)
			component = ((BlockingWrapper) delta[0]).getComponent();

		for (Object object : getExpr().getArray(event)) {
			if (object instanceof ItemType itemType) {
				changeItemType(itemType, mode, component);
			} else if (object instanceof Slot slot) {
				changeSlot(slot, mode, component);
			}
		}
	}

	public void changeItemType(ItemType itemType, ChangeMode mode, BlocksAttacks component) {
		for (ItemData itemData : itemType) {
			ItemStack dataStack = itemData.getStack();
			if (dataStack == null)
				continue;
			changeItemStack(dataStack, mode, component);
		}
	}

	public void changeSlot(Slot slot, ChangeMode mode, BlocksAttacks component) {
		ItemStack itemStack = slot.getItem();
		if (itemStack == null)
			return;
		itemStack = changeItemStack(itemStack, mode, component);
		slot.setItem(itemStack);
	}

	@SuppressWarnings("UnstableApiUsage")
	public ItemStack changeItemStack(ItemStack itemStack, ChangeMode mode, BlocksAttacks component) {
		switch (mode) {
			case SET -> itemStack.setData(DataComponentTypes.BLOCKS_ATTACKS, component);
			case DELETE -> itemStack.unsetData(DataComponentTypes.BLOCKS_ATTACKS);
			case RESET -> itemStack.resetData(DataComponentTypes.BLOCKS_ATTACKS);
		}
		return itemStack;
	}

	@Override
	public Class<BlockingWrapper> getReturnType() {
		return BlockingWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "blocking component";
	}

}
