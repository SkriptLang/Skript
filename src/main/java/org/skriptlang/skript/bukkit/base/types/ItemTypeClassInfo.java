package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.EnchantmentType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.util.Arrays;

@ApiStatus.Internal
public class ItemTypeClassInfo extends ClassInfo<ItemType> {

	public ItemTypeClassInfo() {
		super(ItemType.class, "itemtype");
		this.user("item ?types?", "materials?")
			.name("Item Type")
			.description("An item type is an alias that can result in different items when added to an inventory, " +
					"and unlike <a href='#itemstack'>items</a> they are well suited for checking whether an inventory contains a certain item or whether a certain item is of a certain type.",
				"An item type can also have one or more <a href='#enchantmenttype'>enchantments</a> with or without a specific level defined, " +
					"and can optionally start with 'all' or 'every' to make this item type represent <i>all</i> types that the alias represents, including data ranges.")
			.usage("[<number> [of]] [all/every] <alias> [of <enchantment> [<level>] [,/and <more enchantments...>]]")
			.examples("give 4 torches to the player",
				"add oak slab to the inventory of the block",
				"player's tool is a diamond sword of sharpness",
				"block is dirt or farmland")
			.since("1.0")
			.before("itemstack", "entitydata", "entitytype")
			.after("number", "integer", "long", "time")
			.supplier(() -> Arrays.stream(Material.values())
				.map(ItemType::new)
				.iterator())
			.parser(new ItemTypeParser())
			.cloner(ItemType::clone)
			.serializer(new YggdrasilSerializer<>())
			.property(Property.NAME,
				"An item type's custom name, if set. Can be set or reset.",
				Skript.instance(),
				new ItemTypeNameHandler())
			.property(Property.DISPLAY_NAME,
				"An item type's custom name, if set. Can be set or reset.",
				Skript.instance(),
				new ItemTypeNameHandler())
			.property(Property.AMOUNT,
				"The amount of items in the stack this type represents. E.g. 5 for '5 stone swords'. Can be set.",
				Skript.instance(),
				new ItemTypeAmountHandler());
	}

	private static class ItemTypeParser extends Parser<ItemType> {
		//<editor-fold desc="item type parser" defaultstate="collapsed">
		@Override
		public @Nullable ItemType parse(String s, ParseContext context) {
			return Aliases.parseItemType(s);
		}

		@Override
		public String toString(ItemType t, int flags) {
			return t.toString(flags);
		}

		@Override
		public String getDebugMessage(ItemType t) {
			return t.getDebugMessage();
		}

		@Override
		public String toVariableNameString(ItemType itemType) {
			final StringBuilder result = new StringBuilder("itemtype:");
			result.append(itemType.getInternalAmount());
			result.append(",").append(itemType.isAll());
			// TODO this is missing information
			for (ItemData itemData : itemType.getTypes()) {
				result.append(",").append(itemData.getType());
			}
			EnchantmentType[] enchantmentTypes = itemType.getEnchantmentTypes();
			if (enchantmentTypes != null) {
				result.append("|");
				for (EnchantmentType enchantmentType : enchantmentTypes) {
					Enchantment enchantment = enchantmentType.getType();
					if (enchantment == null)
						continue;
					result.append("#").append(enchantment.getKey());
					result.append(":").append(enchantmentType.getLevel());
				}
			}
			return result.toString();
		}
		//</editor-fold>
	}

	private static class ItemTypeNameHandler implements ExpressionPropertyHandler<ItemType, String> {
		//<editor-fold desc="item type name handler" defaultstate="collapsed">
		@Override
		public String convert(ItemType itemType) {
			return itemType.name();
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
				return new Class[] {String.class};
			return null;
		}

		@Override
		public void change(ItemType itemType, Object @Nullable [] delta, ChangeMode mode) {
			String name = delta != null ? (String) delta[0] : null;
			itemType.setName(name);
		}

		@Override
		public @NotNull Class<String> returnType() {
			return String.class;
		}
		//</editor-fold>
	}

	private static class ItemTypeAmountHandler implements ExpressionPropertyHandler<ItemType, Number> {
		//<editor-fold desc="amount property for item types" defaultstate="collapsed">
		@Override
		public Number convert(ItemType itemType) {
			return itemType.getAmount();
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return new Class[] {Integer.class};
			return null;
		}

		@Override
		public void change(ItemType itemType, Object @Nullable [] delta, ChangeMode mode) {
			if (mode == ChangeMode.SET) {
				assert delta != null;
				itemType.setAmount((Integer) delta[0]);
			}
		}

		@Override
		public @NotNull Class<Number> returnType() {
			return Number.class;
		}
		//</editor-fold>
	}

}
