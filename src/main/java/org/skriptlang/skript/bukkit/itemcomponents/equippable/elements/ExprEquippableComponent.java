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
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component")
@Description("The equippable component of an item. Any changes made to the equippable component will be present on the item. "
	+ "NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.")
@Example("""
	set {_component} to the equippable component of {_item}
	set the equipment slot of {_component} to helmet slot
	""")
@Example("set the equipment slot of {_item} to helmet slot")
@Example("clear the equippable component of {_item}")
@Example("reset the equippable component of {_item}")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquippableComponent extends SimplePropertyExpression<ItemType, EquippableWrapper> implements EquippableExperimentSyntax {

	static {
		register(ExprEquippableComponent.class, EquippableWrapper.class,
			"equippable component[s]", "itemtypes");
	}

	@Override
	public EquippableWrapper convert(ItemType itemType) {
		return new EquippableWrapper(new ItemSource<>(itemType));
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(EquippableWrapper.class);
			default -> null;
		};
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Equippable component = null;
		if (delta != null)
			component = ((EquippableWrapper) delta[0]).getComponent();

		for (ItemType itemType : getExpr().getArray(event)) {
			for (ItemData itemData : itemType) {
				ItemStack dataStack = itemData.getStack();
				if (dataStack == null)
					continue;
				switch (mode) {
					case SET -> {
						assert component != null;
						dataStack.setData(DataComponentTypes.EQUIPPABLE, component);
					}
					case DELETE -> dataStack.unsetData(DataComponentTypes.EQUIPPABLE);
					case RESET -> dataStack.resetData(DataComponentTypes.EQUIPPABLE);
				}
			}
		}
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
