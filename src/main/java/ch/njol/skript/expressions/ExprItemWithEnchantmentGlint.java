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

@Name("Item with Enchantment Glint")
@Description("Get an item with or without enchantment glint.")
@Examples({
	"set {_item with glint} to diamond with enchantment glint",
	"set {_item without glint} to diamond without enchantment glint"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("INSERT VERSION")
public class ExprItemWithEnchantmentGlint extends PropertyExpression<ItemType, ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "getEnchantmentGlintOverride"))
			Skript.registerExpression(ExprItemWithEnchantmentGlint.class, ItemType.class, ExpressionType.PROPERTY, "%itemtypes% with[:out] [enchant[ment]] glint");
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
