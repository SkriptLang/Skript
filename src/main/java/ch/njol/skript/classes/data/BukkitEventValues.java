package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.InventoryUtils;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.events.bukkit.SkriptStartEvent;
import ch.njol.skript.events.bukkit.SkriptStopEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.*;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.event.player.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ch.njol.skript.registrations.EventValues.*;

public final class BukkitEventValues {

	private static final ItemStack AIR_IS = new ItemStack(Material.AIR);

	static {

		// === WorldEvents ===
		EventValues.registerEventValue(WorldEvent.class, World.class, WorldEvent::getWorld);
		// StructureGrowEvent - a WorldEvent
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, event -> event.getLocation().getBlock());

		EventValues.registerEventValue(StructureGrowEvent.class, Block[].class,
			event -> event.getBlocks().stream()
				.map(BlockState::getBlock)
				.toArray(Block[]::new));
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, event -> {
			for (BlockState bs : event.getBlocks()) {
				if (bs.getLocation().equals(event.getLocation()))
					return new BlockStateBlock(bs);
			}
			return event.getLocation().getBlock();
		}, TIME_FUTURE);
		EventValues.registerEventValue(StructureGrowEvent.class, Block[].class, event ->
				event.getBlocks().stream()
					.map(BlockStateBlock::new)
					.toArray(Block[]::new),
			TIME_FUTURE);
		// WeatherEvent - not a WorldEvent (wtf ô_Ô)
		EventValues.registerEventValue(WeatherEvent.class, World.class, WeatherEvent::getWorld);
		// ChunkEvents
		EventValues.registerEventValue(ChunkEvent.class, Chunk.class, ChunkEvent::getChunk);

		// === BlockEvents ===
		EventValues.registerEventValue(BlockEvent.class, Block.class, BlockEvent::getBlock);
		EventValues.registerEventValue(BlockEvent.class, World.class, event -> event.getBlock().getWorld());
		// REMIND workaround of the event's location being at the entity in block events that have an entity event value
		EventValues.registerEventValue(BlockEvent.class, Location.class, event -> BlockUtils.getLocation(event.getBlock()));
		// BlockPlaceEvent
		EventValues.registerEventValue(BlockPlaceEvent.class, Player.class, BlockPlaceEvent::getPlayer);
		EventValues.registerEventValue(BlockPlaceEvent.class, ItemStack.class, BlockPlaceEvent::getItemInHand, TIME_PAST);
		EventValues.registerEventValue(BlockPlaceEvent.class, ItemStack.class, BlockPlaceEvent::getItemInHand);
		EventValues.registerEventValue(BlockPlaceEvent.class, ItemStack.class, event -> {
			ItemStack item = event.getItemInHand().clone();
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
				item.setAmount(item.getAmount() - 1);
			return item;
		}, TIME_FUTURE);
		EventValues.registerEventValue(BlockPlaceEvent.class, Block.class,
			event -> new BlockStateBlock(event.getBlockReplacedState()), TIME_PAST);
		EventValues.registerEventValue(BlockPlaceEvent.class, Direction.class, event -> {
			BlockFace bf = event.getBlockPlaced().getFace(event.getBlockAgainst());
			if (bf != null) {
				return new Direction(new double[]{bf.getModX(), bf.getModY(), bf.getModZ()});
			}
			return Direction.ZERO;
		});
		// BlockFadeEvent
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, BlockEvent::getBlock, TIME_PAST);
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class,
			event -> new DelayedChangeBlock(event.getBlock(), event.getNewState()));
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class,
			event -> new BlockStateBlock(event.getNewState()), TIME_FUTURE);
		// BlockGrowEvent (+ BlockFormEvent)
		EventValues.registerEventValue(BlockGrowEvent.class, Block.class,
			event -> new BlockStateBlock(event.getNewState()));
		EventValues.registerEventValue(BlockGrowEvent.class, Block.class, BlockEvent::getBlock, TIME_PAST);
		// BlockDamageEvent
		EventValues.registerEventValue(BlockDamageEvent.class, Player.class, BlockDamageEvent::getPlayer);
		// BlockBreakEvent
		EventValues.registerEventValue(BlockBreakEvent.class, Player.class, BlockBreakEvent::getPlayer);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, BlockEvent::getBlock, TIME_PAST);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, event -> new DelayedChangeBlock(event.getBlock()));
		// BlockFromToEvent
		EventValues.registerEventValue(BlockFromToEvent.class, Block.class, BlockFromToEvent::getToBlock, TIME_FUTURE);
		// BlockIgniteEvent
		EventValues.registerEventValue(BlockIgniteEvent.class, Player.class, BlockIgniteEvent::getPlayer);
		EventValues.registerEventValue(BlockIgniteEvent.class, Block.class, BlockIgniteEvent::getBlock);
		// BlockDispenseEvent
		EventValues.registerEventValue(BlockDispenseEvent.class, ItemStack.class, BlockDispenseEvent::getItem);
		// BlockCanBuildEvent
		EventValues.registerEventValue(BlockCanBuildEvent.class, Block.class, BlockEvent::getBlock, TIME_PAST);
		EventValues.registerEventValue(BlockCanBuildEvent.class, Block.class, event -> {
			BlockState state = event.getBlock().getState();
			state.setType(event.getMaterial());
			return new BlockStateBlock(state, true);
		});
		// BlockCanBuildEvent#getPlayer was added in 1.13
		if (Skript.methodExists(BlockCanBuildEvent.class, "getPlayer")) {
			EventValues.registerEventValue(BlockCanBuildEvent.class, Player.class, BlockCanBuildEvent::getPlayer);
		}
		// SignChangeEvent
		EventValues.registerEventValue(SignChangeEvent.class, Player.class, SignChangeEvent::getPlayer);
		EventValues.registerEventValue(SignChangeEvent.class, String[].class, SignChangeEvent::getLines);

		// === EntityEvents ===
		EventValues.registerEventValue(EntityEvent.class, Entity.class, EntityEvent::getEntity, TIME_NOW,
			"Use 'attacker' and/or 'victim' in damage/death events", EntityDamageEvent.class, EntityDeathEvent.class);
		EventValues.registerEventValue(EntityEvent.class, CommandSender.class, EntityEvent::getEntity, TIME_NOW,
			"Use 'attacker' and/or 'victim' in damage/death events", EntityDamageEvent.class, EntityDeathEvent.class);
		EventValues.registerEventValue(EntityEvent.class, World.class, event -> event.getEntity().getWorld());
		EventValues.registerEventValue(EntityEvent.class, Location.class, event -> event.getEntity().getLocation());
		// EntityDamageEvent
		EventValues.registerEventValue(EntityDamageEvent.class, DamageCause.class, EntityDamageEvent::getCause);
		EventValues.registerEventValue(EntityDamageByEntityEvent.class, Projectile.class, event -> {
			if (event.getDamager() instanceof Projectile projectile)
				return projectile;
			return null;
		});
		// EntityDeathEvent
		EventValues.registerEventValue(EntityDeathEvent.class, ItemStack[].class, event -> event.getDrops().toArray(new ItemStack[0]));
		EventValues.registerEventValue(EntityDeathEvent.class, Projectile.class, event -> {
			EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
			if (damageEvent instanceof EntityDamageByEntityEvent entityEvent && entityEvent.getDamager() instanceof Projectile projectile)
				return projectile;
			return null;
		});
		EventValues.registerEventValue(EntityDeathEvent.class, DamageCause.class, event -> {
			EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
			return damageEvent == null ? null : damageEvent.getCause();
		});

		// Entity Potion Effect
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffect.class, EntityPotionEffectEvent::getOldEffect, TIME_PAST);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffect.class, EntityPotionEffectEvent::getNewEffect);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffectType.class, EntityPotionEffectEvent::getModifiedType);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, EntityPotionEffectEvent.Cause.class, EntityPotionEffectEvent::getCause);

		// ProjectileHitEvent
		// ProjectileHitEvent#getHitBlock was added in 1.11
		if (Skript.methodExists(ProjectileHitEvent.class, "getHitBlock"))
			EventValues.registerEventValue(ProjectileHitEvent.class, Block.class, ProjectileHitEvent::getHitBlock);
		EventValues.registerEventValue(ProjectileHitEvent.class, Entity.class, event -> {
			assert false;
			return event.getEntity();
		}, TIME_NOW, "Use 'projectile' and/or 'shooter' in projectile hit events", ProjectileHitEvent.class);
		EventValues.registerEventValue(ProjectileHitEvent.class, Projectile.class, ProjectileHitEvent::getEntity);
		if (Skript.methodExists(ProjectileHitEvent.class, "getHitBlockFace")) {
			EventValues.registerEventValue(ProjectileHitEvent.class, Direction.class, event -> {
				BlockFace theHitFace = event.getHitBlockFace();
				if (theHitFace == null) return null;
				return new Direction(theHitFace, TIME_FUTURE);
			});
		}
		// ProjectileLaunchEvent
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Entity.class, event -> {
			assert false;
			return event.getEntity();
		}, TIME_NOW, "Use 'projectile' and/or 'shooter' in shoot events", ProjectileLaunchEvent.class);
		//ProjectileCollideEvent
		if (Skript.classExists("com.destroystokyo.paper.event.entity.ProjectileCollideEvent")) {
			EventValues.registerEventValue(ProjectileCollideEvent.class, Projectile.class, ProjectileCollideEvent::getEntity);
			EventValues.registerEventValue(ProjectileCollideEvent.class, Entity.class, ProjectileCollideEvent::getCollidedWith);
		}
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Projectile.class, ProjectileLaunchEvent::getEntity);
		// EntityTameEvent
		EventValues.registerEventValue(EntityTameEvent.class, Entity.class, EntityTameEvent::getEntity);

		// EntityTeleportEvent
		EventValues.registerEventValue(EntityTeleportEvent.class, Location.class, EntityTeleportEvent::getFrom, TIME_PAST);
		EventValues.registerEventValue(EntityTeleportEvent.class, Location.class, EntityTeleportEvent::getTo);

		// EntityChangeBlockEvent
		EventValues.registerEventValue(EntityChangeBlockEvent.class, Block.class, EntityChangeBlockEvent::getBlock, TIME_PAST);
		EventValues.registerEventValue(EntityChangeBlockEvent.class, Block.class, EntityChangeBlockEvent::getBlock);
		EventValues.registerEventValue(EntityChangeBlockEvent.class, BlockData.class, EntityChangeBlockEvent::getBlockData);
		EventValues.registerEventValue(EntityChangeBlockEvent.class, BlockData.class, EntityChangeBlockEvent::getBlockData, TIME_FUTURE);

		// AreaEffectCloudApplyEvent
		EventValues.registerEventValue(AreaEffectCloudApplyEvent.class, LivingEntity[].class,
			event -> event.getAffectedEntities().toArray(new LivingEntity[0]));
		EventValues.registerEventValue(AreaEffectCloudApplyEvent.class, PotionEffectType.class, new Converter<>() {
			private final boolean HAS_POTION_TYPE_METHOD = Skript.methodExists(AreaEffectCloud.class, "getBasePotionType");

			@Override
			public PotionEffectType convert(AreaEffectCloudApplyEvent event) {
				// TODO needs to be reworked to support multiple values (there can be multiple potion effects)
				if (HAS_POTION_TYPE_METHOD) {
					PotionType base = event.getEntity().getBasePotionType();
					if (base != null)
						return base.getEffectType();
				} else {
					return event.getEntity().getBasePotionData().getType().getEffectType();
				}
				return null;
			}
		});
		// ItemSpawnEvent
		EventValues.registerEventValue(ItemSpawnEvent.class, ItemStack.class, event -> event.getEntity().getItemStack());
		// LightningStrikeEvent
		EventValues.registerEventValue(LightningStrikeEvent.class, Entity.class, LightningStrikeEvent::getLightning);
		// EndermanAttackPlayerEvent
		if (Skript.classExists("com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent")) {
			EventValues.registerEventValue(EndermanAttackPlayerEvent.class, Player.class, EndermanAttackPlayerEvent::getPlayer);
		}

		// --- PlayerEvents ---
		EventValues.registerEventValue(PlayerEvent.class, Player.class, PlayerEvent::getPlayer);
		EventValues.registerEventValue(PlayerEvent.class, World.class, event -> event.getPlayer().getWorld());
		// PlayerBedEnterEvent
		EventValues.registerEventValue(PlayerBedEnterEvent.class, Block.class, PlayerBedEnterEvent::getBed);
		// PlayerBedLeaveEvent
		EventValues.registerEventValue(PlayerBedLeaveEvent.class, Block.class, PlayerBedLeaveEvent::getBed);
		// PlayerBucketEvents
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, PlayerBucketEvent::getBlockClicked);
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, event -> {
			BlockState state = event.getBlockClicked().getState();
			state.setType(Material.AIR);
			state.setRawData((byte) 0);
			return new BlockStateBlock(state, true);
		}, TIME_FUTURE);
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class,
			event -> event.getBlockClicked().getRelative(event.getBlockFace()), TIME_PAST);
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, event -> {
			BlockState state = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
			state.setType(event.getBucket() == Material.WATER_BUCKET ? Material.WATER : Material.LAVA);
			state.setRawData((byte) 0);
			return new BlockStateBlock(state, true);
		});
		// PlayerDropItemEvent
		EventValues.registerEventValue(PlayerDropItemEvent.class, Player.class, PlayerEvent::getPlayer);
		EventValues.registerEventValue(PlayerDropItemEvent.class, Item.class, PlayerDropItemEvent::getItemDrop);
		EventValues.registerEventValue(PlayerDropItemEvent.class, ItemStack.class, event -> event.getItemDrop().getItemStack());
		// EntityDropItemEvent
		EventValues.registerEventValue(EntityDropItemEvent.class, Item.class, EntityDropItemEvent::getItemDrop);
		EventValues.registerEventValue(EntityDropItemEvent.class, ItemStack.class, event -> event.getItemDrop().getItemStack());
		// PlayerPickupItemEvent
		EventValues.registerEventValue(PlayerPickupItemEvent.class, Player.class, PlayerEvent::getPlayer);
		EventValues.registerEventValue(PlayerPickupItemEvent.class, Item.class, PlayerPickupItemEvent::getItem);
		EventValues.registerEventValue(PlayerPickupItemEvent.class, ItemStack.class, event -> event.getItem().getItemStack());
		// EntityPickupItemEvent
		EventValues.registerEventValue(EntityPickupItemEvent.class, Entity.class, EntityPickupItemEvent::getEntity);
		EventValues.registerEventValue(EntityPickupItemEvent.class, Item.class, EntityPickupItemEvent::getItem);
		EventValues.registerEventValue(EntityPickupItemEvent.class, ItemType.class, event -> new ItemType(event.getItem().getItemStack()));
		// PlayerItemConsumeEvent
		EventValues.registerEventValue(PlayerItemConsumeEvent.class, ItemStack.class, PlayerItemConsumeEvent::getItem);
		// PlayerItemBreakEvent
		EventValues.registerEventValue(PlayerItemBreakEvent.class, ItemStack.class, PlayerItemBreakEvent::getBrokenItem);
		// PlayerInteractEntityEvent
		EventValues.registerEventValue(PlayerInteractEntityEvent.class, Entity.class, PlayerInteractEntityEvent::getRightClicked);
		EventValues.registerEventValue(PlayerInteractEntityEvent.class, ItemStack.class, event -> {
			EquipmentSlot hand = event.getHand();
			if (hand == EquipmentSlot.HAND)
				return event.getPlayer().getInventory().getItemInMainHand();
			else if (hand == EquipmentSlot.OFF_HAND)
				return event.getPlayer().getInventory().getItemInOffHand();
			else
				return null;
		});
		// PlayerInteractEvent
		EventValues.registerEventValue(PlayerInteractEvent.class, ItemStack.class, PlayerInteractEvent::getItem);
		EventValues.registerEventValue(PlayerInteractEvent.class, Block.class, PlayerInteractEvent::getClickedBlock);
		EventValues.registerEventValue(PlayerInteractEvent.class, Direction.class,
			event -> new Direction(new double[]{event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ()}));
		// PlayerShearEntityEvent
		EventValues.registerEventValue(PlayerShearEntityEvent.class, Entity.class, PlayerShearEntityEvent::getEntity);
		// PlayerMoveEvent
		EventValues.registerEventValue(PlayerMoveEvent.class, Block.class,
			event -> event.getTo().clone().subtract(0, 0.5, 0).getBlock());
		EventValues.registerEventValue(PlayerMoveEvent.class, Location.class, PlayerMoveEvent::getFrom, TIME_PAST);
		EventValues.registerEventValue(PlayerMoveEvent.class, Location.class, PlayerMoveEvent::getTo);
		EventValues.registerEventValue(PlayerMoveEvent.class, Chunk.class, event -> event.getFrom().getChunk(), TIME_PAST);
		EventValues.registerEventValue(PlayerMoveEvent.class, Chunk.class, event -> event.getTo().getChunk());
		// PlayerItemDamageEvent
		EventValues.registerEventValue(PlayerItemDamageEvent.class, ItemStack.class, PlayerItemDamageEvent::getItem);
		//PlayerItemMendEvent
		EventValues.registerEventValue(PlayerItemMendEvent.class, Player.class, PlayerEvent::getPlayer);
		EventValues.registerEventValue(PlayerItemMendEvent.class, ItemStack.class, PlayerItemMendEvent::getItem);
		EventValues.registerEventValue(PlayerItemMendEvent.class, Entity.class, PlayerItemMendEvent::getExperienceOrb);

		// --- HangingEvents ---

		// Note: will not work in HangingEntityBreakEvent due to event-entity being parsed as HangingBreakByEntityEvent#getRemover() from code down below
		EventValues.registerEventValue(HangingEvent.class, Hanging.class, HangingEvent::getEntity);
		EventValues.registerEventValue(HangingEvent.class, World.class, event -> event.getEntity().getWorld());
		EventValues.registerEventValue(HangingEvent.class, Location.class, event -> event.getEntity().getLocation());

		// HangingBreakEvent
		EventValues.registerEventValue(HangingBreakEvent.class, Entity.class, event -> {
			if (event instanceof HangingBreakByEntityEvent hangingBreakByEntityEvent)
				return hangingBreakByEntityEvent.getRemover();
			return null;
		});
		// HangingPlaceEvent
		EventValues.registerEventValue(HangingPlaceEvent.class, Player.class, HangingPlaceEvent::getPlayer);

		// --- VehicleEvents ---
		EventValues.registerEventValue(VehicleEvent.class, Vehicle.class, VehicleEvent::getVehicle);
		EventValues.registerEventValue(VehicleEvent.class, World.class, event -> event.getVehicle().getWorld());
		EventValues.registerEventValue(VehicleExitEvent.class, LivingEntity.class, VehicleExitEvent::getExited);

		EventValues.registerEventValue(VehicleEnterEvent.class, Entity.class, VehicleEnterEvent::getEntered);

		// We could error here instead but it's preferable to not do it in this case
		EventValues.registerEventValue(VehicleDamageEvent.class, Entity.class, VehicleDamageEvent::getAttacker);

		EventValues.registerEventValue(VehicleDestroyEvent.class, Entity.class, VehicleDestroyEvent::getAttacker);

		EventValues.registerEventValue(VehicleEvent.class, Entity.class, event -> event.getVehicle().getPassenger());


		// === CommandEvents ===
		// PlayerCommandPreprocessEvent is a PlayerEvent
		EventValues.registerEventValue(ServerCommandEvent.class, CommandSender.class, ServerCommandEvent::getSender);
		EventValues.registerEventValue(CommandEvent.class, String[].class, CommandEvent::getArgs);
		EventValues.registerEventValue(CommandEvent.class, CommandSender.class, CommandEvent::getSender);
		EventValues.registerEventValue(CommandEvent.class, World.class, e -> e.getSender() instanceof Player ? ((Player) e.getSender()).getWorld() : null);
		EventValues.registerEventValue(CommandEvent.class, Block.class,
			event -> event.getSender() instanceof BlockCommandSender sender ? sender.getBlock() : null);

		// === ServerEvents ===
		// Script load/unload event
		EventValues.registerEventValue(ScriptEvent.class, CommandSender.class, event -> Bukkit.getConsoleSender());
		// Server load event
		EventValues.registerEventValue(SkriptStartEvent.class, CommandSender.class, event -> Bukkit.getConsoleSender());
		// Server stop event
		EventValues.registerEventValue(SkriptStopEvent.class, CommandSender.class, event -> Bukkit.getConsoleSender());

		// === InventoryEvents ===
		// InventoryClickEvent
		EventValues.registerEventValue(InventoryClickEvent.class, Player.class,
			event -> event.getWhoClicked() instanceof Player player ? player : null);
		EventValues.registerEventValue(InventoryClickEvent.class, World.class, event -> event.getWhoClicked().getWorld());
		EventValues.registerEventValue(InventoryClickEvent.class, ItemStack.class, event -> {
			if (event instanceof CraftItemEvent craftItemEvent)
				return craftItemEvent.getRecipe().getResult();
			return event.getCurrentItem();
		});
		EventValues.registerEventValue(InventoryClickEvent.class, Slot.class, event -> {
			Inventory invi = event.getClickedInventory(); // getInventory is WRONG and dangerous
			if (invi == null)
				return null;
			int slotIndex = event.getSlot();

			// Not all indices point to inventory slots. Equipment, for example
			if (invi instanceof PlayerInventory itemStacks && slotIndex >= 36) {
				return new ch.njol.skript.util.slot.EquipmentSlot(itemStacks.getHolder(), slotIndex);
			} else {
				return new InventorySlot(invi, slotIndex, event.getRawSlot());
			}
		});
		EventValues.registerEventValue(InventoryClickEvent.class, InventoryAction.class, InventoryClickEvent::getAction);
		EventValues.registerEventValue(InventoryClickEvent.class, ClickType.class, InventoryClickEvent::getClick);
		EventValues.registerEventValue(InventoryClickEvent.class, Inventory.class, InventoryClickEvent::getClickedInventory);
		// InventoryDragEvent
		EventValues.registerEventValue(InventoryDragEvent.class, Player.class,
			event -> event.getWhoClicked() instanceof Player player ? player : null);
		EventValues.registerEventValue(InventoryDragEvent.class, World.class, event -> event.getWhoClicked().getWorld());
		EventValues.registerEventValue(InventoryDragEvent.class, ItemStack.class, InventoryDragEvent::getOldCursor, TIME_PAST);
		EventValues.registerEventValue(InventoryDragEvent.class, ItemStack.class, InventoryDragEvent::getCursor);
		EventValues.registerEventValue(InventoryDragEvent.class, ItemStack[].class,
			event -> event.getNewItems().values().toArray(new ItemStack[0]));
		EventValues.registerEventValue(InventoryDragEvent.class, Slot[].class, event -> {
			List<Slot> slots = new ArrayList<>(event.getRawSlots().size());
			InventoryView view = event.getView();
			for (Integer rawSlot : event.getRawSlots()) {
				Inventory inventory = InventoryUtils.getInventory(view, rawSlot);
				Integer slot = InventoryUtils.convertSlot(view, rawSlot);
				if (inventory == null || slot == null)
					continue;
				// Not all indices point to inventory slots. Equipment, for example
				if (inventory instanceof PlayerInventory && slot >= 36) {
					slots.add(new ch.njol.skript.util.slot.EquipmentSlot(((PlayerInventory) view.getBottomInventory()).getHolder(), slot));
				} else {
					slots.add(new InventorySlot(inventory, slot));
				}
			}
			return slots.toArray(new Slot[0]);
		});
		EventValues.registerEventValue(InventoryDragEvent.class, ClickType.class, event -> event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT);
		EventValues.registerEventValue(InventoryDragEvent.class, Inventory[].class, event -> {
			Set<Inventory> inventories = new HashSet<>();
			InventoryView view = event.getView();
			for (Integer rawSlot : event.getRawSlots()) {
				Inventory inventory = InventoryUtils.getInventory(view, rawSlot);
				if (inventory != null)
					inventories.add(inventory);
			}
			return inventories.toArray(new Inventory[0]);
		});
		// PrepareAnvilEvent
		if (Skript.classExists("com.destroystokyo.paper.event.inventory.PrepareResultEvent"))
			EventValues.registerEventValue(PrepareAnvilEvent.class, ItemStack.class, PrepareResultEvent::getResult);
		EventValues.registerEventValue(PrepareAnvilEvent.class, Inventory.class, PrepareAnvilEvent::getInventory);
		// AnvilDamagedEvent
		if (Skript.classExists("com.destroystokyo.paper.event.block.AnvilDamagedEvent")) {
			EventValues.registerEventValue(AnvilDamagedEvent.class, Inventory.class, AnvilDamagedEvent::getInventory);
		}
		//BlockFertilizeEvent
		EventValues.registerEventValue(BlockFertilizeEvent.class, Player.class, BlockFertilizeEvent::getPlayer);
		EventValues.registerEventValue(BlockFertilizeEvent.class, Block[].class, event -> event.getBlocks().stream()
			.map(BlockState::getBlock)
			.toArray(Block[]::new));
		// PrepareItemCraftEvent
		EventValues.registerEventValue(PrepareItemCraftEvent.class, Slot.class, event -> new InventorySlot(event.getInventory(), 0));
		EventValues.registerEventValue(PrepareItemCraftEvent.class, ItemStack.class, event -> {
			ItemStack item = event.getInventory().getResult();
			return item != null ? item : AIR_IS;
		});
		EventValues.registerEventValue(PrepareItemCraftEvent.class, Inventory.class, PrepareItemCraftEvent::getInventory);
		EventValues.registerEventValue(PrepareItemCraftEvent.class, Player.class, event -> {
			List<HumanEntity> viewers = event.getInventory().getViewers(); // Get all viewers
			if (viewers.isEmpty()) // ... if we don't have any
				return null;
			HumanEntity first = viewers.get(0); // Get first viewer and hope it is crafter
			if (first instanceof Player player) // Needs to be player... Usually it is
				return player;
			return null;
		});
		// CraftEvents - recipe namespaced key strings
		EventValues.registerEventValue(CraftItemEvent.class, String.class, event -> {
			Recipe recipe = event.getRecipe();
			if (recipe instanceof Keyed keyed)
				return keyed.getKey().toString();
			return null;
		});
		EventValues.registerEventValue(PrepareItemCraftEvent.class, String.class, event -> {
			Recipe recipe = event.getRecipe();
			if (recipe instanceof Keyed keyed)
				return keyed.getKey().toString();
			return null;
		});
		// CraftItemEvent
		EventValues.registerEventValue(CraftItemEvent.class, ItemStack.class, event -> event.getRecipe().getResult());
		//InventoryOpenEvent
		EventValues.registerEventValue(InventoryOpenEvent.class, Player.class, event -> (Player) event.getPlayer());
		EventValues.registerEventValue(InventoryOpenEvent.class, Inventory.class, InventoryEvent::getInventory);
		//InventoryCloseEvent
		EventValues.registerEventValue(InventoryCloseEvent.class, Player.class, event -> (Player) event.getPlayer());
		EventValues.registerEventValue(InventoryCloseEvent.class, Inventory.class, InventoryEvent::getInventory);
		if (Skript.classExists("org.bukkit.event.inventory.InventoryCloseEvent$Reason"))
			EventValues.registerEventValue(InventoryCloseEvent.class, InventoryCloseEvent.Reason.class, InventoryCloseEvent::getReason);
		//InventoryPickupItemEvent
		EventValues.registerEventValue(InventoryPickupItemEvent.class, Inventory.class, InventoryPickupItemEvent::getInventory);
		EventValues.registerEventValue(InventoryPickupItemEvent.class, Item.class, InventoryPickupItemEvent::getItem);
		EventValues.registerEventValue(InventoryPickupItemEvent.class, ItemStack.class, event -> event.getItem().getItemStack());
		//PortalCreateEvent
		EventValues.registerEventValue(PortalCreateEvent.class, World.class, WorldEvent::getWorld);
		EventValues.registerEventValue(PortalCreateEvent.class, Block[].class, event -> event.getBlocks().stream()
			.map(BlockState::getBlock)
			.toArray(Block[]::new));
		if (Skript.methodExists(PortalCreateEvent.class, "getEntity")) { // Minecraft 1.14+
			EventValues.registerEventValue(PortalCreateEvent.class, Entity.class, PortalCreateEvent::getEntity);
		}
		//PlayerEditBookEvent
		EventValues.registerEventValue(PlayerEditBookEvent.class, ItemStack.class, event -> {
			ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
			book.setItemMeta(event.getPreviousBookMeta());
			return book;
		}, TIME_PAST);
		EventValues.registerEventValue(PlayerEditBookEvent.class, ItemStack.class, event -> {
			ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
			book.setItemMeta(event.getNewBookMeta());
			return book;
		});
		EventValues.registerEventValue(PlayerEditBookEvent.class, String[].class,
			event -> event.getPreviousBookMeta().getPages().toArray(new String[0]), TIME_PAST);
		EventValues.registerEventValue(PlayerEditBookEvent.class, String[].class,
			event -> event.getNewBookMeta().getPages().toArray(new String[0]));
		//ItemDespawnEvent
		EventValues.registerEventValue(ItemDespawnEvent.class, Item.class, ItemDespawnEvent::getEntity);
		EventValues.registerEventValue(ItemDespawnEvent.class, ItemStack.class, event -> event.getEntity().getItemStack());
		//ItemMergeEvent
		EventValues.registerEventValue(ItemMergeEvent.class, Item.class, ItemMergeEvent::getEntity);
		EventValues.registerEventValue(ItemMergeEvent.class, Item.class, ItemMergeEvent::getTarget, TIME_FUTURE);
		EventValues.registerEventValue(ItemMergeEvent.class, ItemStack.class, event -> event.getEntity().getItemStack());
		//PlayerTeleportEvent
		EventValues.registerEventValue(PlayerTeleportEvent.class, TeleportCause.class, PlayerTeleportEvent::getCause);
		//EntityMoveEvent
		if (Skript.classExists("io.papermc.paper.event.entity.EntityMoveEvent")) {
			EventValues.registerEventValue(EntityMoveEvent.class, Location.class, EntityMoveEvent::getFrom);
			EventValues.registerEventValue(EntityMoveEvent.class, Location.class, EntityMoveEvent::getTo, TIME_FUTURE);
		}
		//PlayerToggleFlightEvent
		EventValues.registerEventValue(PlayerToggleFlightEvent.class, Player.class, PlayerEvent::getPlayer);
		//CreatureSpawnEvent
		EventValues.registerEventValue(CreatureSpawnEvent.class, SpawnReason.class, CreatureSpawnEvent::getSpawnReason);
		//FireworkExplodeEvent
		EventValues.registerEventValue(FireworkExplodeEvent.class, Firework.class, FireworkExplodeEvent::getEntity);
		EventValues.registerEventValue(FireworkExplodeEvent.class, FireworkEffect.class, event -> {
			List<FireworkEffect> effects = event.getEntity().getFireworkMeta().getEffects();
			if (effects.isEmpty())
				return null;
			return effects.get(0);
		});
		EventValues.registerEventValue(FireworkExplodeEvent.class, Color[].class, event -> {
			List<FireworkEffect> effects = event.getEntity().getFireworkMeta().getEffects();
			if (effects.isEmpty())
				return null;
			List<Color> colors = new ArrayList<>();
			for (FireworkEffect fireworkEffect : effects) {
				for (org.bukkit.Color color : fireworkEffect.getColors()) {
					if (SkriptColor.fromBukkitColor(color) != null)
						colors.add(SkriptColor.fromBukkitColor(color));
					else
						colors.add(ColorRGB.fromBukkitColor(color));
				}
			}
			if (colors.isEmpty())
				return null;
			return colors.toArray(Color[]::new);
		});
		//PlayerRiptideEvent
		EventValues.registerEventValue(PlayerRiptideEvent.class, ItemStack.class, PlayerRiptideEvent::getItem);
		//PlayerArmorChangeEvent
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerArmorChangeEvent")) {
			EventValues.registerEventValue(PlayerArmorChangeEvent.class, EquipmentSlot.class,
				event -> switch (event.getSlotType()) {
					case HEAD -> EquipmentSlot.HEAD;
					case CHEST -> EquipmentSlot.CHEST;
					case LEGS -> EquipmentSlot.LEGS;
					case FEET -> EquipmentSlot.FEET;
			});
		}
		//PlayerInventorySlotChangeEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerInventorySlotChangeEvent")) {
			EventValues.registerEventValue(PlayerInventorySlotChangeEvent.class, ItemStack.class, PlayerInventorySlotChangeEvent::getNewItemStack);
			EventValues.registerEventValue(PlayerInventorySlotChangeEvent.class, ItemStack.class, PlayerInventorySlotChangeEvent::getOldItemStack, TIME_PAST);
			EventValues.registerEventValue(PlayerInventorySlotChangeEvent.class, Slot.class, event -> {
				PlayerInventory inv = event.getPlayer().getInventory();
				int slotIndex = event.getSlot();
				// Not all indices point to inventory slots. Equipment, for example
				if (slotIndex >= 36) {
					return new ch.njol.skript.util.slot.EquipmentSlot(event.getPlayer(), slotIndex);
				} else {
					return new InventorySlot(inv, slotIndex);
				}
			});
		}
		//PrepareItemEnchantEvent
		EventValues.registerEventValue(PrepareItemEnchantEvent.class, Player.class, PrepareItemEnchantEvent::getEnchanter);
		EventValues.registerEventValue(PrepareItemEnchantEvent.class, ItemStack.class, PrepareItemEnchantEvent::getItem);
		EventValues.registerEventValue(PrepareItemEnchantEvent.class, Block.class, PrepareItemEnchantEvent::getEnchantBlock);
		//EnchantItemEvent
		EventValues.registerEventValue(EnchantItemEvent.class, Player.class, EnchantItemEvent::getEnchanter);
		EventValues.registerEventValue(EnchantItemEvent.class, ItemStack.class, EnchantItemEvent::getItem);
		EventValues.registerEventValue(EnchantItemEvent.class, EnchantmentType[].class,
			event -> event.getEnchantsToAdd().entrySet().stream()
			.map(entry -> new EnchantmentType(entry.getKey(), entry.getValue()))
			.toArray(EnchantmentType[]::new));
		EventValues.registerEventValue(EnchantItemEvent.class, Block.class, EnchantItemEvent::getEnchantBlock);
		EventValues.registerEventValue(HorseJumpEvent.class, Entity.class, HorseJumpEvent::getEntity);
		// PlayerTradeEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerTradeEvent")) {
			EventValues.registerEventValue(PlayerTradeEvent.class, AbstractVillager.class, PlayerTradeEvent::getVillager);
		}
		// PlayerChangedWorldEvent
		EventValues.registerEventValue(PlayerChangedWorldEvent.class, World.class, PlayerChangedWorldEvent::getFrom, TIME_PAST);

		// PlayerEggThrowEvent
		EventValues.registerEventValue(PlayerEggThrowEvent.class, Egg.class, PlayerEggThrowEvent::getEgg);

		// PlayerStopUsingItemEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerStopUsingItemEvent")) {
			EventValues.registerEventValue(PlayerStopUsingItemEvent.class, Timespan.class,
				event -> new Timespan(Timespan.TimePeriod.TICK, event.getTicksHeldFor()));
			EventValues.registerEventValue(PlayerStopUsingItemEvent.class, ItemType.class,
				event -> new ItemType(event.getItem()));
		}

		// LootGenerateEvent
		if (Skript.classExists("org.bukkit.event.world.LootGenerateEvent")) {
			EventValues.registerEventValue(LootGenerateEvent.class, Entity.class, LootGenerateEvent::getEntity);
			EventValues.registerEventValue(LootGenerateEvent.class, Location.class, event -> event.getLootContext().getLocation());
		}

		// EntityResurrectEvent
		EventValues.registerEventValue(EntityResurrectEvent.class, Slot.class, event -> {
			EquipmentSlot hand = event.getHand();
			EntityEquipment equipment = event.getEntity().getEquipment();
			if (equipment == null || hand == null)
				return null;
			return new ch.njol.skript.util.slot.EquipmentSlot(equipment,
				(hand == EquipmentSlot.HAND) ? ch.njol.skript.util.slot.EquipmentSlot.EquipSlot.TOOL
					: ch.njol.skript.util.slot.EquipmentSlot.EquipSlot.OFF_HAND);
		});

		// PlayerItemHeldEvent
		EventValues.registerEventValue(PlayerItemHeldEvent.class, Slot.class,
			event -> new InventorySlot(event.getPlayer().getInventory(), event.getNewSlot()));
		EventValues.registerEventValue(PlayerItemHeldEvent.class, Slot.class,
			event -> new InventorySlot(event.getPlayer().getInventory(), event.getPreviousSlot()), TIME_PAST);

		// PlayerPickupArrowEvent
		// This event value is restricted to MC 1.14+ due to an API change which has the return type changed
		// which throws a NoSuchMethodError if used in a 1.13 server.
		if (Skript.isRunningMinecraft(1, 14))
			EventValues.registerEventValue(PlayerPickupArrowEvent.class, Projectile.class, PlayerPickupArrowEvent::getArrow);

		EventValues.registerEventValue(PlayerPickupArrowEvent.class, ItemStack.class,
			event -> event.getItem().getItemStack());

		//PlayerQuitEvent
		if (Skript.classExists("org.bukkit.event.player.PlayerQuitEvent$QuitReason"))
			EventValues.registerEventValue(PlayerQuitEvent.class, QuitReason.class, PlayerQuitEvent::getReason);

		// PlayerStonecutterRecipeSelectEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent"))
			EventValues.registerEventValue(PlayerStonecutterRecipeSelectEvent.class, ItemStack.class,
				event -> event.getStonecuttingRecipe().getResult());

		// EntityTransformEvent
		EventValues.registerEventValue(EntityTransformEvent.class, Entity[].class,
			event -> event.getTransformedEntities().stream().toArray(Entity[]::new));
		EventValues.registerEventValue(EntityTransformEvent.class, TransformReason.class, EntityTransformEvent::getTransformReason);

		// BellRingEvent - these are BlockEvents and not EntityEvents, so they have declared methods for getEntity()
		if (Skript.classExists("org.bukkit.event.block.BellRingEvent")) {
			EventValues.registerEventValue(BellRingEvent.class, Entity.class, BellRingEvent::getEntity);

			EventValues.registerEventValue(BellRingEvent.class, Direction.class,
				event -> new Direction(event.getDirection(), 1));
		} else if (Skript.classExists("io.papermc.paper.event.block.BellRingEvent")) {
			EventValues.registerEventValue(io.papermc.paper.event.block.BellRingEvent.class, Entity.class, BellRingEvent::getEntity);
		}

		if (Skript.classExists("org.bukkit.event.block.BellResonateEvent")) {
			EventValues.registerEventValue(BellResonateEvent.class, Entity[].class,
				event -> event.getResonatedEntities().toArray(new LivingEntity[0]));
		}

		// InventoryMoveItemEvent
		EventValues.registerEventValue(InventoryMoveItemEvent.class, Inventory.class, InventoryMoveItemEvent::getSource);
		EventValues.registerEventValue(InventoryMoveItemEvent.class, Inventory.class, InventoryMoveItemEvent::getDestination, TIME_FUTURE);
		EventValues.registerEventValue(InventoryMoveItemEvent.class, Block.class,
			event -> event.getSource().getLocation().getBlock());
		EventValues.registerEventValue(InventoryMoveItemEvent.class, Block.class,
			event -> event.getDestination().getLocation().getBlock(), TIME_FUTURE);
		EventValues.registerEventValue(InventoryMoveItemEvent.class, ItemStack.class, InventoryMoveItemEvent::getItem);

		// EntityRegainHealthEvent
		EventValues.registerEventValue(EntityRegainHealthEvent.class, RegainReason.class, EntityRegainHealthEvent::getRegainReason);

		// FurnaceExtractEvent
		EventValues.registerEventValue(FurnaceExtractEvent.class, Player.class, FurnaceExtractEvent::getPlayer);
		EventValues.registerEventValue(FurnaceExtractEvent.class, ItemStack[].class,
			event -> new ItemStack[]{ItemStack.of(event.getItemType(), event.getItemAmount())
		});

		// BlockDropItemEvent
		EventValues.registerEventValue(BlockDropItemEvent.class, Block.class,
			event -> new BlockStateBlock(event.getBlockState()), TIME_PAST);
		EventValues.registerEventValue(BlockDropItemEvent.class, Player.class, BlockDropItemEvent::getPlayer);
		EventValues.registerEventValue(BlockDropItemEvent.class, ItemStack[].class,
			event -> event.getItems().stream().map(Item::getItemStack).toArray(ItemStack[]::new));
		EventValues.registerEventValue(BlockDropItemEvent.class, Entity[].class,
			event -> event.getItems().toArray(Entity[]::new));

		// PlayerExpCooldownChangeEvent
		EventValues.registerEventValue(PlayerExpCooldownChangeEvent.class, ChangeReason.class, PlayerExpCooldownChangeEvent::getReason);
		EventValues.registerEventValue(PlayerExpCooldownChangeEvent.class, Timespan.class,
			event -> new Timespan(Timespan.TimePeriod.TICK, event.getNewCooldown()));
		EventValues.registerEventValue(PlayerExpCooldownChangeEvent.class, Timespan.class,
			event -> new Timespan(Timespan.TimePeriod.TICK, event.getPlayer().getExpCooldown()), TIME_PAST);

		// VehicleMoveEvent
		EventValues.registerEventValue(VehicleMoveEvent.class, Location.class, VehicleMoveEvent::getTo);
		EventValues.registerEventValue(VehicleMoveEvent.class, Location.class, VehicleMoveEvent::getFrom, TIME_PAST);

		// BeaconEffectEvent
		if (Skript.classExists("com.destroystokyo.paper.event.block.BeaconEffectEvent")) {
			EventValues.registerEventValue(BeaconEffectEvent.class, PotionEffectType.class,
				event -> event.getEffect().getType(), TIME_NOW, "Use 'applied effect' in beacon effect events.",
				BeaconEffectEvent.class);
			EventValues.registerEventValue(BeaconEffectEvent.class, Player.class, BeaconEffectEvent::getPlayer);
		}
		// PlayerChangeBeaconEffectEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent")) {
			EventValues.registerEventValue(PlayerChangeBeaconEffectEvent.class, Block.class, PlayerChangeBeaconEffectEvent::getBeacon);
		}

		// PlayerElytraBoostEvent
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent")) {
			EventValues.registerEventValue(PlayerElytraBoostEvent.class, ItemStack.class, PlayerElytraBoostEvent::getItemStack);
			EventValues.registerEventValue(PlayerElytraBoostEvent.class, Entity.class, PlayerElytraBoostEvent::getFirework);
		}

	}

}
