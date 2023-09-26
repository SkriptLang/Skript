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

import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Initiator Inventory")
@Description("Returns the initiator inventory in an on <a href=\"./events.html?search=#inventory_item_move\">inventory item move</a> event.")
@Examples({
	"on inventory item move:",
		"\tholder of event-initiator-inventory is a chest",
		"\tbroadcast \"Item transport requested at %location at holder of event-initiator-inventory%...\""
})
@Events("Inventory Item Move")
@Since("INSERT VERSION")
public class ExprEvtInitiator extends EventValueExpression<Inventory> {

	static {
		register(ExprEvtInitiator.class, Inventory.class, "[event-]initiator[( |-)inventory]");
	}

	public ExprEvtInitiator() {
		super(Inventory.class);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(InventoryMoveItemEvent.class)) {
			Skript.error("'event-initiator' can only be used in an 'inventory item move' event.");
			return false;
		}
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public String toString() {
		return "event-initiator-inventory";
	}

}
