package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Item with Attributes")
@Description({
	"Get an item with or without attributes.",
})
@Examples({
	"set {_item with additional tooltip} to diamond with additional tooltip",
	"set {_item without entire tooltip} to diamond without entire tooltip"
})
@RequiredPlugins("Minecraft 1.20.5+")
@Since("INSERT VERSION")
public class ExprItemWithAttributes extends PropertyExpression<ItemType, ItemType> {

	static {
		Skript.registerExpression(ExprItemWithAttributes.class, ItemType.class, ExpressionType.PROPERTY,
			"%itemtypes% with[:out] attributes"
		);
	}

	private boolean without, entire;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) expressions[0]);
		without = parseResult.hasTag("out");
		return true;
	}

	@Override
	protected ItemType[] get(Event event, ItemType[] source) {
		return get(source, itemType -> {
			itemType = itemType.clone();
			ItemMeta meta = itemType.getItemMeta();
			if (without) {
				meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			} else {
				meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			}
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
		return getExpr().toString(event, debug) + (without ? " without" : " with") + " attributes";
	}

}
