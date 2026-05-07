package ch.njol.skript.util;

import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;

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

	public void grow(Location location) {
		TreeType tree = CollectionUtils.getRandom(types);
		assert tree != null; // No enum member causes empty types
		World world = location.getWorld();
		if (world == null) {
			return;
		}
		world.generateTree(location, tree);
	}

	public void grow(Block block) {
		grow(block.getLocation());
	}

	public TreeType[] getTypes() {
		return types;
	}

	public boolean is(TreeType type) {
		return CollectionUtils.contains(types, type);
	}

}
