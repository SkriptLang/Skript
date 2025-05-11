package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableExperiment;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableWrapper;

@Name("Equippable Component - Swappable")
@Description("If the item can be swapped by right clicking it in your hand. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Examples({
	"set {_item} to be swappable",
	"",
	"set {_component} to the equippable component of {_item}",
	"make {_component} unswappable",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class EffEquipCompSwappable extends Effect implements EquippableExperiment {

	static {
		Skript.registerEffect(EffEquipCompSwappable.class,
			"(set|make) %equippablecomponents% [to [be]] swappable",
			"(set|make) %equippablecomponents% [to [be]] unswappable"
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
		for (EquippableWrapper wrapper : wrappers.getArray(event)) {
			EquippableComponent component = wrapper.getComponent();
			component.setSwappable(swappable);
			wrapper.applyComponent();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "set the " + wrappers.toString(event, debug) + " to be " + (swappable ? "swappable" : "unswappable");
	}
}
