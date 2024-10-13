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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.inventory.meta.ItemMeta;

@Name("Unbreakable Items")
@Description("Creates breakable or unbreakable copies of given items.")
@Examples({
	"set {_item} to unbreakable iron sword",
	"give breakable {_weapon} to all players"
})
@Since("2.2-dev13b, 2.9.0 (breakable)")
public class ExprUnbreakable extends SimplePropertyExpression<ItemType, ItemType> {

	static {
		Skript.registerExpression(ExprUnbreakable.class, ItemType.class, ExpressionType.PROPERTY, "[:un]breakable %itemtypes%");
	}

	private boolean unbreakable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		unbreakable = parseResult.hasTag("un");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public ItemType convert(ItemType itemType) {
		ItemType clone = itemType.clone();
		ItemMeta meta = clone.getItemMeta();
		meta.setUnbreakable(unbreakable);
		clone.setItemMeta(meta);
		return clone;
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	protected String getPropertyName() {
		return unbreakable ? "unbreakable" : "breakable";
	}

}
