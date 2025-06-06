package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Dispense")
@Description("If the item can be dispensed by a dispenser. "
	+ "NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work aas intended.")
@Example("allow {_item} to be dispensed")
@Example("""
	set {_component} to the equippable component of {_item}
	prevent {_component} from being dispensed
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class EffEquipCompDispensable extends Effect implements EquippableExperiment {

	static {
		Skript.registerEffect(EffEquipCompDispensable.class,
			"allow %equippablecomponents% to be dispensed",
			"(block|prevent) %equippablecomponents% from being dispensed"
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean dispensable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		dispensable = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editComponent(component -> component.setDispensable(dispensable)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (dispensable)
			return "allow " + wrappers.toString(event, debug) + " to be dispensed";
		return "prevent " + wrappers.toString(event, debug) + " from being dispensed";
	}

}
