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
import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public final class BukkitEventValues {

	public BukkitEventValues() {
	}

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
		EventValues.registerEventValue(StructureGrowEvent.class, Block[].class, new Getter<Block[], StructureGrowEvent>() {
			@Override
			@Nullable
			public Block[] get(StructureGrowEvent event) {
				return event.getBlocks().stream()
					.map(BlockState::getBlock)
					.toArray(Block[]::new);
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, new Getter<Block, StructureGrowEvent>() {
			@Override
			@Nullable
			public Block get(StructureGrowEvent event) {
				for (final BlockState bs : event.getBlocks()) {
					if (bs.getLocation().equals(event.getLocation()))
						return new BlockStateBlock(bs);
				}
				return event.getLocation().getBlock();
			}
		}, EventValues.TIME_FUTURE);
		EventValues.registerEventValue(StructureGrowEvent.class, Block[].class, new Getter<Block[], StructureGrowEvent>() {
			@Override
			@Nullable
			public Block[] get(StructureGrowEvent event) {
				return event.getBlocks().stream()
					.map(BlockStateBlock::new)
					.toArray(Block[]::new);
			}
		}, EventValues.TIME_FUTURE);
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
		EventValues.registerEventValue(BlockPlaceEvent.class, ItemStack.class, new Getter<ItemStack, BlockPlaceEvent>() {
			@Override
			@Nullable
			public ItemStack get(BlockPlaceEvent event) {
				return event.getItemInHand();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(BlockPlaceEvent.class, ItemStack.class, new Getter<ItemStack, BlockPlaceEvent>() {
			@Override
			@Nullable
			public ItemStack get(BlockPlaceEvent event) {
				return event.getItemInHand();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(BlockPlaceEvent.class, ItemStack.class, new Getter<ItemStack, BlockPlaceEvent>() {
			@Override
			@Nullable
			public ItemStack get(BlockPlaceEvent event) {
				ItemStack item = event.getItemInHand().clone();
				if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
					item.setAmount(item.getAmount() - 1);
				return item;
			}
		}, EventValues.TIME_FUTURE);
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
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				final BlockState s = e.getBlock().getState();
				s.setType(s.getType() == Material.ICE ? Material.WATER : Material.AIR);
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
		EventValues.registerEventValue(SignChangeEvent.class, String[].class, new Getter<String[], SignChangeEvent>() {
			@Override
			@Nullable
			public String[] get(SignChangeEvent event) {
				return event.getLines();
			}
		}, EventValues.TIME_NOW);

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
		EventValues.registerEventValue(EntityDeathEvent.class, ItemStack[].class, new Getter<ItemStack[], EntityDeathEvent>() {
			@Override
			@Nullable
			public ItemStack[] get(EntityDeathEvent event) {
				return event.getDrops().toArray(new ItemStack[0]);
			}
		}, EventValues.TIME_NOW);
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

		// Entity Potion Effect
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffect.class, new Getter<PotionEffect, EntityPotionEffectEvent>() {
			@Override
			public PotionEffect get(EntityPotionEffectEvent event) {
				return event.getOldEffect();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffect.class, new Getter<PotionEffect, EntityPotionEffectEvent>() {
			@Override
			public PotionEffect get(EntityPotionEffectEvent event) {
				return event.getNewEffect();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, PotionEffectType.class, new Getter<PotionEffectType, EntityPotionEffectEvent>() {
			@Override
			public PotionEffectType get(EntityPotionEffectEvent event) {
				return event.getModifiedType();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(EntityPotionEffectEvent.class, EntityPotionEffectEvent.Cause.class, new Getter<EntityPotionEffectEvent.Cause, EntityPotionEffectEvent>() {
			@Override
			public EntityPotionEffectEvent.Cause get(EntityPotionEffectEvent event) {
				return event.getCause();
			}
		}, EventValues.TIME_NOW);

		// ProjectileHitEvent
		// ProjectileHitEvent#getHitBlock was added in 1.11
		if (Skript.methodExists(ProjectileHitEvent.class, "getHitBlock"))
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

		// EntityTeleportEvent
		EventValues.registerEventValue(EntityTeleportEvent.class, Location.class, new Getter<Location, EntityTeleportEvent>() {
			@Override
			public @Nullable Location get(EntityTeleportEvent event) {
				return event.getFrom();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(EntityTeleportEvent.class, Location.class, new Getter<Location, EntityTeleportEvent>() {
			@Override
			public @Nullable Location get(EntityTeleportEvent event) {
				return event.getTo();
			}
		}, EventValues.TIME_NOW);

		// EntityChangeBlockEvent
		EventValues.registerEventValue(EntityChangeBlockEvent.class, Block.class, new Getter<Block, EntityChangeBlockEvent>() {
			@Override
			@Nullable
			public Block get(EntityChangeBlockEvent event) {
				return event.getBlock();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(EntityChangeBlockEvent.class, Block.class, new Getter<Block, EntityChangeBlockEvent>() {
			@Override
			@Nullable
			public Block get(EntityChangeBlockEvent event) {
				return event.getBlock();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(EntityChangeBlockEvent.class, BlockData.class, new Getter<BlockData, EntityChangeBlockEvent>() {
			@Override
			@Nullable
			public BlockData get(EntityChangeBlockEvent event) {
				return event.getBlockData();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(EntityChangeBlockEvent.class, BlockData.class, new Getter<BlockData, EntityChangeBlockEvent>() {
			@Override
			@Nullable
			public BlockData get(EntityChangeBlockEvent event) {
				return event.getBlockData();
			}
		}, EventValues.TIME_FUTURE);

		// AreaEffectCloudApplyEvent
		EventValues.registerEventValue(AreaEffectCloudApplyEvent.class, LivingEntity[].class, new Getter<LivingEntity[], AreaEffectCloudApplyEvent>() {
			@Override
			@Nullable
			public LivingEntity[] get(AreaEffectCloudApplyEvent event) {
				return event.getAffectedEntities().toArray(new LivingEntity[0]);
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(AreaEffectCloudApplyEvent.class, PotionEffectType.class, new Getter<PotionEffectType, AreaEffectCloudApplyEvent>() {
			private final boolean HAS_POTION_TYPE_METHOD = Skript.methodExists(AreaEffectCloud.class, "getBasePotionType");
			@Override
			@Nullable
			public PotionEffectType get(AreaEffectCloudApplyEvent e) {
				// TODO needs to be reworked to support multiple values (there can be multiple potion effects)
				if (HAS_POTION_TYPE_METHOD) {
					PotionType base = e.getEntity().getBasePotionType();
					if (base != null)
						return base.getEffectType();
				} else {
					return e.getEntity().getBasePotionData().getType().getEffectType();
				}
				return null;
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
		// EndermanAttackPlayerEvent
		if (Skript.classExists("com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent")) {
			EventValues.registerEventValue(EndermanAttackPlayerEvent.class, Player.class, new Getter<Player, EndermanAttackPlayerEvent>() {
				@Override
				public Player get(EndermanAttackPlayerEvent event) {
					return event.getPlayer();
				}
			}, EventValues.TIME_NOW);
		}

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
				return e.getBlockClicked();
			}
		}, 0);
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, new Getter<Block, PlayerBucketFillEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBucketFillEvent e) {
				final BlockState s = e.getBlockClicked().getState();
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
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new Getter<Block, PlayerBucketEmptyEvent>() {
			@Override
			public Block get(final PlayerBucketEmptyEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setType(e.getBucket() == Material.WATER_BUCKET ? Material.WATER : Material.LAVA);
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
		// EntityDropItemEvent
		EventValues.registerEventValue(EntityDropItemEvent.class, Item.class, new Getter<Item, EntityDropItemEvent>() {
			@Override
			public Item get(EntityDropItemEvent event) {
				return event.getItemDrop();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(EntityDropItemEvent.class, ItemStack.class, new Getter<ItemStack, EntityDropItemEvent>() {
			@Override
			public ItemStack get(EntityDropItemEvent event) {
				return event.getItemDrop().getItemStack();
			}
		}, EventValues.TIME_NOW);
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
			public Block get(PlayerMoveEvent event) {
				return event.getTo().clone().subtract(0, 0.5, 0).getBlock();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerMoveEvent.class, Location.class, new Getter<Location, PlayerMoveEvent>() {
			@Override
			public Location get(PlayerMoveEvent event) {
				return event.getFrom();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(PlayerMoveEvent.class, Location.class, new Getter<Location, PlayerMoveEvent>() {
			@Override
			public Location get(PlayerMoveEvent event) {
				return event.getTo();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerMoveEvent.class, Chunk.class, new Getter<Chunk, PlayerMoveEvent>() {
			@Override
			public Chunk get(PlayerMoveEvent event) {
				return event.getFrom().getChunk();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(PlayerMoveEvent.class, Chunk.class, new Getter<Chunk, PlayerMoveEvent>() {
			@Override
			public Chunk get(PlayerMoveEvent event) {
				return event.getTo().getChunk();
			}
		}, EventValues.TIME_NOW);
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
		EventValues.registerEventValue(CommandEvent.class, String[].class, new Getter<String[], CommandEvent>() {
			@Override
			public String[] get(CommandEvent event) {
				return event.getArgs();
			}
		}, EventValues.TIME_NOW);
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
				if (invi == null)
					return null;
				int slotIndex = e.getSlot();

				// Not all indices point to inventory slots. Equipment, for example
				if (invi instanceof PlayerInventory && slotIndex >= 36) {
					return new ch.njol.skript.util.slot.EquipmentSlot(((PlayerInventory) invi).getHolder(), slotIndex);
				} else {
					return new InventorySlot(invi, slotIndex, e.getRawSlot());
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
		// InventoryDragEvent
		EventValues.registerEventValue(InventoryDragEvent.class, Player.class, new Getter<Player, InventoryDragEvent>() {
			@Override
			@Nullable
			public Player get(InventoryDragEvent event) {
				return event.getWhoClicked() instanceof Player ? (Player) event.getWhoClicked() : null;
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(InventoryDragEvent.class, World.class, new Getter<World, InventoryDragEvent>() {
			@Override
			@Nullable
			public World get(InventoryDragEvent event) {
				return event.getWhoClicked().getWorld();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(InventoryDragEvent.class, ItemStack.class, new Getter<ItemStack, InventoryDragEvent>() {
			@Override
			@Nullable
			public ItemStack get(InventoryDragEvent event) {
				return event.getOldCursor();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(InventoryDragEvent.class, ItemStack.class, new Getter<ItemStack, InventoryDragEvent>() {
			@Override
			@Nullable
			public ItemStack get(InventoryDragEvent event) {
				return event.getCursor();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(InventoryDragEvent.class, ItemStack[].class, new Getter<ItemStack[], InventoryDragEvent>() {
			@Override
			@Nullable
			public ItemStack[] get(InventoryDragEvent event) {
				return event.getNewItems().values().toArray(new ItemStack[0]);
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(InventoryDragEvent.class, Slot[].class, new Getter<Slot[], InventoryDragEvent>() {
			@Override
			@Nullable
			public Slot[] get(InventoryDragEvent event) {
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
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(InventoryDragEvent.class, ClickType.class, new Getter<ClickType, InventoryDragEvent>() {
			@Override
			@Nullable
			public ClickType get(InventoryDragEvent event) {
				return event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(InventoryDragEvent.class, Inventory[].class, new Getter<Inventory[], InventoryDragEvent>() {
			@Override
			@Nullable
			public Inventory[] get(InventoryDragEvent event) {
				Set<Inventory> inventories = new HashSet<>();
				InventoryView view = event.getView();
				for (Integer rawSlot : event.getRawSlots()) {
					Inventory inventory = InventoryUtils.getInventory(view, rawSlot);
					if (inventory != null)
						inventories.add(inventory);
				}
				return inventories.toArray(new Inventory[0]);
			}
		}, EventValues.TIME_NOW);
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
		EventValues.registerEventValue(BlockFertilizeEvent.class, Block[].class, new Getter<Block[], BlockFertilizeEvent>() {
			@Nullable
			@Override
			public Block[] get(BlockFertilizeEvent event) {
				return event.getBlocks().stream()
					.map(BlockState::getBlock)
					.toArray(Block[]::new);
			}
		}, EventValues.TIME_NOW);
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
		if (Skript.classExists("org.bukkit.event.inventory.InventoryCloseEvent$Reason"))
			EventValues.registerEventValue(InventoryCloseEvent.class, InventoryCloseEvent.Reason.class, new Getter<InventoryCloseEvent.Reason, InventoryCloseEvent>() {
				@Override
				public InventoryCloseEvent.Reason get(InventoryCloseEvent event) {
					return event.getReason();
				}
			}, EventValues.TIME_NOW);
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
		EventValues.registerEventValue(PortalCreateEvent.class, Block[].class, new Getter<Block[], PortalCreateEvent>() {
			@Override
			@Nullable
			public Block[] get(PortalCreateEvent event) {
				return event.getBlocks().stream()
					.map(BlockState::getBlock)
					.toArray(Block[]::new);
			}
		}, EventValues.TIME_NOW);
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
			public ItemStack get(PlayerEditBookEvent event) {
				ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
				book.setItemMeta(event.getPreviousBookMeta());
				return book;
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(PlayerEditBookEvent.class, ItemStack.class, new Getter<ItemStack, PlayerEditBookEvent>() {
			@Override
			public ItemStack get(PlayerEditBookEvent event) {
				ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
				book.setItemMeta(event.getNewBookMeta());
				return book;
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerEditBookEvent.class, String[].class, new Getter<String[], PlayerEditBookEvent>() {
			@Override
			public String[] get(PlayerEditBookEvent event) {
				return event.getPreviousBookMeta().getPages().toArray(new String[0]);
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(PlayerEditBookEvent.class, String[].class, new Getter<String[], PlayerEditBookEvent>() {
			@Override
			public String[] get(PlayerEditBookEvent event) {
				return event.getNewBookMeta().getPages().toArray(new String[0]);
			}
		}, EventValues.TIME_NOW);
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
		EventValues.registerEventValue(FireworkExplodeEvent.class, Color[].class, new Getter<Color[], FireworkExplodeEvent>() {
			@Override
			public Color @Nullable [] get(FireworkExplodeEvent event) {
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
			}
		}, EventValues.TIME_NOW);
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
		//PlayerInventorySlotChangeEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerInventorySlotChangeEvent")) {
			EventValues.registerEventValue(PlayerInventorySlotChangeEvent.class, ItemStack.class, new Getter<ItemStack, PlayerInventorySlotChangeEvent>() {
				@Override
				@Nullable
				public ItemStack get(PlayerInventorySlotChangeEvent event) {
					return event.getNewItemStack();
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(PlayerInventorySlotChangeEvent.class, ItemStack.class, new Getter<ItemStack, PlayerInventorySlotChangeEvent>() {
				@Override
				@Nullable
				public ItemStack get(PlayerInventorySlotChangeEvent event) {
					return event.getOldItemStack();
				}
			}, EventValues.TIME_PAST);
			EventValues.registerEventValue(PlayerInventorySlotChangeEvent.class, Slot.class, new Getter<Slot, PlayerInventorySlotChangeEvent>() {
				@Override
				@Nullable
				public Slot get(PlayerInventorySlotChangeEvent event) {
					PlayerInventory inv = event.getPlayer().getInventory();
					int slotIndex = event.getSlot();
					// Not all indices point to inventory slots. Equipment, for example
					if (slotIndex >= 36) {
						return new ch.njol.skript.util.slot.EquipmentSlot(event.getPlayer(), slotIndex);
					} else {
						return new InventorySlot(inv, slotIndex);
					}
				}
			}, EventValues.TIME_NOW);
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
		EventValues.registerEventValue(EnchantItemEvent.class, EnchantmentType[].class, new Getter<EnchantmentType[], EnchantItemEvent>() {
			@Override
			@Nullable
			public EnchantmentType[] get(EnchantItemEvent event) {
				return event.getEnchantsToAdd().entrySet().stream()
					.map(entry -> new EnchantmentType(entry.getKey(), entry.getValue()))
					.toArray(EnchantmentType[]::new);
			}
		}, EventValues.TIME_NOW);
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
		// PlayerTradeEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerTradeEvent")) {
			EventValues.registerEventValue(PlayerTradeEvent.class, AbstractVillager.class, new Getter<AbstractVillager, PlayerTradeEvent>() {
				@Override
				@Nullable
				public AbstractVillager get(PlayerTradeEvent event) {
					return event.getVillager();
				}
			}, EventValues.TIME_NOW);
		}
		// PlayerChangedWorldEvent
		EventValues.registerEventValue(PlayerChangedWorldEvent.class, World.class, new Getter<World, PlayerChangedWorldEvent>() {
			@Nullable
			@Override
			public World get(PlayerChangedWorldEvent e) {
				return e.getFrom();
			}
		}, -1);

		// PlayerEggThrowEvent
		EventValues.registerEventValue(PlayerEggThrowEvent.class, Egg.class, new Getter<Egg, PlayerEggThrowEvent>() {
			@Override
			@Nullable
			public Egg get(PlayerEggThrowEvent event) {
				return event.getEgg();
			}
		}, EventValues.TIME_NOW);

		// PlayerStopUsingItemEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerStopUsingItemEvent")) {
			EventValues.registerEventValue(PlayerStopUsingItemEvent.class, Timespan.class, new Getter<Timespan, PlayerStopUsingItemEvent>() {
				@Override
				public Timespan get(PlayerStopUsingItemEvent event) {
					return new Timespan(Timespan.TimePeriod.TICK, event.getTicksHeldFor());
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(PlayerStopUsingItemEvent.class, ItemType.class, new Getter<ItemType, PlayerStopUsingItemEvent>() {
				@Override
				public ItemType get(PlayerStopUsingItemEvent event) {
					return new ItemType(event.getItem());
				}
			}, EventValues.TIME_NOW);
		}

		// LootGenerateEvent
		if (Skript.classExists("org.bukkit.event.world.LootGenerateEvent")) {
			EventValues.registerEventValue(LootGenerateEvent.class, Entity.class, new Getter<Entity, LootGenerateEvent>() {
				@Override
				@Nullable
				public Entity get(LootGenerateEvent event) {
					return event.getEntity();
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(LootGenerateEvent.class, Location.class, new Getter<Location, LootGenerateEvent>() {
				@Override
				@Nullable
				public Location get(LootGenerateEvent event) {
					return event.getLootContext().getLocation();
				}
			}, EventValues.TIME_NOW);
		}

		// EntityResurrectEvent
		EventValues.registerEventValue(EntityResurrectEvent.class, Slot.class, new Getter<Slot, EntityResurrectEvent>() {
			@Override
			@Nullable
			public Slot get(EntityResurrectEvent event) {
				EquipmentSlot hand = event.getHand();
				EntityEquipment equipment = event.getEntity().getEquipment();
				if (equipment == null || hand == null)
					return null;
				return new ch.njol.skript.util.slot.EquipmentSlot(equipment,
					(hand == EquipmentSlot.HAND) ? ch.njol.skript.util.slot.EquipmentSlot.EquipSlot.TOOL
						: ch.njol.skript.util.slot.EquipmentSlot.EquipSlot.OFF_HAND);
			}
		}, EventValues.TIME_NOW);

		// PlayerItemHeldEvent
		EventValues.registerEventValue(PlayerItemHeldEvent.class, Slot.class, new Getter<Slot, PlayerItemHeldEvent>() {
			@Override
			@Nullable
			public Slot get(PlayerItemHeldEvent event) {
				return new InventorySlot(event.getPlayer().getInventory(), event.getNewSlot());
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerItemHeldEvent.class, Slot.class, new Getter<Slot, PlayerItemHeldEvent>() {
			@Override
			@Nullable
			public Slot get(PlayerItemHeldEvent event) {
				return new InventorySlot(event.getPlayer().getInventory(), event.getPreviousSlot());
			}
		}, EventValues.TIME_PAST);

		// PlayerPickupArrowEvent
		// This event value is restricted to MC 1.14+ due to an API change which has the return type changed
		// which throws a NoSuchMethodError if used in a 1.13 server.
		if (Skript.isRunningMinecraft(1, 14))
			EventValues.registerEventValue(PlayerPickupArrowEvent.class, Projectile.class, new Getter<Projectile, PlayerPickupArrowEvent>() {
				@Override
				public Projectile get(PlayerPickupArrowEvent event) {
					return event.getArrow();
				}
			}, EventValues.TIME_NOW);

		EventValues.registerEventValue(PlayerPickupArrowEvent.class, ItemStack.class, new Getter<ItemStack, PlayerPickupArrowEvent>() {
			@Override
			@Nullable
			public ItemStack get(PlayerPickupArrowEvent event) {
				return event.getItem().getItemStack();
			}
		}, EventValues.TIME_NOW);

		//PlayerQuitEvent
		if (Skript.classExists("org.bukkit.event.player.PlayerQuitEvent$QuitReason"))
			EventValues.registerEventValue(PlayerQuitEvent.class, QuitReason.class, new Getter<QuitReason, PlayerQuitEvent>() {
				@Override
				@Nullable
				public QuitReason get(PlayerQuitEvent event) {
					return event.getReason();
				}
			}, EventValues.TIME_NOW);

		// PlayerStonecutterRecipeSelectEvent
		if (Skript.classExists("io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent"))
			EventValues.registerEventValue(PlayerStonecutterRecipeSelectEvent.class, ItemStack.class, new Getter<ItemStack, PlayerStonecutterRecipeSelectEvent>() {
				@Override
				public ItemStack get(PlayerStonecutterRecipeSelectEvent event) {
					return event.getStonecuttingRecipe().getResult();
				}
			}, EventValues.TIME_NOW);

		// EntityTransformEvent
		EventValues.registerEventValue(EntityTransformEvent.class, Entity[].class, new Getter<Entity[], EntityTransformEvent>() {
			@Override
			@Nullable
			public Entity[] get(EntityTransformEvent event) {
				return event.getTransformedEntities().stream().toArray(Entity[]::new);
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(EntityTransformEvent.class, TransformReason.class, new Getter<TransformReason, EntityTransformEvent>() {
			@Override
			@Nullable
			public TransformReason get(EntityTransformEvent event) {
				return event.getTransformReason();
			}
		}, EventValues.TIME_NOW);

		// BellRingEvent - these are BlockEvents and not EntityEvents, so they have declared methods for getEntity()
		if (Skript.classExists("org.bukkit.event.block.BellRingEvent")) {
			EventValues.registerEventValue(BellRingEvent.class, Entity.class, new Getter<Entity, BellRingEvent>() {
				@Override
				@Nullable
				public Entity get(BellRingEvent event) {
					return event.getEntity();
				}
			}, EventValues.TIME_NOW);

			EventValues.registerEventValue(BellRingEvent.class, Direction.class, new Getter<Direction, BellRingEvent>() {
				@Override
				public Direction get(BellRingEvent event) {
					return new Direction(event.getDirection(), 1);
				}
			}, EventValues.TIME_NOW);
		} else if (Skript.classExists("io.papermc.paper.event.block.BellRingEvent")) {
			EventValues.registerEventValue(
				io.papermc.paper.event.block.BellRingEvent.class, Entity.class,
				new Getter<Entity, io.papermc.paper.event.block.BellRingEvent>() {
					@Override
					@Nullable
					public Entity get(io.papermc.paper.event.block.BellRingEvent event) {
						return event.getEntity();
					}
				}, EventValues.TIME_NOW);
		}

		if (Skript.classExists("org.bukkit.event.block.BellResonateEvent")) {
			EventValues.registerEventValue(BellResonateEvent.class, Entity[].class, new Getter<Entity[], BellResonateEvent>() {
				@Override
				@Nullable
				public Entity[] get(BellResonateEvent event) {
					return event.getResonatedEntities().toArray(new LivingEntity[0]);
				}
			}, EventValues.TIME_NOW);
		}

		// InventoryMoveItemEvent
		EventValues.registerEventValue(InventoryMoveItemEvent.class, Inventory.class, new Getter<Inventory, InventoryMoveItemEvent>() {
			@Override
			public Inventory get(InventoryMoveItemEvent event) {
				return event.getSource();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(InventoryMoveItemEvent.class, Inventory.class, new Getter<Inventory, InventoryMoveItemEvent>() {
			@Override
			public Inventory get(InventoryMoveItemEvent event) {
				return event.getDestination();
			}
		}, EventValues.TIME_FUTURE);
		EventValues.registerEventValue(InventoryMoveItemEvent.class, Block.class, new Getter<Block, InventoryMoveItemEvent>() {
			@Override
			public Block get(InventoryMoveItemEvent event) {
				return event.getSource().getLocation().getBlock();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(InventoryMoveItemEvent.class, Block.class, new Getter<Block, InventoryMoveItemEvent>() {
			@Override
			public Block get(InventoryMoveItemEvent event) {
				return event.getDestination().getLocation().getBlock();
			}
		}, EventValues.TIME_FUTURE);
		EventValues.registerEventValue(InventoryMoveItemEvent.class, ItemStack.class, new Getter<ItemStack, InventoryMoveItemEvent>() {
			@Override
			public ItemStack get(InventoryMoveItemEvent event) {
				return event.getItem();
			}
		}, EventValues.TIME_NOW);

		// EntityRegainHealthEvent
		EventValues.registerEventValue(EntityRegainHealthEvent.class, RegainReason.class, new Getter<RegainReason, EntityRegainHealthEvent>() {
			@Override
			@Nullable
			public RegainReason get(EntityRegainHealthEvent event) {
				return event.getRegainReason();
			}
		}, EventValues.TIME_NOW);

		// BlockDropItemEvent
		EventValues.registerEventValue(BlockDropItemEvent.class, Block.class, new Getter<Block, BlockDropItemEvent>() {
			@Override
			public Block get(BlockDropItemEvent event) {
				return new BlockStateBlock(event.getBlockState());
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(BlockDropItemEvent.class, Player.class, new Getter<Player, BlockDropItemEvent>() {
			@Override
			public Player get(BlockDropItemEvent event) {
				return event.getPlayer();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(BlockDropItemEvent.class, ItemStack[].class, new Getter<ItemStack[], BlockDropItemEvent>() {
			@Override
			public ItemStack[] get(BlockDropItemEvent event) {
				return event.getItems().stream().map(Item::getItemStack).toArray(ItemStack[]::new);
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(BlockDropItemEvent.class, Entity[].class, new Getter<Entity[], BlockDropItemEvent>() {
			@Override
			public Entity[] get(BlockDropItemEvent event) {
				return event.getItems().toArray(Entity[]::new);
			}
		}, EventValues.TIME_NOW);

		// PlayerExpCooldownChangeEvent
		EventValues.registerEventValue(PlayerExpCooldownChangeEvent.class, ChangeReason.class, new Getter<ChangeReason, PlayerExpCooldownChangeEvent>() {
			@Override
			public ChangeReason get(PlayerExpCooldownChangeEvent event) {
				return event.getReason();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerExpCooldownChangeEvent.class, Timespan.class, new Getter<Timespan, PlayerExpCooldownChangeEvent>() {
			@Override
			public Timespan get(PlayerExpCooldownChangeEvent event) {
				return new Timespan(Timespan.TimePeriod.TICK, event.getNewCooldown());
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerExpCooldownChangeEvent.class, Timespan.class, new Getter<Timespan, PlayerExpCooldownChangeEvent>() {
			@Override
			public Timespan get(PlayerExpCooldownChangeEvent event) {
				return new Timespan(Timespan.TimePeriod.TICK, event.getPlayer().getExpCooldown());
			}
		}, EventValues.TIME_PAST);

		// VehicleMoveEvent
		EventValues.registerEventValue(VehicleMoveEvent.class, Location.class, new Getter<>() {
			@Override
			public Location get(VehicleMoveEvent event) {
				return event.getTo();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(VehicleMoveEvent.class, Location.class, new Getter<>() {
			@Override
			public Location get(VehicleMoveEvent event) {
				return event.getFrom();
			}
		}, EventValues.TIME_PAST);

	}

}
