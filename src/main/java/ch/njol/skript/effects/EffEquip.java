package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
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
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.entity.Steerable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Equip")
@Description({
	"Equips or unequips an entity with the given itemtypes (usually armor).",
	"This effect will replace any armor that the entity is already wearing."
})
@Examples({
	"equip player with diamond helmet",
	"equip player with all diamond armor",
	"unequip diamond chestplate from player",
	"unequip all armor from player",
	"unequip player's armor"
})
@Since("1.0, 2.7 (multiple entities, unequip), 2.10 (wolves)")
public class EffEquip extends Effect {

	private static ItemType CHESTPLATE;
	private static ItemType LEGGINGS;
	private static ItemType BOOTS;
	private static ItemType CARPET = new ItemType(Tag.WOOL_CARPETS);
	private static ItemType WOLF_ARMOR;
	private static final ItemType HORSE_ARMOR = new ItemType(Material.LEATHER_HORSE_ARMOR, Material.IRON_HORSE_ARMOR, Material.GOLDEN_HORSE_ARMOR, Material.DIAMOND_HORSE_ARMOR);
	private static final ItemType SADDLE = new ItemType(Material.SADDLE);
	private static final ItemType CHEST = new ItemType(Material.CHEST);

	static {
		boolean hasWolfArmor = Skript.fieldExists(Material.class, "WOLF_ARMOR");
		WOLF_ARMOR = hasWolfArmor ? new ItemType(Material.WOLF_ARMOR) : new ItemType();

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
        Material.NETHERITE_CHESTPLATE,
				Material.ELYTRA
			);

			LEGGINGS = new ItemType(
				Material.LEATHER_LEGGINGS,
				Material.CHAINMAIL_LEGGINGS,
				Material.GOLDEN_LEGGINGS,
				Material.IRON_LEGGINGS,
				Material.DIAMOND_LEGGINGS,
        Material.NETHERITE_LEGGINGS
			);

			BOOTS = new ItemType(
				Material.LEATHER_BOOTS,
				Material.CHAINMAIL_BOOTS,
				Material.GOLDEN_BOOTS,
				Material.IRON_BOOTS,
				Material.DIAMOND_BOOTS,
        Material.NETHERITE_BOOTS
			);
		}
	}

	private static final ItemType[] ALL_EQUIPMENT = new ItemType[] {CHESTPLATE, LEGGINGS, BOOTS, HORSE_ARMOR, SADDLE, CHEST, CARPET, WOLF_ARMOR};

	static {
		Skript.registerEffect(EffEquip.class,
				"equip [%livingentities%] with %itemtypes%",
				"make %livingentities% wear %itemtypes%",
				"unequip %itemtypes% [from %livingentities%]",
				"unequip %livingentities%'[s] (armo[u]r|equipment)");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;
	private @UnknownNullability Expression<ItemType> itemTypes;

	private boolean equip = true;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
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

	@Override
	protected void execute(Event event) {
		ItemType[] itemTypes;
		boolean unequipHelmet = false;
		if (this.itemTypes != null) {
			itemTypes = this.itemTypes.getArray(event);
		} else {
			itemTypes = ALL_EQUIPMENT;
			unequipHelmet = true;
		}
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Steerable steerable) {
				for (ItemType itemType : itemTypes) {
					if (SADDLE.isOfType(itemType.getMaterial())) {
						steerable.setSaddle(equip);
					}
				}
			} else if (entity instanceof Llama llama) {
				LlamaInventory inv = llama.getInventory();
				for (ItemType itemType : itemTypes) {
					for (ItemStack item : itemType.getAll()) {
						if (CARPET.isOfType(item)) {
							inv.setDecor(equip ? item : null);
						} else if (CHEST.isOfType(item)) {
							llama.setCarryingChest(equip);
						}
					}
				}
			} else if (entity instanceof AbstractHorse horse) {
				AbstractHorseInventory inv = horse.getInventory();
				for (ItemType itemType : itemTypes) {
					for (ItemStack item : itemType.getAll()) {
						if (SADDLE.isOfType(item)) {
							inv.setSaddle(equip ? item : null);
						} else if (HORSE_ARMOR.isOfType(item) && entity instanceof Horse) {
							((HorseInventory) inv).setArmor(equip ? item : null);
						} else if (CHEST.isOfType(item) && entity instanceof ChestedHorse chestedHorse) { // a Donkey, Mule, Llama or TraderLlama. NOT a Horse
							chestedHorse.setCarryingChest(equip);
						}
					}
				}
			} else if (entity instanceof Wolf wolf) {
				EntityEquipment equipment = wolf.getEquipment();
				for (ItemType itemType : itemTypes) {
					for (ItemStack item : itemType.getAll()) {
						if (WOLF_ARMOR.isOfType(item))
							equipment.setItem(EquipmentSlot.BODY, equip ? item : null);
					}
				}
			} else {
				EntityEquipment equipment = entity.getEquipment();
				if (equipment == null)
					continue;
				for (ItemType itemType : itemTypes) {
					for (ItemStack item : itemType.getAll()) {
						if (CHESTPLATE.isOfType(item)) {
							equipment.setChestplate(equip ? item : null);
						} else if (LEGGINGS.isOfType(item)) {
							equipment.setLeggings(equip ? item : null);
						} else if (BOOTS.isOfType(item)) {
							equipment.setBoots(equip ? item : null);
						} else {
							// Apply all other items to head, as all items will appear on a player's head
							equipment.setHelmet(equip ? item : null);
						}
					}
					if (unequipHelmet) { // Since players can wear any helmet, itemTypes won't have the item in the array every time
						equipment.setHelmet(null);
					}
				}
				if (entity instanceof Player player)
					PlayerUtils.updateInventory(player);
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
