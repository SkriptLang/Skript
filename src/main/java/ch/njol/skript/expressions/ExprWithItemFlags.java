package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Item with Item Flags")
@Description("Creates a new item with the specified item flags.")
@Examples({
	"give player diamond sword with item flags hide enchants and hide attributes",
	"set {_item} to player's tool with item flag hide additional tooltip",
	"give player torch with hide placed on item flag",
	"set {_item} to diamond sword with all item flags"
})
@Since("2.10")
public class ExprWithItemFlags extends SimpleExpression<ItemType> {

	static {
		Skript.registerExpression(ExprWithItemFlags.class, ItemType.class, ExpressionType.COMBINED,
			"%itemtypes% with [the] item flag[s] %itemflags%",
			"%itemtypes% with [the] %itemflags% item flag[s]");
	}

	private Expression<ItemFlag> itemFlags;
	private Expression<ItemType> itemTypes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		itemTypes = (Expression<ItemType>) exprs[0];
		itemFlags = (Expression<ItemFlag>) exprs[1];
		return true;
	}

	@Override
	protected ItemType[] get(Event event) {
		ItemType[] types = itemTypes.getArray(event);
		ItemFlag[] flags = itemFlags.getArray(event);

		ItemType[] result = new ItemType[types.length];
		for (int i = 0; i < types.length; i++) {
			ItemType clonedType = types[i].clone();
			ItemMeta meta = clonedType.getItemMeta();
			if (meta != null) {
				meta.addItemFlags(flags);
				clonedType.setItemMeta(meta);
			}
			result[i] = clonedType;
		}

		return result;
	}

	@Override
	public boolean isSingle() {
		return itemTypes.isSingle();
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return itemTypes.toString(event, debug) + " with item flags " + itemFlags.toString(event, debug);
	}

}
