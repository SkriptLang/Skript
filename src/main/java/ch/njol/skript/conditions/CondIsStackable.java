package ch.njol.skript.conditions;

import org.bukkit.inventory.ItemStack;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Stackable")
@Description("Checks whether an item is stackable.")
@Examples({
	"diamond axe is stackable",
	"birch wood is stackable",
	"torch is stackable"
})
@Since("2.7")
public class CondIsStackable extends PropertyCondition<ItemStack> {

	static {
		register(CondIsStackable.class, "stackable", "itemstacks");
	}

	@Override
	public boolean check(ItemStack item) {
		return item.getMaxStackSize() > 1;
	}

	@Override
	protected String getPropertyName() {
		return "stackable";
	}

}
