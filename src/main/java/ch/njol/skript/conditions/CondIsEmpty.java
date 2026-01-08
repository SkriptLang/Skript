package ch.njol.skript.conditions;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.common.properties.conditions.PropCondIsEmpty;
import org.skriptlang.skript.lang.util.SkriptQueue;

/**
 * @deprecated This is being removed in favor of {@link PropCondIsEmpty}
 */
@Name("Is Empty")
@Description("Checks whether an inventory, an inventory slot, a queue, or a text is empty.")
@Example("player's inventory is empty")
@Since("unknown (before 2.1)")
@Deprecated(since="2.13", forRemoval = true)
public class CondIsEmpty extends PropertyCondition<Object> {

	static {
		if (!SkriptConfig.useTypeProperties.value())
			register(CondIsEmpty.class, "empty", "inventories/slots/strings/numbered");
	}

	@Override
	public boolean check(final Object object) {
		if (object instanceof String string)
			return string.isEmpty();
		if (object instanceof SkriptQueue queue)
			return queue.isEmpty();
		if (object instanceof Inventory inventory) {
			for (ItemStack s : inventory.getContents()) {
				if (s != null && s.getType() != Material.AIR)
					return false; // There is an item here!
			}
			return true;
		}
		if (object instanceof Slot slot) {
			final ItemStack item = slot.getItem();
			return item == null || item.getType() == Material.AIR;
		}
		if (object instanceof AnyAmount numbered) {
			return numbered.isEmpty();
		}
		assert false;
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "empty";
	}

}
