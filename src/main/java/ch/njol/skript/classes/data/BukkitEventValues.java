package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.InventoryUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.events.bukkit.SkriptStartEvent;
import ch.njol.skript.events.bukkit.SkriptStopEvent;
import ch.njol.skript.util.*;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.event.player.*;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent;
import io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderEvent;
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
import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.lang.converter.Converter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BukkitEventValues {

	private static final ItemStack AIR_IS = new ItemStack(Material.AIR);

	public static void register(EventValueRegistry registry) {
		// === WorldEvents ===
		registry.register(EventValue.builder(WorldEvent.class, World.class)
			.getter(WorldEvent::getWorld)
			.build());
		// StructureGrowEvent - a WorldEvent
		registry.register(EventValue.builder(StructureGrowEvent.class, Block.class)
			.getter(event -> event.getLocation().getBlock())
			.build());

		registry.register(EventValue.builder(StructureGrowEvent.class, Block[].class)
			.getter(event -> event.getBlocks().stream()
				.map(BlockState::getBlock)
				.toArray(Block[]::new))
			.build());
		registry.register(EventValue.builder(StructureGrowEvent.class, Block.class)
			.getter(event -> {
				for (BlockState bs : event.getBlocks()) {
					if (bs.getLocation().equals(event.getLocation()))
						return new BlockStateBlock(bs);
				}
				return event.getLocation().getBlock();
			})
			.time(Time.FUTURE)
			.build());
		registry.register(EventValue.builder(StructureGrowEvent.class, Block[].class)
			.getter(event -> event.getBlocks().stream()
				.map(BlockStateBlock::new)
				.toArray(Block[]::new))
			.time(Time.FUTURE)
			.build());
		// WeatherEvent - not a WorldEvent (wtf ô_Ô)
		registry.register(EventValue.builder(WeatherEvent.class, World.class)
			.getter(WeatherEvent::getWorld)
			.build());
		// ChunkEvents
		registry.register(EventValue.builder(ChunkEvent.class, Chunk.class)
			.getter(ChunkEvent::getChunk)
			.build());

		// === BlockEvents ===
		registry.register(EventValue.builder(BlockEvent.class, Block.class)
			.getter(BlockEvent::getBlock)
			.build());
		registry.register(EventValue.builder(BlockEvent.class, World.class)
			.getter(event -> event.getBlock().getWorld())
			.build());
		// REMIND workaround of the event's location being at the entity in block events that have an entity event value
		registry.register(EventValue.builder(BlockEvent.class, Location.class)
			.getter(event -> BlockUtils.getLocation(event.getBlock()))
			.build());
		// BlockPlaceEvent
		registry.register(EventValue.builder(BlockPlaceEvent.class, Player.class)
			.getter(BlockPlaceEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(BlockPlaceEvent.class, ItemStack.class)
			.getter(BlockPlaceEvent::getItemInHand)
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(BlockPlaceEvent.class, ItemStack.class)
			.getter(BlockPlaceEvent::getItemInHand)
			.build());
		registry.register(EventValue.builder(BlockPlaceEvent.class, ItemStack.class)
			.getter(event -> {
				ItemStack item = event.getItemInHand().clone();
				if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
					item.setAmount(item.getAmount() - 1);
				return item;
			})
			.time(Time.FUTURE)
			.build());
		registry.register(EventValue.builder(BlockPlaceEvent.class, Block.class)
			.getter(event -> new BlockStateBlock(event.getBlockReplacedState()))
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(BlockPlaceEvent.class, Direction.class)
			.getter(event -> {
				BlockFace bf = event.getBlockPlaced().getFace(event.getBlockAgainst());
				if (bf != null) {
					return new Direction(new double[]{bf.getModX(), bf.getModY(), bf.getModZ()});
				}
				return Direction.ZERO;
			})
			.build());
		// BlockFadeEvent
		registry.register(EventValue.builder(BlockFadeEvent.class, Block.class)
			.getter(BlockEvent::getBlock)
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(BlockFadeEvent.class, Block.class)
			.getter(event -> new DelayedChangeBlock(event.getBlock(), event.getNewState()))
			.build());
		registry.register(EventValue.builder(BlockFadeEvent.class, Block.class)
			.getter(event -> new BlockStateBlock(event.getNewState()))
			.time(Time.FUTURE)
			.build());
		// BlockGrowEvent (+ BlockFormEvent)
		registry.register(EventValue.builder(BlockGrowEvent.class, Block.class)
			.getter(event -> new BlockStateBlock(event.getNewState()))
			.build());
		registry.register(EventValue.builder(BlockGrowEvent.class, Block.class)
			.getter(BlockEvent::getBlock)
			.time(Time.PAST)
			.build());
		// BlockDamageEvent
		registry.register(EventValue.builder(BlockDamageEvent.class, Player.class)
			.getter(BlockDamageEvent::getPlayer)
			.build());
		// BlockBreakEvent
		registry.register(EventValue.builder(BlockBreakEvent.class, Player.class)
			.getter(BlockBreakEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(BlockBreakEvent.class, Block.class)
			.getter(BlockEvent::getBlock)
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(BlockBreakEvent.class, Block.class)
			.getter(event -> new DelayedChangeBlock(event.getBlock()))
			.build());
		// BlockFromToEvent
		registry.register(EventValue.builder(BlockFromToEvent.class, Block.class)
			.getter(BlockFromToEvent::getToBlock)
			.time(Time.FUTURE)
			.build());
		// BlockIgniteEvent
		registry.register(EventValue.builder(BlockIgniteEvent.class, Player.class)
			.getter(BlockIgniteEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(BlockIgniteEvent.class, Block.class)
			.getter(BlockIgniteEvent::getBlock)
			.build());
		// BlockDispenseEvent
		registry.register(EventValue.builder(BlockDispenseEvent.class, ItemStack.class)
			.getter(BlockDispenseEvent::getItem)
			.build());
		// BlockCanBuildEvent
		registry.register(EventValue.builder(BlockCanBuildEvent.class, Block.class)
			.getter(BlockEvent::getBlock)
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(BlockCanBuildEvent.class, Block.class)
			.getter(event -> {
				BlockState state = event.getBlock().getState();
				state.setType(event.getMaterial());
				return new BlockStateBlock(state, true);
			})
			.build());
		// BlockCanBuildEvent#getPlayer was added in 1.13
		if (Skript.methodExists(BlockCanBuildEvent.class, "getPlayer")) {
			registry.register(EventValue.builder(BlockCanBuildEvent.class, Player.class)
				.getter(BlockCanBuildEvent::getPlayer)
				.build());
		}
		// SignChangeEvent
		registry.register(EventValue.builder(SignChangeEvent.class, Player.class)
			.getter(SignChangeEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(SignChangeEvent.class, String[].class)
			.getter(SignChangeEvent::getLines)
			.build());

		// === EntityEvents ===
		registry.register(EventValue.builder(EntityEvent.class, Entity.class)
			.getter(EntityEvent::getEntity)
			.excludes(EntityDamageEvent.class, EntityDeathEvent.class)
			.excludedErrorMessage("Use 'attacker' and/or 'victim' in damage/death events")
			.build());
		registry.register(EventValue.builder(EntityEvent.class, CommandSender.class)
			.getter(EntityEvent::getEntity)
			.excludes(EntityDamageEvent.class, EntityDeathEvent.class)
			.excludedErrorMessage("Use 'attacker' and/or 'victim' in damage/death events")
			.build());
		registry.register(EventValue.builder(EntityEvent.class, World.class)
			.getter(event -> event.getEntity().getWorld())
			.build());
		registry.register(EventValue.builder(EntityEvent.class, Location.class)
			.getter(event -> event.getEntity().getLocation())
			.build());
		registry.register(EventValue.builder(EntityEvent.class, EntityData.class)
			.getter(event -> EntityData.fromEntity(event.getEntity()))
			.excludes(EntityDamageEvent.class, EntityDeathEvent.class)
			.excludedErrorMessage("Use 'type of attacker/victim' in damage/death events.")
			.build());
		// EntityDamageEvent
		registry.register(EventValue.builder(EntityDamageEvent.class, DamageCause.class)
			.getter(EntityDamageEvent::getCause)
			.build());
		registry.register(EventValue.builder(EntityDamageByEntityEvent.class, Projectile.class)
			.getter(event -> {
				if (event.getDamager() instanceof Projectile projectile)
					return projectile;
				return null;
			})
			.build());
		// EntityDeathEvent
		registry.register(EventValue.builder(EntityDeathEvent.class, ItemStack[].class)
			.getter(event -> event.getDrops().toArray(new ItemStack[0]))
			.build());
		registry.register(EventValue.builder(EntityDeathEvent.class, Projectile.class)
			.getter(event -> {
				EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
				if (damageEvent instanceof EntityDamageByEntityEvent entityEvent && entityEvent.getDamager() instanceof Projectile projectile)
					return projectile;
				return null;
			})
			.build());
		registry.register(EventValue.builder(EntityDeathEvent.class, DamageCause.class)
			.getter(event -> {
				EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
				return damageEvent == null ? null : damageEvent.getCause();
			})
			.build());

		// ProjectileHitEvent
		// ProjectileHitEvent#getHitBlock was added in 1.11
		if (Skript.methodExists(ProjectileHitEvent.class, "getHitBlock"))
			registry.register(EventValue.builder(ProjectileHitEvent.class, Block.class)
				.getter(ProjectileHitEvent::getHitBlock)
				.build());
		registry.register(EventValue.builder(ProjectileHitEvent.class, Entity.class)
			.getter(event -> {
				assert false;
				return event.getEntity();
			})
			.excludes(ProjectileHitEvent.class)
			.excludedErrorMessage("Use 'projectile' and/or 'shooter' in projectile hit events")
			.build());
		registry.register(EventValue.builder(ProjectileHitEvent.class, Projectile.class)
			.getter(ProjectileHitEvent::getEntity)
			.build());
		if (Skript.methodExists(ProjectileHitEvent.class, "getHitBlockFace")) {
			registry.register(EventValue.builder(ProjectileHitEvent.class, Direction.class)
				.getter(event -> {
					BlockFace theHitFace = event.getHitBlockFace();
					if (theHitFace == null) return null;
					return new Direction(theHitFace, 1);
				})
				.build());
		}
		// ProjectileLaunchEvent
		registry.register(EventValue.builder(ProjectileLaunchEvent.class, Entity.class)
			.getter(event -> {
				assert false;
				return event.getEntity();
			})
			.excludes(ProjectileLaunchEvent.class)
			.excludedErrorMessage("Use 'projectile' and/or 'shooter' in shoot events")
			.build());
		//ProjectileCollideEvent
		if (Skript.classExists("com.destroystokyo.paper.event.entity.ProjectileCollideEvent")) {
			registry.register(EventValue.builder(ProjectileCollideEvent.class, Projectile.class)
				.getter(ProjectileCollideEvent::getEntity)
				.build());
			registry.register(EventValue.builder(ProjectileCollideEvent.class, Entity.class)
				.getter(ProjectileCollideEvent::getCollidedWith)
				.build());
		}
		registry.register(EventValue.builder(ProjectileLaunchEvent.class, Projectile.class)
			.getter(ProjectileLaunchEvent::getEntity)
			.build());
		// EntityTameEvent
		registry.register(EventValue.builder(EntityTameEvent.class, Entity.class)
			.getter(EntityTameEvent::getEntity)
			.build());

		// EntityChangeBlockEvent
		registry.register(EventValue.builder(EntityChangeBlockEvent.class, Block.class)
			.getter(EntityChangeBlockEvent::getBlock)
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(EntityChangeBlockEvent.class, Block.class)
			.getter(EntityChangeBlockEvent::getBlock)
			.build());
		registry.register(EventValue.builder(EntityChangeBlockEvent.class, BlockData.class)
			.getter(EntityChangeBlockEvent::getBlockData)
			.build());
		registry.register(EventValue.builder(EntityChangeBlockEvent.class, BlockData.class)
			.getter(EntityChangeBlockEvent::getBlockData)
			.time(Time.FUTURE)
			.build());

		// AreaEffectCloudApplyEvent
		registry.register(EventValue.builder(AreaEffectCloudApplyEvent.class, LivingEntity[].class)
			.getter(event -> event.getAffectedEntities().toArray(new LivingEntity[0]))
			.build());
		registry.register(EventValue.builder(AreaEffectCloudApplyEvent.class, PotionEffectType.class)
			.getter(new Converter<>() {
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
			})
			.build());
		// ItemSpawnEvent
		registry.register(EventValue.builder(ItemSpawnEvent.class, ItemStack.class)
			.getter(event -> event.getEntity().getItemStack())
			.build());
		// LightningStrikeEvent
		registry.register(EventValue.builder(LightningStrikeEvent.class, Entity.class)
			.getter(LightningStrikeEvent::getLightning)
			.build());
		// EndermanAttackPlayerEvent
		if (Skript.classExists("com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent")) {
			registry.register(EventValue.builder(EndermanAttackPlayerEvent.class, Player.class)
				.getter(EndermanAttackPlayerEvent::getPlayer)
				.build());
		}

		// --- PlayerEvents ---
		registry.register(EventValue.builder(PlayerEvent.class, Player.class)
			.getter(PlayerEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(PlayerEvent.class, World.class)
			.getter(event -> event.getPlayer().getWorld())
			.build());
		// PlayerBedEnterEvent
		registry.register(EventValue.builder(PlayerBedEnterEvent.class, Block.class)
			.getter(PlayerBedEnterEvent::getBed)
			.build());
		// PlayerBedLeaveEvent
		registry.register(EventValue.builder(PlayerBedLeaveEvent.class, Block.class)
			.getter(PlayerBedLeaveEvent::getBed)
			.build());
		// PlayerBucketEvents
		registry.register(EventValue.builder(PlayerBucketFillEvent.class, Block.class)
			.getter(PlayerBucketEvent::getBlockClicked)
			.build());
		registry.register(EventValue.builder(PlayerBucketFillEvent.class, Block.class)
			.getter(event -> {
				BlockState state = event.getBlockClicked().getState();
				state.setType(Material.AIR);
				return new BlockStateBlock(state, true);
			})
			.time(Time.FUTURE)
			.build());
		registry.register(EventValue.builder(PlayerBucketEmptyEvent.class, Block.class)
			.getter(event -> event.getBlockClicked().getRelative(event.getBlockFace()))
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(PlayerBucketEmptyEvent.class, Block.class)
			.getter(event -> {
				BlockState state = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
				state.setType(event.getBucket() == Material.WATER_BUCKET ? Material.WATER : Material.LAVA);
				return new BlockStateBlock(state, true);
			})
			.build());
		// PlayerDropItemEvent
		registry.register(EventValue.builder(PlayerDropItemEvent.class, Player.class)
			.getter(PlayerEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(PlayerDropItemEvent.class, Item.class)
			.getter(PlayerDropItemEvent::getItemDrop)
			.build());
		registry.register(EventValue.builder(PlayerDropItemEvent.class, ItemStack.class)
			.getter(event -> event.getItemDrop().getItemStack())
			.build());
		registry.register(EventValue.builder(PlayerDropItemEvent.class, Entity.class)
			.getter(PlayerEvent::getPlayer)
			.build());
		// EntityDropItemEvent
		registry.register(EventValue.builder(EntityDropItemEvent.class, Item.class)
			.getter(EntityDropItemEvent::getItemDrop)
			.build());
		registry.register(EventValue.builder(EntityDropItemEvent.class, ItemStack.class)
			.getter(event -> event.getItemDrop().getItemStack())
			.build());
		// PlayerPickupItemEvent
		registry.register(EventValue.builder(PlayerPickupItemEvent.class, Player.class)
			.getter(PlayerEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(PlayerPickupItemEvent.class, Item.class)
			.getter(PlayerPickupItemEvent::getItem)
			.build());
		registry.register(EventValue.builder(PlayerPickupItemEvent.class, ItemStack.class)
			.getter(event -> event.getItem().getItemStack())
			.build());
		registry.register(EventValue.builder(PlayerPickupItemEvent.class, Entity.class)
			.getter(PlayerEvent::getPlayer)
			.build());
		// EntityPickupItemEvent
		registry.register(EventValue.builder(EntityPickupItemEvent.class, Entity.class)
			.getter(EntityPickupItemEvent::getEntity)
			.build());
		registry.register(EventValue.builder(EntityPickupItemEvent.class, Item.class)
			.getter(EntityPickupItemEvent::getItem)
			.build());
		registry.register(EventValue.builder(EntityPickupItemEvent.class, ItemType.class)
			.getter(event -> new ItemType(event.getItem().getItemStack()))
			.build());
		// PlayerItemConsumeEvent
		registry.register(EventValue.builder(PlayerItemConsumeEvent.class, ItemStack.class)
			.getter(PlayerItemConsumeEvent::getItem)
			.registerChanger(ChangeMode.SET, PlayerItemConsumeEvent::setItem)
			.build());
		// PlayerItemBreakEvent
		registry.register(EventValue.builder(PlayerItemBreakEvent.class, ItemStack.class)
			.getter(PlayerItemBreakEvent::getBrokenItem)
			.build());
		// PlayerInteractEntityEvent
		registry.register(EventValue.builder(PlayerInteractEntityEvent.class, Entity.class)
			.getter(PlayerInteractEntityEvent::getRightClicked)
			.build());
		registry.register(EventValue.builder(PlayerInteractEntityEvent.class, ItemStack.class)
			.getter(event -> {
				EquipmentSlot hand = event.getHand();
				if (hand == EquipmentSlot.HAND)
					return event.getPlayer().getInventory().getItemInMainHand();
				else if (hand == EquipmentSlot.OFF_HAND)
					return event.getPlayer().getInventory().getItemInOffHand();
				else
					return null;
			})
			.build());
		// PlayerInteractEvent
		registry.register(EventValue.builder(PlayerInteractEvent.class, ItemStack.class)
			.getter(PlayerInteractEvent::getItem)
			.build());
		registry.register(EventValue.builder(PlayerInteractEvent.class, Block.class)
			.getter(PlayerInteractEvent::getClickedBlock)
			.build());
		registry.register(EventValue.builder(PlayerInteractEvent.class, Direction.class)
			.getter(event -> new Direction(new double[]{event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ()}))
			.build());
		// PlayerShearEntityEvent
		registry.register(EventValue.builder(PlayerShearEntityEvent.class, Entity.class)
			.getter(PlayerShearEntityEvent::getEntity)
			.build());
		// PlayerMoveEvent
		registry.register(EventValue.builder(PlayerMoveEvent.class, Block.class)
			.getter(event -> event.getTo().clone().subtract(0, 0.5, 0).getBlock())
			.build());
		registry.register(EventValue.builder(PlayerMoveEvent.class, Location.class)
			.getter(PlayerMoveEvent::getFrom)
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(PlayerMoveEvent.class, Location.class)
			.getter(PlayerMoveEvent::getTo)
			.build());
		registry.register(EventValue.builder(PlayerMoveEvent.class, Chunk.class)
			.getter(event -> event.getFrom().getChunk())
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(PlayerMoveEvent.class, Chunk.class)
			.getter(event -> event.getTo().getChunk())
			.build());
		// PlayerItemDamageEvent
		registry.register(EventValue.builder(PlayerItemDamageEvent.class, ItemStack.class)
			.getter(PlayerItemDamageEvent::getItem)
			.build());
		//PlayerItemMendEvent
		registry.register(EventValue.builder(PlayerItemMendEvent.class, Player.class)
			.getter(PlayerEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(PlayerItemMendEvent.class, ItemStack.class)
			.getter(PlayerItemMendEvent::getItem)
			.build());
		registry.register(EventValue.builder(PlayerItemMendEvent.class, Entity.class)
			.getter(PlayerItemMendEvent::getExperienceOrb)
			.build());

		// --- HangingEvents ---

		// Note: will not work in HangingEntityBreakEvent due to event-entity being parsed as HangingBreakByEntityEvent#getRemover() from code down below
		registry.register(EventValue.builder(HangingEvent.class, Hanging.class)
			.getter(HangingEvent::getEntity)
			.build());
		registry.register(EventValue.builder(HangingEvent.class, World.class)
			.getter(event -> event.getEntity().getWorld())
			.build());
		registry.register(EventValue.builder(HangingEvent.class, Location.class)
			.getter(event -> event.getEntity().getLocation())
			.build());

		// HangingBreakEvent
		registry.register(EventValue.builder(HangingBreakEvent.class, Entity.class)
			.getter(event -> {
				if (event instanceof HangingBreakByEntityEvent hangingBreakByEntityEvent)
					return hangingBreakByEntityEvent.getRemover();
				return null;
			})
			.build());
		// HangingPlaceEvent
		registry.register(EventValue.builder(HangingPlaceEvent.class, Player.class)
			.getter(HangingPlaceEvent::getPlayer)
			.build());

		// --- VehicleEvents ---
		registry.register(EventValue.builder(VehicleEvent.class, Vehicle.class)
			.getter(VehicleEvent::getVehicle)
			.build());
		registry.register(EventValue.builder(VehicleEvent.class, World.class)
			.getter(event -> event.getVehicle().getWorld())
			.build());
		registry.register(EventValue.builder(VehicleExitEvent.class, LivingEntity.class)
			.getter(VehicleExitEvent::getExited)
			.build());

		registry.register(EventValue.builder(VehicleEnterEvent.class, Entity.class)
			.getter(VehicleEnterEvent::getEntered)
			.build());

		// We could error here instead but it's preferable to not do it in this case
		registry.register(EventValue.builder(VehicleDamageEvent.class, Entity.class)
			.getter(VehicleDamageEvent::getAttacker)
			.build());

		registry.register(EventValue.builder(VehicleDestroyEvent.class, Entity.class)
			.getter(VehicleDestroyEvent::getAttacker)
			.build());

		registry.register(EventValue.builder(VehicleEvent.class, Entity.class)
			.getter(event -> event.getVehicle().getPassenger())
			.build());


		// === CommandEvents ===
		// PlayerCommandPreprocessEvent is a PlayerEvent
		registry.register(EventValue.builder(ServerCommandEvent.class, CommandSender.class)
			.getter(ServerCommandEvent::getSender)
			.build());
		registry.register(EventValue.builder(CommandEvent.class, String[].class)
			.getter(CommandEvent::getArgs)
			.build());
		registry.register(EventValue.builder(CommandEvent.class, CommandSender.class)
			.getter(CommandEvent::getSender)
			.build());
		registry.register(EventValue.builder(CommandEvent.class, World.class)
			.getter(e -> e.getSender() instanceof Player ? ((Player) e.getSender()).getWorld() : null)
			.build());
		registry.register(EventValue.builder(CommandEvent.class, Block.class)
			.getter(event -> event.getSender() instanceof BlockCommandSender sender ? sender.getBlock() : null)
			.build());

		// === ServerEvents ===
		// Script load/unload event
		registry.register(EventValue.builder(ScriptEvent.class, CommandSender.class)
			.getter(event -> Bukkit.getConsoleSender())
			.build());
		// Server load event
		registry.register(EventValue.builder(SkriptStartEvent.class, CommandSender.class)
			.getter(event -> Bukkit.getConsoleSender())
			.build());
		// Server stop event
		registry.register(EventValue.builder(SkriptStopEvent.class, CommandSender.class)
			.getter(event -> Bukkit.getConsoleSender())
			.build());

		// === InventoryEvents ===
		// InventoryClickEvent
		registry.register(EventValue.builder(InventoryClickEvent.class, Player.class)
			.getter(event -> event.getWhoClicked() instanceof Player player ? player : null)
			.build());
		registry.register(EventValue.builder(InventoryClickEvent.class, World.class)
			.getter(event -> event.getWhoClicked().getWorld())
			.build());
		registry.register(EventValue.builder(InventoryClickEvent.class, ItemStack.class)
			.getter(InventoryClickEvent::getCurrentItem)
			.build());
		registry.register(EventValue.builder(InventoryClickEvent.class, Slot.class)
			.getter(event -> {
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
			})
			.build());
		registry.register(EventValue.builder(InventoryClickEvent.class, InventoryAction.class)
			.getter(InventoryClickEvent::getAction)
			.build());
		registry.register(EventValue.builder(InventoryClickEvent.class, ClickType.class)
			.getter(InventoryClickEvent::getClick)
			.build());
		registry.register(EventValue.builder(InventoryClickEvent.class, Inventory.class)
			.getter(InventoryClickEvent::getClickedInventory)
			.build());
		// InventoryDragEvent
		registry.register(EventValue.builder(InventoryDragEvent.class, Player.class)
			.getter(event -> event.getWhoClicked() instanceof Player player ? player : null)
			.build());
		registry.register(EventValue.builder(InventoryDragEvent.class, World.class)
			.getter(event -> event.getWhoClicked().getWorld())
			.build());
		registry.register(EventValue.builder(InventoryDragEvent.class, ItemStack.class)
			.getter(InventoryDragEvent::getOldCursor)
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(InventoryDragEvent.class, ItemStack.class)
			.getter(InventoryDragEvent::getCursor)
			.build());
		registry.register(EventValue.builder(InventoryDragEvent.class, ItemStack[].class)
			.getter(event -> event.getNewItems().values().toArray(new ItemStack[0]))
			.build());
		registry.register(EventValue.builder(InventoryDragEvent.class, Slot[].class)
			.getter(event -> {
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
			})
			.build());
		registry.register(EventValue.builder(InventoryDragEvent.class, ClickType.class)
			.getter(event -> event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT)
			.build());
		registry.register(EventValue.builder(InventoryDragEvent.class, Inventory[].class)
			.getter(event -> {
				Set<Inventory> inventories = new HashSet<>();
				InventoryView view = event.getView();
				for (Integer rawSlot : event.getRawSlots()) {
					Inventory inventory = InventoryUtils.getInventory(view, rawSlot);
					if (inventory != null)
						inventories.add(inventory);
				}
				return inventories.toArray(new Inventory[0]);
			})
			.build());
		// PrepareAnvilEvent
		if (Skript.classExists("com.destroystokyo.paper.event.inventory.PrepareResultEvent"))
			registry.register(EventValue.builder(PrepareAnvilEvent.class, ItemStack.class)
				.getter(PrepareResultEvent::getResult)
				.build());
		//BlockFertilizeEvent
		registry.register(EventValue.builder(BlockFertilizeEvent.class, Player.class)
			.getter(BlockFertilizeEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(BlockFertilizeEvent.class, Block[].class)
			.getter(event -> event.getBlocks().stream()
				.map(BlockState::getBlock)
				.toArray(Block[]::new))
			.build());
		// PrepareItemCraftEvent
		registry.register(EventValue.builder(PrepareItemCraftEvent.class, Slot.class)
			.getter(event -> new InventorySlot(event.getInventory(), 0))
			.build());
		registry.register(EventValue.builder(PrepareItemCraftEvent.class, ItemStack.class)
				.getter(event -> {
				ItemStack item = event.getInventory().getResult();
				return item != null ? item : AIR_IS;
			})
			.build());
		registry.register(EventValue.builder(PrepareItemCraftEvent.class, Player.class)
			.getter(event -> {
				List<HumanEntity> viewers = event.getInventory().getViewers(); // Get all viewers
				if (viewers.isEmpty()) // ... if we don't have any
					return null;
				HumanEntity first = viewers.get(0); // Get first viewer and hope it is crafter
				if (first instanceof Player player) // Needs to be player... Usually it is
					return player;
				return null;
			})
			.build());
		// CraftEvents - recipe namespaced key strings
		registry.register(EventValue.builder(CraftItemEvent.class, String.class)
			.getter(event -> {
				Recipe recipe = event.getRecipe();
				if (recipe instanceof Keyed keyed)
					return keyed.getKey().toString();
				return null;
			})
			.build());
		registry.register(EventValue.builder(PrepareItemCraftEvent.class, String.class)
			.getter(event -> {
				Recipe recipe = event.getRecipe();
				if (recipe instanceof Keyed keyed)
					return keyed.getKey().toString();
				return null;
			})
			.build());
		// CraftItemEvent
		registry.register(EventValue.builder(CraftItemEvent.class, ItemStack.class)
			.getter(event -> {
				Recipe recipe = event.getRecipe();
				if (recipe instanceof ComplexRecipe)
					return event.getCurrentItem();
				return recipe.getResult();
			})
			.build());
		//InventoryEvent
		registry.register(EventValue.builder(InventoryEvent.class, Inventory.class)
			.getter(InventoryEvent::getInventory)
			.build());
		//InventoryOpenEvent
		registry.register(EventValue.builder(InventoryOpenEvent.class, Player.class)
			.getter(event -> (Player) event.getPlayer())
			.build());
		//InventoryCloseEvent
		registry.register(EventValue.builder(InventoryCloseEvent.class, Player.class)
			.getter(event -> (Player) event.getPlayer())
			.build());
		if (Skript.classExists("org.bukkit.event.inventory.InventoryCloseEvent$Reason"))
			registry.register(EventValue.builder(InventoryCloseEvent.class, InventoryCloseEvent.Reason.class)
				.getter(InventoryCloseEvent::getReason)
				.build());
		//InventoryPickupItemEvent
		registry.register(EventValue.builder(InventoryPickupItemEvent.class, Inventory.class)
			.getter(InventoryPickupItemEvent::getInventory)
			.build());
		registry.register(EventValue.builder(InventoryPickupItemEvent.class, Item.class)
			.getter(InventoryPickupItemEvent::getItem)
			.build());
		registry.register(EventValue.builder(InventoryPickupItemEvent.class, ItemStack.class)
			.getter(event -> event.getItem().getItemStack())
			.build());
		//PortalCreateEvent
		registry.register(EventValue.builder(PortalCreateEvent.class, World.class)
			.getter(WorldEvent::getWorld)
			.build());
		registry.register(EventValue.builder(PortalCreateEvent.class, Block[].class)
			.getter(event -> event.getBlocks().stream()
				.map(BlockState::getBlock)
				.toArray(Block[]::new))
			.build());
		if (Skript.methodExists(PortalCreateEvent.class, "getEntity")) { // Minecraft 1.14+
			registry.register(EventValue.builder(PortalCreateEvent.class, Entity.class)
				.getter(PortalCreateEvent::getEntity)
				.build());
		}
		//PlayerEditBookEvent
		registry.register(EventValue.builder(PlayerEditBookEvent.class, ItemStack.class)
			.getter(event -> {
				ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
				book.setItemMeta(event.getPreviousBookMeta());
				return book;
			})
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(PlayerEditBookEvent.class, ItemStack.class)
			.getter(event -> {
				ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
				book.setItemMeta(event.getNewBookMeta());
				return book;
			})
			.build());
		registry.register(EventValue.builder(PlayerEditBookEvent.class, String[].class)
			.getter(event -> event.getPreviousBookMeta().getPages().toArray(new String[0]))
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(PlayerEditBookEvent.class, String[].class)
			.getter(event -> event.getNewBookMeta().getPages().toArray(new String[0]))
			.build());
		//ItemDespawnEvent
		registry.register(EventValue.builder(ItemDespawnEvent.class, Item.class)
			.getter(ItemDespawnEvent::getEntity)
			.build());
		registry.register(EventValue.builder(ItemDespawnEvent.class, ItemStack.class)
			.getter(event -> event.getEntity().getItemStack())
			.build());
		//ItemMergeEvent
		registry.register(EventValue.builder(ItemMergeEvent.class, Item.class)
			.getter(ItemMergeEvent::getEntity)
			.build());
		registry.register(EventValue.builder(ItemMergeEvent.class, Item.class)
			.getter(ItemMergeEvent::getTarget)
			.time(Time.FUTURE)
			.build());
		registry.register(EventValue.builder(ItemMergeEvent.class, ItemStack.class)
			.getter(event -> event.getEntity().getItemStack())
			.build());
		//PlayerTeleportEvent
		registry.register(EventValue.builder(PlayerTeleportEvent.class, TeleportCause.class)
			.getter(PlayerTeleportEvent::getCause)
			.build());
		//EntityMoveEvent
		if (Skript.classExists("io.papermc.paper.event.entity.EntityMoveEvent")) {
			registry.register(EventValue.builder(EntityMoveEvent.class, Location.class)
				.getter(EntityMoveEvent::getFrom)
				.build());
			registry.register(EventValue.builder(EntityMoveEvent.class, Location.class)
				.getter(EntityMoveEvent::getTo)
				.time(Time.FUTURE)
				.build());
		}
		//CreatureSpawnEvent
		registry.register(EventValue.builder(CreatureSpawnEvent.class, SpawnReason.class)
			.getter(CreatureSpawnEvent::getSpawnReason)
			.build());
		//PlayerRespawnEvent - 1.21.5+ added AbstractRespawnEvent as a base class, where prior to that, getRespawnReason was in PlayerRespawnEvent
		if (Skript.classExists("org.bukkit.event.player.AbstractRespawnEvent")) {
			registry.register(EventValue.builder(PlayerRespawnEvent.class, RespawnReason.class)
				.getter(PlayerRespawnEvent::getRespawnReason)
				.build());
		} else {
			try {
				Method method = PlayerRespawnEvent.class.getMethod("getRespawnReason");
				registry.register(EventValue.builder(PlayerRespawnEvent.class, RespawnReason.class)
					.getter(event -> {
						try {
							return (RespawnReason) method.invoke(event);
						} catch (Exception e) {
							return null;
						}
					})
					.build());
			} catch (NoSuchMethodException ignored) {}
		}
		//FireworkExplodeEvent
		registry.register(EventValue.builder(FireworkExplodeEvent.class, Firework.class)
			.getter(FireworkExplodeEvent::getEntity)
			.build());
		registry.register(EventValue.builder(FireworkExplodeEvent.class, FireworkEffect.class)
			.getter(event -> {
				List<FireworkEffect> effects = event.getEntity().getFireworkMeta().getEffects();
				if (effects.isEmpty())
					return null;
				return effects.get(0);
			})
			.build());
		registry.register(EventValue.builder(FireworkExplodeEvent.class, Color[].class)
			.getter(event -> {
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
			})
			.build());
		//PlayerRiptideEvent
		registry.register(EventValue.builder(PlayerRiptideEvent.class, ItemStack.class)
			.getter(PlayerRiptideEvent::getItem)
			.build());
		//PlayerInventorySlotChangeEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerInventorySlotChangeEvent")) {
			registry.register(EventValue.builder(PlayerInventorySlotChangeEvent.class, ItemStack.class)
				.getter(PlayerInventorySlotChangeEvent::getNewItemStack)
				.build());
			registry.register(EventValue.builder(PlayerInventorySlotChangeEvent.class, ItemStack.class)
				.getter(PlayerInventorySlotChangeEvent::getOldItemStack)
				.time(Time.PAST)
				.build());
			registry.register(EventValue.builder(PlayerInventorySlotChangeEvent.class, Slot.class)
				.getter(event -> {
					PlayerInventory inv = event.getPlayer().getInventory();
					int slotIndex = event.getSlot();
					// Not all indices point to inventory slots. Equipment, for example
					if (slotIndex >= 36) {
						return new ch.njol.skript.util.slot.EquipmentSlot(event.getPlayer(), slotIndex);
					} else {
						return new InventorySlot(inv, slotIndex);
					}
				})
				.build());
		}
		//PrepareItemEnchantEvent
		registry.register(EventValue.builder(PrepareItemEnchantEvent.class, Player.class)
			.getter(PrepareItemEnchantEvent::getEnchanter)
			.build());
		registry.register(EventValue.builder(PrepareItemEnchantEvent.class, ItemStack.class)
			.getter(PrepareItemEnchantEvent::getItem)
			.build());
		registry.register(EventValue.builder(PrepareItemEnchantEvent.class, Block.class)
			.getter(PrepareItemEnchantEvent::getEnchantBlock)
			.build());
		//EnchantItemEvent
		registry.register(EventValue.builder(EnchantItemEvent.class, Player.class)
			.getter(EnchantItemEvent::getEnchanter)
			.build());
		registry.register(EventValue.builder(EnchantItemEvent.class, ItemStack.class)
			.getter(EnchantItemEvent::getItem)
			.build());
		registry.register(EventValue.builder(EnchantItemEvent.class, EnchantmentType[].class)
			.getter(event -> event.getEnchantsToAdd().entrySet().stream()
				.map(entry -> new EnchantmentType(entry.getKey(), entry.getValue()))
				.toArray(EnchantmentType[]::new))
			.build());
		registry.register(EventValue.builder(EnchantItemEvent.class, Block.class)
			.getter(EnchantItemEvent::getEnchantBlock)
			.build());
		registry.register(EventValue.builder(HorseJumpEvent.class, Entity.class)
			.getter(HorseJumpEvent::getEntity)
			.build());
		// PlayerTradeEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerTradeEvent")) {
			registry.register(EventValue.builder(PlayerTradeEvent.class, AbstractVillager.class)
				.getter(PlayerTradeEvent::getVillager)
				.build());
		}
		// PlayerChangedWorldEvent
		registry.register(EventValue.builder(PlayerChangedWorldEvent.class, World.class)
			.getter(PlayerChangedWorldEvent::getFrom)
			.time(Time.PAST)
			.build());

		// PlayerEggThrowEvent
		registry.register(EventValue.builder(PlayerEggThrowEvent.class, Egg.class)
			.getter(PlayerEggThrowEvent::getEgg)
			.build());

		// PlayerStopUsingItemEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerStopUsingItemEvent")) {
			registry.register(EventValue.builder(PlayerStopUsingItemEvent.class, Timespan.class)
				.getter(event -> new Timespan(Timespan.TimePeriod.TICK, event.getTicksHeldFor()))
				.build());
			registry.register(EventValue.builder(PlayerStopUsingItemEvent.class, ItemType.class)
				.getter(event -> new ItemType(event.getItem()))
				.build());
		}

		// EntityResurrectEvent
		registry.register(EventValue.builder(EntityResurrectEvent.class, Slot.class)
			.getter(event -> {
				EquipmentSlot hand = event.getHand();
				EntityEquipment equipment = event.getEntity().getEquipment();
				if (equipment == null || hand == null)
					return null;
				return new ch.njol.skript.util.slot.EquipmentSlot(equipment, hand);
			})
			.build());

		// PlayerItemHeldEvent
		registry.register(EventValue.builder(PlayerItemHeldEvent.class, Slot.class)
			.getter(event -> new InventorySlot(event.getPlayer().getInventory(), event.getNewSlot()))
			.build());
		registry.register(EventValue.builder(PlayerItemHeldEvent.class, Slot.class)
			.getter(event -> new InventorySlot(event.getPlayer().getInventory(), event.getPreviousSlot()))
			.time(Time.PAST)
			.build());

		// PlayerPickupArrowEvent
		// This event value is restricted to MC 1.14+ due to an API change which has the return type changed
		// which throws a NoSuchMethodError if used in a 1.13 server.
		if (Skript.isRunningMinecraft(1, 14))
			registry.register(EventValue.builder(PlayerPickupArrowEvent.class, Projectile.class)
				.getter(PlayerPickupArrowEvent::getArrow)
				.build());

		registry.register(EventValue.builder(PlayerPickupArrowEvent.class, ItemStack.class)
			.getter(event -> event.getItem().getItemStack())
			.build());

		//PlayerQuitEvent
		if (Skript.classExists("org.bukkit.event.player.PlayerQuitEvent$QuitReason"))
			registry.register(EventValue.builder(PlayerQuitEvent.class, QuitReason.class)
				.getter(PlayerQuitEvent::getReason)
				.build());

		// PlayerStonecutterRecipeSelectEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent"))
			registry.register(EventValue.builder(PlayerStonecutterRecipeSelectEvent.class, ItemStack.class)
				.getter(event -> event.getStonecuttingRecipe().getResult())
				.build());

		// EntityTransformEvent
		registry.register(EventValue.builder(EntityTransformEvent.class, Entity[].class)
			.getter(event -> event.getTransformedEntities().stream().toArray(Entity[]::new))
			.build());
		registry.register(EventValue.builder(EntityTransformEvent.class, TransformReason.class)
			.getter(EntityTransformEvent::getTransformReason)
			.build());

		// BellRingEvent - these are BlockEvents and not EntityEvents, so they have declared methods for getEntity()
		if (Skript.classExists("org.bukkit.event.block.BellRingEvent")) {
			registry.register(EventValue.builder(BellRingEvent.class, Entity.class)
				.getter(BellRingEvent::getEntity)
				.build());

			registry.register(EventValue.builder(BellRingEvent.class, Direction.class)
				.getter(event -> new Direction(event.getDirection(), 1))
				.build());
		} else if (Skript.classExists("io.papermc.paper.event.block.BellRingEvent")) {
			registry.register(EventValue.builder(io.papermc.paper.event.block.BellRingEvent.class, Entity.class)
				.getter(BellRingEvent::getEntity)
				.build());
		}

		if (Skript.classExists("org.bukkit.event.block.BellResonateEvent")) {
			registry.register(EventValue.builder(BellResonateEvent.class, Entity[].class)
				.getter(event -> event.getResonatedEntities().toArray(new LivingEntity[0]))
				.build());
		}

		// InventoryMoveItemEvent
		registry.register(EventValue.builder(InventoryMoveItemEvent.class, Inventory.class)
			.getter(InventoryMoveItemEvent::getSource)
			.build());
		registry.register(EventValue.builder(InventoryMoveItemEvent.class, Inventory.class)
			.getter(InventoryMoveItemEvent::getDestination)
			.time(Time.FUTURE)
			.build());
		registry.register(EventValue.builder(InventoryMoveItemEvent.class, Block.class)
			.getter(event -> event.getSource().getLocation().getBlock())
			.build());
		registry.register(EventValue.builder(InventoryMoveItemEvent.class, Block.class)
			.getter(event -> event.getDestination().getLocation().getBlock())
			.time(Time.FUTURE)
			.build());
		registry.register(EventValue.builder(InventoryMoveItemEvent.class, ItemStack.class)
			.getter(InventoryMoveItemEvent::getItem)
			.build());

		// EntityRegainHealthEvent
		registry.register(EventValue.builder(EntityRegainHealthEvent.class, RegainReason.class)
			.getter(EntityRegainHealthEvent::getRegainReason)
			.build());

		// FurnaceExtractEvent
		registry.register(EventValue.builder(FurnaceExtractEvent.class, Player.class)
			.getter(FurnaceExtractEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(FurnaceExtractEvent.class, ItemStack[].class)
			.getter(event -> new ItemStack[]{ItemStack.of(event.getItemType(), event.getItemAmount())})
			.build());

		// BlockDropItemEvent
		registry.register(EventValue.builder(BlockDropItemEvent.class, Block.class)
			.getter(event -> new BlockStateBlock(event.getBlockState()))
			.time(Time.PAST)
			.build());
		registry.register(EventValue.builder(BlockDropItemEvent.class, Player.class)
			.getter(BlockDropItemEvent::getPlayer)
			.build());
		registry.register(EventValue.builder(BlockDropItemEvent.class, ItemStack[].class)
			.getter(event -> event.getItems().stream().map(Item::getItemStack).toArray(ItemStack[]::new))
			.build());
		registry.register(EventValue.builder(BlockDropItemEvent.class, Entity[].class)
			.getter(event -> event.getItems().toArray(Entity[]::new))
			.build());

		// PlayerExpCooldownChangeEvent
		registry.register(EventValue.builder(PlayerExpCooldownChangeEvent.class, ChangeReason.class)
			.getter(PlayerExpCooldownChangeEvent::getReason)
			.build());
		registry.register(EventValue.builder(PlayerExpCooldownChangeEvent.class, Timespan.class)
			.getter(event -> new Timespan(Timespan.TimePeriod.TICK, event.getNewCooldown()))
			.build());
		registry.register(EventValue.builder(PlayerExpCooldownChangeEvent.class, Timespan.class)
			.getter(event -> new Timespan(Timespan.TimePeriod.TICK, event.getPlayer().getExpCooldown()))
			.time(Time.PAST)
			.build());

		// VehicleMoveEvent
		registry.register(EventValue.builder(VehicleMoveEvent.class, Location.class)
			.getter(VehicleMoveEvent::getTo)
			.build());
		registry.register(EventValue.builder(VehicleMoveEvent.class, Location.class)
			.getter(VehicleMoveEvent::getFrom)
			.time(Time.PAST)
			.build());

		// BeaconEffectEvent
		if (Skript.classExists("com.destroystokyo.paper.event.block.BeaconEffectEvent")) {
			registry.register(EventValue.builder(BeaconEffectEvent.class, PotionEffectType.class)
				.getter(event -> event.getEffect().getType())
				.excludes(BeaconEffectEvent.class)
				.excludedErrorMessage("Use 'applied effect' in beacon effect events.")
				.build());
			registry.register(EventValue.builder(BeaconEffectEvent.class, Player.class)
				.getter(BeaconEffectEvent::getPlayer)
				.build());
		}
		// PlayerChangeBeaconEffectEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent")) {
			registry.register(EventValue.builder(PlayerChangeBeaconEffectEvent.class, Block.class)
				.getter(PlayerChangeBeaconEffectEvent::getBeacon)
				.build());
		}

		// PlayerElytraBoostEvent
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent")) {
			registry.register(EventValue.builder(PlayerElytraBoostEvent.class, ItemStack.class)
				.getter(PlayerElytraBoostEvent::getItemStack)
				.build());
			registry.register(EventValue.builder(PlayerElytraBoostEvent.class, Entity.class)
				.getter(PlayerElytraBoostEvent::getFirework)
				.build());
		}

		// === WorldBorderEvents ===
		if (Skript.classExists("io.papermc.paper.event.world.border.WorldBorderEvent")) {
			// WorldBorderEvent
			registry.register(EventValue.builder(WorldBorderEvent.class, WorldBorder.class)
				.getter(WorldBorderEvent::getWorldBorder)
				.build());

			// WorldBorderBoundsChangeEvent
			registry.register(EventValue.builder(WorldBorderBoundsChangeEvent.class, Number.class)
				.getter(WorldBorderBoundsChangeEvent::getNewSize)
				.build());
			registry.register(EventValue.builder(WorldBorderBoundsChangeEvent.class, Number.class)
				.getter(WorldBorderBoundsChangeEvent::getOldSize)
				.time(Time.PAST)
				.build());
			registry.register(EventValue.builder(WorldBorderBoundsChangeEvent.class, Timespan.class)
				.getter(event -> new Timespan(event.getDuration()))
				.build());

			// WorldBorderBoundsChangeFinishEvent
			registry.register(EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, Number.class)
				.getter(WorldBorderBoundsChangeFinishEvent::getNewSize)
				.build());
			registry.register(EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, Number.class)
				.getter(WorldBorderBoundsChangeFinishEvent::getOldSize)
				.time(Time.PAST)
				.build());
			registry.register(EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, Timespan.class)
				.getter(event -> new Timespan((long) event.getDuration()))
				.build());

			// WorldBorderCenterChangeEvent
			registry.register(EventValue.builder(WorldBorderCenterChangeEvent.class, Location.class)
				.getter(WorldBorderCenterChangeEvent::getNewCenter)
				.build());
			registry.register(EventValue.builder(WorldBorderCenterChangeEvent.class, Location.class)
				.getter(WorldBorderCenterChangeEvent::getOldCenter)
				.time(Time.PAST)
				.build());
		}

		if (Skript.classExists("org.bukkit.event.block.VaultDisplayItemEvent")) {
			registry.register(EventValue.builder(VaultDisplayItemEvent.class, ItemStack.class)
				.getter(VaultDisplayItemEvent::getDisplayItem)
				.registerChanger(ChangeMode.SET, VaultDisplayItemEvent::setDisplayItem)
				.build());
		}

		registry.register(EventValue.builder(VillagerCareerChangeEvent.class, VillagerCareerChangeEvent.ChangeReason.class)
			.getter(VillagerCareerChangeEvent::getReason)
			.build());
		registry.register(EventValue.builder(VillagerCareerChangeEvent.class, Villager.Profession.class)
			.getter(VillagerCareerChangeEvent::getProfession)
			.registerChanger(ChangeMode.SET, (event, profession) -> {
				if (profession == null)
					return;
				event.setProfession(profession);
			})
			.build());
		registry.register(EventValue.builder(VillagerCareerChangeEvent.class, Villager.Profession.class)
			.getter(event -> event.getEntity().getProfession())
			.time(Time.PAST)
			.build());

	}

}
