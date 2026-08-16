package ch.njol.skript.util;

import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Repesents a grouping of Bukkkit {@link TreeType TreeTypes}
 */
public enum TreeSpecies {
	TREE(TreeType.values()),

	OAK(TreeType.TREE, TreeType.BIG_TREE),
	SMALL_OAK(TreeType.TREE),
	BIG_OAK(TreeType.BIG_TREE),

	SPRUCE(TreeType.REDWOOD, TreeType.TALL_REDWOOD),
	SMALL_SPRUCE(TreeType.REDWOOD),
	BIG_SPRUCE(TreeType.TALL_REDWOOD),
	MEGA_SPRUCE(TreeType.MEGA_REDWOOD),

	BIRCH(TreeType.BIRCH, TreeType.TALL_BIRCH),
	SMALL_BIRCH(TreeType.BIRCH),
	TALL_BIRCH(TreeType.TALL_BIRCH),

	JUNGLE(TreeType.SMALL_JUNGLE, TreeType.JUNGLE, TreeType.COCOA_TREE),
	SMALL_JUNGLE(TreeType.SMALL_JUNGLE),
	BIG_JUNGLE(TreeType.JUNGLE),
	COCOA_TREE(TreeType.COCOA_TREE),

	JUNGLE_BUSH(TreeType.JUNGLE_BUSH),

	ACACIA(TreeType.ACACIA),
	DARK_OAK(TreeType.DARK_OAK),
	SWAMP(TreeType.SWAMP),

	MUSHROOM(TreeType.RED_MUSHROOM, TreeType.BROWN_MUSHROOM),
	RED_MUSHROOM(TreeType.RED_MUSHROOM),
	BROWN_MUSHROOM(TreeType.BROWN_MUSHROOM),

	MANGROVE(TreeType.MANGROVE, TreeType.TALL_MANGROVE),
	SMALL_MANGROVE(TreeType.MANGROVE),
	BIG_MANGROVE(TreeType.TALL_MANGROVE),

	AZALEA(TreeType.AZALEA),

	PALE_OAK(TreeType.PALE_OAK, TreeType.PALE_OAK_CREAKING),
	PALE_OAK_NORMAL(TreeType.PALE_OAK),
	PALE_OAK_CREAKING(TreeType.PALE_OAK_CREAKING),

	CHERRY(TreeType.CHERRY),

	CRIMSON_FUNGUS(TreeType.CRIMSON_FUNGUS),
	WARPED_FUNGUS(TreeType.WARPED_FUNGUS),

	CHORUS_PLANT(TreeType.CHORUS_PLANT),
	;

	private final TreeType[] types;

	TreeSpecies(TreeType... types) {
		this.types = types;
	}

	/**
	 * Grow a tree at a location.
	 * <p>
	 * If the species is a group, a random tree type is selected.
	 * </p>
	 *
	 * @param location Location to grow the tree at
	 */
	public void grow(Location location) {
		TreeType tree = CollectionUtils.getRandom(types);
		assert tree != null; // No enum member causes empty types
		World world = location.getWorld();
		if (world == null) {
			return;
		}
		world.generateTree(location, tree);
	}

	/**
	 * Grow a tree at a location.
	 * <p>
	 * If the species is a group, a random tree type is selected.
	 * </p>
	 *
	 * @param block Block to grow the tree at
	 */
	public void grow(Block block) {
		grow(block.getLocation());
	}

	/**
	 * Get the TreeTypes that make up this species.
	 *
	 * @return TreeTypes that make up this species
	 */
	public TreeType[] getTypes() {
		return types;
	}

	/**
	 * Check if this species contains a specific tree type.
	 *
	 * @param type TreeType to check for
	 * @return True if the species contains the type, false otherwise
	 */
	public boolean is(TreeType type) {
		return CollectionUtils.contains(types, type);
	}

}
