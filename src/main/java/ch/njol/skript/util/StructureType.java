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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;
import ch.njol.util.coll.CollectionUtils;

public enum StructureType {
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
	
	private StructureType(final TreeType... types) {
		this.types = types;
		name = new Noun("tree types." + name() + ".name");
	}
	
	public void grow(final Location loc) {
		TreeType tree = CollectionUtils.getRandom(types);
		assert tree != null; // No enum member causes empty types
		loc.getWorld().generateTree(loc, tree);
	}
	
	public void grow(final Block b) {
		TreeType tree = CollectionUtils.getRandom(types);
		assert tree != null; // No enum member causes empty types
		b.getWorld().generateTree(b.getLocation(), tree);
	}
	
	public TreeType[] getTypes() {
		return types;
	}
	
	@Override
	public String toString() {
		return name.toString();
	}
	
	public String toString(final int flags) {
		return name.toString(flags);
	}
	
	public Noun getName() {
		return name;
	}
	
	public boolean is(final TreeType type) {
		return CollectionUtils.contains(types, type);
	}
	
	/**
	 * lazy
	 */
	final static Map<Pattern, StructureType> parseMap = new HashMap<>();
	
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				parseMap.clear();
			}
		});
	}
	
	@Nullable
	public static StructureType fromName(String s) {
		if (parseMap.isEmpty()) {
			for (final StructureType t : values()) {
				final String pattern = Language.get("tree types." + t.name() + ".pattern");
				parseMap.put(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE), t);
			}
		}
		s = "" + s.toLowerCase(Locale.ENGLISH);
		for (final Entry<Pattern, StructureType> e : parseMap.entrySet()) {
			if (e.getKey().matcher(s).matches())
				return e.getValue();
		}
		return null;
	}
	
}
