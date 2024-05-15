/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

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
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Maximum Stack Size")
@Description({
	"The maximum stack size of an item (e.g. 64 for torches, 16 for buckets, and 1 for swords), or of an inventory to have items to stack up to.",
	"Since MC 1.20.5 onwards, the maximum stack size of items can be changed (any integer from 1 to 99), and the said item can be stacked up to the set value, up to the maximum stack size of an inventory."
})
@Examples({
	"send \"You can only pick up %max stack size of player's tool% of %type of (player's tool)%\" to player",
	"",
	"set the maximum stack size of inventory of all players to 16",
	"",
	"add 8 to the maximum stack size of player's tool",
	"",
	"reset the maximum stack size of {_gui}"
})
@Since("2.1, INSERT VERSION (changeable, inventory support)")
@RequiredPlugins("MC 1.20.5+ (changeable item max stack size)")
public class ExprMaxStack extends SimplePropertyExpression<Object, Integer> {

	static {
		register(ExprMaxStack.class, Integer.class, "max[imum] stack[[ ]size]", "itemtypes/inventories");
	}

	private static final boolean CHANGEABLE_ITEM_STACK_SIZE = Skript.methodExists(ItemMeta.class, "setMaxStackSize", Integer.class);

	@Override
	@Nullable
	public Integer convert(Object source) {
		if (source instanceof ItemType) {
			return ((ItemType) source).getRandom().getMaxStackSize();
		} else if (source instanceof Inventory) {
			return (((Inventory) source).getMaxStackSize());
		} else {
			// Invalid source -- return null
			return null;
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
            case ADD:
			case DELETE:
            case REMOVE:
			case RESET:
			case SET:
				if (!CHANGEABLE_ITEM_STACK_SIZE && ItemType.class.isAssignableFrom(getExpr().getReturnType())) {
					Skript.error("Changers for maximum stack size of items requires Minecraft 1.20.5 or newer");
					return null;
				}
				return CollectionUtils.array(Integer.class);
			default:
				return null;
        }
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		for (Object source : getExpr().getArray(event)) {
			Integer change = null;
			if (mode != ChangeMode.DELETE && mode != ChangeMode.RESET)
				change = (int) delta[0];
			if (source instanceof ItemType) {
				if (!CHANGEABLE_ITEM_STACK_SIZE)
					continue;
				ItemType itemType = ((ItemType) source);
				int size = itemType.getRandom().getMaxStackSize();
                switch (mode) {
                    case ADD:
						size += change;
                        break;
                    case SET:
						size = change;
						break;
                    case REMOVE:
						size -= change;
                        break;
                }
				ItemMeta meta = itemType.getItemMeta();
				// Minecraft only accepts stack size from 1 to 99
				meta.setMaxStackSize(change != null ? Math2.fit(1, size, 99) : null);
				itemType.setItemMeta(meta);
			} else if (source instanceof Inventory) {
				Inventory inv = ((Inventory) source);
				int size = inv.getMaxStackSize();
				switch (mode) {
					case ADD:
						size += change;
						break;
					case SET:
						size = change;
						break;
					case REMOVE:
						size -= change;
						break;
					case DELETE:
					case RESET:
						size = Bukkit.createInventory(null, inv.getType()).getMaxStackSize();
						break;
				}
				inv.setMaxStackSize(size);
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

}
