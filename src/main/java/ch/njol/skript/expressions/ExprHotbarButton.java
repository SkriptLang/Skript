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

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.eclipse.jdt.annotation.Nullable;

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

@Name("Hotbar Button")
@Description("The hotbar button clicked in an <a href='events.html#inventory_click'>inventory click</a> event.")
@Examples({"on inventory click:",
		"	send \"You clicked the hotbar button %hotbar button%!\""})
@Since("2.5")
public class ExprHotbarButton extends SimpleExpression<Long> {
	
	static {
		Skript.registerExpression(ExprHotbarButton.class, Long.class, ExpressionType.SIMPLE, "[the] hotbar button");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (!getParser().isCurrentEvent(InventoryClickEvent.class)) {
			Skript.error("The 'hotbar button' expression may only be used in an inventory click event.");
			return false;
		}
		return true;
	}
	
	@Nullable
	@Override
	protected Long[] get(Event e) {
		if (e instanceof InventoryClickEvent)
			return new Long[] {(long) ((InventoryClickEvent) e).getHotbarButton()};
		return null;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the hotbar button";
	}

}
