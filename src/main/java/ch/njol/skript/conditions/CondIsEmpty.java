package ch.njol.skript.conditions;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.util.slot.Slot;

/**
 * @author Peter Güttinger
 */
@Name("Is Empty")
@Description("Checks whether an inventory, an inventory slot, or a text is empty.")
@Examples("player's inventory is empty")
@Since("<i>unknown</i> (before 2.1)")
public class CondIsEmpty extends PropertyCondition<Object> {
	
	static {
		register(CondIsEmpty.class, "empty", "inventories/slots/strings");
	}
	
	@Override
	public boolean check(final Object o) {
		if (o instanceof String)
			return ((String) o).isEmpty();
		if (o instanceof Inventory) {
			for (ItemStack s : ((Inventory) o).getContents()) {
				if (s != null && s.getType() != Material.AIR)
					return false; // There is an item here!
			}
			return true;
		}
		if (o instanceof Slot) {
			final Slot s = (Slot) o;
			final ItemStack i = s.getItem();
			return i == null || i.getType() == Material.AIR;
		}
		assert false;
		return false;
	}
	
	@Override
	protected String getPropertyName() {
		return "empty";
	}
	
}
