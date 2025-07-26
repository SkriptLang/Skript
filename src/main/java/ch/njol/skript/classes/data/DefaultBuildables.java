package ch.njol.skript.classes.data;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.BuildableObject;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

public class DefaultBuildables {

	public static <T> void registerBuildable(Class<T> type) {
		Converters.registerConverter(type, BuildableObject.class, object -> new BuildableObject<T>() {
			@Override
			public T getSource() {
				return object;
			}

			@Override
			public Class<? extends T> getReturnType() {
				return type;
			}
		});
	}

	public static <F, T> void registerBuildable(Class<F> fromType, Class<T> toType, Converter<F, T> converter) {
		Converters.registerConverter(fromType, BuildableObject.class, object -> new BuildableObject<T>() {
			@Override
			public T getSource() {
				return converter.convert(object);
			}

			@Override
			public Class<? extends T> getReturnType() {
				return toType;
			}
		});
	}

	static {
		registerBuildable(ItemType.class);
		registerBuildable(InventoryType.class, Inventory.class, inventoryType -> Bukkit.createInventory(null, inventoryType));
		registerBuildable(Inventory.class);
	}

}
