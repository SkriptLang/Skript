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
package ch.njol.skript.classes.data;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.command.Commands;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.util.BlockInventoryHolder;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

public class DefaultConverters {
	
	public DefaultConverters() {}
	
	static {
		// Number to subtypes converters
		Converters.registerConverter(Number.class, Byte.class, Number::byteValue);
		Converters.registerConverter(Number.class, Double.class, Number::doubleValue);
		Converters.registerConverter(Number.class, Float.class, Number::floatValue);
		Converters.registerConverter(Number.class, Integer.class, Number::intValue);
		Converters.registerConverter(Number.class, Long.class, Number::longValue);
		Converters.registerConverter(Number.class, Short.class, Number::shortValue);

		// OfflinePlayer - PlayerInventory
		Converters.registerConverter(OfflinePlayer.class, PlayerInventory.class, p -> {
			if (!p.isOnline())
				return null;
			Player online = p.getPlayer();
			assert online != null;
			return online.getInventory();
		}, Commands.CONVERTER_NO_COMMAND_ARGUMENTS);

		// OfflinePlayer - Player
		Converters.registerConverter(OfflinePlayer.class, Player.class, OfflinePlayer::getPlayer, Commands.CONVERTER_NO_COMMAND_ARGUMENTS);

		// CommandSender - Player
		Converters.registerConverter(CommandSender.class, Player.class, s -> {
			if (s instanceof Player)
				return (Player) s;
			return null;
		});

		// BlockCommandSender - Block
		Converters.registerConverter(BlockCommandSender.class, Block.class, BlockCommandSender::getBlock);

		// Entity - Player
		Converters.registerConverter(Entity.class, Player.class, e -> {
			if (e instanceof Player)
				return (Player) e;
			return null;
		});

		// Entity - LivingEntity // Entity->Player is used if this doesn't exist
		Converters.registerConverter(Entity.class, LivingEntity.class, e -> {
			if (e instanceof LivingEntity)
				return (LivingEntity) e;
			return null;
		});
		
		// Block - Inventory
		Converters.registerConverter(Block.class, Inventory.class, b -> {
			if (b.getState() instanceof InventoryHolder)
				return ((InventoryHolder) b.getState()).getInventory();
			return null;
		}, Commands.CONVERTER_NO_COMMAND_ARGUMENTS);
		
		// Entity - Inventory
		Converters.registerConverter(Entity.class, Inventory.class, e -> {
			if (e instanceof InventoryHolder)
				return ((InventoryHolder) e).getInventory();
			return null;
		}, Commands.CONVERTER_NO_COMMAND_ARGUMENTS);
		
		// Block - ItemType
		Converters.registerConverter(Block.class, ItemType.class, ItemType::new, Converter.NO_LEFT_CHAINING | Commands.CONVERTER_NO_COMMAND_ARGUMENTS);

		// Block - Location
		Converters.registerConverter(Block.class, Location.class, BlockUtils::getLocation, Commands.CONVERTER_NO_COMMAND_ARGUMENTS);
		
		// Entity - Location
		Converters.registerConverter(Entity.class, Location.class, Entity::getLocation, Commands.CONVERTER_NO_COMMAND_ARGUMENTS);

		// Entity - EntityData
		Converters.registerConverter(Entity.class, EntityData.class, EntityData::fromEntity, Commands.CONVERTER_NO_COMMAND_ARGUMENTS | Converter.NO_RIGHT_CHAINING);

		// EntityData - EntityType
		Converters.registerConverter(EntityData.class, EntityType.class, data -> new EntityType(data, -1));
		
		// ItemType - ItemStack
		Converters.registerConverter(ItemType.class, ItemStack.class, ItemType::getRandom);
		Converters.registerConverter(ItemStack.class, ItemType.class, ItemType::new);
		
		// Experience - XpOrbData
		Converters.registerConverter(Experience.class, XpOrbData.class, e -> new XpOrbData(e.getXP()));
		Converters.registerConverter(XpOrbData.class, Experience.class, e -> new Experience(e.getExperience()));
		
		// Slot - ItemType
		Converters.registerConverter(Slot.class, ItemType.class, s -> {
			ItemStack i = s.getItem();
			return new ItemType(i != null ? i : new ItemStack(Material.AIR, 1));
		});
		
		// Block - InventoryHolder
		Converters.registerConverter(Block.class, InventoryHolder.class, b -> {
			BlockState s = b.getState();
			if (s instanceof InventoryHolder)
				return (InventoryHolder) s;
			return null;
		}, Converter.NO_RIGHT_CHAINING | Commands.CONVERTER_NO_COMMAND_ARGUMENTS);
		Converters.registerConverter(InventoryHolder.class, Block.class, holder -> {
			if (holder instanceof BlockState)
				return new BlockInventoryHolder((BlockState) holder);
			if (holder instanceof DoubleChest)
				return holder.getInventory().getLocation().getBlock();
			return null;
		}, Converter.NO_CHAINING);

		// InventoryHolder - Entity
		Converters.registerConverter(InventoryHolder.class, Entity.class, holder -> {
			if (holder instanceof Entity)
				return (Entity) holder;
			return null;
		}, Converter.NO_CHAINING);
		
		// Enchantment - EnchantmentType
		Converters.registerConverter(Enchantment.class, EnchantmentType.class, e -> new EnchantmentType(e, -1));

		// Vector - Direction
		Converters.registerConverter(Vector.class, Direction.class, Direction::new);

		// EnchantmentOffer - EnchantmentType
		Converters.registerConverter(EnchantmentOffer.class, EnchantmentType.class, eo -> new EnchantmentType(eo.getEnchantment(), eo.getEnchantmentLevel()));

		Converters.registerConverter(String.class, World.class, Bukkit::getWorld);

//		// Entity - String (UUID) // Very slow, thus disabled for now
//		Converters.registerConverter(String.class, Entity.class, new Converter<String, Entity>() {
//
//			@Override
//			@Nullable
//			public Entity convert(String f) {
//				Collection<? extends Player> players = PlayerUtils.getOnlinePlayers();
//				for (Player p : players) {
//					if (p.getName().equals(f) || p.getUniqueId().toString().equals(f))
//						return p;
//				}
//
//				return null;
//			}
//
//		});

		// Number - Vector; DISABLED due to performance problems
//		Converters.registerConverter(Number.class, Vector.class, new Converter<Number, Vector>() {
//			@Override
//			@Nullable
//			public Vector convert(Number number) {
//				return new Vector(number.doubleValue(), number.doubleValue(), number.doubleValue());
//			}
//		});

//		// World - Time
//		Skript.registerConverter(World.class, Time.class, new Converter<World, Time>() {
//			@Override
//			public Time convert(final World w) {
//				if (w == null)
//					return null;
//				return new Time((int) w.getTime());
//			}
//		});

//		// Slot - Inventory
//		Skript.addConverter(Slot.class, Inventory.class, new Converter<Slot, Inventory>() {
//			@Override
//			public Inventory convert(final Slot s) {
//				if (s == null)
//					return null;
//				return s.getInventory();
//			}
//		});

//		// Item - ItemStack
//		Converters.registerConverter(Item.class, ItemStack.class, new Converter<Item, ItemStack>() {
//			@Override
//			public ItemStack convert(final Item i) {
//				return i.getItemStack();
//			}
//		});

		// Location - World
//		Skript.registerConverter(Location.class, World.class, new Converter<Location, World>() {
//			private final static long serialVersionUID = 3270661123492313649L;
//
//			@Override
//			public World convert(final Location l) {
//				if (l == null)
//					return null;
//				return l.getWorld();
//			}
//		});

		// Location - Block
//		Converters.registerConverter(Location.class, Block.class, new Converter<Location, Block>() {
//			@Override
//			public Block convert(final Location l) {
//				return l.getBlock();
//			}
//		});

	}

}
