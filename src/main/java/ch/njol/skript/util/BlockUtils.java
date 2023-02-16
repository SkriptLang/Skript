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
package ch.njol.skript.util;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockSetter;
import ch.njol.skript.bukkitutil.block.BlockValues;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

/**
 * TODO !Update with every version [blocks] - also update aliases-*.sk
 */
public class BlockUtils {
	
	/**
	 * Sets the given block.
	 * @param block Block to set.
	 * @param type New type of the block.
	 * @param blockValues Block values to apply after setting the type.
	 * @param applyPhysics Whether physics should be applied or not.
	 * @return Whether setting block succeeded or not (currently always true).
	 */
	public static boolean set(Block block, Material type, @Nullable BlockValues blockValues, boolean applyPhysics) {
		int flags = BlockSetter.ROTATE | BlockSetter.ROTATE_FIX_TYPE | BlockSetter.MULTIPART;
		if (applyPhysics)
			flags |= BlockSetter.APPLY_PHYSICS;
		BlockCompat.SETTER.setBlock(block, type, blockValues, flags);

		return true;
	}
	
	public static boolean set(Block block, ItemData type, boolean applyPhysics) {
		return set(block, type.getType(), type.getBlockValues(), applyPhysics);
	}
	
	public static void sendBlockChange(Player player, Location location, Material type, @Nullable BlockValues blockValues) {
		BlockCompat.SETTER.sendBlockChange(player, location, type, blockValues);
	}
	
	@SuppressWarnings("null")
	public static Iterable<Block> getBlocksAround(Block b) {
		return Arrays.asList(b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST));
	}
	
	@SuppressWarnings("null")
	public static Iterable<BlockFace> getFaces() {
		return Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
	}
	
	/**
	 * @param b A block
	 * @return Location of the block, including its direction
	 */
	@Nullable
	public static Location getLocation(@Nullable Block b) {
		if (b == null)
			return null;
		Location l = b.getLocation().add(0.5, 0.5, 0.5);
		BlockFace blockFace = Direction.getFacing(b);
		if (blockFace != BlockFace.SELF) {
			l.setPitch(Direction.getPitch(Math.asin(blockFace.getModY())));
			l.setYaw(Direction.getYaw(Math.atan2(blockFace.getModZ(), blockFace.getModX())));
		}
		return l;
	}
	
	@Nullable
	public static BlockData createBlockData(String dataString) {
		// Skript uses a comma to separate lists, so we use a semi colon as a delimiter
		// Here we are just replacing it back to a comma to create a new block data
		String data = dataString.replace(";", ",");
		// Remove white space within square brackets ([ lit = false] -> [lit=false])
		data = data.replaceAll(" (?=[^\\[]*])", "");
		// Remove white space between last word and square bracket
		data = data.replaceAll("\\s+\\[", "[");
		// And replace white space between namespace with underscores
		data = data.replace(" ", "_");
		
		try {
			return Bukkit.createBlockData(data.startsWith("minecraft:") ? data : "minecraft:" + data);
		} catch (IllegalArgumentException ignored) {
			try {
				// we use the original dataString param here as we want the alias before modifications
				String alias = dataString.substring(0, dataString.lastIndexOf("["));
				data = data.substring(data.lastIndexOf("["));
				ItemType type = Aliases.parseItemType(alias);
				if (type == null)
					return null;
				return Bukkit.createBlockData(type.getMaterial(), data);
			} catch (IllegalArgumentException | StringIndexOutOfBoundsException alsoIgnored) {
				return null;
			}
		}
	}

	/**
	 * Get the string version of a block, including type and location.
	 * ex: 'stone' at 1.5, 1.5, 1.5 in world 'world'
	 *
	 * @param block Block to get string of
	 * @param flags
	 * @return String version of block
	 */
	@Nullable
	public static String blockToString(Block block, int flags) {
		String type = ItemType.toString(block, flags);
		Location location = getLocation(block);
		if (location == null) {
			return null;
		}

		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		String world = location.getWorld().getName();

		return String.format("'%s' at %s, %s, %s in world '%s'", type, x, y, z, world);
	}

	/**
	 * Extracts the actual CraftBukkit block from the given argument,
	 * by extracting the block from {@link DelayedChangeBlock} if the given argument is a {@link DelayedChangeBlock}.
	 *
	 * @return the actual CB block from the given argument
	 */
	public static Block extractBlock(Block block) {
		return block instanceof DelayedChangeBlock ? ((DelayedChangeBlock) block).block : block;
	}

}
