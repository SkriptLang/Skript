package org.skriptlang.skript.bukkit.item.elements;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item Model")
@Description("""
	The item model of an item. \
	Accepts a Namespaced Key which takes the form of "namespace:key", e.g. "minecraft:dirt". \
	See <a href='https://minecraft.wiki/w/Identifier'> this article</a> for more detail. \
	""")
@Example("set the item model of player's held item to \"diamond\"")
@Example("set the item model of {_item} to \"minecraft:dirt\"")
@Since("INSERT VERSION")
public class ExprItemModel extends SimplePropertyExpression<ItemType, String> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprItemModel.class, String.class, "item model", "itemtypes", true)
				.supplier(ExprItemModel::new)
				.build()
		);
	}

	@Override
	public @Nullable String convert(ItemType from) {
		NamespacedKey key = from.getItemMeta().getItemModel();
		if (key == null)
			return null;

		return key.asString();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(String.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		NamespacedKey key = null;
		if (delta != null) {
			key = NamespacedUtils.checkValidationAndSend((String) delta[0], this);
			if (key == null) {
				return;
			}
		}

		for (ItemType item : getExpr().getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			meta.setItemModel(key);
			item.setItemMeta(meta);
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "item model";
	}

}
