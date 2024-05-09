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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Can Hold")
@Description("Tests whether a player, an inventory, or a slot can hold the given item.")
@Examples({
	"block can hold 200 cobblestone",
	"player has enough space for 64 feathers",
	"slot 0 of player can hold a diamond sword"
})
@Since("1.0, INSERT VERSION (slots)")
public class CondCanHold extends Condition {
	
	static {
		Skript.registerCondition(CondCanHold.class,
				"%inventories% (can hold|ha(s|ve) [enough] space (for|to hold)) %itemtypes%",
				"%inventories% (can(no|')t hold|(ha(s|ve) not|ha(s|ve)n't|do[es]n't have) [enough] space (for|to hold)) %itemtypes%",
				"%slots% (can hold|ha(s|ve) [enough] space (for|to hold)) %itemtype%",
				"%slots% (can(no|')t hold|(ha(s|ve) not|ha(s|ve)n't|do[es]n't have) [enough] space (for|to hold)) %itemtype%");
	}

	@Nullable
	private Expression<Inventory> invis;
	@Nullable
	private Expression<Slot> slots;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern >= 3) {
			slots = (Expression<Slot>) exprs[0];
        } else {
			invis = (Expression<Inventory>) exprs[0];
		}
		items = (Expression<ItemType>) exprs[1];
		if (items instanceof Literal) {
			for (ItemType t : ((Literal<ItemType>) items).getAll()) {
				t = t.getItem();
				if (!(t.isAll() || t.getTypes().size() == 1)) {
					Skript.error("The condition 'can hold' can currently only be used with aliases that start with 'every' or 'all', or only represent one item.");
					return false;
				}
			}
		}
		setNegated(matchedPattern % 2 == 0);
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		// Condition inventory has space
		if (invis != null) {
			return invis.check(event,
				invi -> {
					if (!items.getAnd()) {
						return items.check(event,
							t -> t.getItem().hasSpace(invi));
					}
					final ItemStack[] buf = ItemType.getStorageContents(invi);
					return items.check(event,
						t -> t.getItem().addTo(buf));
				}, isNegated());
		}
		// Condition slot has space
		ItemType itemType = items.getSingle(event);
		if (itemType == null)
			return false;
		ItemStack itemStack = itemType.getRandom();
		return slots.check(event, slot -> {
			ItemStack slotItemStack = slot.getItem();
			// null check is due to slot is outside of GUI
			if (slotItemStack == null)
				return false;
			// Check if it can fit more of the same item
			if (!itemStack.isSimilar(slotItemStack))
				return false;
            return slotItemStack.getMaxStackSize() - slotItemStack.getAmount() <= itemStack.getAmount();
        });
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.CAN, event, debug, (invis != null ? invis : slots),
				"hold " + items.toString(event, debug));
	}
	
}
