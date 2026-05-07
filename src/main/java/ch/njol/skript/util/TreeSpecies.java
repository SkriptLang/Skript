package ch.njol.skript.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.coll.CollectionUtils;

public enum TreeSpecies {
	TREE(TreeType.TREE, TreeType.BIG_TREE, TreeType.REDWOOD, TreeType.TALL_REDWOOD, TreeType.MEGA_REDWOOD,
			TreeType.BIRCH, TreeType.TALL_BIRCH, TreeType.SMALL_JUNGLE, TreeType.JUNGLE, TreeType.COCOA_TREE,
			TreeType.ACACIA, TreeType.DARK_OAK, TreeType.SWAMP),
	
	REGULAR(TreeType.TREE, TreeType.BIG_TREE), SMALL_REGULAR(TreeType.TREE), BIG_REGULAR(TreeType.BIG_TREE),
	REDWOOD(TreeType.REDWOOD, TreeType.TALL_REDWOOD), SMALL_REDWOOD(TreeType.REDWOOD), BIG_REDWOOD(TreeType.TALL_REDWOOD),
	MEGA_REDWOOD(TreeType.MEGA_REDWOOD),
	BIRCH(TreeType.BIRCH), TALL_BIRCH(TreeType.TALL_BIRCH),
	JUNGLE(TreeType.SMALL_JUNGLE, TreeType.JUNGLE), SMALL_JUNGLE(TreeType.SMALL_JUNGLE), BIG_JUNGLE(TreeType.JUNGLE),
	JUNGLE_BUSH(TreeType.JUNGLE_BUSH), COCOA_TREE(TreeType.COCOA_TREE),
	ACACIA(TreeType.ACACIA), DARK_OAK(TreeType.DARK_OAK),
	SWAMP(TreeType.SWAMP),
	
	MUSHROOM(TreeType.RED_MUSHROOM, TreeType.BROWN_MUSHROOM),
	RED_MUSHROOM(TreeType.RED_MUSHROOM), BROWN_MUSHROOM(TreeType.BROWN_MUSHROOM),
	
	;
	
	private Noun name;
	private final TreeType[] types;
	
	private TreeSpecies(TreeType... types) {
		this.types = types;
		name = new Noun("tree types." + name() + ".name");
	}

	public void grow(Location location) {
		TreeType tree = CollectionUtils.getRandom(types);
		assert tree != null; // No enum member causes empty types
		location.getWorld().generateTree(location, tree);
	}

	public void grow(Block block) {
		TreeType tree = CollectionUtils.getRandom(types);
		assert tree != null; // No enum member causes empty types
		block.getWorld().generateTree(block.getLocation(), tree);
	}
	
	public TreeType[] getTypes() {
		return types;
	}
	
	@Override
	public String toString() {
		return name.toString();
	}
	
	public String toString(int flags) {
		return name.toString(flags);
	}

	public Noun getName() {
		return name;
	}

	public boolean is(TreeType type) {
		return CollectionUtils.contains(types, type);
	}
	
	/**
	 * lazy
	 */
	final static Map<Pattern, TreeSpecies> parseMap = new HashMap<>();
	
	static {
		Language.addListener(parseMap::clear);
	}
	
	@Nullable
	public static TreeSpecies fromName(String input) {
		if (parseMap.isEmpty()) {
			for (TreeSpecies type : values()) {
				String pattern = Language.get("tree types." + type.name() + ".pattern");
				parseMap.put(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE), type);
			}
		}
		input = input.toLowerCase(Locale.ENGLISH);
		for (Entry<Pattern, TreeSpecies> entry : parseMap.entrySet()) {
			if (entry.getKey().matcher(input).matches())
				return entry.getValue();
		}
		return null;
	}
	
}
