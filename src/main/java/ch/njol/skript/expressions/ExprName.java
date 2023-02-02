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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.World;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.md_5.bungee.api.ChatColor;

@Name("Name / Display Name / Tab List Name")
@Description({
	"Represents the Minecraft account, display or tab list name of a player, or the custom name of an item, entity, block, inventory, gamerule or world.",
	"",
	"<ul>",
	"\t<li><strong>Players</strong>",
	"\t\t<ul>",
	"\t\t\t<li><strong>Name:</strong> The Minecraft account name of the player. Can't be changed, but 'display name' can be changed.</li>",
	"\t\t\t<li><strong>Display Name:</strong> The name of the player that is displayed in messages. " +
		"This name can be changed freely and can include color codes, and is shared among all plugins (e.g. chat plugins will use the display name).</li>",
	"\t\t</ul>",
	"\t</li>",
	"\t<li><strong>Entities</strong>",
	"\t\t<ul>",
	"\t\t\t<li><strong>Name:</strong> The custom name of the entity. Can be changed. But for living entities, " +
		"the players will have to target the entity to see its name tag. For non-living entities, the name will not be visible at all. To prevent this, use 'display name'.</li>",
	"\t\t\t<li><strong>Display Name:</strong> The custom name of the entity. Can be changed, " +
		"which will also enable <em>custom name visibility</em> of the entity so name tag of the entity will be visible always.</li>",
	"\t\t</ul>",
	"\t</li>",
	"\t<li><strong>Items</strong>",
	"\t\t<ul>",
	"\t\t\t<li><strong>Name and Display Name:</strong> The <em>custom</em> name of the item (not the Minecraft locale name). Can be changed.</li>",
	"\t\t</ul>",
	"\t</li>",
	"\t<li><strong>Inventories</strong>",
	"\t\t<ul>",
	"\t\t\t<li><strong>Name and Display Name:</strong> The name/title of the inventory. " +
		"Changing name of an inventory means opening the same inventory with the same contents but with a different name to its current viewers.</li>",
	"\t\t</ul>",
	"\t</li>",
	"\t<li><strong>Gamerules (1.13+)</strong>",
	"\t\t<ul>",
	"\t\t\t<li><strong>Name:</strong> The name of the gamerule. Cannot be changed.</li>",
	"\t\t</ul>",
	"\t</li>",
	"\t<li><strong>Worlds</strong>",
	"\t\t<ul>",
	"\t\t\t<li><strong>Name:</strong> The name of the world. Cannot be changed.</li>",
	"\t\t</ul>",
	"\t</li>",
	"</ul>"
})
@Examples({
	"on join:",
	"\tplayer has permission \"name.red\"",
	"\tset the player's display name to \"&lt;red&gt;[admin] &lt;gold&gt;%name of player%\"",
	"\tset the player's tab list name to \"&lt;green&gt;%player's name%\"",
	"set the name of the player's tool to \"Legendary Sword of Awesomeness\""
})
@Since("before 2.1, 2.2-dev20 (inventory name), 2.4 (non-living entity support, changeable inventory name), INSERT VERSION (worlds)")
public class ExprName extends SimplePropertyExpression<Object, String> {

	@Nullable
	static final MethodHandle TITLE_METHOD;
	static final boolean HAS_GAMERULES;

	static {
		HAS_GAMERULES = Skript.classExists("org.bukkit.GameRule");
		register(ExprName.class, String.class, "(1¦name[s]|2¦(display|nick|chat|custom)[ ]name[s])", "offlineplayers/entities/blocks/itemtypes/inventories/slots/worlds"
			+ (HAS_GAMERULES ? "/gamerules" : ""));
		register(ExprName.class, String.class, "(3¦(player|tab)[ ]list name[s])", "players");

		// Get the old method for getting the name of an inventory.
		MethodHandle _METHOD = null;
		try {
			_METHOD = MethodHandles.lookup().findVirtual(Inventory.class, "getTitle", MethodType.methodType(String.class));
		} catch (IllegalAccessException | NoSuchMethodException ignored) {}
		TITLE_METHOD = _METHOD;
	}

	/*
	 * 1 = "name",
	 * 2 = "display name",
	 * 3 = "tablist name"
	 */
	private int mark;
	private static final ItemType AIR = Aliases.javaItemType("air");

	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		mark = parseResult.mark;
		setExpr(exprs[0]);
		if (mark != 1 && World.class.isAssignableFrom(getExpr().getReturnType())) {
			Skript.error("Can't use 'display name' with worlds. Use 'name' instead.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public String convert(Object o) {
		if (o instanceof OfflinePlayer && ((OfflinePlayer) o).isOnline())
			o = ((OfflinePlayer) o).getPlayer();

		if (o instanceof Player) {
			switch (mark) {
				case 1:
					return ((Player) o).getName();
				case 2:
					return ((Player) o).getDisplayName();
				case 3:
					return ((Player) o).getPlayerListName();
			}
		} else if (o instanceof OfflinePlayer) {
			return mark == 1 ? ((OfflinePlayer) o).getName() : null;
		} else if (o instanceof Entity) {
			return ((Entity) o).getCustomName();
		} else if (o instanceof Block) {
			BlockState state = ((Block) o).getState();
			if (state instanceof Nameable)
				return ((Nameable) state).getCustomName();
		} else if (o instanceof ItemType) {
			ItemMeta m = ((ItemType) o).getItemMeta();
			return m.hasDisplayName() ? m.getDisplayName() : null;
		} else if (o instanceof Inventory) {
			if (TITLE_METHOD != null) {
				try {
					return (String) TITLE_METHOD.invoke(o);
				} catch (Throwable ex) {
					Skript.exception(ex);
					return null;
				}
			} else {
				if (!((Inventory) o).getViewers().isEmpty())
					return ((Inventory) o).getViewers().get(0).getOpenInventory().getTitle();
				return null;
			}
		} else if (o instanceof Slot) {
			ItemStack is = ((Slot) o).getItem();
			if (is != null && is.hasItemMeta()) {
				ItemMeta m = is.getItemMeta();
				return m.hasDisplayName() ? m.getDisplayName() : null;
			}
		} else if (o instanceof World) {
			return ((World) o).getName();
		} else if (HAS_GAMERULES && o instanceof GameRule) {
			return ((GameRule) o).getName();
		}
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			if (mark == 1) {
				if (Player.class.isAssignableFrom(getExpr().getReturnType())) {
					Skript.error("Can't change the Minecraft name of a player. Change the 'display name' or 'tab list name' instead.");
					return null;
				} else if (World.class.isAssignableFrom(getExpr().getReturnType())) {
					return null;
				}
			}
			return CollectionUtils.array(String.class);
		}
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		String name = delta != null ? (String) delta[0] : null;
		for (Object o : getExpr().getArray(e)) {
			if (o instanceof Player) {
				switch (mark) {
					case 2:
						((Player) o).setDisplayName(name != null ? name + ChatColor.RESET : ((Player) o).getName());
						break;
					case 3: // Null check not necessary. This method will use the player's name if 'name' is null.
						((Player) o).setPlayerListName(name);
						break;
				}
			} else if (o instanceof Entity) {
				((Entity) o).setCustomName(name);
				if (mark == 2 || mode == ChangeMode.RESET) // Using "display name"
					((Entity) o).setCustomNameVisible(name != null);
				if (o instanceof LivingEntity)
					((LivingEntity) o).setRemoveWhenFarAway(name == null);
			} else if (o instanceof Block) {
				BlockState state = ((Block) o).getState();
				if (state instanceof Nameable) {
					((Nameable) state).setCustomName(name);
					state.update();
				}
			} else if (o instanceof ItemType) {
				ItemType i = (ItemType) o;
				ItemMeta m = i.getItemMeta();
				m.setDisplayName(name);
				i.setItemMeta(m);
			} else if (o instanceof Inventory) {
				Inventory inv = (Inventory) o;

				if (inv.getViewers().isEmpty())
					return;
				// Create a clone to avoid a ConcurrentModificationException
				List<HumanEntity> viewers = new ArrayList<>(inv.getViewers());

				InventoryType type = inv.getType();
				if (!type.isCreatable())
					return;
				if (name == null)
					name = type.getDefaultTitle();

				Inventory copy;
				if (type == InventoryType.CHEST) {
					copy = Bukkit.createInventory(inv.getHolder(), inv.getSize(), name);
				} else {
					copy = Bukkit.createInventory(inv.getHolder(), type, name);
				}
				copy.setContents(inv.getContents());
				viewers.forEach(viewer -> viewer.openInventory(copy));
			} else if (o instanceof Slot) {
				Slot s = (Slot) o;
				ItemStack is = s.getItem();
				if (is != null && !AIR.isOfType(is)) {
					ItemMeta m = is.hasItemMeta() ? is.getItemMeta() : Bukkit.getItemFactory().getItemMeta(is.getType());
					m.setDisplayName(name);
					is.setItemMeta(m);
					s.setItem(is);
				}
			}
		}
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		switch (mark) {
			case 1: return "name";
			case 2: return "display name";
			case 3: return "tablist name";
			default: return "name";
		}
	}

}
