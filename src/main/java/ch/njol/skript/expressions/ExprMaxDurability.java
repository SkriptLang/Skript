package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Max Durability")
@Description({"The maximum durability of an item. Changing requires Minecraft 1.20.5+",
	"Note: 'delete' will remove the max durability from the item (making it a non-damageable item). Delete requires Paper 1.21+"})
@Examples({
	"maximum durability of diamond sword",
	"if max durability of player's tool is not 0: # Item is damageable",
	"set max durability of player's tool to 5000",
	"add 5 to max durability of player's tool",
	"reset max durability of player's tool",
	"delete max durability of player's tool"
})
@RequiredPlugins("Minecraft 1.20.5+ (custom amount)")
@Since("2.5, 2.9.0 (change)")
public class ExprMaxDurability extends SimplePropertyExpression<Object, Integer> {

	static {
		register(ExprMaxDurability.class, Integer.class, "max[imum] (durabilit(y|ies)|damage)", "itemtypes/itemstacks/slots");
	}

	@Override
	@Nullable
	public Integer convert(Object object) {
		ItemStack itemStack = ItemUtils.asItemStack(object);
		if (itemStack == null)
			return null;
		return ItemUtils.getMaxDamage(itemStack);
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		if (ItemUtils.HAS_MAX_DAMAGE) {
			switch (mode) {
				case SET:
				case ADD:
				case REMOVE:
				case RESET:
					return CollectionUtils.array(Number.class);
				case DELETE:
					if (ItemUtils.HAS_RESET)
						return CollectionUtils.array();
			}
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int change = delta == null ? 0 : ((Number) delta[0]).intValue();
		if (mode == ChangeMode.REMOVE)
			change = -change;

		for (Object object : getExpr().getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				continue;

			int newValue;
			switch (mode) {
				case ADD:
				case REMOVE:
					newValue = ItemUtils.getMaxDamage(itemStack) + change;
					break;
				case SET:
					newValue = change;
					break;
				case DELETE:
					newValue = 0;
					break;
				default:
					newValue = itemStack.getType().getMaxDurability();
			}

			ItemUtils.setMaxDamage(itemStack, newValue);
			if (object instanceof Slot)
				((Slot) object).setItem(itemStack);
			if (object instanceof ItemType)
				((ItemType) object).setItemMeta(itemStack.getItemMeta());
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "max durability";
	}

}
