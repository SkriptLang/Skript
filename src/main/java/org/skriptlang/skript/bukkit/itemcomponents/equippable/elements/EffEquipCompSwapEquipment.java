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

@Name("Equippable Component - Swap Equipment")
@Description("If the item can be swapped by right clicking with it in your hand. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("allow {_item} to swap equipment")
@Example("""
	set {_component} to the equippable component of {_item}
	prevent {_component} from swapping equipment on right click
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class EffEquipCompSwapEquipment extends Effect implements EquippableExperiment {

	static {
		Skript.registerEffect(EffEquipCompSwapEquipment.class,
			"allow %equippablecomponents% to swap equipment [on right click|when right clicked]",
			"prevent %equippablecomponents% from swapping equipment [on right click|when right clicked]"
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean swappable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		swappable = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editComponent(component -> component.setSwappable(swappable)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (swappable)
			return "allow " + wrappers.toString(event, debug) + " to swap equipment";
		return "prevent " + wrappers.toString(event, debug) + " from swapping equipment";
	}
}
