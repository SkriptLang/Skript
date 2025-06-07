package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Can Equip On Entities")
@Description("Whether an entity should equip the item when right clicking on the entity with the item. "
	+ "NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work aas intended.")
@Example("if {_item} can be equipped on entities:")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21.5+")
public class CondEquipCompInteract extends PropertyCondition<EquippableWrapper> implements EquippableExperiment {

	static {
		if (Skript.methodExists(EquippableComponent.class, "isEquipOnInteract"))
			register(CondEquipCompInteract.class, PropertyType.CAN, "be (equipped|put) on[to] entities", "equippablecomponents");
	}

	private Expression<EquippableWrapper> wrappers;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		return wrapper.getComponent().isEquipOnInteract();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}

	@Override
	protected String getPropertyName() {
		return "be equipped onto entities";
	}

}
