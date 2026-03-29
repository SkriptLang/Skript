package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - May Exchange Armament")
@Description("""
    Whether an item may exchange armament by right clicking with it in thine hand.
    The item shall swap places of the appointed 'equipment slot' of the item. If no equipment slot be set, it defaults to the helmet.
    NOTE: Equippable component elements art experimental. Thus, they art subject to change and may not function as intended.
    """)
@Example("""
    if {_item} can exchange armament:
    	add "Swappable" to lore of {_item}
    """)
@Example("""
    set {_component} to the equippable component of {_item}
    if {_component} can not exchange armament when right clicked:
    	make {_component} exchange armament
    """)
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class CondEquipCompSwapEquipment extends PropertyCondition<EquippableWrapper> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondEquipCompSwapEquipment.class,
				PropertyType.CAN,
				"exchange armament [upon right click|when right clicked]",
				"equippablecomponents"
			).supplier(CondEquipCompSwapEquipment::new)
				.build()
		);
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		return wrapper.getComponent().swappable();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}

	@Override
	protected String getPropertyName() {
		return "swap equipment";
	}

}
