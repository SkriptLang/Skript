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
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

import ch.njol.skript.bukkitutil.BukkitUnsafe;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockValues;
import ch.njol.skript.entity.EntityData;

/**
 * Provides aliases on Bukkit/Spigot platform.
 */
public class AliasesProvider {
	
	/**
	 * When an alias is not found, it will requested from this provider.
	 * Null when this is global aliases provider.
	 */
	@Nullable
	private final AliasesProvider parent;
	
	/**
	 * All aliases that are currently loaded by this provider.
	 */
	private final Map<String, ItemType> aliases;

	/**
	 * All materials that are currently loaded by this provider.
	 */
	private final Set<Material> materials;
	
	/**
	 * Tags are in JSON format. We may need GSON when merging tags
	 * (which might be done if variations are used).
	 */
	private final Gson gson;
	
	/**
	 * Represents a variation of material. It could, for example, define one
	 * more tag or change base id, but keep tag intact.
	 */
	public static class Variation {
		
		@Nullable
		private final String id;
		private final int insertPoint;
		
		private final Map<String, Object> tags;
		private final Map<String, String> states;
		
		public Variation(@Nullable String id, int insertPoint, Map<String, Object> tags, Map<String, String> states) {
			this.id = id;
			this.insertPoint = insertPoint;
			this.tags = tags;
			this.states = states;
		}
		
		@Nullable
		public String getId() {
			return id;
		}
		
		public int getInsertPoint() {
			return insertPoint;
		}
		
		@Nullable
		public String insertId(@Nullable String inserted) {
			if (inserted == null) // Nothing to insert
				return id;
			inserted = inserted.substring(0, inserted.length() - 1); // Strip out -
			if (id == null) // Inserting to nothing
				return inserted;
			
			String id = this.id;
			assert id != null;
			if (insertPoint == -1) // No place where to insert
				return inserted;
			
			// Insert given string to in middle of our id
			String before = id.substring(0, insertPoint);
			String after = id.substring(insertPoint + 1);
			return before + inserted + after;
		}
		
		public Map<String, Object> getTags() {
			return tags;
		}


		public Map<String,String> getBlockStates() {
			return states;
		}


		public Variation merge(Variation other) {
			// Merge tags and block states
			Map<String, Object> mergedTags = new HashMap<>(other.tags);
			mergedTags.putAll(tags);
			Map<String, String> mergedStates = new HashMap<>(other.states);
			mergedStates.putAll(states);
			
			// Potentially merge ids
			String id = insertId(other.id);
			
			return new Variation(id, -1, mergedTags, mergedStates);
		}
	}
	
	public static class VariationGroup {
		
		public final List<String> keys;
		
		public final List<Variation> values;
		
		public VariationGroup() {
			this.keys = new ArrayList<>();
			this.values = new ArrayList<>();
		}
		
		public void put(String key, Variation value) {
			keys.add(key);
			values.add(value);
		}
	}
	
	/**
	 * Contains all variations.
	 */
	private final Map<String, VariationGroup> variations;
	
	/**
	 * Allows looking up aliases based on item datas created runtime.
	 */
	private final AliasesMap aliasesMap;
	
	/**
	 * Constructs a new aliases provider with no data.
	 */
	public AliasesProvider(int expectedCount, @Nullable AliasesProvider parent) {
		this.parent = parent;
		this.aliases = new HashMap<>(expectedCount);
		this.variations = new HashMap<>(expectedCount / 20);
		this.aliasesMap = new AliasesMap();
		this.materials = new ObjectOpenHashSet<>();
		
		this.gson = new Gson();
	}
	
	/**
	 * Uses GSON to parse Mojang's JSON format to a map.
	 * @param raw Raw JSON.
	 * @return String,Object map.
	 */
	@SuppressWarnings({"null", "unchecked"})
	public Map<String, Object> parseMojangson(String raw) {
		return (Map<String, Object>) gson.fromJson(raw, Object.class);
	}
	
	/**
	 * Applies given tags to an item stack.
	 * @param stack Item stack.
	 * @param tags Tags.
	 * @return Additional flags for the item.
	 */
	public int applyTags(ItemStack stack, Map<String, Object> tags) {
		// Hack damage tag into item
		Object damage = tags.get("Damage");
		int flags = 0;
		if (damage instanceof Number) { // Use helper for version compatibility
			ItemUtils.setDamage(stack, ((Number) damage).shortValue());
			tags.remove("Damage");
			flags |= ItemFlags.CHANGED_DURABILITY;
		}
		
		if (tags.isEmpty()) // No real tags to apply
			return flags;
		
		// Apply random tags using JSON
		String json = gson.toJson(tags);
		assert json != null;
		BukkitUnsafe.modifyItemStack(stack, json);
		flags |= ItemFlags.CHANGED_TAGS;
		
		return flags;
	}
	
	/**
	 * Name of an alias used by {@link #addAlias(AliasName, String, Map, Map)}
	 * for registration.
	 */
	public static class AliasName {
		
		/**
		 * Singular for of alias name.
		 */
		public final String singular;
		
		/**
		 * Plural form of alias name.
		 */
		public final String plural;
		
		/**
		 * Gender of alias name.
		 */
		public final int gender;

		public AliasName(String singular, String plural, int gender) {
			super();
			this.singular = singular;
			this.plural = plural;
			this.gender = gender;
		}
		
	}
	
	/**
	 * Adds an alias to this provider.
	 * @param name Name of alias without any patterns or variation blocks.
	 * @param id Id of material.
	 * @param tags Tags for material.
	 * @param blockStates Block states.
	 */
	public void addAlias(AliasName name, String id, @Nullable Map<String, Object> tags, Map<String, String> blockStates) {
		// First, try to find if aliases already has a type with this id
		// (so that aliases can refer to each other)
		ItemType typeOfId = getAlias(id);
		EntityData<?> related = null;
		List<ItemData> datas;
		if (typeOfId != null) { // If it exists, use datas from it
			datas = typeOfId.getTypes();
		} else { // ... but quite often, we just got Vanilla id
			// Prepare and modify ItemStack (using somewhat Unsafe methods)
			Material material = BukkitUnsafe.getMaterialFromMinecraftId(id);
			if (material == null) { // If server doesn't recognize id, do not proceed
				throw new InvalidMinecraftIdException(id);
			}

			materials.add(material);
			
			// Hacky: get related entity from block states
			String entityName = blockStates.remove("relatedEntity");
			if (entityName != null) {
				related = EntityData.parse(entityName);
			}
			
			// Apply (NBT) tags to item stack
			ItemStack stack = new ItemStack(material);
			int itemFlags = 0;
			if (tags != null) {
				itemFlags = applyTags(stack, new HashMap<>(tags));
			}
			
			// Parse block state to block values
			BlockValues blockValues = BlockCompat.INSTANCE.createBlockValues(material, blockStates, stack, itemFlags);
			
			ItemData data = new ItemData(stack, blockValues);
			data.isAlias = true;
			data.itemFlags = itemFlags;
			
			// Deduplicate item data if this has been loaded before
			AliasesMap.Match canonical = aliasesMap.exactMatch(data);
			if (canonical.getQuality().isAtLeast(MatchQuality.EXACT)) {
				AliasesMap.AliasData aliasData = canonical.getData();
				assert aliasData != null; // Match quality guarantees this
				data = aliasData.getItem();
			}
			
			datas = Collections.singletonList(data);
		}
		
		// If this is first time we're defining an item, store additional data about it
		if (typeOfId == null) {
			ItemData data = datas.get(0);
			// Most accurately named alias for this item SHOULD be defined first
			MaterialName materialName = new MaterialName(data.type, name.singular, name.plural, name.gender);
			aliasesMap.addAlias(new AliasesMap.AliasData(data, materialName, id, related));
		}

		// Check if there is item type with this name already, create otherwise
		ItemType type = aliases.get(name.singular);
		if (type == null)
			type = aliases.get(name.plural);
		if (type == null) {
			type = new ItemType();
			aliases.put(name.singular, type); // Singular form
			aliases.put(name.plural, type); // Plural form
			type.addAll(datas);
		} else { // There is already an item type with this name, we need to *only* add new data
			newDataLoop: for (ItemData newData : datas) {
				for (ItemData existingData : type.getTypes()) {
					if (newData == existingData || newData.matchAlias(existingData).isAtLeast(MatchQuality.EXACT)) // Don't add this data, the item type already contains it!
						continue newDataLoop;
				}
				type.add(newData);
			}
		}
	}
	
	public void addVariationGroup(String name, VariationGroup group) {
		variations.put(name, group);
	}
	
	@Nullable
	public VariationGroup getVariationGroup(String name) {
		return variations.get(name);
	}

	@Nullable
	public ItemType getAlias(String alias) {
		ItemType item = aliases.get(alias);
		if (item == null && parent != null) {
			return parent.getAlias(alias);
		}
		return item;
	}
	
	public AliasesMap.@Nullable AliasData getAliasData(ItemData item) {
		AliasesMap.AliasData data = aliasesMap.matchAlias(item).getData();
		if (data == null && parent != null) {
			return parent.getAliasData(item);
		}
		return data;
	}

	@Nullable
	public String getMinecraftId(ItemData item) {
		AliasesMap.AliasData data = getAliasData(item);
		if (data != null) {
			return data.getMinecraftId();
		}
		return null;
	}
	
	@Nullable
	public MaterialName getMaterialName(ItemData item) {
		AliasesMap.AliasData data = getAliasData(item);
		if (data != null) {
			return data.getName();
		}
		return null;
	}
	
	@Nullable
	public EntityData<?> getRelatedEntity(ItemData item) {
		AliasesMap.AliasData data = getAliasData(item);
		if (data != null) {
			return data.getRelatedEntity();
		}
		return null;
	}

	public void clearAliases() {
		aliases.clear();
		variations.clear();
		aliasesMap.clear();
	}

	public int getAliasCount() {
		return aliases.size();
	}

	/**
	 * Check if this provider has an alias for the given material.
	 * @param material Material to check alias for
	 * @return True if this material has an alias
	 */
	public boolean hasAliasForMaterial(Material material) {
		return materials.contains(material);
	}

}
