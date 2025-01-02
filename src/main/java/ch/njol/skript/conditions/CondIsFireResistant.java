package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.inventory.meta.ItemMeta;

@Name("Is Fire Resistant")
@Description("Checks whether an item is fire resistant.")
@Examples({
	"if player's tool is fire resistant:",
	"if {_items::*} aren't resistant to fire:"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public class CondIsFireResistant extends PropertyCondition<ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "isFireResistant"))
			PropertyCondition.register(CondIsFireResistant.class, "(fire resistant|resistant to fire)", "itemtypes");
	}

	@Override
	public boolean check(ItemType item) {
		return item.getItemMeta().isFireResistant();
	}

	@Override
	public String getPropertyName() {
		return "fire resistant";
	}

}
