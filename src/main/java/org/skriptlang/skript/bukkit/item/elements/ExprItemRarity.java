package org.skriptlang.skript.bukkit.item.elements;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item Rarity")
@Description("The item rarity of an item.")
@Example("set the item rarity of player's tool to epic")
@Example("set the item rarity of {_item} to uncommon")
@Example("set {_rarity} to item rarity of player's held item")
@Since("INSERT VERSION")
public class ExprItemRarity extends SimplePropertyExpression<ItemType, ItemRarity> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprItemRarity.class,
				ItemRarity.class,
				"item rarity",
				"itemtypes",
				true
			)
				.supplier(ExprItemRarity::new)
				.build()
		);
	}

	@Override
	public @Nullable ItemRarity convert(ItemType item) {
		return item.getRandom() != null ? item.getRandom().getData(DataComponentTypes.RARITY) : null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(ItemRarity.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ItemRarity rarity = null;

		if (delta != null)
			rarity = (ItemRarity) delta[0];

		for (ItemType item : getExpr().getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			meta.setRarity(rarity);
			item.setItemMeta(meta);
		}
	}

	@Override
	public Class<? extends ItemRarity> getReturnType() {
		return ItemRarity.class;
	}

	@Override
	protected String getPropertyName() {
		return "item rarity";
	}

}
