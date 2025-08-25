package ch.njol.skript.classes.data;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.CustomizableRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class DefaultBuildables {

	static {
		// REGISTER
		CustomizableRegistry.registerCustomizable(InventoryType.class, Inventory.class, type -> Bukkit.createInventory(null, type));

		// DISALLOW
		CustomizableRegistry.registerDisallowed(Entity.class, EntityData.class, EntityType.class);
	}

}
