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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Inventory Raw Slot")
@Description({"The raw slot number clicked. This slot number is unique for the view."})
@Examples({"on inventory click:",
		"\tif rows of top inventory = 6: # double chest",
		"\t\tif clicked raw slot > 53: # double chest last slot index is 53",
		"\t\t\tYou clicked a slot outside the %top inventory%! (slot: %raw slot%)"})
@Since("INSERT VERSION")
@Events("inventory click")
public class ExprRawSlot extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprRawSlot.class, Number.class, ExpressionType.SIMPLE, "[click[ed]] (unique|raw) [inventory] slot");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(InventoryClickEvent.class)) { // TODO Support InventoryDragEvent#getRawSlot() when Skript supports that event
			Skript.error("Cannot use 'raw slot' expression outside a inventory click event");
			return false;
		}
		return true;
	}

	@Override
	public Number[] get(Event e) {
		if (!(e instanceof InventoryClickEvent))
			return null;
		return new Number[] {((InventoryClickEvent) e).getRawSlot()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "raw inventory slot";
	}

}
