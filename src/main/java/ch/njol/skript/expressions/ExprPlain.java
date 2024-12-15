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
import ch.njol.skript.aliases.ItemData;
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

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Plain Item")
@Description("A plain item is an item with no modifications. It can be used to convert items to their default state or to match with other default items.")
@Examples({"if the player's tool is a plain diamond: # check if player's tool has no modifications",
		"\tsend \"You are holding a plain diamond!\""})
@Since("2.6")
public class ExprPlain extends SimpleExpression<ItemType> {
	
	@SuppressWarnings("null")
	private Expression<ItemType> item;
	
	static {
		Skript.registerExpression(ExprPlain.class, ItemType.class, ExpressionType.COMBINED, "[a[n]] (plain|unmodified) %itemtype%");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		item = (Expression<ItemType>) exprs[0];
		return true;
	}
	
	@Override
	@Nullable
	protected ItemType[] get(Event e) {
		ItemType itemType = item.getSingle(e);
		if (itemType == null)
			return new ItemType[0];
		ItemData data = new ItemData(itemType.getMaterial());
		data.setPlain(true);
		ItemType plain = new ItemType(data);
		return new ItemType[]{plain};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "plain " + item.toString(e, debug);
	}
	
}
