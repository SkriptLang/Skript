package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ch.njol.skript.aliases.ItemType;

import java.util.ArrayList;
import java.util.List;

@Name("Item Flags")
@Description("Returns or modifies the item flags of an item.")
@Examples({
	"set item flags of player's tool to hide enchants and hide attributes",
	"add hide potion effects to item flags of player's held item",
	"remove hide enchants from item flags of {legendary sword}"
})
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
	protected ItemFlag[] get(Event e, ItemType[] source) {
		List<ItemFlag> flags = new ArrayList<>();
		for (ItemType itemType : source) {
			ItemStack item = itemType.getRandom();
			if (item != null && item.hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					flags.addAll(meta.getItemFlags());
				}
			}
		}
		return flags.toArray(new ItemFlag[0]);
	}

	@Override
	public Class<? extends ItemFlag> getReturnType() {
		return ItemFlag.class;
	}

	@Override
	public String toString(Event e, boolean debug) {
		return "item flags of " + getExpr().toString(e, debug);
	}

	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET, DELETE -> new Class[]{ItemFlag[].class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object[] delta, ch.njol.skript.classes.Changer.ChangeMode mode) {
		ItemFlag[] flags = (ItemFlag[]) delta;

		for (ItemType itemType : getExpr().getArray(event)) {
			ItemStack item = itemType.getRandom();
			if (item != null) {
				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					switch (mode) {
						case SET -> {
							meta.removeItemFlags(ItemFlag.values());
							meta.addItemFlags(flags);
						}
						case ADD -> meta.addItemFlags(flags);
						case REMOVE -> meta.removeItemFlags(flags);
						case RESET, DELETE, REMOVE_ALL -> meta.removeItemFlags(ItemFlag.values());
						default -> {
							continue;
						}
					}
					item.setItemMeta(meta);
				}
			}
		}
	}

}
