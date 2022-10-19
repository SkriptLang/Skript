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
package ch.njol.skript.bukkitutil.block;

import java.io.StreamCorruptedException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemFlags;
import ch.njol.skript.aliases.MatchQuality;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;

/**
 * Block compatibility implemented with magic numbers. No other choice until
 * Spigot 1.13.
 */
public class MagicBlockCompat implements BlockCompat {
	
	public static final MethodHandle setRawDataMethod;
	private static final MethodHandle getBlockDataMethod;
	public static final MethodHandle setDataMethod;
	
	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			MethodHandle mh = lookup.findVirtual(BlockState.class, "setRawData",
					MethodType.methodType(void.class, byte.class));
			assert mh != null;
			setRawDataMethod = mh;
			mh = lookup.findVirtual(FallingBlock.class, "getBlockData",
					MethodType.methodType(byte.class));
			assert mh != null;
			getBlockDataMethod = mh;
			mh = lookup.findVirtual(Block.class, "setData",
					MethodType.methodType(void.class, byte.class));
			assert mh != null;
			setDataMethod = mh;
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new Error(e);
		}
	}
	
	@SuppressWarnings({"deprecation"})
	private static class MagicBlockValues extends BlockValues {

		static {
			Variables.yggdrasil.registerSingleClass(MagicBlockValues.class, "MagicBlockValues");
		}

		private Material id;
		short data;
		private int itemFlags;

		/**
		 * Used for serialization
		 */
		private MagicBlockValues() {}

		public MagicBlockValues(BlockState block) {
			this.id = ItemUtils.asItem(block.getType());
			this.data = block.getRawData(); // Some black magic here, please look away...
			// We don't know whether block data 0 has been set explicitly
			this.itemFlags = ItemFlags.CHANGED_DURABILITY;
		}
		
		public MagicBlockValues(Material id, short data, int itemFlags) {
			this.id = id;
			this.data = data;
			this.itemFlags = itemFlags;
		}
		
		@Override
		public boolean isDefault() {
			return itemFlags == 0; // No tag or durability changes
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (!(other instanceof MagicBlockValues))
				return false;
			MagicBlockValues magic = (MagicBlockValues) other;
			if (isDefault() || magic.isDefault()) {
				return id == magic.id; // Compare only ids, other has not specified data constraints
			} else { // Compare ids and data
				return id == magic.id && data == magic.data;
			}
		}

		@Override
		public int hashCode() {
			// FindBugs reports "Scariest" bug when done with just ordinal << 8 | data
			// byte -> int widening seems to be a bit weird in Java
			return (id.ordinal() << 8) | (data & 0xff);
		}

		@Override
		public MatchQuality match(BlockValues other) {
			if (!(other instanceof MagicBlockValues)) {
				throw new IllegalArgumentException("wrong block compat");
			}
			MagicBlockValues magic = (MagicBlockValues) other;
			if (id == magic.id) {
				if (data == magic.data) {
					return MatchQuality.EXACT;
				} else {
					if ((magic.itemFlags & ItemFlags.CHANGED_DURABILITY) == 0) {
						return MatchQuality.SAME_ITEM; // Other doesn't care about durability
					} else {
						return MatchQuality.SAME_MATERIAL;
					}
				}
			} else {
				return MatchQuality.DIFFERENT;
			}
		}

		@Override
		public Fields serialize() {
			Fields fields = new Fields();
			fields.putObject("material", id.ordinal());
			fields.putPrimitive("data", data);
			fields.putPrimitive("itemFlags", itemFlags);
			return fields;
		}

		@Override
		public void deserialize(@NonNull Fields fields) throws StreamCorruptedException {
			this.id = Material.valueOf(fields.getAndRemoveObject("material", String.class));
			this.data = fields.getAndRemovePrimitive("data", Short.class);
			this.itemFlags = fields.getAndRemovePrimitive("itemFlags", Integer.class);
		}
	}
	
	private static class MagicBlockSetter implements BlockSetter {

		public MagicBlockSetter() {}

		@Override
		public void setBlock(Block block, Material type, @Nullable BlockValues values, int flags) {
			block.setType(type);
			
			if (values != null) {
				MagicBlockValues ourValues = (MagicBlockValues) values;
				try {
					setDataMethod.invokeExact(block, (byte) ourValues.data);
				} catch (Throwable e) {
					Skript.exception(e);
				}
			}
		}
		
		@Override
		public void sendBlockChange(Player player, Location location, Material type, @Nullable BlockValues values) {
			byte data = values != null ? (byte) ((MagicBlockValues) values).data : 0;
			player.sendBlockChange(location, type, data);
		}
	}

	@Override
	public BlockValues getBlockValues(BlockState block) {
		return new MagicBlockValues(block);
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState fallingBlockToState(FallingBlock entity) {
		BlockState state = entity.getWorld().getBlockAt(0, 0, 0).getState();
		state.setType(entity.getMaterial());
		try {
			setRawDataMethod.invokeExact(state, (byte) getBlockDataMethod.invokeExact(entity));
		} catch (Throwable e) {
			Skript.exception(e);
		}
		return state;
	}
	
	@Nullable
	@Override
	public BlockValues createBlockValues(Material type, Map<String, String> states, @Nullable ItemStack item, int itemFlags) {
		short damage = 0;
		if (item != null) {
			damage = (short) ItemUtils.getDamage(item);
		}
		return new MagicBlockValues(type, damage, itemFlags);
	}

	@Override
	public boolean isEmpty(Material type) {
		return type == Material.AIR;
	}

	@Override
	public boolean isLiquid(Material type) {
		// TODO moving water and lava
		return type == Material.WATER || type == Material.LAVA;
	}

	@Override
	@Nullable
	public BlockValues getBlockValues(ItemStack stack) {
		short data = (short) ItemUtils.getDamage(stack);
		return new MagicBlockValues(stack.getType(), data, ItemFlags.CHANGED_DURABILITY | ItemFlags.CHANGED_TAGS);
	}

	@Override
	public BlockSetter getSetter() {
		return new MagicBlockSetter();
	}
	
}
