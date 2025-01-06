package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Inventory Slot")
@Description("Represents a slot in an inventory. It can be used to change the item in an inventory too.")
@Examples({
	"if slot 0 of player is air:",
		"\tset slot 0 of player to 2 stones",
		"\tremove 1 stone from slot 0 of player",
		"\tadd 2 stones to slot 0 of player",
		"\tclear slot 1 of player",
	"",
	"loop all of the slots of player:",
		"\tif index of loop-value is evenly divisible by 2:",
			"\tclear loop-value"
})
@Since("2.2-dev24, INSERT VERSION (all slots)")
public class ExprInventorySlot extends SimpleExpression<Slot> {
	
	static {
		Skript.registerExpression(ExprInventorySlot.class, Slot.class, ExpressionType.COMBINED,
			"[the] slot[s] %numbers% of %inventories%",
			"%inventory%'[s] slot[s] %numbers%",
			"all [[of] the] slots of %inventories%");
	}

	private @Nullable Expression<Number> slots;
	private Expression<Inventory> inventories;
	private boolean allSlots;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		allSlots = matchedPattern == 2;
		if (allSlots) {
			//noinspection unchecked
			inventories = (Expression<Inventory>) exprs[0];
		} else {
			//noinspection unchecked
			slots = (Expression<Number>) (matchedPattern == 0 ? exprs[0] : exprs[1]);
			//noinspection unchecked
			inventories = (Expression<Inventory>) (matchedPattern == 0 ? exprs[1] : exprs[0]);
		}
		return true;
	}

	@Override
	protected Slot @Nullable [] get(Event event) {
		List<Slot> inventorySlots = new ArrayList<>();
		Number[] slots = this.slots != null ? this.slots.getArray(event) : null;
		for (Inventory inventory : inventories.getArray(event)) {
			int invSize = inventory.getSize();
			if (allSlots) {
				if (inventory instanceof PlayerInventory playerInventory) {
					HumanEntity humanEntity = playerInventory.getHolder();
					assert humanEntity != null;
					invSize -= 4;
					for (int i = 36; i < 40; i++)
						inventorySlots.add(new EquipmentSlot(humanEntity, i));
				}
				for (int i = 0; i < invSize; i++)
					inventorySlots.add(new InventorySlot(inventory, i));
			} else if (slots != null) {
				for (Number slot : slots) {
					int slotIndex = slot.intValue();
					if (slotIndex < 0 || slotIndex > invSize)
						continue;
					if (inventory instanceof PlayerInventory playerInventory && slotIndex >= 36) {
						HumanEntity humanEntity = playerInventory.getHolder();
						assert humanEntity != null;
						inventorySlots.add(new EquipmentSlot(humanEntity, slotIndex));
					} else {
						inventorySlots.add(new InventorySlot(inventory, slotIndex));
					}
				}
			}
		}
		if (inventorySlots.isEmpty())
			return null;
		return inventorySlots.toArray(new Slot[0]);
	}
	
	@Override
	public boolean isSingle() {
		if (allSlots)
			return false;
		return slots.isSingle() && inventories.isSingle();
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (allSlots)
			return "all of the slots of " + inventories.toString(event, debug);
		return "slots " + slots.toString(event, debug) + " of " + inventories.toString(event, debug);
	}

}
