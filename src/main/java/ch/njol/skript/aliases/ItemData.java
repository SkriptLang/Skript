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

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUnsafe;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockValues;
import ch.njol.skript.localization.Message;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ItemData implements Cloneable, YggdrasilExtendedSerializable {
	
	static {
		Variables.yggdrasil.registerSingleClass(ItemData.class, "NewItemData");
		Variables.yggdrasil.registerSingleClass(OldItemData.class, "ItemData");
	}
	
	/**
	 * Represents old ItemData (before aliases rework and MC 1.13).
	 */
	public static class OldItemData {
		
		int typeid = -1;
		public short dataMin = -1;
		public short dataMax = -1;
	}

	static final ItemFactory itemFactory = Bukkit.getServer().getItemFactory();
	
	// Load or create material registry
	static {
		Path materialsFile = Paths.get(Skript.getInstance().getDataFolder().getAbsolutePath(), "materials.json");
		if (Files.exists(materialsFile)) {
			try {
				Files.delete(materialsFile);
			} catch (IOException e) {
				Skript.exception(e, "Failed to remove legacy material registry file!");
			}
		}
	}
	
	private final static Message m_named = new Message("aliases.named");
	
	/**
	 * Before 1.13, data values ("block states") are applicable to items.
	 *
	 * @deprecated before 1.13 is no longer supported
	 */
	@Deprecated
	public static final boolean itemDataValues = false;
	
	/**
	 * ItemStack, which is used for everything but serialization.
	 */
	transient ItemStack stack;
	
	/**
	 * Type of the item as Bukkit material. Serialized manually.
	 */
	transient Material type;
	
	/**
	 * If this represents all possible items.
	 */
	boolean isAnything;
	
	/**
	 * When this ItemData represents a block, this contains information to
	 * allow comparing it against other blocks.
	 */
	@Nullable
	BlockValues blockValues;
	
	/**
	 * Whether this represents an item (that definitely cannot have
	 * block states) or a block, which might have them.
	 */
	boolean itemForm;
	
	/**
	 * If this item is an alias or a clone of one that has not been
	 * modified after loading the aliases.
	 */
	boolean isAlias = false;
	
	/**
	 * Whether this item is a 'plain' item type.
	 * This is used for comparison, as any item type matched to this item type must also be plain OR be an alias.
	 * This variable should not be used directly. Use {@link #isPlain()}.
	 * @see ch.njol.skript.expressions.ExprPlain
	 */
	private boolean plain = false;
	
	/**
	 * Some properties about this item.
	 */
	int itemFlags;
	
	public ItemData(Material type, @Nullable String tags) {
		this.type = type;
		
		this.stack = new ItemStack(type);
		this.blockValues = BlockCompat.INSTANCE.getBlockValues(stack);
		if (tags != null) {
			applyTags(tags);
		}
	}
	
	public ItemData(Material type, int amount) {
		this.type = type;
		this.stack = new ItemStack(type, Math.abs(amount));
		this.blockValues = BlockCompat.INSTANCE.getBlockValues(stack);
	}
	
	public ItemData(Material type) {
		this(type, 1);
	}
	
	public ItemData(ItemData data) {
		this.stack = data.stack.clone();
		this.type = data.type;
		this.blockValues = data.blockValues;
		this.isAlias = data.isAlias;
		this.plain = data.plain;
		this.itemFlags = data.itemFlags;
	}
	
	public ItemData(ItemStack stack, @Nullable BlockValues values) {
		this.stack = stack;
		this.type = stack.getType();
		this.blockValues = values;
		
		// Set ItemFlags as accurately as possible based on given stack
		if (type.getMaxDurability() != 0) {
			// We're not checking current damage; when it is 0, it might still be explicitly set
			// Play safe and mark ALL items that may have durability to have it changed
			itemFlags |= ItemFlags.CHANGED_DURABILITY;
		}
		// All data made from stacks may have changed tags
		// We cannot assume that lack of tags indicates that they can be
		// ignored in comparisons; they may well have been explicitly removed
		// See issue #2714 for examples of bad things that this causes
		itemFlags |= ItemFlags.CHANGED_TAGS;
	}
	
	public ItemData(ItemStack stack) {
		this(stack, BlockCompat.INSTANCE.getBlockValues(stack));
		this.itemForm = true;
	}
	
	public ItemData(BlockState block) {
		this.type = ItemUtils.asItem(block.getType());
		this.stack = new ItemStack(type);
		this.blockValues = BlockCompat.INSTANCE.getBlockValues(block);
	}
	
	public ItemData(Block block) {
		this(block.getState());
	}
	
	/**
	 * Only to be used for serialization.
	 */
	@SuppressWarnings("null") // Yeah, only for internal use
	public ItemData() {}
	
	/**
	 * Tests whether the given item is of this type.
	 * 
	 * @param item
	 * @return Whether the given item is of this type.
	 */
	public boolean isOfType(@Nullable ItemStack item) {
		if (item == null)
			return type == Material.AIR;
		
		if (type != item.getType())
			return false; // Obvious mismatch
		
		if (itemFlags != 0) { // Either stack has tags (or durability)
			if (ItemUtils.getDamage(stack) != ItemUtils.getDamage(item))
				return false; // On 1.12 and below, damage is not in meta
			if (stack.hasItemMeta() == item.hasItemMeta()) // Compare ItemMeta as in isSimilar() of ItemStack
				return stack.hasItemMeta() ? itemFactory.equals(stack.getItemMeta(), item.getItemMeta()) : true;
			else
				return false;
		}
		return true;
	}
	
	/**
	 * Returns <code>Aliases.{@link Aliases#getMaterialName(ItemData, boolean) getMaterialName}(ItemData, boolean)</code>
	 * called with this object and relevant plurarily setting.
	 */
	@Override
	public String toString() {
		return toString(false, false);
	}
	
	public String toString(final boolean debug, final boolean plural) {
		StringBuilder builder = new StringBuilder(Aliases.getMaterialName(this, plural));
		ItemMeta meta = stack.getItemMeta();
		if (meta != null && meta.hasDisplayName()) {
			builder.append(" ").append(m_named).append(" ");
			builder.append(meta.getDisplayName());
		}
		return builder.toString();
	}
	
	/**
	 * @return The item's gender or -1 if no name is found
	 */
	public int getGender() {
		return Aliases.getGender(this);
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ItemData))
			return false;
		
		ItemData other = (ItemData) obj;
		if (isAlias) { // This is alias, other item might not be
			return other.matchAlias(this).isAtLeast(MatchQuality.SAME_ITEM);
		} else { // This is not alias, but other might be
			return matchAlias(other).isAtLeast(MatchQuality.SAME_ITEM);
		}
	}
	
	@Override
	public int hashCode() {
		int hash = type.hashCode(); // Has collisions, but probably not too many of them
		if (blockValues == null || (blockValues != null && blockValues.isDefault())) {
			hash = hash * 37 + 1;
		}
		return hash;
	}
	
	/**
	 * Checks how well this item matches the given item.
	 * @param item Other item, preferably an alias.
	 * @return Match quality, according to following criteria:
	 * <table>
	 * <tr><td>{@link MatchQuality#EXACT}
	 * 	<td>This and the given item have exactly same
	 * 	{@link Material}, {@link ItemMeta} and {@link BlockValues}.
	 * <tr><td>{@link MatchQuality#SAME_ITEM}
	 * 	<td>This and the given item share a {@link Material}. {@link ItemMeta}
	 * 	of this item contains all values that {@link ItemMeta} of given has.
	 *  In addition to that, it may contain other values. {@link BlockValues}
	 *  are handled similarly.
	 * <tr><td>{@link MatchQuality#SAME_MATERIAL}
	 * 	<td>This and the given item share a material.
	 * <tr><td>{@link MatchQuality#DIFFERENT}
	 * 	<td>This and the given item do not meet any of above criteria.
	 *  They are completely different.
	 * </table>
	 */
	public MatchQuality matchAlias(ItemData item) {
		if (isAnything || item.isAnything) {
			return MatchQuality.EXACT; // TODO different match quality?
		}
		
		// Ensure that both items share the material
		if (item.getType() != getType()) {
			return MatchQuality.DIFFERENT;
		}

		BlockValues values = blockValues;
		// Items (held in inventories) don't have block values
		// If this is an item, given item must not have them either
		if (itemForm && item.blockValues != null && !item.blockValues.isDefault()) {
			return MatchQuality.SAME_MATERIAL;
		}

		/*
		 * Initially, expect exact match. Lower expectations as new differences
		 * between items are discovered.
		 */
		MatchQuality quality = MatchQuality.EXACT;
		
		// Check that block values of given item match ours
		if (values != null) {
			if (item.blockValues != null) { // Other item has block values, so match against them
				quality = values.match(item.blockValues);
			} else { // Other item has no block values, but we do
				quality = MatchQuality.SAME_MATERIAL;
			}
		}
		
		// See if we need to compare durability; for blocks, BlockValues handles this when needed
		// From API perspective, durability is part of ItemMeta; however, we need to support 1.12 and older
		if (itemForm && ItemUtils.getDamage(stack) != ItemUtils.getDamage(item.stack)) {
			if (item.hasFlag(ItemFlags.CHANGED_DURABILITY)) { // Given item requests we match its durability
				quality = MatchQuality.SAME_MATERIAL;
			} else { // Given item doesn't care about durability, so are still same enough
				quality = MatchQuality.SAME_ITEM;
			}
		}
		
		// See if we need to compare item metas (excluding durability)
		if (quality.isAtLeast(MatchQuality.SAME_ITEM) && stack.hasItemMeta() || item.stack.hasItemMeta()) { // Item meta checks could lower this
			MatchQuality metaQuality = compareItemMetas(getItemMeta(), item.getItemMeta());
			
			// If given item doesn't care about meta, promote to SAME_ITEM
			// I.e. we checked meta only to eliminate EXACT match
			if (metaQuality == MatchQuality.SAME_MATERIAL && !item.hasFlag(ItemFlags.CHANGED_TAGS)) {
				quality = MatchQuality.SAME_ITEM;
			} else if (quality.isBetter(metaQuality)) { // Otherwise just allow meta to lower quality
				quality = metaQuality;
			}
		}
		
		return quality;
	}
	
	/**
	 * Checks if this item has given flag.
	 * @param flag Flag found in {@link ItemFlags}.
	 * @return If this item has the flag.
	 */
	private boolean hasFlag(int flag) {
		return (itemFlags & flag) != 0;
	}
	
	/**
	 * Compares {@link ItemMeta}s for {@link #matchAlias(ItemData)}.
	 * Note that this method assumes the metas are coming from similar types.
	 * @param first Meta of this item.
	 * @param second Meta of given item.
	 * @return Match quality of metas.
	 * Lowest is {@link MatchQuality#SAME_MATERIAL}.
	 */
	private static MatchQuality compareItemMetas(ItemMeta first, ItemMeta second) {
		MatchQuality quality = MatchQuality.EXACT; // Lowered as we go on
		MatchQuality newQuality; // Used to prevent upgrading the quality
		
		// Display name
		String ourName = first.hasDisplayName() ? first.getDisplayName() : null;
		String theirName = second.hasDisplayName() ? second.getDisplayName() : null;
		if (!Objects.equals(ourName, theirName)) {
			newQuality = (ourName != null && theirName == null) ? MatchQuality.SAME_ITEM : MatchQuality.SAME_MATERIAL;
			if (!newQuality.isBetter(quality))
				quality = newQuality;
		}
		
		// Lore
		List<String> ourLore = first.hasLore() ? first.getLore() : null;
		List<String> theirLore = second.hasLore() ? second.getLore() : null;
		if (!Objects.equals(ourLore, theirLore)) {
			newQuality = (ourLore != null && theirLore == null) ? MatchQuality.SAME_ITEM : MatchQuality.SAME_MATERIAL;
			if (!newQuality.isBetter(quality))
				quality = newQuality;
		}
		
		// Enchantments
		Map<Enchantment, Integer> ourEnchants = first.getEnchants();
		Map<Enchantment, Integer> theirEnchants = second.getEnchants();
		if (!Objects.equals(ourEnchants, theirEnchants)) {
			newQuality = (!ourEnchants.isEmpty() && theirEnchants.isEmpty()) ? MatchQuality.SAME_ITEM : MatchQuality.SAME_MATERIAL;
			if (!newQuality.isBetter(quality))
				quality = newQuality;
		}
		
		// Item flags
		Set<ItemFlag> ourFlags = first.getItemFlags();
		Set<ItemFlag> theirFlags = second.getItemFlags();
		if (!Objects.equals(ourFlags, theirFlags)) {
			newQuality = (!ourFlags.isEmpty() && theirFlags.isEmpty()) ? MatchQuality.SAME_ITEM : MatchQuality.SAME_MATERIAL;
			if (!newQuality.isBetter(quality))
				quality = newQuality;
		}

		// awful but we have to make these values the same so that they don't matter for comparison
		// clone to avoid affecting user
		first = first.clone();
		second = second.clone();

		first.setDisplayName(null);
		second.setDisplayName(null);

		first.setLore(null);
		second.setLore(null);

		for (Enchantment ourEnchant : ourEnchants.keySet())
			first.removeEnchant(ourEnchant);
		for (Enchantment theirEnchant : theirEnchants.keySet())
			second.removeEnchant(theirEnchant);

		for (ItemFlag ourFlag : ourFlags)
			first.removeItemFlags(ourFlag);
		for (ItemFlag theirFlag : theirFlags)
			second.removeItemFlags(theirFlag);

		return first.equals(second) ? quality : MatchQuality.SAME_MATERIAL;
	}
	
	/**
	 * Checks if this item is a 'default' of type. Default items must have not
	 * had their ItemMeta (tags) modified or have block states. Only aliases
	 * can be default items.
	 * @return If this item can be considered the default item of its type.
	 */
	public boolean isDefault() {
		return itemFlags == 0 && blockValues == null;
	}
	
	/**
	 * Checks if this item is an alias or a clone of one that has not been
	 * modified after loading the aliases.
	 *
	 * @return True if is an alias or unmodified clone
	 */
	public boolean isAlias() {
		return isAlias || (itemFlags == 0 && blockValues == null);
	}
	
	/**
	 * Computes the intersection of two ItemDatas. The data range of the returned item data will be the real intersection of the two data ranges, and the type id will be the one
	 * set if any.
	 * 
	 * @param other
	 * @return A new ItemData which is the intersection of the given types, or null if the intersection of the data ranges is empty or both datas have an id != -1 which are not the
	 *         same.
	 */
	@Nullable
	public ItemData intersection(final ItemData other) {
		if (other.type != type) // Different type, no intersection possible
			return null;
		
		// TODO implement meta intersection
		return this;
	}
	
	/**
	 * Returns the ItemStack backing this ItemData.
	 * It is not a copy, so please be careful.
	 * @return Item stack.
	 */
	public ItemStack getStack() {
		return stack;
	}
	
	@Override
	public ItemData clone() {
		return new ItemData(this);
	}
	
	public Material getType() {
		return type;
	}
	
	@Nullable
	public BlockValues getBlockValues() {
		return blockValues;
	}
	
	public ItemMeta getItemMeta() {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) { // AIR has null item meta!
			meta = itemFactory.getItemMeta(Material.STONE);
		}
		assert meta != null;
		return meta;
	}
	
	public void setItemMeta(ItemMeta meta) {
		stack.setItemMeta(meta);
		isAlias = false; // This is no longer exact alias
		plain = false; // This is no longer a plain item
		itemFlags |= ItemFlags.CHANGED_TAGS;
	}
	
	public int getDurability() {
		return ItemUtils.getDamage(stack);
	}
	
	public void setDurability(int durability) {
		ItemUtils.setDamage(stack, durability);
		isAlias = false; // Change happened
		plain = false; // This is no longer a plain item
		itemFlags |= ItemFlags.CHANGED_DURABILITY;
	}
	
	/**
	 * Checks if this item type was created through {@link ch.njol.skript.expressions.ExprPlain}
	 * and thus has no modifications made to it.
	 * @return Whether this item type is 'plain'
	 */
	public boolean isPlain() {
		return plain;
	}
	
	public void setPlain(boolean plain) {
		this.plain = plain;
	}
	
	/**
	 * Compares this ItemData with another to determine if they are matching "plain" items.
	 * For these ItemDatas to match, they must share a {@link Material}. One of the following must also be true:
	 * <ul>
	 * <li>This ItemData is plain AND the other ItemData is plain</li>
	 * <li>This ItemData is plain AND the other ItemData is an alias</li>
	 * <li>This ItemData is an alias AND the other ItemData is plain</li>
	 * </ul>
	 * @param other The ItemData to compare with.
	 * @return Whether these items can be "plain matched"
	 * @see ch.njol.skript.expressions.ExprPlain
	 */
	public boolean matchPlain(ItemData other) {
		return getType() == other.getType() && ((isPlain() && other.isPlain()) || (isPlain() && other.isAlias()) || (isAlias() && other.isPlain()));
	}

	@Override
	public Fields serialize() throws NotSerializableException {
		Fields fields = new Fields(this); // ItemStack is transient, will be ignored
		fields.putPrimitive("id", type.ordinal());
		fields.putObject("meta", stack.getItemMeta());
		return fields;
	}

	private static final Material[] materials = Material.values();

	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		this.type = materials[fields.getAndRemovePrimitive("id", int.class)];
		ItemMeta meta = fields.getAndRemoveObject("meta", ItemMeta.class);

		// Initialize ItemStack
		this.stack = new ItemStack(type);
		stack.setItemMeta(meta); // Just set meta to it

		fields.setFields(this); // Everything but ItemStack and Material
	}
	
	/**
	 * Creates a plain copy of this ItemData. It will have same material,
	 * amount of 1 and same block values. Tags will also be copied, with
	 * following exceptions:
	 * <ul>
	 * <li>Damage: 1.13 tag-damage is only used for actual durability.
	 * Present on 1.12 and older versions.
	 * <li>Name: custom names made with anvil do not change item type
	 * </ul>
	 * @return A modified copy of this item data.
	 */
	public ItemData aliasCopy() {
		ItemData data = new ItemData();
		data.stack = new ItemStack(type, 1);
		
		if (stack.hasItemMeta()) {
			ItemMeta meta = stack.getItemMeta(); // Creates a copy
			meta.setDisplayName(null); // Clear display name
			if (!itemFactory.getItemMeta(type).equals(meta)) // there may be different tags (e.g. potions)
				data.itemFlags |= ItemFlags.CHANGED_TAGS;
			data.stack.setItemMeta(meta);
		}
		ItemUtils.setDamage(data.stack, 0); // Set to undamaged

		data.type = type;
		data.blockValues = blockValues;
		data.itemForm = itemForm;
		return data;
	}
	
	/**
	 * Applies tags to this item.
	 * @param tags Tags in Mojang's JSON format.
	 */
	public void applyTags(String tags) {
		BukkitUnsafe.modifyItemStack(stack, tags);
		itemFlags |= ItemFlags.CHANGED_TAGS;
	}
	
}
