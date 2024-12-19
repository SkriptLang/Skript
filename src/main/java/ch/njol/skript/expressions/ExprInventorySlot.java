package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

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

@Name("Inventory Slot")
@Description({"Represents a slot in an inventory. It can be used to change the item in an inventory too."})
@Examples({"if slot 0 of player is air:",
	"\tset slot 0 of player to 2 stones",
	"\tremove 1 stone from slot 0 of player",
	"\tadd 2 stones to slot 0 of player",
	"\tclear slot 1 of player"})
@Since("2.2-dev24")
public class ExprInventorySlot extends SimpleExpression<Slot> {
	
	static {
		Skript.registerExpression(ExprInventorySlot.class, Slot.class, ExpressionType.COMBINED,
				"[the] slot[s] %numbers% of %inventory%", "%inventory%'[s] slot[s] %numbers%");
	}

	@SuppressWarnings("null")
	private Expression<Number> slots;
	@SuppressWarnings("null")
	private Expression<Inventory> invis;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0){
			 slots = (Expression<Number>) exprs[0];
			 invis = (Expression<Inventory>) exprs[1];
		} else {
			 slots = (Expression<Number>) exprs[1];
			 invis = (Expression<Inventory>) exprs[0];			
		}
		return true;
	}

	@Override
	@Nullable
	protected Slot[] get(Event event) {
		Inventory invi = invis.getSingle(event);
		if (invi == null)
			return null;
		
		List<Slot> inventorySlots = new ArrayList<>();
		for (Number slot : slots.getArray(event)) {
			if (slot.intValue() >= 0 && slot.intValue() < invi.getSize()) {
				int slotIndex = slot.intValue();
				// Not all indices point to inventory slots. Equipment, for example
				if (invi instanceof PlayerInventory && slotIndex >= 36) {
					HumanEntity holder = ((PlayerInventory) invi).getHolder();
					assert holder != null;
					inventorySlots.add(new EquipmentSlot(holder, slotIndex));
				} else {
					inventorySlots.add(new InventorySlot(invi, slot.intValue()));
				}
			}
		}
		
		if (inventorySlots.isEmpty())
			return null;
		return inventorySlots.toArray(new Slot[inventorySlots.size()]);
	}
	
	@Override
	public boolean isSingle() {
		return slots.isSingle();
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "slots " + slots.toString(e, debug) + " of " + invis.toString(e, debug);
	}
}
