package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("First Empty Slot in Inventory")
@Description("Returns the first empty slot in an inventory. If no empty slot is found, it returns nothing.")
@Example("set the first empty slot in player's inventory to 5 diamonds")
@Example("""
	if the first empty slot in player's inventory is not set:
		message "No empty slot available in your inventory!" to player
	""")
@Since("2.12")
@Keywords({"full", "inventory", "empty", "air", "slot"})
public class ExprFirstEmptySlot extends SimplePropertyExpression<Inventory, Slot> {

	static {
		// support `first empty slot in inventory` as well as typical property syntax
		List<String> patterns = new ArrayList<>(Arrays.asList(getPatterns("first empty slot[s]", "inventories")));
		patterns.add("[the] first empty slot[s] in %inventories%");
		Skript.registerExpression(ExprFirstEmptySlot.class, Slot.class, ExpressionType.PROPERTY, patterns.toArray(new String[0]));
	}

	@Override
	public @Nullable Slot convert(Inventory from) {
		int slotIndex = from.firstEmpty();
		if (slotIndex == -1)
			return null; // No empty slot found
		return new InventorySlot(from, slotIndex);
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	protected String getPropertyName() {
		return "first empty slot";
	}

}
