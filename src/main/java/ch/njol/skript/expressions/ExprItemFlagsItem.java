package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Item with Item Flags")
@Description("Creates a new item with the specified item flags or with all item flags.")
@Examples({
	"give player diamond sword with item flags hide enchants and hide attributes",
	"set {_item} to player's tool with item flag hide additional tooltip",
	"give player torch with hide placed on item flag",
	"set {_item} to diamond sword with all item flags"
})
@Since("INSERT VERSION")
public class ExprItemFlagsItem extends SimpleExpression<ItemType> {

	static {
		Skript.registerExpression(ExprItemFlagsItem.class, ItemType.class, ExpressionType.COMBINED,
			"%itemtype% with item[ ]flag[s] %itemflags%",
			"%itemtype% with %itemflags% item[ ]flag[s]",
			"%itemtype% with all item[ ]flags");
	}

	private Expression<ItemFlag> itemFlag;
	private Expression<ItemType> itemType;
	private boolean allFlags;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		itemType = (Expression<ItemType>) exprs[0];
		if (matchedPattern == 2) {
			allFlags = true;
		} else {
			itemFlag = (Expression<ItemFlag>) exprs[1];
		}
		return true;
	}

	@Override
	protected ItemType[] get(Event event) {
		ItemType[] types = itemType.getArray(event);
		ItemFlag[] flags = allFlags ? ItemFlag.values() : itemFlag.getArray(event);

		for (int i = 0; i < types.length; i++) {
			ItemStack itemStack = types[i].getRandom();
			if (itemStack != null) {
				ItemMeta meta = itemStack.getItemMeta();
				if (meta != null) {
					meta.addItemFlags(flags);
					itemStack.setItemMeta(meta);
					types[i] = new ItemType(itemStack);
				}
			}
		}

		return types;
	}

	@Override
	public boolean isSingle() {
		return itemType.isSingle();
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (allFlags) {
			return itemType.toString(event, debug) + " with all item flags";
		} else {
			return itemType.toString(event, debug) + " with item flags " + itemFlag.toString(event, debug);
		}
	}
}