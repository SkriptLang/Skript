package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableWrapper;

@Name("Consumable Component - Has Particles")
@Description("""
	Whether an item has particles enabled when being consumed.
	NOTE: Consumable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	if {_item} has consumption particles enabled:
		disable the consume particles of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class CondConsCompParticles extends PropertyCondition<ConsumableWrapper> implements ConsumableExperimentSyntax {

	static {
		register(CondConsCompParticles.class, PropertyType.HAVE, "consum(e|ption) particles [enabled]", "consumablecomponents");
	}

	@Override
	public boolean check(ConsumableWrapper wrapper) {
		return wrapper.getComponent().hasConsumeParticles();
	}

	@Override
	protected String getPropertyName() {
		return "consume particles";
	}

}
