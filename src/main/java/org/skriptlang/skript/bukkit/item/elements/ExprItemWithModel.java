package org.skriptlang.skript.bukkit.item.elements;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item With Model")
@Description("""
	Returns a copy of an item with a custom item model. \
	Accepts a Namespaced Key which takes the form of "namespace:key", e.g. "minecraft:dirt". \
	See <a href='https://minecraft.wiki/w/Identifier'> this article</a> for more detail. \
	""")
@Example("""
	set {_item} to emerald with model "minecraft:diamond" named "fake diamond"'
	give {_item} to player
	""")
@Since("INSERT VERSION")
@Keywords("item model")
public class ExprItemWithModel extends PropertyExpression<ItemType, ItemType> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			DefaultSyntaxInfos.Expression.builder(ExprItemWithModel.class, ItemType.class)
				.addPattern("%itemtype% with [the] [item] model %string%")
				.supplier(ExprItemWithModel::new)
				.build()
		);
	}

	private Expression<String> key;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		key = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	protected ItemType[] get(Event event, ItemType[] source) {
		String key = this.key.getSingle(event);
		if (key == null)
			return source;

		NamespacedKey namespacedKey = NamespacedUtils.checkValidationAndSend(key, this);
		if (namespacedKey == null)
			return source;

		return get(source, itemType -> {
			itemType = itemType.clone();
			ItemMeta itemMeta = itemType.getItemMeta();
			itemMeta.setItemModel(namespacedKey);
			itemType.setItemMeta(itemMeta);
			return itemType;
		});
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + " with item model " + key.toString(event, debug);
	}

}
