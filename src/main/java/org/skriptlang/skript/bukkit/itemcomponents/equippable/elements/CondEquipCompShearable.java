package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Can Be Sheared Off")
@Description("""
	Whether an item can be sheared off of an entity.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	if {_item} can be sheared off:
		add "Shearable" to lore of {_item}
	""")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} can not be sheared off:
		allow {_component} to be sheared off
	""")
@RequiredPlugins("Minecraft 1.21.6+")
@Since("INSERT VERSION")
public class CondEquipCompShearable extends PropertyCondition<EquippableWrapper> implements EquippableExperimentSyntax {

	static {
		if (EquippableWrapper.HAS_CAN_BE_SHEARED)
			register(CondEquipCompShearable.class, PropertyType.CAN, "be sheared off [of entities]", "equippablecomponents");
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		//noinspection UnstableApiUsage
		return wrapper.getComponent().canBeSheared();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}

	@Override
	protected String getPropertyName() {
		return "be sheared off of entities";
	}

}
