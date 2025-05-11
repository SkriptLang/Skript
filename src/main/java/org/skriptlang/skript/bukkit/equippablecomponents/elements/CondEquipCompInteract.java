package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableExperiment;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableWrapper;

@Name("Equippable Component - Can Equip On Interact")
@Description("Whether an item can be equipped when interacted with. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("if {_item} can equip on interaction:")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21.5+")
public class CondEquipCompInteract extends PropertyCondition<EquippableWrapper> implements EquippableExperiment {

	static {
		if (Skript.methodExists(EquippableComponent.class, "isEquipOnInteract"))
			Skript.registerCondition(CondEquipCompInteract.class, ConditionType.PROPERTY,
				"%equippablecomponents% can equip on interact[ion]",
				"%equippablecomponents% (can not|can't) equip on interact[ion]"
			);
	}

	private Expression<EquippableWrapper> wrappers;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		setNegated(matchedPattern == 1);
		setExpr(wrappers);
		return true;
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		EquippableComponent component = wrapper.getComponent();
		return component.isEquipOnInteract();
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return wrappers.toString(event, debug) + (isNegated() ? " can not " : " can ")
			+ "equip on interaction";
	}

}
