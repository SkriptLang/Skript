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
package ch.njol.skript.aliases;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.entity.EntityData;

/**
 * Stores the aliases.
 */
public class AliasesMap {
	
	public static class Match {
		
		private final MatchQuality quality;
		
		@Nullable
		private final AliasData data;
		
		public Match(MatchQuality quality, @Nullable AliasData data) {
			this.quality = quality;
			this.data = data;
		}
		
		/**
		 * Gets quality of this match.
		 * @return Match quality.
		 */
		public MatchQuality getQuality() {
			return quality;
		}
		
		/**
		 * Retrieves the alias data of this match. Provided that
		 * {@link #getQuality()} is at least {@link MatchQuality#SAME_MATERIAL}
		 * this will be not null; otherwise, it may or may not be null.
		 * @return Alias data for matching item.
		 */
		@Nullable
		public AliasData getData() {
			return data;
		}
	}
	
	public static class AliasData {
		
		/**
		 * The item associated with this alias.
		 */
		private final ItemData item;
		
		/**
		 * Name of this alias.
		 */
		private final MaterialName name;
		
		/**
		 * Minecraft ID of this alias.
		 */
		private final String minecraftId;
		
		/**
		 * Entity related to this alias.
		 */
		@Nullable
		private final EntityData<?> relatedEntity;
		
		public AliasData(ItemData item, MaterialName name, String minecraftId, @Nullable EntityData<?> relatedEntity) {
			this.item = item;
			this.name = name;
			this.minecraftId = minecraftId;
			this.relatedEntity = relatedEntity;
		}

		public ItemData getItem() {
			return item;
		}
		
		public MaterialName getName() {
			return name;
		}
		
		public String getMinecraftId() {
			return minecraftId;
		}
		
		@Nullable
		public EntityData<?> getRelatedEntity() {
			return relatedEntity;
		}
	}
		
	private static class MaterialEntry {
		
		/**
		 * The default alias for this material.
		 */
		@Nullable
		public AliasData defaultItem;
		
		/**
		 * All different aliases that share this material.
		 */
		public final List<AliasData> items;
		
		public MaterialEntry() {
			this.items = new ArrayList<>();
		}
		
	}
	
	/**
	 * One material entry per material. Ordinal of material is index of entry.
	 */
	private MaterialEntry[] materialEntries;
	
	@SuppressWarnings("null") // clear() initializes material entries
	public AliasesMap() {
		clear();
	}
	
	private MaterialEntry getEntry(ItemData item) {
		MaterialEntry entry = materialEntries[item.getType().ordinal()];
		assert entry != null;
		return entry;
	}
	
	public void addAlias(AliasData data) {
		MaterialEntry entry = getEntry(data.getItem());
		if (data.getItem().isDefault()) {
			entry.defaultItem = data;
		} else {
			entry.items.add(data);
		}
	}
	
	/**
	 * Attempts to get the closest matching alias for given item.
	 * @param item Item to find closest alias for.
	 * @return The match, containing the alias data and match quality.
	 */
	public Match matchAlias(ItemData item) {
		MaterialEntry entry = getEntry(item);
		
		// Special case: no aliases available!
		if (entry.defaultItem == null && entry.items.isEmpty()) {
			return new Match(MatchQuality.DIFFERENT, null);
		}
		
		// Try to find the best match
		MatchQuality maxQuality = MatchQuality.DIFFERENT;
		AliasData bestMatch = null;
		for (AliasData data : entry.items) {
			MatchQuality quality = item.matchAlias(data.getItem());
			if (quality.isBetter(maxQuality)) {
				maxQuality = quality;
				bestMatch = data;
			}
		}
		
		// Check that we found a reasonably good match
		// Just same material id -> default item
		if (maxQuality.isBetter(MatchQuality.SAME_MATERIAL)) {
			assert bestMatch != null; // Re-setting quality sets this too
			return new Match(maxQuality, bestMatch);
		} else { // Try default item
			AliasData defaultItem = entry.defaultItem;
			if (defaultItem != null) { // Just match against it
				return new Match(item.matchAlias(defaultItem.getItem()), defaultItem);
			} else { // No default item, no match
				if (bestMatch != null) { // Initially ignored this, but it is best match
					return new Match(MatchQuality.SAME_MATERIAL, bestMatch);
				}
			}
		}
		
		throw new AssertionError(); // Shouldn't have reached here
	}
	
	/**
	 * Attempts to find an alias that exactly matches the given item.
	 * @param item Item to match.
	 * @return An exact match, or no match.
	 */
	public Match exactMatch(ItemData item) {
		MaterialEntry entry = getEntry(item);
		
		// Special case: no aliases available!
		if (entry.defaultItem == null && entry.items.isEmpty()) {
			return new Match(MatchQuality.DIFFERENT, null);
		}
		
		for (AliasData data : entry.items) {
			if (item.matchAlias(data.getItem()) == MatchQuality.EXACT) {
				return new Match(MatchQuality.EXACT, data);
			}
		}
		
		return new Match(MatchQuality.DIFFERENT, null);
	}

	/**
	 * Clears all data from this aliases map.
	 */
	public void clear() {
		materialEntries = new MaterialEntry[Material.values().length];
		for (int i = 0; i < materialEntries.length; i++) {
			materialEntries[i] = new MaterialEntry();
		}
	}
}
