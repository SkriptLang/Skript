/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.PlayerInventory;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;

@Name("Hotbar Slot")
@Description({"The currently selected hotbar <a href='./classes.html#slot'>slot</a>. To retrieve its number use <a href='#ExprSlotIndex'>Slot Index</a> expression."})
@Examples({"message \"%player's current hotbar slot%\"",
            "set player's selected hotbar slot to slot 4 of player",
			"send \"index of player's current hotbar slot = 1\" # second slot from the left"})
@Since("2.2-dev36")
public class ExprHotbarSlot extends SimplePropertyExpression<Player, Slot> {

	static {
		register(ExprHotbarSlot.class, Slot.class, "[([currently] selected|current)] hotbar slot", "players");
	}
	
	@Override
	@Nullable
	public Slot convert(Player p) {
		PlayerInventory invi = p.getInventory();
		assert invi != null;
		return new InventorySlot(invi, invi.getHeldItemSlot());
	}
	
	@Override
	protected String getPropertyName() {
		return "hotbar slot";
	}
	
	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET)
			return new Class[] {Slot.class};
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		assert delta != null;
		Slot slot = (Slot) delta[0];
		if (!(slot instanceof InventorySlot))
			return; // Only inventory slots can be hotbar slots
		
		int index = ((InventorySlot) slot).getIndex();
		if (index > 8) // Only slots in hotbar can be current hotbar slot
			return;
		
		for (Player p : getExpr().getArray(e)) {
			p.getInventory().setHeldItemSlot(index);
		}
	}
	
}
