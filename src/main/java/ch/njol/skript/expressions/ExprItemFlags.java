package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
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
import ch.njol.skript.aliases.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Name("Item Flags")
@Description("The item flags of an item. Can be modified.")
@Examples({
	"set item flags of player's tool to hide enchants and hide attributes",
	"add hide potion effects to item flags of player's held item",
	"remove hide enchants from item flags of {legendary sword}"
})
@Since("2.10")
public class ExprItemFlags extends PropertyExpression<ItemType, ItemFlag> {

	static {
		register(ExprItemFlags.class, ItemFlag.class, "item flags", "itemtypes");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends ItemType>) exprs[0]);
		return true;
	}

	@Override
	protected ItemFlag[] get(Event event, ItemType[] source) {
		Set<ItemFlag> flags = new HashSet<>();
		for (ItemType itemType : source) {
			ItemMeta meta = itemType.getItemMeta();
			flags.addAll(meta.getItemFlags());
		}
		return flags.toArray(new ItemFlag[0]);
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET, DELETE -> CollectionUtils.array(ItemFlag[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ItemFlag[] flags = delta != null ? (ItemFlag[]) delta : new ItemFlag[0];

		for (ItemType itemType : getExpr().getArray(event)) {
			ItemMeta meta = itemType.getItemMeta();
			switch (mode) {
				case SET -> {
					meta.removeItemFlags(ItemFlag.values());
					meta.addItemFlags(flags);
				}
				case ADD -> meta.addItemFlags(flags);
				case REMOVE -> meta.removeItemFlags(flags);
				case RESET, DELETE -> meta.removeItemFlags(ItemFlag.values());
				default -> {
					return;
				}
			}
			itemType.setItemMeta(meta);
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
	public String toString(Event event, boolean debug) {
		return "item flags of " + getExpr().toString(event, debug);
	}

}
