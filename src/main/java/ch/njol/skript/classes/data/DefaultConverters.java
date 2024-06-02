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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.command.Commands;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.util.common.*;
import ch.njol.skript.util.*;
import ch.njol.skript.util.scoreboard.Criterion;
import ch.njol.skript.util.scoreboard.ScoreUtils;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.*;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.Collection;
import java.util.List;

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

		// Anything with a name -> AnyNamed
		Converters.registerConverter(OfflinePlayer.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		if (Skript.classExists("org.bukkit.generator.WorldInfo"))
			Converters.registerConverter(World.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		else //noinspection RedundantCast getName method is on World itself in older versions
			Converters.registerConverter(World.class, AnyNamed.class, thing -> () -> ((World) thing).getName(), Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(GameRule.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Server.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Plugin.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(WorldType.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Team.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Objective.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Criterion.class, AnyNamed.class, thing -> thing::name, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Nameable.class, AnyNamed.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			nameable -> new AnyNamed() {
				@Override
				public @UnknownNullability String name() {
					//noinspection deprecation
					return nameable.getCustomName();
				}

				@Override
				public boolean nameSupportsChange() {
					return true;
				}

				@Override
				public void setName(String name) throws UnsupportedOperationException {
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
					if (state instanceof Nameable)
						//noinspection deprecation
						return ((Nameable) state).getCustomName();
					return null;
				}

				@Override
				public boolean nameSupportsChange() {
					return true;
				}

				@Override
				public void setName(String name) throws UnsupportedOperationException {
					BlockState state = block.getState();
					if (state instanceof Nameable)
						//noinspection deprecation
						((Nameable) state).setCustomName(name);
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(CommandSender.class, AnyNamed.class, thing -> thing::getName, Converter.NO_RIGHT_CHAINING);
		// Command senders should be done last because there might be a better alternative above

		// Anything with a name -> AnyDisplayNamed
		Converters.registerConverter(Team.class, AnyDisplayNamed.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			team -> new AnyDisplayNamed() {
				@Override
				public @UnknownNullability String displayName() {
					return team.getDisplayName();
				}

				@Override
				public boolean displayNameSupportsChange() {
					return true;
				}

				@Override
				public void setDisplayName(String name) throws UnsupportedOperationException {
					team.setDisplayName(name);
				}
			},//</editor-fold>
			Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Objective.class, AnyDisplayNamed.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			objective -> new AnyDisplayNamed() {
				@Override
				public @UnknownNullability String displayName() {
					return objective.getDisplayName();
				}

				@Override
				public boolean displayNameSupportsChange() {
					return true;
				}

				@Override
				public void setDisplayName(String name) throws UnsupportedOperationException {
					objective.setDisplayName(name);
				}
			},//</editor-fold>
			Converter.NO_RIGHT_CHAINING);

		// Anything with an amount -> AnyAmount
		Converters.registerConverter(ItemStack.class, AnyAmount.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			item -> new AnyAmount() {

				@Override
				public @NotNull Number amount() {
					return item.getAmount();
				}

				@Override
				public boolean amountSupportsChange() {
					return true;
				}

				@Override
				public void setAmount(@Nullable Number amount) throws UnsupportedOperationException {
					item.setAmount(amount != null ? amount.intValue() : 0);
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);

		// Anything that contains -> AnyContainer
		Converters.registerConverter(Team.class, AnyContains.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			team -> new AnyContains<Object>() {
				@Override
				public boolean contains(Object value) {
					if (ScoreUtils.ARE_CRITERIA_AVAILABLE) {
						return value instanceof OfflinePlayer && team.hasPlayer((OfflinePlayer) value)
							|| value instanceof Entity && team.hasEntity((Entity) value);
					} else if (value instanceof OfflinePlayer) {
						String name = ((OfflinePlayer) value).getName();
						if (name == null)
							return false;
						return team.hasEntry(name);
					} else if (value instanceof Entity) {
						return team.hasEntry(((Entity) value).getUniqueId().toString());
					}
					return false;
				}

				@Override
				public boolean isSafeToCheck(Object value) {
					return value instanceof OfflinePlayer || value instanceof Entity;
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);

		// Anything with members -> AnyMembers
		Converters.registerConverter(Team.class, AnyMembers.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			team -> new AnyMembers<Object>() {
				@Override
				public Collection<Object> members() {
					return ScoreUtils.getMembers(team);
				}

				@Override
				public boolean membersSupportChanges() {
					return true;
				}

				@Override
				public boolean isSafeMemberType(@Nullable Object member) {
					return member instanceof OfflinePlayer || member instanceof Entity;
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);

		// Anything with prefix -> AnyPrefixed
		Converters.registerConverter(Team.class, AnyPrefixed.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			team -> new AnyPrefixed() {
				@Override
				public @NotNull String prefix() {
					return Utils.replaceChatStyles(team.getPrefix());
				}

				@Override
				public boolean prefixSupportsChange() {
					return true;
				}

				@Override
				public void setPrefix(String prefix) throws UnsupportedOperationException {
					team.setPrefix(prefix);
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Player.class, AnyPrefixed.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			player -> new AnyPrefixed() {
				@Override
				public String prefix() {
					if (Skript.isHookEnabled(VaultHook.class)) {
						return Utils.replaceChatStyles(VaultHook.chat.getPlayerPrefix(player));
					}
					return ScoreUtils.getTeamPrefix(player);
				}

				@Override
				public boolean prefixSupportsChange() {
					return Skript.isHookEnabled(VaultHook.class);
				}

				@Override
				public void setPrefix(String prefix) throws UnsupportedOperationException {
					VaultHook.chat.setPlayerPrefix(player, prefix);
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);

		// Anything with suffix -> AnySuffixed
		Converters.registerConverter(Team.class, AnySuffixed.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			team -> new AnySuffixed() {
				@Override
				public @NotNull String suffix() {
					return Utils.replaceChatStyles(team.getSuffix());
				}

				@Override
				public boolean suffixSupportsChange() {
					return true;
				}

				@Override
				public void setSuffix(String suffix) throws UnsupportedOperationException {
					team.setSuffix(suffix);
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Player.class, AnySuffixed.class, //<editor-fold desc="Converter" defaultstate="collapsed">
			player -> new AnySuffixed() {
				@Override
				public String suffix() {
					if (Skript.isHookEnabled(VaultHook.class)) {
						return Utils.replaceChatStyles(VaultHook.chat.getPlayerSuffix(player));
					}
					return ScoreUtils.getTeamSuffix(player);
				}

				@Override
				public boolean suffixSupportsChange() {
					return Skript.isHookEnabled(VaultHook.class);
				}

				@Override
				public void setSuffix(String suffix) throws UnsupportedOperationException {
					VaultHook.chat.setPlayerSuffix(player, suffix);
				}
			},
			//</editor-fold>
			Converter.NO_RIGHT_CHAINING);

		// InventoryHolder - Location
		// since the individual ones can't be trusted to chain.
		Converters.registerConverter(InventoryHolder.class, Location.class, holder -> {
			if (holder instanceof Entity)
				return ((Entity) holder).getLocation();
			if (holder instanceof Block)
				return ((Block) holder).getLocation();
			if (holder instanceof BlockState)
				return BlockUtils.getLocation(((BlockState) holder).getBlock());
			return null;
		});

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
