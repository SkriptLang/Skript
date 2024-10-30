package ch.njol.skript.effects;

import ch.njol.skript.Skript;
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
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;
import org.jetbrains.annotations.Nullable;

@Name("Equip")
@Description(
	"Equips or unequips an entity with some given armor or give them items. " +
	"This will replace any armor that the entity is wearing."
)
@Examples({
	"equip player with diamond helmet",
	"equip player with all diamond armor",
	"unequip diamond chestplate from player",
	"unequip all armor from player",
	"unequip player's armor",
	"make player wear a stained glass pane as a hat",
	"equip player with diamond sword"
})
@Since("1.0, 2.7 (multiple entities, unequip), INSERT VERSION (giving items/as hat)")
public class EffEquip extends Effect {

	private static final boolean SUPPORTS_STEERABLE = Skript.classExists("org.bukkit.entity.Steerable");

	private static final ItemType HELMET;
	private static final ItemType CHESTPLATE;
	private static final ItemType LEGGINGS;
	private static final ItemType BOOTS;
	private static final ItemType CARPET;
	private static final ItemType ELYTRA = new ItemType(Material.ELYTRA);
	private static final ItemType HORSE_ARMOR = new ItemType(Material.IRON_HORSE_ARMOR, Material.GOLDEN_HORSE_ARMOR, Material.DIAMOND_HORSE_ARMOR);
	private static final ItemType SADDLE = new ItemType(Material.SADDLE);
	private static final ItemType CHEST = new ItemType(Material.CHEST);

	static {
		boolean usesWoolCarpetTag = Skript.fieldExists(Tag.class, "WOOL_CARPET");
		CARPET = new ItemType(usesWoolCarpetTag ? Tag.WOOL_CARPETS : Tag.CARPETS);
		// added in 1.20.6
		if (Skript.fieldExists(Tag.class, "ITEM_CHEST_ARMOR")) {
			HELMET = new ItemType(Tag.ITEMS_HEAD_ARMOR);
			CHESTPLATE = new ItemType(Tag.ITEMS_CHEST_ARMOR);
			LEGGINGS = new ItemType(Tag.ITEMS_LEG_ARMOR);
			BOOTS = new ItemType(Tag.ITEMS_FOOT_ARMOR);
		} else {
			HELMET = new ItemType(
				Material.LEATHER_HELMET,
				Material.CHAINMAIL_HELMET,
				Material.GOLDEN_HELMET,
				Material.IRON_HELMET,
				Material.DIAMOND_HELMET,
				Material.NETHERITE_HELMET,
				Material.TURTLE_HELMET
			);

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

	static {
		Skript.registerEffect(EffEquip.class,
			"equip [%livingentities%] with %itemtypes% [hat:as [a|their] (hat|helmet|cap)]",
			"make %livingentities% wear %itemtypes% [hat:as [a|their] (hat|helmet|cap)]",
			"unequip %itemtypes% [(of|from) %livingentities%]",
			"unequip %livingentities%'[s] (armor|equipment)"
		);
	}

	private Expression<LivingEntity> entities;
	private @Nullable Expression<ItemType> itemTypes;
	private boolean isEquipWith;
	private boolean isHat;
	private boolean equip;
	private boolean isUnequipAll;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parser) {
		isEquipWith = matchedPattern == 0;
		isUnequipAll = matchedPattern == 3;
		isHat = parser.hasTag("hat");

		if (matchedPattern == 0 || matchedPattern == 1) {
			//noinspection unchecked
			entities = (Expression<LivingEntity>) expressions[0];
			//noinspection unchecked
			itemTypes = (Expression<ItemType>) expressions[1];
			equip = true;
		} else if (matchedPattern == 2) {
			//noinspection unchecked
			itemTypes = (Expression<ItemType>) expressions[0];
			//noinspection unchecked
			entities = (Expression<LivingEntity>) expressions[1];
			equip = false;
		} else if (matchedPattern == 3) {
			//noinspection unchecked
			entities = (Expression<LivingEntity>) expressions[0];
			equip = false;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		ItemType[] itemTypes;
		if (this.itemTypes != null) {
			itemTypes = this.itemTypes.getArray(event);
		} else {
			itemTypes = new ItemType[0];
		}
		for (LivingEntity entity : entities.getArray(event)) {
			if (SUPPORTS_STEERABLE && entity instanceof Steerable steerable) {
				if (isUnequipAll) { // shortcut
					steerable.setSaddle(false);
					continue;
				}
				for (ItemType itemType : itemTypes) {
					if (SADDLE.isOfType(itemType.getMaterial())) {
						steerable.setSaddle(equip);
						break;
					}
				}
			} else if (entity instanceof Pig pig) {
				if (isUnequipAll) { // shortcut
					pig.setSaddle(false);
					continue;
				}
				for (ItemType itemType : itemTypes) {
					if (itemType.isOfType(Material.SADDLE)) {
						pig.setSaddle(equip);
						break;
					}
				}
			} else if (entity instanceof Llama llama) {
				LlamaInventory inv = llama.getInventory();
				if (isUnequipAll) { // shortcut
					inv.setDecor(null);
					llama.setCarryingChest(false);
					continue;
				}
				for (ItemType itemType : itemTypes) {
					for (ItemStack item : itemType.getAll()) {
						if (CARPET.isOfType(item)) {
							inv.setDecor(equip ? item : null);
						} else if (CHEST.isOfType(item)) {
							llama.setCarryingChest(equip);
						}
					}
				}
			} else if (entity instanceof AbstractHorse abstractHorse) {
				// Spigot's API is bad, just bad... Abstract horse doesn't have horse inventory!
				Inventory inv = abstractHorse.getInventory();
				if (isUnequipAll) { // shortcut
					inv.setItem(0, null);
					inv.setItem(1, null);
					if (entity instanceof ChestedHorse chestedHorse)
						chestedHorse.setCarryingChest(false);
					continue;
				}
				for (ItemType itemType : itemTypes) {
					for (ItemStack item : itemType.getAll()) {
						if (SADDLE.isOfType(item)) {
							inv.setItem(0, equip ? item : null); // Slot 0=saddle
						} else if (HORSE_ARMOR.isOfType(item)) {
							inv.setItem(1, equip ? item : null); // Slot 1=armor
						} else if (CHEST.isOfType(item) && entity instanceof ChestedHorse chestedHorse) {
							chestedHorse.setCarryingChest(equip);
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
			return "equip " + entities.toString(event, debug) + " with " +
				(itemTypes == null ? "unknown itemtypes" : itemTypes.toString(event, debug)) +
				(isHat ? " as a hat" : "");
		} else if (itemTypes != null) {
			return "unequip " + itemTypes.toString(event, debug) + " from " + entities.toString(event, debug);
		} else {
			return "unequip " + entities.toString(event, debug) + "'s equipment";
		}
	}

}
