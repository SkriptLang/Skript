package org.skriptlang.skript.bukkit.tags;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.Keyed;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.tags.elements.conditions.CondIsTagged;
import org.skriptlang.skript.bukkit.tags.elements.effects.EffRegisterTag;
import org.skriptlang.skript.bukkit.tags.elements.expressions.*;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

public class TagModule extends HierarchicalAddonModule {

	// paper tags
	public static final boolean PAPER_TAGS_EXIST = Skript.classExists("com.destroystokyo.paper.MaterialTags");

	// tag object
	public static TagRegistry tagRegistry;

	public TagModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.Tag");
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		// Classes
		Classes.registerClass(new ClassInfo<>(Tag.class, "minecrafttag")
			.user("minecraft ?tags?")
			.name("Minecraft Tag")
			.description("A tag that classifies a material, or entity.")
			.since("2.10")
			.parser(new Parser<Tag<?>>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(Tag<?> tag, int flags) {
					return "tag " + tag.getKey();
				}

				@Override
				public String toVariableNameString(Tag<?> tag) {
					return toString(tag, 0);
				}
			}));

		// compare tags by keys, not by object instance.
		Comparators.registerComparator(Tag.class, Tag.class, (a, b) -> Relation.get(a.getKey().equals(b.getKey())));

		// init tags
		tagRegistry = new TagRegistry();
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondIsTagged::register,
			EffRegisterTag::register,
			ExprTag::register,
			ExprTagContents::register,
			ExprTagKey::register,
			ExprTagsOf::register,
			ExprTagsOfType::register
		);
	}

	/**
	 * Retrieves a Keyed array based on the type of the provided input object.
	 *
	 * @param input the input object to determine the keyed value, can be of type Entity,
	 *              EntityData, ItemType, ItemStack, Slot, Block, or BlockData.
	 * @return a Keyed array corresponding to the input's type, or null if the input is null
	 *         or if no corresponding Keyed value can be determined. ItemTypes may return multiple values,
	 *         though everything else will return a single element array.
	 */
	@Contract(value = "null -> null", pure = true)
	public static @Nullable Keyed[] getKeyed(Object input) {
		Keyed value = null;
		Keyed[] values = null;
		if (input == null)
			return null;
		if (input instanceof Entity entity) {
			value = entity.getType();
		} if (input instanceof EntityData<?> data) {
			value = EntityUtils.toBukkitEntityType(data);
		} else if (input instanceof ItemType itemType) {
			values = itemType.getMaterials();
		} else if (input instanceof ItemStack itemStack) {
			value = itemStack.getType();
		} else if (input instanceof Slot slot) {
			ItemStack stack = slot.getItem();
			if (stack == null)
				return null;
			value = stack.getType();
		} else if (input instanceof Block block) {
			value = block.getType();
		} else if (input instanceof BlockData data) {
			value = data.getMaterial();
		}
		if (value == null)
			return values;
		return new Keyed[]{value};
	}

	@Override
	public String name() {
		return "tags";
	}

}
