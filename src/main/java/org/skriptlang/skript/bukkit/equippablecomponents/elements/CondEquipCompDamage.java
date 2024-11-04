package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

@Name("Equippable Component - Is Damageable")
@Description("Checks if the item can be damaged when the wearer gets injured.")
@Examples({
	"if {_item} is damageable:",
		"\tadd \"Damageable\" to lore of {_item}",
	"",
	"set {_component} to the equippable component of {_item}",
	"if {_component} is not damageable:",
		"\tmake {_component} damageable"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class CondEquipCompDamage extends PropertyCondition<Object> {

	static {
		Skript.registerCondition(CondEquipCompDamage.class, ConditionType.PROPERTY,
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (is|are) [:un]damageable",
			"[the] %equippablecomponents% (is|are) [:un]damageable",
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (isn't|is not|aren't|are not) [:un]damageable",
			"[the] %equippablecomponents% (isn't|is not|aren't|are not) [:un]damageable");
	}

	private Expression<?> objects;
	private boolean damageable;
	private boolean isComponents;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		objects = exprs[0];
		damageable = !parseResult.hasTag("un");
		isComponents = matchedPattern == 1 || matchedPattern == 3;
		setNegated(matchedPattern >= 2);
		return true;
	}

	@Override
	public boolean check(Object object) {
		if (object instanceof EquippableComponent component) {
			return component.isDamageOnHurt() == damageable;
		} else {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack != null)
				return itemStack.getItemMeta().getEquippable().isDamageOnHurt() == damageable;
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
			(isNegated() ? "are not" : "are") + " " + (damageable ? "damageable" : "undamageable");
	}

}
