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

import ch.njol.skript.aliases.ItemData;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Steerable;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;
import org.jetbrains.annotations.Nullable;
import org.bukkit.inventory.PlayerInventory;
import org.eclipse.jdt.annotation.Nullable;

@Name("Equip")
@Description("Equips or unequips an entity with some given armor or give them items. This will replace any armor that the entity is wearing.")
@Examples({
		"equip player with diamond helmet",
		"equip player with all diamond armor",
		"unequip diamond chestplate from player",
		"unequip all armor from player",
		"unequip player's armor",
		"equip player with stained glass pane as a hat",
		"equip player with diamond sword"
})
@Since("1.0, 2.7 (multiple entities, unequip), INSERT VERSION (giving items/as hat)")
public class EffEquip extends Effect {

	static {
		Skript.registerEffect(EffEquip.class,
				"equip [%livingentities%] with %itemtypes% [hat:as [a] (hat|helmet|cap)]",
				"make %livingentities% wear %itemtypes%",
				"unequip %itemtypes% [from %livingentities%]",
				"unequip %livingentities%'[s] (armor|equipment)"
			);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;
	@Nullable
	private Expression<ItemType> itemTypes;
	private boolean isEquipWith;
	private boolean isHat;

	private boolean equip = true;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		isEquipWith = matchedPattern == 0;
		isHat = parser.hasTag("hat");
		if (matchedPattern == 0 || matchedPattern == 1) {
			entities = (Expression<LivingEntity>) exprs[0];
			itemTypes = (Expression<ItemType>) exprs[1];
		} else if (matchedPattern == 2) {
			itemTypes = (Expression<ItemType>) exprs[0];
			entities = (Expression<LivingEntity>) exprs[1];
			equip = false;
		} else if (matchedPattern == 3) {
			entities = (Expression<LivingEntity>) exprs[0];
			equip = false;
		}
		return true;
	}

	private static final boolean SUPPORTS_STEERABLE = Skript.classExists("org.bukkit.entity.Steerable");

	private static ItemType CHESTPLATE;
	private static ItemType LEGGINGS;
	private static ItemType BOOTS;
	private static ItemType CARPET;
	private static final ItemType HORSE_ARMOR = new ItemType(Material.IRON_HORSE_ARMOR, Material.GOLDEN_HORSE_ARMOR, Material.DIAMOND_HORSE_ARMOR);
	private static final ItemType SADDLE = new ItemType(Material.SADDLE);
	private static final ItemType CHEST = new ItemType(Material.CHEST);

	static {
		boolean usesWoolCarpetTag = Skript.fieldExists(Tag.class, "WOOL_CARPET");
		CARPET = new ItemType(usesWoolCarpetTag ? Tag.WOOL_CARPETS : Tag.CARPETS);
		// added in 1.20.6
		if (Skript.fieldExists(Tag.class, "ITEM_CHEST_ARMOR")) {
			CHESTPLATE = new ItemType(Tag.ITEMS_CHEST_ARMOR);
			LEGGINGS = new ItemType(Tag.ITEMS_LEG_ARMOR);
			BOOTS = new ItemType(Tag.ITEMS_FOOT_ARMOR);
		} else {
			CHESTPLATE = new ItemType(
				Material.LEATHER_CHESTPLATE,
				Material.CHAINMAIL_CHESTPLATE,
				Material.GOLDEN_CHESTPLATE,
				Material.IRON_CHESTPLATE,
				Material.DIAMOND_CHESTPLATE,
				Material.ELYTRA
			);

			LEGGINGS = new ItemType(
				Material.LEATHER_LEGGINGS,
				Material.CHAINMAIL_LEGGINGS,
				Material.GOLDEN_LEGGINGS,
				Material.IRON_LEGGINGS,
				Material.DIAMOND_LEGGINGS
			);

			BOOTS = new ItemType(
				Material.LEATHER_BOOTS,
				Material.CHAINMAIL_BOOTS,
				Material.GOLDEN_BOOTS,
				Material.IRON_BOOTS,
				Material.DIAMOND_BOOTS
			);

			// netherite
			if (Skript.isRunningMinecraft(1,16)) {
				CHESTPLATE.add(new ItemData(Material.NETHERITE_CHESTPLATE));
				LEGGINGS.add(new ItemData(Material.NETHERITE_LEGGINGS));
				BOOTS.add(new ItemData(Material.NETHERITE_BOOTS));
			}
		}
	}



	@Override
	protected void execute(Event event) {
		ItemType[] itemTypes;
		if (this.itemTypes != null) {
			itemTypes = this.itemTypes.getArray(event);
		} else {
			itemTypes = new ItemType[0];
		}
		boolean isUnequipAll = !equip && itemTypes.length == 0;
		for (LivingEntity entity : entities.getArray(event)) {
			if (SUPPORTS_STEERABLE && entity instanceof Steerable) {
				if (isUnequipAll) { // shortcut
					((Steerable) entity).setSaddle(false);
					continue;
				}
				for (ItemType itemType : itemTypes) {
					if (SADDLE.isOfType(itemType.getMaterial())) {
						((Steerable) entity).setSaddle(equip);
						break;
					}
				}
			} else if (entity instanceof Pig) {
				if (isUnequipAll) { // shortcut
					((Pig) entity).setSaddle(false);
					continue;
				}
				for (ItemType itemType : itemTypes) {
					if (itemType.isOfType(Material.SADDLE)) {
						((Pig) entity).setSaddle(equip);
						break;
					}
				}
			} else if (entity instanceof Llama) {
				LlamaInventory inv = ((Llama) entity).getInventory();
				if (isUnequipAll) { // shortcut
					inv.setDecor(null);
					((Llama) entity).setCarryingChest(false);
					continue;
				}
				for (ItemType itemType : itemTypes) {
					for (ItemStack item : itemType.getAll()) {
						if (CARPET.isOfType(item)) {
							inv.setDecor(equip ? item : null);
						} else if (CHEST.isOfType(item)) {
							((Llama) entity).setCarryingChest(equip);
						}
					}
				}
			} else if (entity instanceof AbstractHorse) {
				// Spigot's API is bad, just bad... Abstract horse doesn't have horse inventory!
				Inventory inv = ((AbstractHorse) entity).getInventory();
				if (isUnequipAll) { // shortcut
					inv.setItem(0, null);
					inv.setItem(1, null);
					if (entity instanceof ChestedHorse)
						((ChestedHorse) entity).setCarryingChest(false);
					continue;
				}
				for (ItemType itemType : itemTypes) {
					for (ItemStack item : itemType.getAll()) {
						if (SADDLE.isOfType(item)) {
							inv.setItem(0, equip ? item : null); // Slot 0=saddle
						} else if (HORSE_ARMOR.isOfType(item)) {
							inv.setItem(1, equip ? item : null); // Slot 1=armor
						} else if (CHEST.isOfType(item) && entity instanceof ChestedHorse) {
							((ChestedHorse) entity).setCarryingChest(equip);
						}
					}
				}
			} else { // players and other entities
				EntityEquipment equipment = entity.getEquipment();
				if (equipment == null)
					continue;
				boolean isPlayer = entity instanceof Player;
				if (isUnequipAll) { // shortcut
					// We shouldn't affect player's inventory by removing anything other than armor
					equipment.setHelmet(null);
					equipment.setChestplate(null);
					equipment.setLeggings(null);
					equipment.setBoots(null);
					if (isPlayer)
						PlayerUtils.updateInventory((Player) entity);
					continue;
				}
				for (ItemType itemType : itemTypes) {
					for (ItemStack item : itemType.getAll()) {
						 if (isHat || HELMET.isOfType(item)) {
							 // Apply all other items to head (if isHat), as all items will appear on a player's head
							 equipment.setHelmet(equip ? item : null);
						 } else if (CHESTPLATE.isOfType(item) || ELYTRA.isOfType(item)) {
							equipment.setChestplate(equip ? item : null);
						} else if (LEGGINGS.isOfType(item)) {
							equipment.setLeggings(equip ? item : null);
						} else if (BOOTS.isOfType(item)) {
							equipment.setBoots(equip ? item : null);
						} else if (isEquipWith && isPlayer && item != null) { // only to players
							((Player) entity).getInventory().addItem(item);
						}
					}
				}
				if (isPlayer)
					PlayerUtils.updateInventory((Player) entity);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (equip) {
			assert itemTypes != null;
			return "equip " + entities.toString(event, debug) + " with " + itemTypes.toString(event, debug);
		} else if (itemTypes != null) {
			return "unequip " + itemTypes.toString(event, debug) + " from " + entities.toString(event, debug);
		} else {
			return "unequip " + entities.toString(event, debug) + "'s equipment";
		}
	}

}
