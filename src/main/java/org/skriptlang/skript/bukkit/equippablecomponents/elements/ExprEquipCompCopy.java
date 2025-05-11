package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableExperiment;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableWrapper;

@Name("Equippable Component - Copy")
@Description("Grab a copy of an equippable component of an item. Any changes made to the copy will not be present on the item. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("set {_component} to the copied equippable component of {_item}")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21.2+")
public class ExprEquipCompCopy extends PropertyExpression<ItemStack, EquippableWrapper> implements EquippableExperiment {

	static {
		register(ExprEquipCompCopy.class, EquippableWrapper.class,
			"((copy|copies) of [the]|copied) equippable component[s]", "itemstacks");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<ItemStack>) exprs[0]);
		return true;
	}

	@Override
	protected EquippableWrapper[] get(Event event, ItemStack[] source) {
		return get(source, itemStack -> new EquippableWrapper(itemStack.getItemMeta().getEquippable()));
	}

	@Override
	public Class<EquippableWrapper> getReturnType() {
		return EquippableWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the copied equippable components of " + getExpr().toString(event, debug);
	}

}
