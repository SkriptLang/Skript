package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

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
import io.papermc.paper.datacomponent.item.Tool;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

@Name("Tool Component")
@Description("The tool component of an item. Any changes made to the tool component will be present on the item.")
@Example("""
	set {_component} to the tool component of {_item}
	set the mining speed of {_component} to 20
	""")
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class ExprToolComponent extends SimplePropertyExpression<Object, ToolWrapper> implements ToolExperiment {

	static {
		register(ExprToolComponent.class, ToolWrapper.class, "tool component[s]", "slots/itemtypes");
	}

	@Override
	public @Nullable ToolWrapper convert(Object object) {
		ItemSource<?> itemSource = null;
		if (object instanceof ItemType itemType) {
			itemSource = new ItemSource<>(itemType);
		} else if (object instanceof Slot slot) {
			itemSource = ItemSource.fromSlot(slot);
		}
		return itemSource == null ? null : new ToolWrapper(itemSource);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(ToolWrapper.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Tool component = null;
		if (delta != null)
			component = ((ToolWrapper) delta[0]).getComponent();

		for (Object object : getExpr().getArray(event)) {
			if (object instanceof ItemType itemType) {
				changeItemType(itemType, mode, component);
			} else if (object instanceof Slot slot) {
				changeSlot(slot, mode, component);
			}
		}
	}

	public void changeItemType(ItemType itemType, ChangeMode mode, Tool component) {
		for (ItemData itemData : itemType) {
			ItemStack dataStack = itemData.getStack();
			if (dataStack == null)
				continue;
			changeItemStack(dataStack, mode, component);
		}
	}

	public void changeSlot(Slot slot, ChangeMode mode, Tool component) {
		ItemStack itemStack = slot.getItem();
		if (itemStack == null)
			return;
		itemStack = changeItemStack(itemStack, mode, component);
		slot.setItem(itemStack);
	}

	@SuppressWarnings("UnstableApiUsage")
	public ItemStack changeItemStack(ItemStack itemStack, ChangeMode mode, Tool component) {
		switch (mode) {
			case SET -> itemStack.setData(DataComponentTypes.TOOL, component);
			case DELETE -> itemStack.unsetData(DataComponentTypes.TOOL);
			case RESET -> itemStack.resetData(DataComponentTypes.TOOL);
		}
		return itemStack;
	}

	@Override
	public Class<ToolWrapper> getReturnType() {
		return ToolWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "tool component";
	}

}
