package ch.njol.skript.classes.data;

import ch.njol.skript.lang.BuildableRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class DefaultBuildables {


	static {
		BuildableRegistry.registerBuildable(InventoryType.class, Inventory.class, type -> Bukkit.createInventory(null, type));
	}

}
