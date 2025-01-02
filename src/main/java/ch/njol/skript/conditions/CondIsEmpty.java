package ch.njol.skript.conditions;

<<<<<<< HEAD
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.bukkitutil.ItemUtils;
=======
>>>>>>> 5a4fb7ed02618575e7b19af507ae2047ebb2892e
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.Material;
import org.skriptlang.skript.lang.util.SkriptQueue;

@Name("Is Empty")
@Description({
	"Checks whether an inventory, an inventory slot, a queue, or a text is empty.",
	"An entity can be a type, which will check if there are no passengers."
})
@Examples("player's inventory is empty")
@Since("<i>unknown</i> (before 2.1), INSERT VERSION (Entity)")
public class CondIsEmpty extends PropertyCondition<Object> {

	static {
		register(CondIsEmpty.class, "empty", "entities/inventories/slots/strings/numbered");
	}

	@Override
	public boolean check(final Object object) {
		if (object instanceof String string)
			return string.isEmpty();
		if (object instanceof SkriptQueue queue)
			return queue.isEmpty();
		if (object instanceof Inventory) {
			for (ItemStack s : ((Inventory) object).getContents()) {
				if (s != null && s.getType() != Material.AIR)
					return false; // There is an item here!
			}
			return true;
		}
		if (object instanceof Slot slot) {
			final ItemStack item = slot.getItem();
			return item == null || item.getType() == Material.AIR;
		}
		if (object instanceof Entity entity)
			return entity.isEmpty();
		if (object instanceof AnyAmount numbered)
			return numbered.isEmpty();
		assert false;
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "empty";
	}

}
