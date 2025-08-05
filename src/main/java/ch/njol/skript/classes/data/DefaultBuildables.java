package ch.njol.skript.classes.data;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.BuildableRegistry;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class DefaultBuildables {

	static {
		// REGISTER
		BuildableRegistry.registerBuildable(InventoryType.class, Inventory.class, type -> Bukkit.createInventory(null, type));

		// DISALLOW
		BuildableRegistry.registerDisallowed(Entity.class, EntityData.class, EntityType.class);
		BuildableRegistry.registerDisallowed(DamageSource.class);
	}

}
