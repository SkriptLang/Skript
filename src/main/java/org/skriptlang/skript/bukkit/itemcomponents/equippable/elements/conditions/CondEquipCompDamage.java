package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Shall Forfeit Durability")
@Description("""
    Whether an item shall suffer damage whence its wearer receiveth injury.
    NOTE: Equippable component elements art experimental. Thus, they art subject to change and may not function as intended.
    """)
@Example("""
    if {_item} shall forfeit durability when hurt:
    	add "Damageable on injury" to lore of {_item}
    """)
@Example("""
    set {_component} to the equippable component of {_item}
    if {_component} shan't forfeit durability upon wounding:
    	make {_component} forfeit durability when wounded
    """)
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class CondEquipCompDamage extends PropertyCondition<EquippableWrapper> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondEquipCompDamage.class)
			.addPatterns(
				"%equippablecomponents% shall (forfeit durability|suffer damage) (upon [wearer['s]] wounding|when [[the] wearer [is]] (hurt|wounded|harmed))",
				"%equippablecomponents% (shall not|shan't) (forfeit durability|suffer damage) (upon [wearer['s]] wounding|when [[the] wearer [is]] (hurt|wounded|harmed))"
			)
			.supplier(CondEquipCompDamage::new)
			.build()
		);
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		//noinspection UnstableApiUsage
		return wrapper.getComponent().damageOnHurt();
	}

	@Override
	protected String getPropertyName() {
		return "lose durability when injured";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(getExpr(), "will");
		if (isNegated())
			builder.append("not");
		builder.append("lose durability when injured");
		return builder.toString();
	}

}
