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
import org.bukkit.inventory.InventoryHolder;
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
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

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
@Since("before 2.1, 2.2-dev20 (inventory name), 2.4 (non-living entity support, changeable inventory name), 2.7 (worlds)")
public class ExprName extends SimplePropertyExpression<Object, String> {

	@Nullable
	private static BungeeComponentSerializer serializer;
	static final boolean HAS_GAMERULES;

	static {
		// Check for Adventure API
		if (Skript.classExists("net.kyori.adventure.text.Component") &&
				Skript.methodExists(Bukkit.class, "createInventory", InventoryHolder.class, int.class, Component.class))
			serializer = BungeeComponentSerializer.get();
		HAS_GAMERULES = Skript.classExists("org.bukkit.GameRule");
		register(ExprName.class, String.class, "(1¦name[s]|2¦(display|nick|chat|custom)[ ]name[s])", "offlineplayers/entities/blocks/itemtypes/inventories/slots/worlds"
			+ (HAS_GAMERULES ? "/gamerules" : ""));
		register(ExprName.class, String.class, "(3¦(player|tab)[ ]list name[s])", "players");
	}

	/*
	 * 1 = "name",
	 * 2 = "display name",
	 * 3 = "tablist name"
	 */
	private int mark;
	private static final ItemType AIR = Aliases.javaItemType("air");

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
	public String convert(Object object) {
		if (object instanceof OfflinePlayer && ((OfflinePlayer) object).isOnline())
			object = ((OfflinePlayer) object).getPlayer();

		if (object instanceof Player) {
			switch (mark) {
				case 1:
					return ((Player) object).getName();
				case 2:
					return ((Player) object).getDisplayName();
				case 3:
					return ((Player) object).getPlayerListName();
			}
		} else if (object instanceof OfflinePlayer) {
			return mark == 1 ? ((OfflinePlayer) object).getName() : null;
		} else if (object instanceof Entity) {
			return ((Entity) object).getCustomName();
		} else if (object instanceof Block) {
			BlockState state = ((Block) object).getState();
			if (state instanceof Nameable)
				return ((Nameable) state).getCustomName();
		} else if (object instanceof ItemType) {
			ItemMeta m = ((ItemType) object).getItemMeta();
			return m.hasDisplayName() ? m.getDisplayName() : null;
		} else if (object instanceof Inventory) {
			Inventory inventory = (Inventory) object;
			if (inventory.getViewers().isEmpty())
				return null;
			return inventory.getViewers().get(0).getOpenInventory().getTitle();
		} else if (object instanceof Slot) {
			ItemStack is = ((Slot) object).getItem();
			if (is != null && is.hasItemMeta()) {
				ItemMeta m = is.getItemMeta();
				return m.hasDisplayName() ? m.getDisplayName() : null;
			}
		} else if (object instanceof World) {
			return ((World) object).getName();
		} else if (HAS_GAMERULES && object instanceof GameRule) {
			return ((GameRule) object).getName();
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
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		String name = delta != null ? (String) delta[0] : null;
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Player) {
				switch (mark) {
					case 2:
						((Player) object).setDisplayName(name != null ? name + ChatColor.RESET : ((Player) object).getName());
						break;
					case 3: // Null check not necessary. This method will use the player's name if 'name' is null.
						((Player) object).setPlayerListName(name);
						break;
				}
			} else if (object instanceof Entity) {
				((Entity) object).setCustomName(name);
				if (mark == 2 || mode == ChangeMode.RESET) // Using "display name"
					((Entity) object).setCustomNameVisible(name != null);
				if (object instanceof LivingEntity)
					((LivingEntity) object).setRemoveWhenFarAway(name == null);
			} else if (object instanceof Block) {
				BlockState state = ((Block) object).getState();
				if (state instanceof Nameable) {
					((Nameable) state).setCustomName(name);
					state.update();
				}
			} else if (object instanceof ItemType) {
				ItemType i = (ItemType) object;
				ItemMeta m = i.getItemMeta();
				m.setDisplayName(name);
				i.setItemMeta(m);
			} else if (object instanceof Inventory) {
				Inventory inv = (Inventory) object;

				if (inv.getViewers().isEmpty())
					return;
				// Create a clone to avoid a ConcurrentModificationException
				List<HumanEntity> viewers = new ArrayList<>(inv.getViewers());

				InventoryType type = inv.getType();
				if (!type.isCreatable())
					return;

				Inventory copy;
				if (serializer == null) {
					if (name == null)
						name = type.getDefaultTitle();
					if (type == InventoryType.CHEST) {
						copy = Bukkit.createInventory(inv.getHolder(), inv.getSize(), name);
					} else {
						copy = Bukkit.createInventory(inv.getHolder(), type, name);
					}
				} else {
					Component component = type.defaultTitle();
					if (name != null) {
						BaseComponent[] components = BungeeConverter.convert(ChatMessages.parseToArray(name));
						component = serializer.deserialize(components);
					}
					if (type == InventoryType.CHEST) {
						copy = Bukkit.createInventory(inv.getHolder(), inv.getSize(), component);
					} else {
						copy = Bukkit.createInventory(inv.getHolder(), type, component);
					}
				}
				copy.setContents(inv.getContents());
				viewers.forEach(viewer -> viewer.openInventory(copy));
			} else if (object instanceof Slot) {
				Slot s = (Slot) object;
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
