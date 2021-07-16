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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.BlockInventoryHolder;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.slot.Slot;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("rawtypes")
public class DefaultConverters {
	
	public DefaultConverters() {}
	
	static {
		// Integer - Long
		Converters.registerConverter(Integer.class, Long.class, Integer::longValue);

		// OfflinePlayer - PlayerInventory
		Converters.registerConverter(OfflinePlayer.class, PlayerInventory.class, new Converter<OfflinePlayer, PlayerInventory>() {
			@Override
			@Nullable
			public PlayerInventory convert(final OfflinePlayer p) {
				if (!p.isOnline())
					return null;
				Player online = p.getPlayer();
				assert online != null;
				return online.getInventory();
			}
		}, Converter.NO_COMMAND_ARGUMENTS);
		// OfflinePlayer - Player
		Converters.registerConverter(OfflinePlayer.class, Player.class, new Converter<OfflinePlayer, Player>() {
			@Override
			@Nullable
			public Player convert(final OfflinePlayer p) {
				return p.getPlayer();
			}
		}, Converter.NO_COMMAND_ARGUMENTS);
		
		// TODO improve handling of interfaces
		// CommandSender - Player
		Converters.registerConverter(CommandSender.class, Player.class, new Converter<CommandSender, Player>() {
			@Override
			@Nullable
			public Player convert(final CommandSender s) {
				if (s instanceof Player)
					return (Player) s;
				return null;
			}
		});
		// BlockCommandSender - Block
		Converters.registerConverter(BlockCommandSender.class, Block.class, new Converter<BlockCommandSender, Block>() {
			@Override
			@Nullable
			public Block convert(final BlockCommandSender s) {
				return s.getBlock();
			}
		});
		// Entity - Player
		Converters.registerConverter(Entity.class, Player.class, new Converter<Entity, Player>() {
			@Override
			@Nullable
			public Player convert(final Entity e) {
				if (e instanceof Player)
					return (Player) e;
				return null;
			}
		});
		// Entity - LivingEntity // Entity->Player is used if this doesn't exist
		Converters.registerConverter(Entity.class, LivingEntity.class, new Converter<Entity, LivingEntity>() {
			@Override
			@Nullable
			public LivingEntity convert(final Entity e) {
				if (e instanceof LivingEntity)
					return (LivingEntity) e;
				return null;
			}
		});
		
		// Block - Inventory
		Converters.registerConverter(Block.class, Inventory.class, new Converter<Block, Inventory>() {
			@Override
			@Nullable
			public Inventory convert(final Block b) {
				if (b.getState() instanceof InventoryHolder)
					return ((InventoryHolder) b.getState()).getInventory();
				return null;
			}
		}, Converter.NO_COMMAND_ARGUMENTS);
		
		// Entity - Inventory
		Converters.registerConverter(Entity.class, Inventory.class, new Converter<Entity, Inventory>() {
			@Override
			@Nullable
			public Inventory convert(final Entity e) {
				if (e instanceof InventoryHolder)
					return ((InventoryHolder) e).getInventory();
				return null;
			}
		}, Converter.NO_COMMAND_ARGUMENTS);
		
		// Block - ItemType
		Converters.registerConverter(Block.class, ItemType.class, new Converter<Block, ItemType>() {
			@Override
			public ItemType convert(final Block b) {
				return new ItemType(b);
			}
		}, Converter.NO_LEFT_CHAINING | Converter.NO_COMMAND_ARGUMENTS);
		
		// Location - Block
//		Converters.registerConverter(Location.class, Block.class, new Converter<Location, Block>() {
//			@Override
//			public Block convert(final Location l) {
//				return l.getBlock();
//			}
//		});
		Converters.registerConverter(Block.class, Location.class, new Converter<Block, Location>() {
			@Override
			@Nullable
			public Location convert(final Block b) {
				return BlockUtils.getLocation(b);
			}
		}, Converter.NO_COMMAND_ARGUMENTS);
		
		// Entity - Location
		Converters.registerConverter(Entity.class, Location.class, new Converter<Entity, Location>() {
			@Override
			@Nullable
			public Location convert(final Entity e) {
				return e.getLocation();
			}
		}, Converter.NO_COMMAND_ARGUMENTS);
		// Entity - EntityData
		Converters.registerConverter(Entity.class, EntityData.class, new Converter<Entity, EntityData>() {
			@Override
			public EntityData convert(final Entity e) {
				return EntityData.fromEntity(e);
			}
		}, Converter.NO_COMMAND_ARGUMENTS | Converter.NO_RIGHT_CHAINING);
		// EntityData - EntityType
		Converters.registerConverter(EntityData.class, EntityType.class, new Converter<EntityData, EntityType>() {
			@Override
			public EntityType convert(final EntityData data) {
				return new EntityType(data, -1);
			}
		});
		
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
		
		// ItemType - ItemStack
		Converters.registerConverter(ItemType.class, ItemStack.class, new Converter<ItemType, ItemStack>() {
			@Override
			@Nullable
			public ItemStack convert(final ItemType i) {
				return i.getRandom();
			}
		});
		Converters.registerConverter(ItemStack.class, ItemType.class, new Converter<ItemStack, ItemType>() {
			@Override
			public ItemType convert(final ItemStack i) {
				return new ItemType(i);
			}
		});
		
		// Experience - XpOrbData
		Converters.registerConverter(Experience.class, XpOrbData.class, new Converter<Experience, XpOrbData>() {
			@Override
			public XpOrbData convert(final Experience e) {
				return new XpOrbData(e.getXP());
			}
		});
		Converters.registerConverter(XpOrbData.class, Experience.class, new Converter<XpOrbData, Experience>() {
			@Override
			public Experience convert(final XpOrbData e) {
				return new Experience(e.getExperience());
			}
		});
		
//		// Item - ItemStack
//		Converters.registerConverter(Item.class, ItemStack.class, new Converter<Item, ItemStack>() {
//			@Override
//			public ItemStack convert(final Item i) {
//				return i.getItemStack();
//			}
//		});
		
		// Slot - ItemType
		Converters.registerConverter(Slot.class, ItemType.class, new Converter<Slot, ItemType>() {
			@Override
			public ItemType convert(final Slot s) {
				final ItemStack i = s.getItem();
				return new ItemType(i != null ? i : new ItemStack(Material.AIR, 1));
			}
		});
//		// Slot - Inventory
//		Skript.addConverter(Slot.class, Inventory.class, new Converter<Slot, Inventory>() {
//			@Override
//			public Inventory convert(final Slot s) {
//				if (s == null)
//					return null;
//				return s.getInventory();
//			}
//		});
		
		// Block - InventoryHolder
		Converters.registerConverter(Block.class, InventoryHolder.class, new Converter<Block, InventoryHolder>() {
			@Override
			@Nullable
			public InventoryHolder convert(final Block b) {
				final BlockState s = b.getState();
				if (s instanceof InventoryHolder)
					return (InventoryHolder) s;
				return null;
			}
		}, Converter.NO_RIGHT_CHAINING | Converter.NO_COMMAND_ARGUMENTS);
		
		Converters.registerConverter(InventoryHolder.class, Block.class, new Converter<InventoryHolder, Block>() {
			@Override
			@Nullable
			public Block convert(final InventoryHolder holder) {
				if (holder instanceof BlockState)
					return new BlockInventoryHolder((BlockState) holder);
				return null;
			}
		});
		
		Converters.registerConverter(InventoryHolder.class, Entity.class, new Converter<InventoryHolder, Entity>() {
			@Override
			@Nullable
			public Entity convert(InventoryHolder holder) {
				if (holder instanceof Entity)
					return (Entity) holder;
				return null;
			}
		});
		
//		// World - Time
//		Skript.registerConverter(World.class, Time.class, new Converter<World, Time>() {
//			@Override
//			public Time convert(final World w) {
//				if (w == null)
//					return null;
//				return new Time((int) w.getTime());
//			}
//		});
		
		// Enchantment - EnchantmentType
		Converters.registerConverter(Enchantment.class, EnchantmentType.class, new Converter<Enchantment, EnchantmentType>() {
			@Override
			public EnchantmentType convert(final Enchantment e) {
				return new EnchantmentType(e, -1);
			}
		});
		
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

		// Vector - Direction
		Converters.registerConverter(Vector.class, Direction.class, new Converter<Vector, Direction>() {
			@Override
			@Nullable
			public Direction convert(Vector vector) {
				return new Direction(vector);
			}
		});
		
		// EnchantmentOffer Converters
		if (Skript.isRunningMinecraft(1, 11)) {
			// EnchantmentOffer - EnchantmentType
			Converters.registerConverter(EnchantmentOffer.class, EnchantmentType.class, new Converter<EnchantmentOffer, EnchantmentType>() {
				@Nullable
				@Override
				public EnchantmentType convert(EnchantmentOffer eo) {
					return new EnchantmentType(eo.getEnchantment(), eo.getEnchantmentLevel());
				}
			});
		}
	}
}
