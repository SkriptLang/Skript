package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

@Name("Equippable Component - Is Dispensable")
@Description("Checks if the item can be dispensed by a dispenser.")
@Examples({
	"if {_item} is dispensable:",
		"\tadd \"Dispensable\" to lore of {_item}",
	"",
	"set {_component} to the equippable component of {_item}",
	"if {_component} is not dispensable:",
		"\tmake {_component} dispensable"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class CondEquipCompDispensable extends PropertyCondition<Object> {

	static {
		Skript.registerCondition(CondEquipCompDispensable.class, ConditionType.PROPERTY,
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (is|are) [:un]dispensable",
			"[the] %equippablecomponents% (is|are) [:un]dispensable",
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (isn't|is not|aren't|are not) [:un]dispensable",
			"[the] %equippablecomponents% (isn't|is not|aren't|are not) [:un]dispensable");
	}

	private Expression<?> objects;
	private boolean dispensable;
	private boolean isComponents;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		dispensable = !parseResult.hasTag("un");
		isComponents = matchedPattern == 1 || matchedPattern == 3;
		setNegated(matchedPattern >= 2);
		return true;
	}

	@Override
	public boolean check(Object object) {
		if (object instanceof EquippableComponent component) {
			return component.isDispensable() == dispensable;
		} else {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack != null)
				return itemStack.getItemMeta().getEquippable().isDispensable() == dispensable;
		}
		return isNegated();
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + (isComponents ? "" : "equippable components of ") + objects.toString(event, debug) + " " +
			(isNegated() ? "are not" : "are") + " " + (dispensable ? "dispensable" : "undispensable");
	}

}
