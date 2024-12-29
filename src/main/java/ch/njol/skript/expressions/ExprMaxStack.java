package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;

@Name("Maximum Stack Size")
@Description({
	"The maximum stack size of an item (e.g. 64 for torches, 16 for buckets, 1 for swords, etc.) or inventory.",
	"In 1.20.5+, the maximum stack size of items can be changed to any integer from 1 to 99, and stacked up to the maximum stack size of the inventory they're in."
})
@Examples({
	"send \"You can hold %max stack size of player's tool% of %type of player's tool% in a slot.\" to player",
	"set the maximum stack size of inventory of all players to 16",
	"add 8 to the maximum stack size of player's tool",
	"reset the maximum stack size of {_gui}"
})
@Since("2.1, INSERT VERSION (changeable, inventories)")
@RequiredPlugins("Spigot 1.20.5+ (changeable)")
public class ExprMaxStack extends SimplePropertyExpression<Object, Integer> {

	static {
		register(ExprMaxStack.class, Integer.class, "max[imum] stack[[ ]size]", "itemtypes/inventories");
	}

	private static final boolean CHANGEABLE_ITEM_STACK_SIZE = Skript.methodExists(ItemMeta.class, "setMaxStackSize", Integer.class);

	@Override
	public @Nullable Integer convert(Object from) {
		if (from instanceof ItemType itemType)
			return getMaxStackSize(itemType);
		if (from instanceof Inventory inventory)
			return inventory.getMaxStackSize();
		return null;
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, RESET, SET -> {
                if (!CHANGEABLE_ITEM_STACK_SIZE && ItemType.class.isAssignableFrom(getExpr().getReturnType())) {
                    Skript.error("Changing the maximum stack size of items requires Minecraft 1.20.5 or newer!");
                    yield null;
                }
                yield CollectionUtils.array(Integer.class);
            }
            default -> null;
        };
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int change = delta == null ? 0 : ((Number) delta[0]).intValue();
		for (Object source : getExpr().getArray(event)) {
			if (source instanceof ItemType itemType) {
				if (!CHANGEABLE_ITEM_STACK_SIZE)
					continue;
				int size = getMaxStackSize(itemType);
                switch (mode) {
                    case ADD -> size += change;
                    case SET -> size = change;
                    case REMOVE -> size -= change;
                }
				ItemMeta meta = itemType.getItemMeta();
				// Minecraft only accepts stack size from 1 to 99
				meta.setMaxStackSize(mode != ChangeMode.RESET ? Math2.fit(1, size, 99) : null);
				itemType.setItemMeta(meta);
			} else if (source instanceof Inventory inventory) {
                int size = inventory.getMaxStackSize();
				switch (mode) {
					case ADD -> size += change;
					case SET -> size = change;
					case REMOVE -> size -= change;
					case RESET -> size = Bukkit.createInventory(null, inventory.getType()).getMaxStackSize();
				}
				inventory.setMaxStackSize(size);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "maximum stack size";
	}

	private static int getMaxStackSize(ItemType itemType) {
		Object item = itemType.getRandomStackOrMaterial();
		if (item instanceof ItemStack stack)
			return stack.getMaxStackSize();
		if (item instanceof Material material)
			return material.getMaxStackSize();
		throw new UnsupportedOperationException();
	}

}
