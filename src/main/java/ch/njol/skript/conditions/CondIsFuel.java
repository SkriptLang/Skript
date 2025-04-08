package ch.njol.skript.conditions;

import org.bukkit.Material;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;

@Name("Is Fuel")
@Description("Checks whether an item can be used as fuel in a furnace.")
@Examples({
	"on right click on furnace:",
		"\tif player's tool is not fuel:",
			"\t\tsend \"Please hold a valid fuel item in your hand\"",
			"\t\tcancel event"
})
@Since("2.5.1")
public class CondIsFuel extends PropertyCondition<ItemType> {
	
	static {
		if (Skript.methodExists(Material.class, "isFuel")) {
			register(CondIsFuel.class, "[furnace] fuel", "itemtypes");
		}
	}
	
	@Override
	public boolean check(ItemType item) {
		return item.getMaterial().isFuel();
	}
	
	@Override
	protected String getPropertyName() {
		return "fuel";
	}
	
}
