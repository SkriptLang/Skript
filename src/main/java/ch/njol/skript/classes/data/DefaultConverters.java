package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.bukkitutil.SkullUtils;
import ch.njol.skript.command.Commands;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.lang.util.common.AnyOwner;
import ch.njol.skript.util.*;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Skull;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.script.Script;

import java.util.UUID;

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
			if (holder instanceof Entity entity)
				return entity;
			return null;
		}, Converter.NO_CHAINING);

		// Anything with a name -> AnyNamed
		Converters.registerConverter(OfflinePlayer.class, AnyNamed.class, player -> player::getName, Converter.NO_RIGHT_CHAINING);
		if (Skript.classExists("org.bukkit.generator.WorldInfo"))
			Converters.registerConverter(World.class, AnyNamed.class, world -> world::getName, Converter.NO_RIGHT_CHAINING);
		else //noinspection RedundantCast getName method is on World itself in older versions
			Converters.registerConverter(World.class, AnyNamed.class, world -> () -> ((World) world).getName(), Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(GameRule.class, AnyNamed.class, rule -> rule::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Server.class, AnyNamed.class, server -> server::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Plugin.class, AnyNamed.class, plugin -> plugin::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(WorldType.class, AnyNamed.class, type -> type::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Team.class, AnyNamed.class, team -> team::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Objective.class, AnyNamed.class, objective -> objective::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Nameable.class, AnyNamed.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			nameable -> new AnyNamed() {
				@Override
				public @UnknownNullability String name() {
					//noinspection deprecation
					return nameable.getCustomName();
				}

				@Override
				public boolean supportsNameChange() {
					return true;
				}

				@Override
				public void setName(String name) {
					//noinspection deprecation
					nameable.setCustomName(name);
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Block.class, AnyNamed.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			block -> new AnyNamed() {
				@Override
				public @UnknownNullability String name() {
					BlockState state = block.getState();
					if (state instanceof Nameable nameable)
						//noinspection deprecation
						return nameable.getCustomName();
					return null;
				}

				@Override
				public boolean supportsNameChange() {
					return true;
				}

				@Override
				public void setName(String name) {
					BlockState state = block.getState();
					if (state instanceof Nameable nameable) {
						//noinspection deprecation
						nameable.setCustomName(name);
						state.update(true, false);
					}
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(CommandSender.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		// Command senders should be done last because there might be a better alternative above

		// Anything with an amount -> AnyAmount
		Converters.registerConverter(ItemStack.class, AnyAmount.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			item -> new AnyAmount() {

				@Override
				public @NotNull Number amount() {
					return item.getAmount();
				}

				@Override
				public boolean supportsAmountChange() {
					return true;
				}

				@Override
				public void setAmount(Number amount) {
					item.setAmount(amount != null ? amount.intValue() : 0);
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);

		// InventoryHolder - Location
		// since the individual ones can't be trusted to chain.
		Converters.registerConverter(InventoryHolder.class, Location.class, holder -> {
			if (holder instanceof Entity entity)
				return entity.getLocation();
			if (holder instanceof Block block)
				return block.getLocation();
			if (holder instanceof BlockState state)
				return BlockUtils.getLocation(state.getBlock());
			if (holder instanceof DoubleChest doubleChest) {
				if (doubleChest.getLeftSide() != null) {
					return BlockUtils.getLocation(((BlockState) doubleChest.getLeftSide()).getBlock());
				} else if (doubleChest.getRightSide() != null) {
					return BlockUtils.getLocation(((BlockState) doubleChest.getRightSide()).getBlock());
				}
			}
			return null;
		});

		// Enchantment - EnchantmentType
		Converters.registerConverter(Enchantment.class, EnchantmentType.class, e -> new EnchantmentType(e, -1));

		// Vector - Direction
		Converters.registerConverter(Vector.class, Direction.class, Direction::new);

		// EnchantmentOffer - EnchantmentType
		Converters.registerConverter(EnchantmentOffer.class, EnchantmentType.class, eo -> new EnchantmentType(eo.getEnchantment(), eo.getEnchantmentLevel()));

		Converters.registerConverter(String.class, World.class, Bukkit::getWorld);

		if (Skript.classExists("org.bukkit.entity.EntitySnapshot"))
			Converters.registerConverter(EntitySnapshot.class, EntityData.class, snapshot -> EntityUtils.toSkriptEntityData(snapshot.getEntityType()));

		// Script -> Config & Node
		Converters.registerConverter(Script.class, Config.class, Script::getConfig);
		Converters.registerConverter(Config.class, Node.class, Config::getMainNode);

		// UUID -> String
		Converters.registerConverter(UUID.class, String.class, UUID::toString);

		// Item(Entity) -> AnyOwner
		Converters.registerConverter(Item.class, AnyOwner.class, from -> new AnyOwner<UUID>() {
			@Override
			public UUID getOwner() {
				return from.getOwner();
			}

			@Override
			public void setOwner(@Nullable UUID value) {
				from.setOwner(value);
			}

			@Override
			public boolean supportsChangingOwner() {
				return true;
			}

			@Override
			public boolean supportsChangeValue(Class<?> classy) {
				return UUID.class.isAssignableFrom(classy);
			}
		});

		// Block -> AnyOwner
		Converters.registerConverter(Block.class, AnyOwner.class, from -> {
			BlockState state = from.getState();
			if (!(state instanceof Skull))
				return null;
			return new AnyOwner<OfflinePlayer>() {
				@Override
				public OfflinePlayer getOwner() {
					return SkullUtils.getOwningPlayer(from);
				}

				@Override
				public void setOwner(@Nullable OfflinePlayer value) {
					if (value == null)
						return;
					SkullUtils.setOwningPlayer(from, value);
				}

				@Override
				public boolean supportsChangingOwner() {
					return true;
				}

				@Override
				public boolean supportsChangeValue(Class<?> classy) {
					return OfflinePlayer.class.isAssignableFrom(classy);
				}
			};
		});

		// Entity -> AnyOwner
		Converters.registerConverter(Entity.class, AnyOwner.class, from -> {
			if (from instanceof AreaEffectCloud areaEffectCloud) {
				return new AnyOwner<UUID>() {

					@Override
					public @UnknownNullability UUID getOwner() {
						return areaEffectCloud.getOwnerUniqueId();
					}

					@Override
					public void setOwner(@Nullable UUID value) {
						areaEffectCloud.setOwnerUniqueId(value);
					}

					@Override
					public boolean supportsChangingOwner() {
						return true;
					}

					@Override
					public boolean supportsChangeValue(Class<?> classy) {
						return UUID.class.isAssignableFrom(classy);
					}
				};
			} else if (from instanceof EvokerFangs evokerFangs) {
				return new AnyOwner<LivingEntity>() {
					@Override
					public @UnknownNullability LivingEntity getOwner() {
						return evokerFangs.getOwner();
					}

					@Override
					public void setOwner(@Nullable LivingEntity value) {
						evokerFangs.setOwner(value);
					}

					@Override
					public boolean supportsChangingOwner() {
						return true;
					}

					@Override
					public boolean supportsChangeValue(Class<?> classy) {
						return LivingEntity.class.isAssignableFrom(classy);
					}
				};
			} else if (from instanceof Tameable tameable) {
				return new AnyOwner<AnimalTamer>() {
					@Override
					public @UnknownNullability AnimalTamer getOwner() {
						return tameable.getOwner();
					}

					@Override
					public void setOwner(@Nullable AnimalTamer value) {
						tameable.setOwner(value);
					}

					@Override
					public boolean supportsChangingOwner() {
						return true;
					}

					@Override
					public boolean supportsChangeValue(Class<?> classy) {
						return AnimalTamer.class.isAssignableFrom(classy);
					}
				};
			}
			return null;
		});

		// ItemType -> AnyOwner
		Converters.registerConverter(ItemType.class, AnyOwner.class, from -> {
			if (!(from.getItemMeta() instanceof SkullMeta))
				return null;
			return new AnyOwner<OfflinePlayer>() {
				@Override
				public @UnknownNullability OfflinePlayer getOwner() {
					return SkullUtils.getOwningPlayer(from);
				}

				@Override
				public void setOwner(@Nullable OfflinePlayer value) {
					SkullUtils.setOwningPlayer(from, value);
				}

				@Override
				public boolean supportsChangingOwner() {
					return true;
				}

				@Override
				public boolean supportsChangeValue(Class<?> classy) {
					return OfflinePlayer.class.isAssignableFrom(classy);
				}
			};
		});

	}

}
