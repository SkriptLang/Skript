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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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
import ch.njol.util.Kleenean;

@Name("Inventory Holder/Viewers/Rows/Slots")
@Description({"Gets the amount of rows/slots, viewers and holder of an inventory.",
	"",
	"NOTE: 'Viewers' expression returns a list of players viewing the inventory. Note that a player is considered to be viewing their own inventory and internal crafting screen even when said inventory is not open."})
@Examples({"event-inventory's amount of rows",
		   "holder of player's top inventory",
		   "{_inventory}'s viewers"})
@Since("2.2-dev34, 2.5 (slots)")
public class ExprInventoryInfo extends SimpleExpression<Object> {
	
	private final static int HOLDER = 1, VIEWERS = 2, ROWS = 3, SLOTS = 4;
	
	static {
		Skript.registerExpression(ExprInventoryInfo.class, Object.class, ExpressionType.PROPERTY,
				"(" + HOLDER + "¦holder[s]|" + VIEWERS + "¦viewers|" + ROWS + "¦[amount of] rows|" + SLOTS + "¦[amount of] slots)" + " of %inventories%",
				"%inventories%'[s] (" + HOLDER + "¦holder[s]|" + VIEWERS + "¦viewers|" + ROWS + "¦[amount of] rows|" + SLOTS + "¦[amount of] slots)");
	}
	
	@SuppressWarnings("null")
	private Expression<Inventory> inventories;
	private int type;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		inventories = (Expression<Inventory>) exprs[0];
		type = parseResult.mark;
		return true;
	}

	@Override
	protected Object[] get(Event e) {
		Inventory[] inventories = this.inventories.getArray(e);
		List<Object> objects = new ArrayList<>();
		switch (type) {
			case HOLDER:
				for (Inventory inventory : inventories) {
					InventoryHolder holder = inventory.getHolder();
					if (holder != null)
						objects.add(holder);
				}
				break;
			case ROWS:
				for (Inventory inventory : inventories) {
					int size = inventory.getSize();
					if (size < 9) // Hoppers have a size of 5, we don't want to return 0
						objects.add(1);
					else
						objects.add(size / 9);
				}
				break;
			case SLOTS:
				for (Inventory inventory : inventories) {
					objects.add(inventory.getSize());
				}
				break;
			case VIEWERS:
				for (Inventory inventory : inventories) {
					objects.addAll(inventory.getViewers());
				}
				break;
			default:
				return new Object[0];
		}
		return objects.toArray(new Object[0]);

	}
	
	@Override
	public boolean isSingle() {
		return inventories.isSingle() && type != VIEWERS;
	}

	@Override
	public Class<?> getReturnType() {
		return type == HOLDER ? InventoryHolder.class : (type == ROWS || type == SLOTS) ? Number.class : Player.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (type == HOLDER ? "holder of " : type == ROWS ? "rows of " : type == SLOTS ? "slots of " : "viewers of ") + inventories.toString(e, debug);
	}

}
