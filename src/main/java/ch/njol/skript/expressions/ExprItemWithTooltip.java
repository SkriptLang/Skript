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

@Name("Item with Tooltip")
@Description({
	"Get an item with or without entire/additional tooltip.",
	"If changing the 'entire' tooltip of an item, nothing will show up when a player hovers over it.",
	"If changing the 'additional' tooltip, only specific parts (which change per item) will be hidden."
})
@Example("set {_item with additional tooltip} to diamond with additional tooltip")
@Example("set {_item without entire tooltip} to diamond without entire tooltip")
@RequiredPlugins("Minecraft 1.20.5+")
@Since("2.11")
public class ExprItemWithTooltip extends PropertyExpression<ItemType, ItemType> {

	static {
		if (Skript.methodExists(ItemMeta.class, "isHideTooltip")) {// this method was added in the same version as the additional tooltip item flag
			Skript.registerExpression(ExprItemWithTooltip.class, ItemType.class, ExpressionType.PROPERTY,
				"%itemtypes% with[:out] [entire|:additional] tool[ ]tip[s]"
			);
		}
	}

	private boolean without, entire;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) expressions[0]);
		without = parseResult.hasTag("out");
		entire = !parseResult.hasTag("additional");
		return true;
	}

	@Override
	protected ItemType[] get(Event event, ItemType[] source) {
		return get(source, itemType -> {
			itemType = itemType.clone();
			ItemMeta meta = itemType.getItemMeta();
			if (entire) {
				meta.setHideTooltip(without);
			} else {
				if (without) {
					meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
				} else {
					meta.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
				}
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
		return getExpr().toString(event, debug) + (without ? " without" : " with") + (entire ? " entire" : " additional") + " tooltip";
	}

}
