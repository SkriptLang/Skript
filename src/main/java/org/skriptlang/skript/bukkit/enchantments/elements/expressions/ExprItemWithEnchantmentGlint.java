package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Item with Enchantment Glint")
@Description("Get an item with or without enchantment glint.")
@Example("set {_item with glint} to diamond with enchantment glint")
@Example("set {_item without glint} to diamond without enchantment glint")
@Since("2.10")
public class ExprItemWithEnchantmentGlint extends PropertyExpression<ItemType, ItemType> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprItemWithEnchantmentGlint.class, ItemType.class)
			.addPattern("%itemtypes% with[:out] [enchant[ment]] glint")
			.supplier(ExprItemWithEnchantmentGlint::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.build());
	}

	private boolean glint;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) expressions[0]);
		glint = !parseResult.hasTag("out");
		return true;
	}

	@Override
	protected ItemType[] get(Event event, ItemType[] source) {
		return get(source, itemType -> {
			itemType = itemType.clone();
			ItemMeta meta = itemType.getItemMeta();
			meta.setEnchantmentGlintOverride(glint);
			itemType.setItemMeta(meta);
			return itemType;
        });
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + (glint ? " with" : " without") + " enchantment glint";
	}

}
