package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Damage Value/Durability")
@Description("The damage value/durability of an item.")
@Examples({
	"set damage value of player's tool to 10",
	"reset the durability of {_item}",
	"set durability of player's held item to 0"
})
@Since("1.2, 2.7 (durability reversed)")
public class ExprDurability extends SimplePropertyExpression<Object, Integer> {

	private boolean durability;

	static {
		register(ExprDurability.class, Integer.class, "(damage[s] [value[s]]|1:durabilit(y|ies))", "itemtypes/itemstacks/slots");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		durability = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Integer convert(Object object) {
		ItemStack itemStack = ItemUtils.asItemStack(object);
		if (itemStack == null)
			return null;
		int damage = ItemUtils.getDamage(itemStack);
		return convertToDamage(itemStack, damage);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
			case RESET:
				return CollectionUtils.array(Number.class);
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

			int newAmount;
			switch (mode) {
				case ADD:
				case REMOVE:
					int current = convertToDamage(itemStack, ItemUtils.getDamage(itemStack));
					newAmount = current + change;
					break;
				case SET:
					newAmount = change;
					break;
				default:
					newAmount = 0;
			}

			ItemUtils.setDamage(itemStack, convertToDamage(itemStack, newAmount));
			if (object instanceof Slot)
				((Slot) object).setItem(itemStack);
			else if (object instanceof ItemType)
				((ItemType) object).setItemMeta(itemStack.getItemMeta());
		}
	}

	private int convertToDamage(ItemStack itemStack, int value) {
		if (!durability)
			return value;

		int maxDurability = ItemUtils.getMaxDamage(itemStack);

		if (maxDurability == 0)
			return 0;
		return maxDurability - value;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String getPropertyName() {
		return durability ? "durability" : "damage";
	}

}
