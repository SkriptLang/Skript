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

import java.util.Objects;

import ch.njol.skript.aliases.MatchQuality;
import ch.njol.skript.entity.RabbitData;
import ch.njol.skript.util.GameruleValue;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.entity.BoatData;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.StructureType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.SlotWithIndex;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

@SuppressWarnings({"rawtypes"})
public class DefaultComparators {
	
	public DefaultComparators() {}
	
	static {
		
		// Number - Number
		Comparators.registerComparator(Number.class, Number.class, new Comparator<Number, Number>() {
			@Override
			public Relation compare(final Number n1, final Number n2) {
				if (n1 instanceof Long && n2 instanceof Long)
					return Relation.get(n1.longValue() - n2.longValue());
				Double d1 = n1.doubleValue(),
					   d2 = n2.doubleValue();
				if (d1.isNaN() || d2.isNaN()) {
					return Relation.SMALLER;
				} else if (d1.isInfinite() || d2.isInfinite()) {
					return d1 > d2 ? Relation.GREATER : d1 < d2 ? Relation.SMALLER : Relation.EQUAL;
				} else {
					final double diff = d1 - d2;
					if (Math.abs(diff) < Skript.EPSILON)
						return Relation.EQUAL;
					return Relation.get(diff);
				}
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Slot - Slot
		Comparators.registerComparator(Slot.class, Slot.class, new Comparator<Slot, Slot>() {

			@Override
			public Relation compare(Slot o1, Slot o2) {
				if (o1 instanceof EquipmentSlot != o2 instanceof EquipmentSlot)
					return Relation.NOT_EQUAL;
				if (o1.isSameSlot(o2))
					return Relation.EQUAL;
				return Relation.NOT_EQUAL;
			}

			@Override
			public boolean supportsOrdering() {
				return false;
			}
			
		});
		
		// Slot - Number
		Comparators.registerComparator(Slot.class, Number.class, new Comparator<Slot, Number>() {

			@Override
			public Relation compare(Slot o1, Number o2) {
				if (o1 instanceof SlotWithIndex) {
					boolean same = ((SlotWithIndex) o1).getIndex() == o2.intValue();
					if (same) // Slot has index and the index is same with number
						return Relation.EQUAL;
				}
				return Relation.NOT_EQUAL;
			}

			@Override
			public boolean supportsOrdering() {
				return false;
			}
			
		});
		
		// Slot - ItemType
		Comparators.registerComparator(Slot.class, ItemType.class, new Comparator<Slot, ItemType>() {
			@Override
			public Relation compare(Slot slot, ItemType item) {
				ItemStack stack = slot.getItem();
				if (stack == null)
					return Comparators.compare(new ItemType(Material.AIR), item);
				return Comparators.compare(new ItemType(stack), item);
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// ItemStack - ItemType
		Comparators.registerComparator(ItemStack.class, ItemType.class, new Comparator<ItemStack, ItemType>() {
			@Override
			public Relation compare(final ItemStack is, final ItemType it) {
				return Comparators.compare(new ItemType(is), it);
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Block - ItemType
		Comparators.registerComparator(Block.class, ItemType.class, new Comparator<Block, ItemType>() {
			@Override
			public Relation compare(final Block b, final ItemType it) {
				return Relation.get(it.isOfType(b));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Block - BlockData
		if (Skript.classExists("org.bukkit.block.data.BlockData")) {
			Comparators.registerComparator(Block.class, BlockData.class, new Comparator<Block, BlockData>() {
				@Override
				public Relation compare(Block block, BlockData data) {
					return Relation.get(block.getBlockData().matches(data));
				}
				
				@Override
				public boolean supportsOrdering() {
					return false;
				}
			});
		}
		
		// ItemType - ItemType
		Comparators.registerComparator(ItemType.class, ItemType.class, new Comparator<ItemType, ItemType>() {
			@Override
			public Relation compare(final ItemType i1, final ItemType i2) {
				if (i1.isAll() != i2.isAll())
					return Relation.NOT_EQUAL;
				if (i1.getAmount() != i2.getAmount())
					return Relation.NOT_EQUAL;
				for (ItemData myType : i1.getTypes()) {
					for (ItemData otherType : i2.getTypes()) {
						if (myType.matchPlain(otherType)) {
							return Relation.EQUAL;
						}
						boolean plain = myType.isPlain() != otherType.isPlain();
						// Don't require an EXACT match if the other ItemData is an alias. They only need to share a material.
						if (myType.matchAlias(otherType).isAtLeast(plain ? MatchQuality.EXACT : otherType.isAlias() && !myType.isAlias() ? MatchQuality.SAME_MATERIAL : MatchQuality.SAME_ITEM)) {
							return Relation.EQUAL;
						}
					}
				}
				return Relation.NOT_EQUAL;
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Block - Block
		Comparators.registerComparator(Block.class, Block.class, new Comparator<Block, Block>() {
			@Override
			public Relation compare(final Block b1, final Block b2) {
				return Relation.get(b1.equals(b2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Entity - EntityData
		Comparators.registerComparator(Entity.class, EntityData.class, new Comparator<Entity, EntityData>() {
			@Override
			public Relation compare(final Entity e, final EntityData t) {
				return Relation.get(t.isInstance(e));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		// EntityData - EntityData
		Comparators.registerComparator(EntityData.class, EntityData.class, new Comparator<EntityData, EntityData>() {
			@Override
			public Relation compare(final EntityData t1, final EntityData t2) {
				return Relation.get(t2.isSupertypeOf(t1));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
	}
	
	// EntityData - ItemType
	public final static Comparator<EntityData, ItemType> entityItemComparator = new Comparator<EntityData, ItemType>() {
		@Override
		public Relation compare(final EntityData e, final ItemType i) {
			// TODO fix broken comparisions - will probably require updating potion API of Skript
			
			if (e instanceof Item)
				return Relation.get(i.isOfType(((Item) e).getItemStack()));
//			if (e instanceof ThrownPotion)
//				return Relation.get(i.isOfType(Material.POTION.getId(), PotionEffectUtils.guessData((ThrownPotion) e)));
//			if (Skript.classExists("org.bukkit.entity.WitherSkull") && e instanceof WitherSkull)
//				return Relation.get(i.isOfType(Material.SKULL_ITEM.getId(), (short) 1));
			if (e instanceof BoatData)
				return Relation.get(((BoatData)e).isOfItemType(i));
			if (e instanceof RabbitData)
				return Relation.get(i.isOfType(Material.RABBIT));
			for (ItemData data : i.getTypes()) {
				assert data != null;
				EntityData<?> entity = Aliases.getRelatedEntity(data);
				if (entity != null && entity.getType().isAssignableFrom(e.getType()))
					return Relation.EQUAL;
			}
			return Relation.NOT_EQUAL;
		}
		
		@Override
		public boolean supportsOrdering() {
			return false;
		}
	};
	static {
		Comparators.registerComparator(EntityData.class, ItemType.class, entityItemComparator);
		
		// Entity - ItemType
		// This skips (entity -> entitydata) == itemtype
		// It was not working reliably, because there is a converter chain
		// entity -> player -> inventoryholder -> block that sometimes takes a priority
		Comparators.registerComparator(Entity.class, ItemType.class, new Comparator<Entity, ItemType>() {

			@Override
			public Relation compare(Entity entity, ItemType item) {
				return entityItemComparator.compare(EntityData.fromEntity(entity), item);
			}

			@Override
			public boolean supportsOrdering() {
				return false;
			}
			
		});
	}
	
	static {
		// CommandSender - CommandSender
		Comparators.registerComparator(CommandSender.class, CommandSender.class, new Comparator<CommandSender, CommandSender>() {
			@Override
			public Relation compare(final CommandSender s1, final CommandSender s2) {
				return Relation.get(s1.equals(s2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// OfflinePlayer - OfflinePlayer
		Comparators.registerComparator(OfflinePlayer.class, OfflinePlayer.class, new Comparator<OfflinePlayer, OfflinePlayer>() {
			@Override
			public Relation compare(final OfflinePlayer p1, final OfflinePlayer p2) {
				return Relation.get(Objects.equals(p1.getName(), p2.getName()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// OfflinePlayer - String
		Comparators.registerComparator(OfflinePlayer.class, String.class, new Comparator<OfflinePlayer, String>() {
			@Override
			public Relation compare(final OfflinePlayer p, final String name) {
				String offlineName = p.getName();
				return offlineName == null ? Relation.NOT_EQUAL : Relation.get(offlineName.equalsIgnoreCase(name));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// World - String
		Comparators.registerComparator(World.class, String.class, new Comparator<World, String>() {
			@Override
			public Relation compare(final World w, final String name) {
				return Relation.get(w.getName().equalsIgnoreCase(name));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// String - String
		Comparators.registerComparator(String.class, String.class, new Comparator<String, String>() {
			@Override
			public Relation compare(final String s1, final String s2) {
				return Relation.get(StringUtils.equals(s1, s2, SkriptConfig.caseSensitive.value()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Date - Date
		Comparators.registerComparator(Date.class, Date.class, new Comparator<Date, Date>() {
			@Override
			public Relation compare(final Date d1, final Date d2) {
				return Relation.get(d1.compareTo(d2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Time - Time
		Comparators.registerComparator(Time.class, Time.class, new Comparator<Time, Time>() {
			@Override
			public Relation compare(final Time t1, final Time t2) {
				return Relation.get(t1.getTime() - t2.getTime());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Timespan - Timespan
		Comparators.registerComparator(Timespan.class, Timespan.class, new Comparator<Timespan, Timespan>() {
			@Override
			public Relation compare(final Timespan t1, final Timespan t2) {
				return Relation.get(t1.getMilliSeconds() - t2.getMilliSeconds());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		// Time - Timeperiod
		Comparators.registerComparator(Time.class, Timeperiod.class, new Comparator<Time, Timeperiod>() {
			@Override
			public Relation compare(final Time t, final Timeperiod p) {
				return Relation.get(p.contains(t));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// StructureType - StructureType
		Comparators.registerComparator(StructureType.class, StructureType.class, new Comparator<StructureType, StructureType>() {
			@Override
			public Relation compare(final StructureType s1, final StructureType s2) {
				return Relation.get(CollectionUtils.containsAll(s2.getTypes(), s2.getTypes()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// Object - ClassInfo
		Comparators.registerComparator(Object.class, ClassInfo.class, new Comparator<Object, ClassInfo>() {
			@Override
			public Relation compare(final Object o, final ClassInfo c) {
				return Relation.get(c.getC().isInstance(o) || o instanceof ClassInfo && c.getC().isAssignableFrom(((ClassInfo<?>) o).getC()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		// DamageCause - ItemType
		ItemType lava = Aliases.javaItemType("lava");
		Comparators.registerComparator(DamageCause.class, ItemType.class, new Comparator<DamageCause, ItemType>() {
			@Override
			public Relation compare(DamageCause dc, ItemType t) {
				switch (dc) {
					case FIRE:
						return Relation.get(t.isOfType(Material.FIRE));
					case LAVA:
						return Relation.get(t.equals(lava));
					case MAGIC:
						return Relation.get(t.isOfType(Material.POTION));
				}
				if (Skript.fieldExists(DamageCause.class, "HOT_FLOOR")
						&& dc.equals(DamageCause.HOT_FLOOR)) {
					return Relation.get(t.isOfType(Material.MAGMA_BLOCK));
				}

				return Relation.NOT_EQUAL;
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		// DamageCause - EntityData
		Comparators.registerComparator(DamageCause.class, EntityData.class, new Comparator<DamageCause, EntityData>() {
			@Override
			public Relation compare(final DamageCause dc, final EntityData e) {
				switch (dc) {
					case ENTITY_ATTACK:
						return Relation.get(e.isSupertypeOf(EntityData.fromClass(Entity.class)));
					case PROJECTILE:
						return Relation.get(e.isSupertypeOf(EntityData.fromClass(Projectile.class)));
					case WITHER:
						return Relation.get(e.isSupertypeOf(EntityData.fromClass(Wither.class)));
					case FALLING_BLOCK:
						return Relation.get(e.isSupertypeOf(EntityData.fromClass(FallingBlock.class)));
						//$CASES-OMITTED$
					default:
						return Relation.NOT_EQUAL;
				}
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		Comparators.registerComparator(GameruleValue.class, GameruleValue.class, new Comparator<GameruleValue, GameruleValue>() {
			@Override
			public Relation compare(GameruleValue o1, GameruleValue o2) {
				return Relation.get(o1.equals(o2));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		
		Comparators.registerComparator(GameruleValue.class, Number.class, new Comparator<GameruleValue, Number>() {
			@Override
			public Relation compare(GameruleValue o1, Number o2) {
				if (!(o1.getGameruleValue() instanceof Number)) return Relation.NOT_EQUAL;
				Number gameruleValue = (Number) o1.getGameruleValue();
				return Comparators.compare(gameruleValue, o2);
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		Comparators.registerComparator(GameruleValue.class, Boolean.class, new Comparator<GameruleValue, Boolean>() {
			@Override
			public Relation compare(GameruleValue o1, Boolean o2) {
				if (!(o1.getGameruleValue() instanceof Boolean)) return Relation.NOT_EQUAL;
				return Relation.get(o2.equals(o1.getGameruleValue()));
			}
			
			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});

		// EnchantmentOffer Comparators
		if (Skript.isRunningMinecraft(1, 11)) {
			// EnchantmentOffer - EnchantmentType
			Comparators.registerComparator(EnchantmentOffer.class, EnchantmentType.class, new Comparator<EnchantmentOffer, EnchantmentType>() {
				@Override
				public Relation compare(EnchantmentOffer eo, EnchantmentType et) {
					return Relation.get(eo.getEnchantment() == et.getType() && eo.getEnchantmentLevel() == et.getLevel());
				}
				
				@Override
				public boolean supportsOrdering() {
					return false;
				}
			});
			// EnchantmentOffer - Experience
			Comparators.registerComparator(EnchantmentOffer.class, Experience.class, new Comparator<EnchantmentOffer, Experience>() {
				@Override
				public Relation compare(EnchantmentOffer eo, Experience exp) {
					return Relation.get(eo.getCost() == exp.getXP());
				}
				
				@Override public boolean supportsOrdering() {
					return false;
				}
			});
		}
	}
	
}
