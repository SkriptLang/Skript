package ch.njol.skript.aliases;

import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockValues;
import ch.njol.skript.localization.Message;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.SimpleClassSerializer.NonInstantiableClassSerializer;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.Set;

public class ItemData implements Cloneable, YggdrasilExtendedSerializable {

	static {
		Variables.yggdrasil.registerClassResolver(new NonInstantiableClassSerializer<>(ItemData.class, "ItemData") {
			@Override
			public Fields serialize(ItemData itemData) throws NotSerializableException {
				return itemData.serialize();
			}

			@Override
			protected ItemData deserialize(Fields fields) throws NotSerializableException, StreamCorruptedException {
				ItemData itemData = new ItemData();
				itemData.deserialize(fields);
				return itemData;
			}

			@Override
			public @Nullable Class<? extends ItemData> getClass(String id) {
				// both "ItemData" and "NewItemData" should point to this class
				if (id.equals(this.id) || id.equals("NewItemData")) {
					return this.type;
				}
				return null;
			}
		});
	}

	private static final ItemFactory itemFactory = Bukkit.getServer().getItemFactory();
	private static final Message M_NAMED = new Message("aliases.named");

	/**
	 * Type of the item as Bukkit material. Serialized manually.
	 */
	transient Material type;

	/**
	 * If this represents all possible items.
	 */
	boolean isAnything;

	/**
	 * ItemStack, which is used for everything but serialization.
	 */
	@Nullable ItemStack stack;

	/**
	 * When this ItemData represents a block, this contains information to
	 * allow comparing it against other blocks.
	 */
	@Nullable BlockValues blockValues;

	/**
	 * Whether this represents an item (that definitely cannot have block states) or a block, which might have them.
	 */
	boolean itemForm;

	/**
	 * If this item is an alias or a clone of one that has not been modified after loading the aliases.
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

	/**
	 * Only to be used for serialization and cloning.
	 */
	private ItemData() { }

	public ItemData(Material type) {
		this(type, 1);
	}

	public ItemData(Material type, int amount) {
		this.type = type;
		if (type.isItem())
			this.stack = ItemStack.of(type, Math.abs(amount));
		this.blockValues = BlockCompat.INSTANCE.getBlockValues(type);
	}

	public ItemData(Material material, @Nullable BlockValues values) {
		this.type = material;
		this.blockValues = values;
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

	public ItemData(BlockData blockData) {
		this.type = blockData.getMaterial();
		if (type.isItem())
			this.stack = new ItemStack(type);
		this.blockValues = BlockCompat.INSTANCE.getBlockValues(blockData);
	}

	public ItemData(Block block) {
		this(block.getBlockData());
	}

	/**
	 * Tests whether the given item is of this type.
	 * 
	 * @param item The stack to compare too.
	 * @return Whether the given item is of this type.
	 */
	public boolean isOfType(@Nullable ItemStack item) {
		if (item == null)
			return type == Material.AIR;

		if (type != item.getType())
			return false; // Obvious mismatch

		if (stack != null && itemFlags != 0) { // Either stack has tags (or durability)
			if (ItemUtils.getDamage(stack) != ItemUtils.getDamage(item))
				return false; // On 1.12 and below, damage is not in meta
			if (stack.hasItemMeta() == item.hasItemMeta()) // Compare ItemMeta as in isSimilar() of ItemStack
				return !stack.hasItemMeta() || itemFactory.equals(stack.getItemMeta(), item.getItemMeta());
			return false;
		}
		return true;
	}

	/**
	 * Returns <code>Aliases.{@link Aliases#getMaterialName(ItemData, boolean) getMaterialName}(ItemData, boolean)</code>
	 * called with this object and relevant plurality setting.
	 */
	@Override
	public String toString() {
		return toString(false, false);
	}

	public String toString(final boolean debug, final boolean plural) {
		StringBuilder builder = new StringBuilder(Aliases.getMaterialName(this, plural));
		ItemMeta meta = stack != null ? stack.getItemMeta() : null;
		if (meta != null && meta.hasDisplayName()) {
			builder.append(" ")
				.append(M_NAMED)
				.append(" ")
				.append(TextComponentParser.instance().toString(meta.displayName()));
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
		if (!(obj instanceof ItemData other))
			return false;

		if (isAlias()) { // This is alias, other item might not be
			return other.matchAlias(this).isAtLeast(MatchQuality.SAME_ITEM);
		} else { // This is not alias, but other might be
			return matchAlias(other).isAtLeast(MatchQuality.SAME_ITEM);
		}
	}

	@Override
	public int hashCode() {
		int hash = type.hashCode(); // Has collisions, but probably not too many of them
		if (blockValues == null || blockValues.isDefault()) {
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
				return MatchQuality.SAME_MATERIAL;
			}
		}

		if (this.stack == null || item.stack == null) { // nothing else to compare
			if (this.stack != null) { // one has a stack, the other does not
				return MatchQuality.SAME_MATERIAL;
			}
			return quality;
		}

		// See if we need to compare durability; for blocks, BlockValues handles this when needed
		// From API perspective, durability is part of ItemMeta; however, we need to support 1.12 and older
		if (itemForm && !stack.getDataOrDefault(DataComponentTypes.DAMAGE, 0).equals(item.stack.getDataOrDefault(DataComponentTypes.DAMAGE, 0))) {
			if (item.hasFlag(ItemFlags.CHANGED_DURABILITY)) { // Given item requests we match its durability
				return MatchQuality.SAME_MATERIAL;
			} else { // Given item doesn't care about durability, so are still same enough
				quality = MatchQuality.SAME_ITEM;
			}
		}

		if (item.hasFlag(ItemFlags.CHANGED_TAGS)) {
			// other item has specific data we should compare to
			quality = quality.min(compareItemStacks(this.stack, item.stack));
		} else if (quality == MatchQuality.EXACT && !stack.matchesWithoutData(item.stack, DAMAGE_ONLY, true)) {
			// other item doesn't care about meta, it's not EXACT, but is still SAME_ITEM
			quality = MatchQuality.SAME_ITEM;
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

	private static final DataComponentType[] ALL_TYPES = RegistryAccess.registryAccess()
		.getRegistry(RegistryKey.DATA_COMPONENT_TYPE)
		.stream()
		.filter(type -> type != DataComponentTypes.DAMAGE)
		.toArray(DataComponentType[]::new);

	private static final Set<DataComponentType> DAMAGE_ONLY = Set.of(DataComponentTypes.DAMAGE);

	private static MatchQuality compareItemStacks(ItemStack ourStack, ItemStack theirStack) {
		// early check for equality (allows fewer isDataOverridden calls and is about 50% faster as a result)
		if (ourStack.matchesWithoutData(theirStack, DAMAGE_ONLY, true))
			return MatchQuality.EXACT;

		Set<DataComponentType> excluded = new HashSet<>();
		for (var type : ALL_TYPES) {
			// if we have data and they do not, then we may be a superset, exclude in final check
			if (ourStack.isDataOverridden(type) && !theirStack.isDataOverridden(type))
				excluded.add(type);
		}

		// since at this point we know we are different, if there are no exclusions the difference MUST
		// either be 1 component that we override differently, or a component that they override and we do not.
		if (excluded.isEmpty())
			return MatchQuality.SAME_MATERIAL;

		excluded.add(DataComponentTypes.DAMAGE); // we handle damage separately
		return ourStack.matchesWithoutData(theirStack, excluded, true) ? MatchQuality.SAME_ITEM : MatchQuality.SAME_MATERIAL;
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
	 * Returns the ItemStack backing this ItemData.
	 * It is not a copy, so please be careful.
	 * @return Item stack.
	 */
	public @Nullable ItemStack getStack() {
		return stack;
	}

	@Override
	public ItemData clone() {
		ItemData itemData;
		try {
			itemData = (ItemData) super.clone();
		} catch (CloneNotSupportedException e) { // should not be possible
			throw new RuntimeException(e);
		}
		if (itemData.stack != null) {
			itemData.stack = itemData.stack.clone();
		}
		return itemData;
	}

	public Material getType() {
		return type;
	}

	@Nullable
	public BlockValues getBlockValues() {
		return blockValues;
	}

	public ItemMeta getItemMeta() {
		ItemMeta meta = stack != null ? stack.getItemMeta() : null;
		if (meta == null) { // AIR has null item meta!
			meta = itemFactory.getItemMeta(Material.STONE);
		}
		assert meta != null;
		return meta;
	}

	public void setItemMeta(ItemMeta meta) {
		if (stack == null)
			return;
		stack.setItemMeta(meta);
		isAlias = false; // This is no longer exact alias
		plain = false; // This is no longer a plain item
		itemFlags |= ItemFlags.CHANGED_TAGS;
	}

	public int getDurability() {
		if (stack == null)
			return 0; // no damage?
		return ItemUtils.getDamage(stack);
	}

	public void setDurability(int durability) {
		if (stack == null)
			return;
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
		Fields fields = new Fields(this);
		fields.putObject("key", type.getKey().toString());
		return fields;
	}

	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		if (fields.hasField("key")) {
			String key = fields.getAndRemoveObject("key", String.class);
			if (key == null)
				throw new StreamCorruptedException("Material key is null");
			this.type = Material.matchMaterial(key);
		} else {
			// attempt back compat deserialization, though using ordinals is not reliable
			this.type = Material.values()[fields.getAndRemovePrimitive("id", int.class)];
		}

		if (fields.hasField("meta")) { // on older versions, we only stored meta
			ItemMeta meta = fields.getAndRemoveObject("meta", ItemMeta.class);
			// Initialize ItemStack
			ItemStack itemStack = null;
			if (meta != null && type.isItem()) {
				itemStack = ItemStack.of(type);
				itemStack.setItemMeta(meta); // Just set meta to it
			}
			fields.putObject("stack", itemStack);
		}

		fields.setFields(this); // Everything but ItemStack and Material
	}

	private static final DataComponentType.Valued<?>[] COPYABLE_FOR_BASE = new DataComponentType.Valued[]{
		DataComponentTypes.POTION_CONTENTS, DataComponentTypes.INSTRUMENT
	};

	/**
	 * Returns a base version of this item data, i.e. only contains
	 * the minimum ItemMeta info to represent the item, in the case of potions/goat horns.
 	 * @return Plain item type.
	 */
	public @Nullable ItemData getBaseCopy() {
		ItemData data = new ItemData();
		data.type = type;
		data.blockValues = blockValues;
		data.itemForm = itemForm;
		if (stack != null) {
			data.stack = new ItemStack(type, 1);
			// note: there is a bulk copy method, but we need to be sure a change is actually applied
			//noinspection rawtypes
			for (DataComponentType.Valued type : COPYABLE_FOR_BASE) {
				if (stack.hasData(type)) {
					//noinspection unchecked, ConstantConditions - presence is checked
					data.stack.setData(type, stack.getData(type));
					data.itemFlags = ItemFlags.CHANGED_TAGS;
				}
			}
		}
		return data;
	}

}
