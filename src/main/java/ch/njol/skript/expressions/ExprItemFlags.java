package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Name("ItemFlag - The ItemFlags of an Item")
@Description("Get or Set the ItemFlags of an item")
@Examples({"set {_flags::*} to item flags of player's tool",
	"add hide enchants to item flags of player's tool",
	"add hide attributes to item flags of player's tool",
	"add hide enchants and hide attributes to item flags of player's tool",
	"remove hide enchants from item flags of player's tool",
	"remove hide attributes from item flags of player's tool",
	"delete item flags of player's tool",
	"reset item flags of player's tool"})
@Since("INSERT VERSION")
public class ExprItemFlags extends PropertyExpression<ItemType, ItemFlag> {

	static {
		register(ExprItemFlags.class, ItemFlag.class, "item[ ]flags", "itemtypes");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends ItemType>) exprs[0]);
		return true;
	}

	@Override
	protected ItemFlag[] get(Event event, ItemType[] source) {
		List<ItemFlag> itemFlagList = new ArrayList<>();
		for (ItemType itemType : source) {
			itemFlagList.addAll(itemType.getItemMeta().getItemFlags());
		}
		return itemFlagList.toArray(new ItemFlag[0]);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE) {
			return CollectionUtils.array(ItemFlag[].class);
		} else if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET) {
			return CollectionUtils.array();
		}
		return null;
	}

	@SuppressWarnings({"NullableProblems", "ConstantValue"})
	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET) {
			for (ItemType itemType : getExpr().getArray(event)) {
				ItemMeta itemMeta = itemType.getItemMeta();
				Set<ItemFlag> itemFlags = itemMeta.getItemFlags();
				itemMeta.removeItemFlags(itemFlags.toArray(new ItemFlag[0]));
				itemType.setItemMeta(itemMeta);
			}
		} else if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE) {
			if (delta != null && delta instanceof ItemFlag[] itemFlags) {
				for (ItemType itemType : getExpr().getArray(event)) {
					ItemMeta itemMeta = itemType.getItemMeta();
					if (mode == Changer.ChangeMode.ADD) {
						itemMeta.addItemFlags(itemFlags);
					} else {
						itemMeta.removeItemFlags(itemFlags);
					}
					itemType.setItemMeta(itemMeta);
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ItemFlag> getReturnType() {
		return ItemFlag.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "itemflags of " + getExpr().toString(event, debug);
	}

}
