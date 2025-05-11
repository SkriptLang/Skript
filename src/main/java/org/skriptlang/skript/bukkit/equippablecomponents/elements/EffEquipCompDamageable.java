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

@Name("Equippable Component - Damageable")
@Description("If the item should take damage when the wearer gets injured. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Examples({
	"set {_item} to be damageable",
	"",
	"set {_component} to the equippable component of {_item}",
	"make {_component} undamageable",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class EffEquipCompDamageable extends Effect implements EquippableExperiment {

	static {
		Skript.registerEffect(EffEquipCompDamageable.class,
			"(set|make) %equippablecomponents% [to [be]] damageable",
			"(set|make) %equippablecomponents% [to [be]] undamageable"
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean damageable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		damageable = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (EquippableWrapper wrapper : wrappers.getArray(event)) {
			EquippableComponent component = wrapper.getComponent();
			component.setDamageOnHurt(damageable);
			wrapper.applyComponent();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "set the " + wrappers.toString(event, debug) + " to be " + (damageable ? "damageable" : "undamageable");
	}

}
