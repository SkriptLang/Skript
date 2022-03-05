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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("With Enchantments")
@Description("Represents an item with enchantments. If the item is an enchanted book, it may also have stored enchantments.")
@Examples({"give player enchanted book with stored enchantment unbreaking 1 and sharpness 2",
		"give player 3 diamonds with power 1"})
@Since("INSERT VERSION")
public class ExprItemWithEnchantments extends SimpleExpression<ItemType> {

	static {
		Skript.registerExpression(ExprItemWithEnchantments.class, ItemType.class, ExpressionType.COMBINED,
			"%itemtype% (with|of) [(1¦stored)] [enchant[ment[s]]] %enchantmenttypes%"); // Added support for non-stored enchantments, check https://github.com/SkriptLang/Skript/issues/1836
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> item;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<EnchantmentType> enchs;
	private boolean isStored;

	@Override
	@SuppressWarnings({"null","unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		item = (Expression<ItemType>) exprs[0];
		enchs = (Expression<EnchantmentType>) exprs[1];
		isStored = parseResult.mark == 1;
		return true;
	}

	@Override
	@Nullable
	protected ItemType[] get(Event e) {
		EnchantmentType[] enchs = this.enchs.getArray(e);
		ItemType item = this.item.getSingle(e);
		if (enchs == null || enchs.length < 1 || item == null)
			return null;

		if (isStored) {
			if (item.getEnchantmentStorageMeta() != null)
				item.addStoredEnchantments(enchs);
		} else {
			item.addEnchantments(enchs);
		}
		return new ItemType[]{item};
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return item.toString(e, debug) + " with " + (isStored ? "stored " : "") + "enchantments " + enchs.toString(e, debug);
	}
	
}
