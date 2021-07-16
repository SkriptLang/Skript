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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Amount of Items")
@Description("Counts how many of a particular <a href='../classes.html#itemtype'>item type</a> are in a given inventory.")
@Examples("message \"You have %number of ores in the player's inventory% ores in your inventory.\"")
@Since("2.0")
public class ExprAmountOfItems extends SimpleExpression<Long> {
	static {
		Skript.registerExpression(ExprAmountOfItems.class, Long.class, ExpressionType.PROPERTY, "[the] (amount|number) of %itemtypes% (in|of) %inventories%");
	}
	
	@SuppressWarnings("null")
	private Expression<ItemType> items;
	@SuppressWarnings("null")
	private Expression<Inventory> invis;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		invis = (Expression<Inventory>) exprs[1];
		return true;
	}
	
	@Override
	protected Long[] get(final Event e) {
		long r = 0;
		final ItemType[] types = items.getArray(e);
		for (final Inventory invi : invis.getArray(e)) {
			itemsLoop: for (final ItemStack i : invi.getContents()) {
				for (final ItemType t : types) {
					if (t.isOfType(i)) {
						r += i == null ? 1 : i.getAmount();
						continue itemsLoop;
					}
				}
			}
		}
		return new Long[] {r};
	}
	
	@Override
	public Long[] getAll(final Event e) {
		long r = 0;
		final ItemType[] types = items.getAll(e);
		for (final Inventory invi : invis.getAll(e)) {
			itemsLoop: for (final ItemStack i : invi.getContents()) {
				for (final ItemType t : types) {
					if (t.isOfType(i)) {
						r += i == null ? 1 : i.getAmount();
						continue itemsLoop;
					}
				}
			}
		}
		return new Long[] {r};
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "number of " + items + " in " + invis;
	}
}
