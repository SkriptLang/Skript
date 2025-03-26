package ch.njol.skript.bukkitutil.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Manages setting blocks.
 */
public interface BlockSetter {
	
	/**
	 * Attempts to automatically correct rotation and direction of the block
	 * when setting it. Note that this will NOT overwrite any existing data
	 * supplied in block values.
	 */
	public static int ROTATE = 1;
	
	/**
	 * Overrides rotation and direction that might have been specified in block
	 * values when {@link #ROTATE} is also set.
	 */
	public static int ROTATE_FORCE = 1 << 1;
	
	/**
	 * Changes type of the block if that is needed to get the correct rotation.
	 */
	public static int ROTATE_FIX_TYPE = 1 << 2;
	
	/**
	 * Takes rotation or direction of the block (depending on the block)
	 * and attempts to place other parts of it according to those. For example,
	 * placing beds and doors should be simple enough with this flag.
	 */
	public static int MULTIPART = 1 << 3;
	
	/**
	 * When placing the block, apply physics.
	 */
	public static int APPLY_PHYSICS = 1 << 4;
	
	/**
	 * Sets the given block.
	 * @param block Block to set.
	 * @param type New type of the block.
	 * @param values Additional block data, such as block states.
	 * @param flags Flags for block setter.
	 */
	void setBlock(Block block, Material type, @Nullable BlockValues values, int flags);
	
	/**
	 * Send a block change to a player.
	 * <p>This will send a fake block change to the player, and will not change the block on the server.</p>
	 *
	 * @param player Player to send change to
	 * @param location Location of block to change
	 * @param type Material of change
	 * @param values Additional block data, such as block states.
	 */
	void sendBlockChange(Player player, Location location, Material type, @Nullable BlockValues values);
	
}
