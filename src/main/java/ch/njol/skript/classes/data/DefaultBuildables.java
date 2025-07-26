package ch.njol.skript.classes.data;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.BuildableObject;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

public class DefaultBuildables {

	static {
		Converters.registerConverter(ItemType.class, BuildableObject.class, itemType -> new BuildableObject<ItemType>() {
			@Override
			public ItemType getSource() {
				return itemType;
			}

			@Override
			public Class<? extends ItemType> getReturnType() {
				return ItemType.class;
			}
		}, Converter.NO_CHAINING);

		Converters.registerConverter(InventoryType.class, BuildableObject.class, inventoryType -> new BuildableObject<Inventory>() {
			@Override
			public Inventory getSource() {
				return Bukkit.createInventory(null, inventoryType);
			}

			@Override
			public Class<? extends Inventory> getReturnType() {
				return Inventory.class;
			}
		}, Converter.NO_CHAINING);
	}

}
