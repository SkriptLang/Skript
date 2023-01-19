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
package ch.njol.skript.effects;

import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Steerable;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Equip")
@Description("Equips or unequips an entity with some given armor. This will replace any armor that the entity is wearing.")
@Examples({"equip player with diamond helmet",
		"equip player with all diamond armor",
		"unequip diamond chestplate from player",
		"unequip all armor from player"})
@Since("1.0, INSERT VERSION (multiple entities, unequip)")
public class EffEquip extends Effect {

	static {
		Skript.registerEffect(EffEquip.class,
				"equip [%livingentities%] with %itemtypes%",
				"make %livingentities% wear %itemtypes%",
				"unequip %itemtypes% [from %livingentities%]");
	}

	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	@SuppressWarnings("null")
	private Expression<ItemType> itemTypes;

	private boolean equip = true;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (matchedPattern == 0 || matchedPattern == 1) {
			entities = (Expression<LivingEntity>) exprs[0];
			itemTypes = (Expression<ItemType>) exprs[1];
		} else if (matchedPattern == 2) {
			itemTypes = (Expression<ItemType>) exprs[0];
			entities = (Expression<LivingEntity>) exprs[1];
			equip = false;
		}
		return true;
	}

	private static final boolean SUPPORTS_HORSES = Skript.classExists("org.bukkit.entity.Horse");
	private static final boolean NEW_HORSES = Skript.classExists("org.bukkit.entity.AbstractHorse");
	private static final boolean SUPPORTS_LLAMAS = Skript.classExists("org.bukkit.entity.Llama");
	private static final boolean SUPPORTS_STEERABLE = Skript.classExists("org.bukkit.entity.Steerable");

	private static final ItemType CHESTPLATE = Aliases.javaItemType("chestplate");
	private static final ItemType LEGGINGS = Aliases.javaItemType("leggings");
	private static final ItemType BOOTS = Aliases.javaItemType("boots");
	private static final ItemType HORSE_ARMOR = Aliases.javaItemType("horse armor");
	private static final ItemType SADDLE = Aliases.javaItemType("saddle");
	private static final ItemType CHEST = Aliases.javaItemType("chest");
	private static final ItemType CARPET = Aliases.javaItemType("carpet");

	@Override
	@SuppressWarnings("deprecation")
	protected void execute(Event event) {
		ItemType[] ts = itemTypes.getArray(event);
		for (LivingEntity en : entities.getArray(event)) {
			if (SUPPORTS_STEERABLE && en instanceof Steerable) {
				for (ItemType it : ts) {
					if (SADDLE.isOfType(it.getMaterial())) {
						((Steerable) en).setSaddle(equip);
					}
				}
				continue;
			} else if (en instanceof Pig) {
				for (ItemType t : ts) {
					if (t.isOfType(Material.SADDLE)) {
						((Pig) en).setSaddle(equip);
						break;
					}
				}
				continue;
			} else if (SUPPORTS_LLAMAS && en instanceof Llama) {
				LlamaInventory inv = ((Llama) en).getInventory();
				for (ItemType t : ts) {
					for (ItemStack item : t.getAll()) {
						if (CARPET.isOfType(item)) {
							inv.setDecor(equip ? item : new ItemStack(Material.AIR));
						} else if (CHEST.isOfType(item)) {
							((Llama) en).setCarryingChest(equip);
						}
					}
				}
				continue;
			} else if (NEW_HORSES && en instanceof AbstractHorse) {
				// Spigot's API is bad, just bad... Abstract horse doesn't have horse inventory!
				Inventory inv = ((AbstractHorse) en).getInventory();
				for (ItemType t : ts) {
					for (ItemStack item : t.getAll()) {
						if (SADDLE.isOfType(item)) {
							inv.setItem(0, equip ? item : new ItemStack(Material.AIR)); // Slot 0=saddle
						} else if (HORSE_ARMOR.isOfType(item)) {
							inv.setItem(1, equip ? item : new ItemStack(Material.AIR)); // Slot 1=armor
						} else if (CHEST.isOfType(item) && en instanceof ChestedHorse) {
							((ChestedHorse) en).setCarryingChest(equip);
						}
					}
				}
				continue;
			} else if (SUPPORTS_HORSES && en instanceof Horse) {
				HorseInventory inv = ((Horse) en).getInventory();
				for (ItemType t : ts) {
					for (ItemStack item : t.getAll()) {
						if (SADDLE.isOfType(item)) {
							inv.setSaddle(equip ? item : new ItemStack(Material.AIR));
						} else if (HORSE_ARMOR.isOfType(item)) {
							inv.setArmor(equip ? item : new ItemStack(Material.AIR));
						} else if (CHEST.isOfType(item)) {
							((Horse) en).setCarryingChest(equip);
						}
					}
				}
				continue;
			}
			EntityEquipment equipment = en.getEquipment();
			if (equipment == null)
				continue;
			for (ItemType t : ts) {
				for (ItemStack item : t.getAll()) {
					if (CHESTPLATE.isOfType(item)) {
						equipment.setChestplate(equip ? item : new ItemStack(Material.AIR));
					} else if (LEGGINGS.isOfType(item)) {
						equipment.setLeggings(equip ? item : new ItemStack(Material.AIR));
					} else if (BOOTS.isOfType(item)) {
						equipment.setBoots(equip ? item : new ItemStack(Material.AIR));
					} else {
						// Apply all other items to head, as all items will appear on a player's head
						equipment.setHelmet(equip ? item : new ItemStack(Material.AIR));
					}
				}
			}
			if (en instanceof Player)
				PlayerUtils.updateInventory((Player) en);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (equip) {
			return "equip " + entities.toString(event, debug) + " with " + itemTypes.toString(event, debug);
		} else {
			return "unequip " + itemTypes.toString(event, debug) + " from " + entities.toString(event, debug);
		}
	}

}
