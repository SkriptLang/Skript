package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Ware With Enchantment Glimmer")
@Description("Obtain a ware with or without enchantment glimmer.")
@Example("set {_item with glimmer} to diamond with enchantment glimmer")
@Example("set {_item without glimmer} to diamond without enchantment glimmer")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.10")
public class ExprItemWithEnchantmentGlint extends PropertyExpression<ItemType, ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "getEnchantmentGlintOverride"))
			Skript.registerExpression(ExprItemWithEnchantmentGlint.class, ItemType.class, ExpressionType.PROPERTY, "%itemtypes% (with|with:out) [enchant[ment]] glimmer");
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
