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

@Name("Equippable Component - Is Swappable")
@Description("Whether an item can be swapped by right clicking in it your hand. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("""
	if {_item} is swappable:
		add "Swappable" to lore of {_item}
	""")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} is not swappable:
		make {_component} swappable
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class CondEquipCompSwappable extends PropertyCondition<EquippableWrapper> implements EquippableExperiment {

	static {
		Skript.registerCondition(CondEquipCompSwappable.class, ConditionType.PROPERTY,
			"%equippablecomponents% (is|are) [:un]swappable",
			"%equippablecomponents% (isn't|is not|aren't|are not) [:un]swappable"
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean swappable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		swappable = !parseResult.hasTag("un");
		setNegated(matchedPattern == 1);
		setExpr(wrappers);
		return true;
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		return wrapper.getComponent().isSwappable() == swappable;
	}

	@Override
	protected String getPropertyName() {
		return "swappable";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + wrappers.toString(event, debug) + (isNegated() ? " are not " : " are ")
			+ (swappable ? "swappable" : "unswappable");
	}

}
