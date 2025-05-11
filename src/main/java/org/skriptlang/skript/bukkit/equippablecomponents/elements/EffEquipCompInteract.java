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

@Name("Equippable Component - Equip On Interaction")
@Description("If the item should be equipped when interacted with. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("make {_item} equip on interaction")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21.5+")
public class EffEquipCompInteract extends Effect implements EquippableExperiment {

	static {
		if (Skript.methodExists(EquippableComponent.class, "setEquipOnInteract", boolean.class))
			Skript.registerEffect(EffEquipCompInteract.class, "(set|make) %equippablecomponents% [:not] equip on interact[ion]");
	}

	private boolean equip;
	private Expression<EquippableWrapper> wrappers;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		equip = !parseResult.hasTag("not");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (EquippableWrapper wrapper : wrappers.getArray(event)) {
			EquippableComponent component = wrapper.getComponent();
			component.setEquipOnInteract(equip);
			wrapper.applyComponent();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + wrappers.toString(event, debug) + (equip ? "" : " not ")
			+ "equip on interaction";
	}

}
