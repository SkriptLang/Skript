package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component")
@Description("The equippable component of an item. Any changes made to the equippable component will be present on the item. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("""
	set {_component} to the equippable component of {_item}
	set the equipment slot of {_component} to helmet slot
	""")
@Example("set the equipment slot of {_item} to helmet slot")
@Example("clear the equippable component of {_item}")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquippableComponent extends PropertyExpression<ItemStack, EquippableWrapper> implements EquippableExperiment {

	static {
		register(ExprEquippableComponent.class,  EquippableWrapper.class,
			"equippable component[s]", "itemstacks");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends ItemStack>) exprs[0]);
		return true;
	}

	@Override
	protected EquippableWrapper @Nullable [] get(Event event, ItemStack[] source) {
		return get(source, EquippableWrapper::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(EquippableWrapper.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		EquippableComponent equippableComponent = null;
		if (delta != null && delta[0] != null)
			equippableComponent = ((EquippableWrapper) delta[0]).getComponent();

		for (ItemStack itemStack : getExpr().getArray(event)) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setEquippable(equippableComponent);
			itemStack.setItemMeta(itemMeta);
		}
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<EquippableWrapper> getReturnType() {
		return EquippableWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the equippable components of " + getExpr().toString(event, debug);
	}

}
