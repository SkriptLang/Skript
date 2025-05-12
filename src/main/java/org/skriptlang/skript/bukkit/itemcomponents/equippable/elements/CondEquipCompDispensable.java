package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Is Dispensable")
@Description("Whether an item can be dispensed by a dispenser. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("""
	if {_item} is dispensable:
		add "Dispensable" to lore of {_item}
	""")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} is not dispensable:
		make {_component} dispensable
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class CondEquipCompDispensable extends PropertyCondition<EquippableWrapper> implements EquippableExperiment {

	static {
		Skript.registerCondition(CondEquipCompDispensable.class, ConditionType.PROPERTY,
			"%equippablecomponents% (is|are) [:un]dispensable",
			"%equippablecomponents% (isn't|is not|aren't|are not) [:un]dispensable"
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean dispensable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		dispensable = !parseResult.hasTag("un");
		setNegated(matchedPattern == 1);
		setExpr(wrappers);
		return true;
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		return wrapper.getComponent().isDispensable() == dispensable;
	}

	@Override
	protected String getPropertyName() {
		return "dispensable";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return wrappers.toString(event, debug) + (isNegated() ? " are not " : " are ")
			+ (dispensable ? "dispensable" : "undispensable");
	}

}
