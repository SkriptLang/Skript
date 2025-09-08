package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
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
		register(CondConsCompParticles.class, PropertyType.HAVE, "consum(e|ption) particles [enabled|:disabled]", "consumablecomponents");
	}

	private boolean checkEnabled;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkEnabled = !parseResult.hasTag("disabled");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(ConsumableWrapper wrapper) {
		return wrapper.getComponent().hasConsumeParticles() == checkEnabled;
	}

	@Override
	protected String getPropertyName() {
		if (checkEnabled)
			return "consume particles enabled";
		return "consume particles disabled";
	}

}
