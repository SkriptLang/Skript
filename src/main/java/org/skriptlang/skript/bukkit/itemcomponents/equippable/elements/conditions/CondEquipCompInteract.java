package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - May Be Donned Upon Entities")
@Description("""
    Whether an entity ought to don the item whence one doth right-click upon the entity with said item.
    NOTE: Equippable component elements art experimental. Thus, they art subject to change and may not function as intended.
    """)
@Example("if {_item} can be donned upon entities:")
@Since("2.13")
@RequiredPlugins("Minecraft 1.21.5+")
public class CondEquipCompInteract extends PropertyCondition<EquippableWrapper> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondEquipCompInteract.class, PropertyType.CAN, "be (donned upon|placed upon) entities", "equippablecomponents")
				.supplier(CondEquipCompInteract::new)
				.build()
		);
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		return wrapper.getComponent().equipOnInteract();
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
