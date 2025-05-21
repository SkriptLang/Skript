package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Can Equip On Entities")
@Description("Whether an entity should equip the item when right clicking on the entity with the item. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("if {_item} can be equipped on entities:")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21.5+")
public class CondEquipCompInteract extends PropertyCondition<EquippableWrapper> implements EquippableExperiment {

	static {
		if (Skript.methodExists(EquippableComponent.class, "isEquipOnInteract"))
			Skript.registerCondition(CondEquipCompInteract.class, ConditionType.PROPERTY,
				"%equippablecomponents% can be equipped on[to] entities",
				"%equippablecomponents% (can not|can't) be equipped on[to] entities"
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
		return wrapper.getComponent().isEquipOnInteract();
	}

	@Override
	protected String getPropertyName() {
		return "equipped onto entities";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(wrappers, "can");
		if (isNegated())
			builder.append("not");
		builder.append("be equipped onto entities");
		return builder.toString();
	}

}
