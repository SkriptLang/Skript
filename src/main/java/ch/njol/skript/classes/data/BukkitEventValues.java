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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.events.EvtMoveOn;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.events.bukkit.SkriptStartEvent;
import ch.njol.skript.events.bukkit.SkriptStopEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.DelayedChangeBlock;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.FireworkEffect;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public final class BukkitEventValues {
	
	public BukkitEventValues() {}

	private static final ItemStack AIR_IS = new ItemStack(Material.AIR);

	static {
		
		// === WorldEvents ===
		EventValues.registerEventValue(WorldEvent.class, World.class, new Getter<World, WorldEvent>() {
			@Override
			@Nullable
			public World get(final WorldEvent e) {
				return e.getWorld();
			}
		}, 0);
		// StructureGrowEvent - a WorldEvent
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, new Getter<Block, StructureGrowEvent>() {
			@Override
			@Nullable
			public Block get(final StructureGrowEvent e) {
				return e.getLocation().getBlock();
			}
		}, 0);
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, new Getter<Block, StructureGrowEvent>() {
			@Override
			@Nullable
			public Block get(final StructureGrowEvent e) {
				for (final BlockState bs : e.getBlocks()) {
					if (bs.getLocation().equals(e.getLocation()))
						return new BlockStateBlock(bs);
				}
				return e.getLocation().getBlock();
			}
		}, 1);
		// WeatherEvent - not a WorldEvent (wtf ô_Ô)
		EventValues.registerEventValue(WeatherEvent.class, World.class, new Getter<World, WeatherEvent>() {
			@Override
			@Nullable
			public World get(final WeatherEvent e) {
				return e.getWorld();
			}
		}, 0);
		// ChunkEvents
		EventValues.registerEventValue(ChunkEvent.class, Chunk.class, new Getter<Chunk, ChunkEvent>() {
			@Override
			@Nullable
			public Chunk get(final ChunkEvent e) {
				return e.getChunk();
			}
		}, 0);
		
		// === BlockEvents ===
		EventValues.registerEventValue(BlockEvent.class, Block.class, new Getter<Block, BlockEvent>() {
			@Override
			@Nullable
			public Block get(final BlockEvent e) {
				return e.getBlock();
			}
		}, 0);
		EventValues.registerEventValue(BlockEvent.class, World.class, new Getter<World, BlockEvent>() {
			@Override
			@Nullable
			public World get(final BlockEvent e) {
				return e.getBlock().getWorld();
			}
		}, 0);
		// REMIND workaround of the event's location being at the entity in block events that have an entity event value
		EventValues.registerEventValue(BlockEvent.class, Location.class, new Getter<Location, BlockEvent>() {
			@Override
			@Nullable
			public Location get(final BlockEvent e) {
				return BlockUtils.getLocation(e.getBlock());
			}
		}, 0);
		// BlockPlaceEvent
		EventValues.registerEventValue(BlockPlaceEvent.class, Player.class, new Getter<Player, BlockPlaceEvent>() {
			@Override
			@Nullable
			public Player get(final BlockPlaceEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(BlockPlaceEvent.class, Block.class, new Getter<Block, BlockPlaceEvent>() {
			@Override
			public Block get(final BlockPlaceEvent e) {
				return new BlockStateBlock(e.getBlockReplacedState());
			}
		}, -1);
		EventValues.registerEventValue(BlockPlaceEvent.class, Direction.class, new Getter<Direction, BlockPlaceEvent>() {
			@Override
			@Nullable
			public Direction get(final BlockPlaceEvent e) {
				BlockFace bf = e.getBlockPlaced().getFace(e.getBlockAgainst());
				if (bf != null) {
					return new Direction(new double[] {bf.getModX(), bf.getModY(), bf.getModZ()});
				}
				return Direction.ZERO;
			}
		}, 0);
		// BlockFadeEvent
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new Getter<Block, BlockFadeEvent>() {
			@Override
			@Nullable
			public Block get(final BlockFadeEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new Getter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return new DelayedChangeBlock(e.getBlock(), e.getNewState());
			}
		}, 0);
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new Getter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return new BlockStateBlock(e.getNewState());
			}
		}, 1);
		// BlockGrowEvent (+ BlockFormEvent)
		EventValues.registerEventValue(BlockGrowEvent.class, Block.class, new Getter<Block, BlockGrowEvent>() {
			@Override
			@Nullable
			public Block get(final BlockGrowEvent e) {
				if (e instanceof BlockSpreadEvent)
					return e.getBlock();
				return new BlockStateBlock(e.getNewState());
			}
		}, 0);
		EventValues.registerEventValue(BlockGrowEvent.class, Block.class, new Getter<Block, BlockGrowEvent>() {
			@Override
			@Nullable
			public Block get(final BlockGrowEvent e) {
				return e.getBlock();
			}
		}, -1);
		// BlockDamageEvent
		EventValues.registerEventValue(BlockDamageEvent.class, Player.class, new Getter<Player, BlockDamageEvent>() {
			@Override
			@Nullable
			public Player get(final BlockDamageEvent e) {
				return e.getPlayer();
			}
		}, 0);
		// BlockBreakEvent
		EventValues.registerEventValue(BlockBreakEvent.class, Player.class, new Getter<Player, BlockBreakEvent>() {
			@Override
			@Nullable
			public Player get(final BlockBreakEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			@Nullable
			public Block get(final BlockBreakEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				return new DelayedChangeBlock(e.getBlock());
			}
		}, 0);
		ItemType stationaryWater = Aliases.javaItemType("stationary water");
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				final BlockState s = e.getBlock().getState();
				s.setType(s.getType() == Material.ICE ? stationaryWater.getMaterial() : Material.AIR);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		// BlockFromToEvent
		EventValues.registerEventValue(BlockFromToEvent.class, Block.class, new Getter<Block, BlockFromToEvent>() {
			@Override
			@Nullable
			public Block get(final BlockFromToEvent e) {
				return e.getToBlock();
			}
		}, 1);
		// BlockIgniteEvent
		EventValues.registerEventValue(BlockIgniteEvent.class, Player.class, new Getter<Player, BlockIgniteEvent>() {
			@Override
			@Nullable
			public Player get(final BlockIgniteEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(BlockIgniteEvent.class, Block.class, new Getter<Block, BlockIgniteEvent>() {
			@Override
			@Nullable
			public Block get(final BlockIgniteEvent e) {
				return e.getIgnitingBlock();
			}
		}, 0);
		// BlockDispenseEvent
		EventValues.registerEventValue(BlockDispenseEvent.class, ItemStack.class, new Getter<ItemStack, BlockDispenseEvent>() {
			@Override
			@Nullable
			public ItemStack get(final BlockDispenseEvent e) {
				return e.getItem();
			}
		}, 0);
		// BlockCanBuildEvent
		EventValues.registerEventValue(BlockCanBuildEvent.class, Block.class, new Getter<Block, BlockCanBuildEvent>() {
			@Override
			@Nullable
			public Block get(final BlockCanBuildEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockCanBuildEvent.class, Block.class, new Getter<Block, BlockCanBuildEvent>() {
			@Override
			public Block get(final BlockCanBuildEvent e) {
				final BlockState s = e.getBlock().getState();
				s.setType(e.getMaterial());
				return new BlockStateBlock(s, true);
			}
		}, 0);
		// BlockCanBuildEvent#getPlayer was added in 1.13
		if (Skript.methodExists(BlockCanBuildEvent.class, "getPlayer")) {
			EventValues.registerEventValue(BlockCanBuildEvent.class, Player.class, new Getter<Player, BlockCanBuildEvent>() {
				@Override
				@Nullable
				public Player get(final BlockCanBuildEvent e) {
					return e.getPlayer();
				}
			}, 0);
		}
		// SignChangeEvent
		EventValues.registerEventValue(SignChangeEvent.class, Player.class, new Getter<Player, SignChangeEvent>() {
			@Override
			@Nullable
			public Player get(final SignChangeEvent e) {
				return e.getPlayer();
			}
		}, 0);
		
		// === EntityEvents ===
		EventValues.registerEventValue(EntityEvent.class, Entity.class, new Getter<Entity, EntityEvent>() {
			@Override
			@Nullable
			public Entity get(final EntityEvent e) {
				return e.getEntity();
			}
		}, 0, "Use 'attacker' and/or 'victim' in damage/death events", EntityDamageEvent.class, EntityDeathEvent.class);
		EventValues.registerEventValue(EntityEvent.class, CommandSender.class, new Getter<CommandSender, EntityEvent>() {
			@Override
			@Nullable
			public CommandSender get(final EntityEvent e) {
				return e.getEntity();
			}
		}, 0, "Use 'attacker' and/or 'victim' in damage/death events", EntityDamageEvent.class, EntityDeathEvent.class);
		EventValues.registerEventValue(EntityEvent.class, World.class, new Getter<World, EntityEvent>() {
			@Override
			@Nullable
			public World get(final EntityEvent e) {
				return e.getEntity().getWorld();
			}
		}, 0);
		EventValues.registerEventValue(EntityEvent.class, Location.class, new Getter<Location, EntityEvent>() {
			@Override
			@Nullable
			public Location get(final EntityEvent e) {
				return e.getEntity().getLocation();
			}
		}, 0);
		// EntityDamageEvent
		EventValues.registerEventValue(EntityDamageEvent.class, DamageCause.class, new Getter<DamageCause, EntityDamageEvent>() {
			@Override
			@Nullable
			public DamageCause get(final EntityDamageEvent e) {
				return e.getCause();
			}
		}, 0);
		EventValues.registerEventValue(EntityDamageByEntityEvent.class, Projectile.class, new Getter<Projectile, EntityDamageByEntityEvent>() {
			@Override
			@Nullable
			public Projectile get(final EntityDamageByEntityEvent e) {
				if (e.getDamager() instanceof Projectile)
					return (Projectile) e.getDamager();
				return null;
			}
		}, 0);
		// EntityDeathEvent
		EventValues.registerEventValue(EntityDeathEvent.class, Projectile.class, new Getter<Projectile, EntityDeathEvent>() {
			@Override
			@Nullable
			public Projectile get(final EntityDeathEvent e) {
				final EntityDamageEvent ldc = e.getEntity().getLastDamageCause();
				if (ldc instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) ldc).getDamager() instanceof Projectile)
					return (Projectile) ((EntityDamageByEntityEvent) ldc).getDamager();
				return null;
			}
		}, 0);
		EventValues.registerEventValue(EntityDeathEvent.class, DamageCause.class, new Getter<DamageCause, EntityDeathEvent>() {
			@Override
			@Nullable
			public DamageCause get(final EntityDeathEvent e) {
				final EntityDamageEvent ldc = e.getEntity().getLastDamageCause();
				return ldc == null ? null : ldc.getCause();
			}
		}, 0);
		// ProjectileHitEvent
		// ProjectileHitEvent#getHitBlock was added in 1.11
		if(Skript.methodExists(ProjectileHitEvent.class, "getHitBlock"))
			EventValues.registerEventValue(ProjectileHitEvent.class, Block.class, new Getter<Block, ProjectileHitEvent>() {
				@Nullable
				@Override
				public Block get(ProjectileHitEvent e) {
					return e.getHitBlock();
				}
			}, 0);
		EventValues.registerEventValue(ProjectileHitEvent.class, Entity.class, new Getter<Entity, ProjectileHitEvent>() {
			@Override
			@Nullable
			public Entity get(final ProjectileHitEvent e) {
				assert false;
				return e.getEntity();
			}
		}, 0, "Use 'projectile' and/or 'shooter' in projectile hit events", ProjectileHitEvent.class);
		EventValues.registerEventValue(ProjectileHitEvent.class, Projectile.class, new Getter<Projectile, ProjectileHitEvent>() {
			@Override
			@Nullable
			public Projectile get(final ProjectileHitEvent e) {
				return e.getEntity();
			}
		}, 0);
		if (Skript.methodExists(ProjectileHitEvent.class, "getHitBlockFace")) {
			EventValues.registerEventValue(ProjectileHitEvent.class, Direction.class, new Getter<Direction, ProjectileHitEvent>() {
				@Override
				@Nullable
				public Direction get(final ProjectileHitEvent e) {
					BlockFace theHitFace = e.getHitBlockFace();
					if (theHitFace == null) return null;
					return new Direction(theHitFace, 1);
				}
			}, 0);
		}
		// ProjectileLaunchEvent
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Entity.class, new Getter<Entity, ProjectileLaunchEvent>() {
			@Override
			@Nullable
			public Entity get(final ProjectileLaunchEvent e) {
				assert false;
				return e.getEntity();
			}
		}, 0, "Use 'projectile' and/or 'shooter' in shoot events", ProjectileLaunchEvent.class);
		//ProjectileCollideEvent
		if (Skript.classExists("com.destroystokyo.paper.event.entity.ProjectileCollideEvent")) {
			EventValues.registerEventValue(ProjectileCollideEvent.class, Projectile.class, new Getter<Projectile, ProjectileCollideEvent>() {
				@Nullable
				@Override
				public Projectile get(ProjectileCollideEvent evt) {
					return evt.getEntity();
				}
			}, 0);
			EventValues.registerEventValue(ProjectileCollideEvent.class, Entity.class, new Getter<Entity, ProjectileCollideEvent>() {
				@Nullable
				@Override
				public Entity get(ProjectileCollideEvent evt) {
					return evt.getCollidedWith();
				}
			}, 0);
		}
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Projectile.class, new Getter<Projectile, ProjectileLaunchEvent>() {
			@Override
			@Nullable
			public Projectile get(final ProjectileLaunchEvent e) {
				return e.getEntity();
			}
		}, 0);
		// EntityTameEvent
		EventValues.registerEventValue(EntityTameEvent.class, Entity.class, new Getter<Entity, EntityTameEvent>() {
			@Override
			@Nullable
			public Entity get(final EntityTameEvent e) {
				return e.getEntity();
			}
		}, 0);
		// EntityChangeBlockEvent
		EventValues.registerEventValue(EntityChangeBlockEvent.class, Block.class, new Getter<Block, EntityChangeBlockEvent>() {
			@Override
			@Nullable
			public Block get(final EntityChangeBlockEvent e) {
				return e.getBlock();
			}
		}, 0);
		EventValues.registerEventValue(AreaEffectCloudApplyEvent.class, PotionEffectType.class, new Getter<PotionEffectType, AreaEffectCloudApplyEvent>() {
			@Override
			@Nullable
			public PotionEffectType get(AreaEffectCloudApplyEvent e) {
				return e.getEntity().getBasePotionData().getType().getEffectType(); // Whoops this is a bit long call...
			}
		}, 0);
		// ItemSpawnEvent
		EventValues.registerEventValue(ItemSpawnEvent.class, ItemStack.class, new Getter<ItemStack, ItemSpawnEvent>() {
			@Override
			@Nullable
			public ItemStack get(final ItemSpawnEvent e) {
				return e.getEntity().getItemStack();
			}
		}, 0);
		// LightningStrikeEvent
		EventValues.registerEventValue(LightningStrikeEvent.class, Entity.class, new Getter<Entity, LightningStrikeEvent>() {
			@Override
			public Entity get(LightningStrikeEvent event) {
				return event.getLightning();
			}
		}, 0);
		
		// --- PlayerEvents ---
		EventValues.registerEventValue(PlayerEvent.class, Player.class, new Getter<Player, PlayerEvent>() {
			@Override
			@Nullable
			public Player get(final PlayerEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(PlayerEvent.class, World.class, new Getter<World, PlayerEvent>() {
			@Override
			@Nullable
			public World get(final PlayerEvent e) {
				return e.getPlayer().getWorld();
			}
		}, 0);
		// PlayerBedEnterEvent
		EventValues.registerEventValue(PlayerBedEnterEvent.class, Block.class, new Getter<Block, PlayerBedEnterEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBedEnterEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBedLeaveEvent
		EventValues.registerEventValue(PlayerBedLeaveEvent.class, Block.class, new Getter<Block, PlayerBedLeaveEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBedLeaveEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBucketEvents
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, new Getter<Block, PlayerBucketFillEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBucketFillEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, 0);
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, new Getter<Block, PlayerBucketFillEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBucketFillEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setType(Material.AIR);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new Getter<Block, PlayerBucketEmptyEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBucketEmptyEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, -1);
		ItemType stationaryLava = Aliases.javaItemType("stationary lava");
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new Getter<Block, PlayerBucketEmptyEvent>() {
			@Override
			public Block get(final PlayerBucketEmptyEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setType(e.getBucket() == Material.WATER_BUCKET ? stationaryWater.getMaterial() : stationaryLava.getMaterial());
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 0);
		// PlayerDropItemEvent
		EventValues.registerEventValue(PlayerDropItemEvent.class, Player.class, new Getter<Player, PlayerDropItemEvent>() {
			@Override
			@Nullable
			public Player get(PlayerDropItemEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(PlayerDropItemEvent.class, Item.class, new Getter<Item, PlayerDropItemEvent>() {
			@Override
			@Nullable
			public Item get(final PlayerDropItemEvent e) {
				return e.getItemDrop();
			}
		}, 0);
		EventValues.registerEventValue(PlayerDropItemEvent.class, ItemStack.class, new Getter<ItemStack, PlayerDropItemEvent>() {
			@Override
			@Nullable
			public ItemStack get(final PlayerDropItemEvent e) {
				return e.getItemDrop().getItemStack();
			}
		}, 0);
		// PlayerPickupItemEvent
		EventValues.registerEventValue(PlayerPickupItemEvent.class, Player.class, new Getter<Player, PlayerPickupItemEvent>() {
			@Override
			@Nullable
			public Player get(PlayerPickupItemEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(PlayerPickupItemEvent.class, Item.class, new Getter<Item, PlayerPickupItemEvent>() {
			@Override
			@Nullable
			public Item get(final PlayerPickupItemEvent e) {
				return e.getItem();
			}
		}, 0);
		EventValues.registerEventValue(PlayerPickupItemEvent.class, ItemStack.class, new Getter<ItemStack, PlayerPickupItemEvent>() {
			@Override
			@Nullable
			public ItemStack get(final PlayerPickupItemEvent e) {
				return e.getItem().getItemStack();
			}
		}, 0);
		// EntityPickupItemEvent
		EventValues.registerEventValue(EntityPickupItemEvent.class, Entity.class, new Getter<Entity, EntityPickupItemEvent>() {
			@Override
			public @Nullable Entity get(EntityPickupItemEvent e) {
				return e.getEntity();
			}
		}, 0);
		EventValues.registerEventValue(EntityPickupItemEvent.class, Item.class, new Getter<Item, EntityPickupItemEvent>() {
			@Override
			@Nullable
			public Item get(final EntityPickupItemEvent e) {
				return e.getItem();
			}
		}, 0);
		EventValues.registerEventValue(EntityPickupItemEvent.class, ItemType.class, new Getter<ItemType, EntityPickupItemEvent>() {
			@Override
			@Nullable
			public ItemType get(final EntityPickupItemEvent e) {
				return new ItemType(e.getItem().getItemStack());
			}
		}, 0);
		// PlayerItemConsumeEvent
		EventValues.registerEventValue(PlayerItemConsumeEvent.class, ItemStack.class, new Getter<ItemStack, PlayerItemConsumeEvent>() {
			@Override
			@Nullable
			public ItemStack get(final PlayerItemConsumeEvent e) {
				return e.getItem();
			}
		}, 0);
		// PlayerItemBreakEvent
		EventValues.registerEventValue(PlayerItemBreakEvent.class, ItemStack.class, new Getter<ItemStack, PlayerItemBreakEvent>() {
			@Override
			@Nullable
			public ItemStack get(final PlayerItemBreakEvent e) {
				return e.getBrokenItem();
			}
		}, 0);
		// PlayerInteractEntityEvent
		EventValues.registerEventValue(PlayerInteractEntityEvent.class, Entity.class, new Getter<Entity, PlayerInteractEntityEvent>() {
			@Override
			@Nullable
			public Entity get(final PlayerInteractEntityEvent e) {
				return e.getRightClicked();
			}
		}, 0);
		EventValues.registerEventValue(PlayerInteractEntityEvent.class, ItemStack.class, new Getter<ItemStack, PlayerInteractEntityEvent>() {
			@Override
			@Nullable
			public ItemStack get(final PlayerInteractEntityEvent e) {
        EquipmentSlot hand = e.getHand();
        if (hand == EquipmentSlot.HAND)
          return e.getPlayer().getInventory().getItemInMainHand();
        else if (hand == EquipmentSlot.OFF_HAND)
          return e.getPlayer().getInventory().getItemInOffHand();
        else
          return null;
			}
		}, 0);
		// PlayerInteractEvent
		EventValues.registerEventValue(PlayerInteractEvent.class, ItemStack.class, new Getter<ItemStack, PlayerInteractEvent>() {
			@Override
			@Nullable
			public ItemStack get(final PlayerInteractEvent e) {
				return e.getItem();
			}
		}, 0);
		EventValues.registerEventValue(PlayerInteractEvent.class, Block.class, new Getter<Block, PlayerInteractEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerInteractEvent e) {
				return e.getClickedBlock();
			}
		}, 0);
		EventValues.registerEventValue(PlayerInteractEvent.class, Direction.class, new Getter<Direction, PlayerInteractEvent>() {
			@Override
			@Nullable
			public Direction get(final PlayerInteractEvent e) {
				return new Direction(new double[] {e.getBlockFace().getModX(), e.getBlockFace().getModY(), e.getBlockFace().getModZ()});
			}
		}, 0);
		// PlayerShearEntityEvent
		EventValues.registerEventValue(PlayerShearEntityEvent.class, Entity.class, new Getter<Entity, PlayerShearEntityEvent>() {
			@Override
			@Nullable
			public Entity get(final PlayerShearEntityEvent e) {
				return e.getEntity();
			}
		}, 0);
		// PlayerMoveEvent
		EventValues.registerEventValue(PlayerMoveEvent.class, Block.class, new Getter<Block, PlayerMoveEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerMoveEvent e) {
				return EvtMoveOn.getBlock(e);
			}
		}, 0);
		// PlayerItemDamageEvent
		EventValues.registerEventValue(PlayerItemDamageEvent.class, ItemStack.class, new Getter<ItemStack, PlayerItemDamageEvent>() {
			@Override
			public ItemStack get(PlayerItemDamageEvent event) {
				return event.getItem();
			}
		}, 0);
		//PlayerItemMendEvent
    EventValues.registerEventValue(PlayerItemMendEvent.class, Player.class, new Getter<Player, PlayerItemMendEvent>() {
      @Override
      @Nullable
      public Player get(PlayerItemMendEvent e) {
        return e.getPlayer();
      }
    }, 0);
    EventValues.registerEventValue(PlayerItemMendEvent.class, ItemStack.class, new Getter<ItemStack, PlayerItemMendEvent>() {
      @Override
      @Nullable
      public ItemStack get(PlayerItemMendEvent e) {
        return e.getItem();
      }
    }, 0);
    EventValues.registerEventValue(PlayerItemMendEvent.class, Entity.class, new Getter<Entity, PlayerItemMendEvent>() {
      @Override
      @Nullable
      public Entity get(PlayerItemMendEvent e) {
        return e.getExperienceOrb();
      }
    }, 0);

		// --- HangingEvents ---

		// Note: will not work in HangingEntityBreakEvent due to event-entity being parsed as HangingBreakByEntityEvent#getRemover() from code down below
		EventValues.registerEventValue(HangingEvent.class, Hanging.class, new Getter<Hanging, HangingEvent>() {
			@Override
			@Nullable
			public Hanging get(final HangingEvent e) {
				return e.getEntity();
			}
		}, 0);
		EventValues.registerEventValue(HangingEvent.class, World.class, new Getter<World, HangingEvent>() {
			@Override
			@Nullable
			public World get(final HangingEvent e) {
				return e.getEntity().getWorld();
			}
		}, 0);
		EventValues.registerEventValue(HangingEvent.class, Location.class, new Getter<Location, HangingEvent>() {
			@Override
			@Nullable
			public Location get(final HangingEvent e) {
				return e.getEntity().getLocation();
			}
		}, 0);
			
		// HangingBreakEvent
		EventValues.registerEventValue(HangingBreakEvent.class, Entity.class, new Getter<Entity, HangingBreakEvent>() {
			@Nullable
			@Override
			public Entity get(HangingBreakEvent e) {
				if (e instanceof HangingBreakByEntityEvent)
					return ((HangingBreakByEntityEvent) e).getRemover();
				return null;
			}
		}, 0);
		// HangingPlaceEvent
		EventValues.registerEventValue(HangingPlaceEvent.class, Player.class, new Getter<Player, HangingPlaceEvent>() {
			@Override
			@Nullable
			public Player get(final HangingPlaceEvent e) {
				return e.getPlayer();
			}
		}, 0);
		
		// --- VehicleEvents ---
		EventValues.registerEventValue(VehicleEvent.class, Vehicle.class, new Getter<Vehicle, VehicleEvent>() {
			@Override
			@Nullable
			public Vehicle get(final VehicleEvent e) {
				return e.getVehicle();
			}
		}, 0);
		EventValues.registerEventValue(VehicleEvent.class, World.class, new Getter<World, VehicleEvent>() {
			@Override
			@Nullable
			public World get(final VehicleEvent e) {
				return e.getVehicle().getWorld();
			}
		}, 0);
		EventValues.registerEventValue(VehicleExitEvent.class, LivingEntity.class, new Getter<LivingEntity, VehicleExitEvent>() {
			@Override
			@Nullable
			public LivingEntity get(final VehicleExitEvent e) {
				return e.getExited();
			}
		}, 0);
		
		EventValues.registerEventValue(VehicleEnterEvent.class, Entity.class, new Getter<Entity, VehicleEnterEvent>() {
			@Nullable
			@Override
			public Entity get(VehicleEnterEvent e) {
				return e.getEntered();
			}
		}, 0);
		
		// We could error here instead but it's preferable to not do it in this case
		EventValues.registerEventValue(VehicleDamageEvent.class, Entity.class, new Getter<Entity, VehicleDamageEvent>() {
			@Nullable
			@Override
			public Entity get(VehicleDamageEvent e) {
				return e.getAttacker();
			}
		}, 0);
		
		EventValues.registerEventValue(VehicleDestroyEvent.class, Entity.class, new Getter<Entity, VehicleDestroyEvent>() {
			@Nullable
			@Override
			public Entity get(VehicleDestroyEvent e) {
				return e.getAttacker();
			}
		}, 0);
		
		EventValues.registerEventValue(VehicleEvent.class, Entity.class, new Getter<Entity, VehicleEvent>() {
			@Override
			@Nullable
			public Entity get(final VehicleEvent e) {
				return e.getVehicle().getPassenger();
			}
		}, 0);
		
		
		// === CommandEvents ===
		// PlayerCommandPreprocessEvent is a PlayerEvent
		EventValues.registerEventValue(ServerCommandEvent.class, CommandSender.class, new Getter<CommandSender, ServerCommandEvent>() {
			@Override
			@Nullable
			public CommandSender get(final ServerCommandEvent e) {
				return e.getSender();
			}
		}, 0);
		EventValues.registerEventValue(CommandEvent.class, CommandSender.class, new Getter<CommandSender, CommandEvent>() {
			@Override
			public CommandSender get(final CommandEvent e) {
				return e.getSender();
			}
		}, 0);
		EventValues.registerEventValue(CommandEvent.class, World.class, new Getter<World, CommandEvent>() {
			@Override
			@Nullable
			public World get(final CommandEvent e) {
				return e.getSender() instanceof Player ? ((Player) e.getSender()).getWorld() : null;
			}
		}, 0);
		
		// === ServerEvents ===
		// Script load/unload event
		EventValues.registerEventValue(ScriptEvent.class, CommandSender.class, new Getter<CommandSender, ScriptEvent>() {
			@Nullable
			@Override
			public CommandSender get(ScriptEvent e) {
				return Bukkit.getConsoleSender();
			}
		}, 0);
		// Server load event
		EventValues.registerEventValue(SkriptStartEvent.class, CommandSender.class, new Getter<CommandSender, SkriptStartEvent>() {
			@Nullable
			@Override
			public CommandSender get(SkriptStartEvent e) {
				return Bukkit.getConsoleSender();
			}
		}, 0);
		// Server stop event
		EventValues.registerEventValue(SkriptStopEvent.class, CommandSender.class, new Getter<CommandSender, SkriptStopEvent>() {
			@Nullable
			@Override
			public CommandSender get(SkriptStopEvent e) {
				return Bukkit.getConsoleSender();
			}
		}, 0);
		
		// === InventoryEvents ===
		// InventoryClickEvent
		EventValues.registerEventValue(InventoryClickEvent.class, Player.class, new Getter<Player, InventoryClickEvent>() {
			@Override
			@Nullable
			public Player get(final InventoryClickEvent e) {
				return e.getWhoClicked() instanceof Player ? (Player) e.getWhoClicked() : null;
			}
		}, 0);
		EventValues.registerEventValue(InventoryClickEvent.class, World.class, new Getter<World, InventoryClickEvent>() {
			@Override
			@Nullable
			public World get(final InventoryClickEvent e) {
				return e.getWhoClicked().getWorld();
			}
		}, 0);
		EventValues.registerEventValue(InventoryClickEvent.class, ItemStack.class, new Getter<ItemStack, InventoryClickEvent>() {
			@Override
			@Nullable
			public ItemStack get(final InventoryClickEvent e) {
				if (e instanceof CraftItemEvent)
					return ((CraftItemEvent) e).getRecipe().getResult();
				return e.getCurrentItem();
			}
		}, 0);
		EventValues.registerEventValue(InventoryClickEvent.class, Slot.class, new Getter<Slot, InventoryClickEvent>() {
			@SuppressWarnings("null")
			@Override
			@Nullable
			public Slot get(final InventoryClickEvent e) {
				Inventory invi = e.getClickedInventory(); // getInventory is WRONG and dangerous
				int slotIndex = e.getSlot();
				
				// Not all indices point to inventory slots. Equipment, for example
				if (invi instanceof PlayerInventory && slotIndex >= 36) {
					return new ch.njol.skript.util.slot.EquipmentSlot(((PlayerInventory) invi).getHolder(), slotIndex);
				} else {
					return new InventorySlot(invi, slotIndex);
				}
			}
		}, 0);
		EventValues.registerEventValue(InventoryClickEvent.class, InventoryAction.class, new Getter<InventoryAction, InventoryClickEvent>() {
			@Override
			@Nullable
			public InventoryAction get(final InventoryClickEvent e) {
				return e.getAction();
			}
		}, 0);
		EventValues.registerEventValue(InventoryClickEvent.class, ClickType.class, new Getter<ClickType, InventoryClickEvent>() {
			@Override
			@Nullable
			public ClickType get(final InventoryClickEvent e) {
				return e.getClick();
			}
		}, 0);
		EventValues.registerEventValue(InventoryClickEvent.class, Inventory.class, new Getter<Inventory, InventoryClickEvent>() {
			@Override
			@Nullable
			public Inventory get(final InventoryClickEvent e) {
				return e.getClickedInventory();
			}
		}, 0);
		// PrepareAnvilEvent
		EventValues.registerEventValue(PrepareAnvilEvent.class, ItemStack.class, new Getter<ItemStack, PrepareAnvilEvent>() {
			@Override
			@Nullable
			public ItemStack get(PrepareAnvilEvent e) {
				return e.getResult();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PrepareAnvilEvent.class, Inventory.class, new Getter<Inventory, PrepareAnvilEvent>() {
			@Override
			@Nullable
			public Inventory get(PrepareAnvilEvent e) {
				return e.getInventory();
			}
		}, EventValues.TIME_NOW);
		// AnvilDamagedEvent
		if (Skript.classExists("com.destroystokyo.paper.event.block.AnvilDamagedEvent")) {
			EventValues.registerEventValue(AnvilDamagedEvent.class, Inventory.class, new Getter<Inventory, AnvilDamagedEvent>() {
				@Override
				@Nullable
				public Inventory get(AnvilDamagedEvent e) {
					return e.getInventory();
				}
			}, EventValues.TIME_NOW);
		}
		//BlockFertilizeEvent
		EventValues.registerEventValue(BlockFertilizeEvent.class, Player.class, new Getter<Player, BlockFertilizeEvent>() {
			@Nullable
			@Override
			public Player get(BlockFertilizeEvent event) {
				return event.getPlayer();
			}
		}, 0);
		// PrepareItemCraftEvent
		EventValues.registerEventValue(PrepareItemCraftEvent.class, Slot.class, new Getter<Slot, PrepareItemCraftEvent>() {
			@Override
			public Slot get(final PrepareItemCraftEvent e) {
				return new InventorySlot(e.getInventory(), 0);
			}
		}, 0);
		EventValues.registerEventValue(PrepareItemCraftEvent.class, ItemStack.class, new Getter<ItemStack, PrepareItemCraftEvent>() {
			@Override
			public ItemStack get(PrepareItemCraftEvent e) {
				ItemStack item = e.getInventory().getResult();
				return item != null ? item : AIR_IS;
			}
		}, 0);
		EventValues.registerEventValue(PrepareItemCraftEvent.class, Inventory.class, new Getter<Inventory, PrepareItemCraftEvent>() {
			@Override
			public Inventory get(PrepareItemCraftEvent e) {
				return e.getInventory();
			}
		}, 0);
		EventValues.registerEventValue(PrepareItemCraftEvent.class, Player.class, new Getter<Player, PrepareItemCraftEvent>() {
			@Override
			@Nullable
			public Player get(final PrepareItemCraftEvent e) {
				List<HumanEntity> viewers = e.getInventory().getViewers(); // Get all viewers
				if (viewers.size() == 0) // ... if we don't have any
					return null;
				HumanEntity first = viewers.get(0); // Get first viewer and hope it is crafter
				if (first instanceof Player) // Needs to be player... Usually it is
					return (Player) first;
				return null;
			}
		}, 0);
		// CraftEvents - recipe namespaced key strings
		EventValues.registerEventValue(CraftItemEvent.class, String.class, new Getter<String, CraftItemEvent>() {
			@Nullable
			@Override
			public String get(CraftItemEvent e) {
				Recipe recipe = e.getRecipe();
				if (recipe instanceof Keyed)
					return ((Keyed) recipe).getKey().toString();
				return null;
			}
		}, 0);
		EventValues.registerEventValue(PrepareItemCraftEvent.class, String.class, new Getter<String, PrepareItemCraftEvent>() {
			@Nullable
			@Override
			public String get(PrepareItemCraftEvent e) {
				Recipe recipe = e.getRecipe();
				if (recipe instanceof Keyed)
					return ((Keyed) recipe).getKey().toString();
				return null;
			}
		}, 0);
		// CraftItemEvent
		EventValues.registerEventValue(CraftItemEvent.class, ItemStack.class, new Getter<ItemStack, CraftItemEvent>() {
			@Override
			@Nullable
			public ItemStack get(CraftItemEvent e) {
				return e.getRecipe().getResult();
			}
		}, 0);
		//InventoryOpenEvent
		EventValues.registerEventValue(InventoryOpenEvent.class, Player.class, new Getter<Player, InventoryOpenEvent>() {
			@Override
			@Nullable
			public Player get(final InventoryOpenEvent e) {
				return (Player) e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(InventoryOpenEvent.class, Inventory.class, new Getter<Inventory, InventoryOpenEvent>() {
			@Override
			@Nullable
			public Inventory get(final InventoryOpenEvent e) {
				return e.getInventory();
			}
		}, 0);
		//InventoryCloseEvent
		EventValues.registerEventValue(InventoryCloseEvent.class, Player.class, new Getter<Player, InventoryCloseEvent>() {
			@Override
			@Nullable
			public Player get(final InventoryCloseEvent e) {
				return (Player) e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(InventoryCloseEvent.class, Inventory.class, new Getter<Inventory, InventoryCloseEvent>() {
			@Override
			@Nullable
			public Inventory get(final InventoryCloseEvent e) {
				return e.getInventory();
			}
		}, 0);
		//InventoryPickupItemEvent
		EventValues.registerEventValue(InventoryPickupItemEvent.class, Inventory.class, new Getter<Inventory, InventoryPickupItemEvent>() {
			@Nullable
			@Override
			public Inventory get(InventoryPickupItemEvent event) {
				return event.getInventory();
			}
		}, 0);
		EventValues.registerEventValue(InventoryPickupItemEvent.class, Item.class, new Getter<Item, InventoryPickupItemEvent>() {
			@Nullable
			@Override
			public Item get(InventoryPickupItemEvent event) {
				return event.getItem();
			}
		}, 0);
		EventValues.registerEventValue(InventoryPickupItemEvent.class, ItemStack.class, new Getter<ItemStack, InventoryPickupItemEvent>() {
			@Nullable
			@Override
			public ItemStack get(InventoryPickupItemEvent event) {
				return event.getItem().getItemStack();
			}
		}, 0);
		//PortalCreateEvent
		EventValues.registerEventValue(PortalCreateEvent.class, World.class, new Getter<World, PortalCreateEvent>() {
			@Override
			@Nullable
			public World get(final PortalCreateEvent e) {
				return e.getWorld();
			}
		}, 0);
		if (Skript.methodExists(PortalCreateEvent.class, "getEntity")) { // Minecraft 1.14+
			EventValues.registerEventValue(PortalCreateEvent.class, Entity.class, new Getter<Entity, PortalCreateEvent>() {
				@Override
				@Nullable
				public Entity get(final PortalCreateEvent e) {
					return e.getEntity();
				}
			}, 0);
		}
		//PlayerEditBookEvent
		EventValues.registerEventValue(PlayerEditBookEvent.class, ItemStack.class, new Getter<ItemStack, PlayerEditBookEvent>() {
			@Override
			public ItemStack get(PlayerEditBookEvent e) {
				ItemStack book = new ItemStack(e.getPlayer().getItemInHand().getType());
				book.setItemMeta(e.getNewBookMeta());
				return book; // TODO: Find better way to derive this event value
			}
		}, 0);
		//ItemDespawnEvent
		EventValues.registerEventValue(ItemDespawnEvent.class, Item.class, new Getter<Item, ItemDespawnEvent>() {
			@Override
			@Nullable
			public Item get(ItemDespawnEvent e) {
				return e.getEntity();
			}
		}, 0);
		EventValues.registerEventValue(ItemDespawnEvent.class, ItemStack.class, new Getter<ItemStack, ItemDespawnEvent>() {
			@Override
			@Nullable
			public ItemStack get(ItemDespawnEvent e) {
				return e.getEntity().getItemStack();
			}
		}, 0);
		//ItemMergeEvent
		EventValues.registerEventValue(ItemMergeEvent.class, Item.class, new Getter<Item, ItemMergeEvent>() {
			@Override
			@Nullable
			public Item get(ItemMergeEvent e) {
				return e.getEntity();
			}
		}, 0);
		EventValues.registerEventValue(ItemMergeEvent.class, Item.class, new Getter<Item, ItemMergeEvent>() {
			@Override
			@Nullable
			public Item get(ItemMergeEvent e) {
				return e.getTarget();
			}
		}, 1);
		EventValues.registerEventValue(ItemMergeEvent.class, ItemStack.class, new Getter<ItemStack, ItemMergeEvent>() {
			@Override
			@Nullable
			public ItemStack get(ItemMergeEvent e) {
				return e.getEntity().getItemStack();
			}
		}, 0);
		//PlayerTeleportEvent
		EventValues.registerEventValue(PlayerTeleportEvent.class, TeleportCause.class, new Getter<TeleportCause, PlayerTeleportEvent>() {
			@Override
			@Nullable
			public TeleportCause get(final PlayerTeleportEvent e) {
				return e.getCause();
			}
		}, 0);
		//PlayerMoveEvent
		EventValues.registerEventValue(PlayerMoveEvent.class, Location.class, new Getter<Location, PlayerMoveEvent>() {
			@Override
			@Nullable
			public Location get(PlayerMoveEvent e) {
				return e.getFrom();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(PlayerMoveEvent.class, Location.class, new Getter<Location, PlayerMoveEvent>() {
			@Override
			@Nullable
			public Location get(PlayerMoveEvent e) {
				return e.getTo();
			}
		}, EventValues.TIME_NOW);
		//EntityMoveEvent
		if (Skript.classExists("io.papermc.paper.event.entity.EntityMoveEvent")) {
			EventValues.registerEventValue(EntityMoveEvent.class, Location.class, new Getter<Location, EntityMoveEvent>() {
				@Override
				@Nullable
				public Location get(EntityMoveEvent e) {
					return e.getFrom();
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(EntityMoveEvent.class, Location.class, new Getter<Location, EntityMoveEvent>() {
				@Override
				@Nullable
				public Location get(EntityMoveEvent e) {
					return e.getTo();
				}
			}, EventValues.TIME_FUTURE);
		}
		//PlayerToggleFlightEvent
		EventValues.registerEventValue(PlayerToggleFlightEvent.class, Player.class, new Getter<Player, PlayerToggleFlightEvent>() {
			@Override
			@Nullable
			public Player get(PlayerToggleFlightEvent e) {
				return e.getPlayer();
			}
		}, 0);
		//CreatureSpawnEvent
		EventValues.registerEventValue(CreatureSpawnEvent.class, SpawnReason.class, new Getter<SpawnReason, CreatureSpawnEvent>() {
			@Override
			@Nullable
			public SpawnReason get(CreatureSpawnEvent e) {
				return e.getSpawnReason();
			}
		}, 0);
		//FireworkExplodeEvent
		EventValues.registerEventValue(FireworkExplodeEvent.class, Firework.class, new Getter<Firework, FireworkExplodeEvent>() {
			@Override
			@Nullable
			public Firework get(FireworkExplodeEvent e) {
				return e.getEntity();
			}
		}, 0);
		EventValues.registerEventValue(FireworkExplodeEvent.class, FireworkEffect.class, new Getter<FireworkEffect, FireworkExplodeEvent>() {
			@Override
			@Nullable
			public FireworkEffect get(FireworkExplodeEvent e) {
				List<FireworkEffect> effects = e.getEntity().getFireworkMeta().getEffects();
				if (effects.size() == 0)
					return null;
				return effects.get(0);
			}
		}, 0);
		//PlayerRiptideEvent
    EventValues.registerEventValue(PlayerRiptideEvent.class, ItemStack.class, new Getter<ItemStack, PlayerRiptideEvent>() {
      @Override
      public ItemStack get(PlayerRiptideEvent e) {
        return e.getItem();
      }
    }, 0);
		//PlayerArmorChangeEvent
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerArmorChangeEvent")) {
			EventValues.registerEventValue(PlayerArmorChangeEvent.class, ItemStack.class, new Getter<ItemStack, PlayerArmorChangeEvent>() {
				@Override
				@Nullable
				public ItemStack get(PlayerArmorChangeEvent e) {
					return e.getNewItem();
				}
			}, 0);
		}
		//PrepareItemEnchantEvent
		EventValues.registerEventValue(PrepareItemEnchantEvent.class, Player.class, new Getter<Player, PrepareItemEnchantEvent>() {
			@Override
			@Nullable
			public Player get(PrepareItemEnchantEvent e) {
				return e.getEnchanter();
			}
		}, 0);
		EventValues.registerEventValue(PrepareItemEnchantEvent.class, ItemStack.class, new Getter<ItemStack, PrepareItemEnchantEvent>() {
			@Override
			@Nullable
			public ItemStack get(PrepareItemEnchantEvent e) {
				return e.getItem();
			}
		}, 0);
		EventValues.registerEventValue(PrepareItemEnchantEvent.class, Block.class, new Getter<Block, PrepareItemEnchantEvent>() {
			@Override
			@Nullable
			public Block get(PrepareItemEnchantEvent e) {
				return e.getEnchantBlock();
			}
		}, 0);
		//EnchantItemEvent
		EventValues.registerEventValue(EnchantItemEvent.class, Player.class, new Getter<Player, EnchantItemEvent>() {
			@Override
			@Nullable
			public Player get(EnchantItemEvent e) {
				return e.getEnchanter();
			}
		}, 0);
		EventValues.registerEventValue(EnchantItemEvent.class, ItemStack.class, new Getter<ItemStack, EnchantItemEvent>() {
			@Override
			@Nullable
			public ItemStack get(EnchantItemEvent e) {
				return e.getItem();
			}
		}, 0);
		EventValues.registerEventValue(EnchantItemEvent.class, Block.class, new Getter<Block, EnchantItemEvent>() {
			@Override
			@Nullable
			public Block get(EnchantItemEvent e) {
				return e.getEnchantBlock();
			}
		}, 0);
		EventValues.registerEventValue(HorseJumpEvent.class, Entity.class, new Getter<Entity, HorseJumpEvent>() {
			@Nullable
			@Override
			public Entity get(HorseJumpEvent evt) {
				return evt.getEntity();
			}
		}, 0);
		// PlayerChangedWorldEvent
		EventValues.registerEventValue(PlayerChangedWorldEvent.class, World.class, new Getter<World, PlayerChangedWorldEvent>() {
			@Nullable
			@Override
			public World get(PlayerChangedWorldEvent e) {
				return e.getFrom();
			}
		}, -1);
	}
}
