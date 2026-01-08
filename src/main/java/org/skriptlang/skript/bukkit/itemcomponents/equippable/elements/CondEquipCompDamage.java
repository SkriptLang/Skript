package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Will Lose Durability")
@Description("""
	Whether an item will be damaged when the wearer gets injured.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	if {_item} will lose durability when hurt:
		add "Damageable on injury" to lore of {_item}
	""")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} won't lose durability on injury:
		make {_component} lose durability when injured
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class CondEquipCompDamage extends PropertyCondition<EquippableWrapper> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondEquipCompDamage.class)
			.addPatterns(
				"%equippablecomponents% will (lose durability|be damaged) (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured|damaged))",
				"%equippablecomponents% (will not|won't) (lose durability|be damaged) (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured|damaged))"
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
