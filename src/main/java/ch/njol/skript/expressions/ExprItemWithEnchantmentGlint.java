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
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Item with Enchantment Glint")
@Description("Get an item with or without enchantment glint. Items with enchantment glint")
@Examples({
	"set {_item with glint} to diamond with enchantment glint",
	"set {_item without glint} to diamond without enchantment glint"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("INSERT VERSION")
public class ExprItemWithEnchantmentGlint extends PropertyExpression<ItemType, ItemType> {

	static {
		if (Skript.isRunningMinecraft(1, 20, 5))
			Skript.registerExpression(ExprItemWithCustomModelData.class, ItemType.class, ExpressionType.PROPERTY, "%itemtypes% with[:out] [enchant[ment]] glint");
	}

	private boolean glint;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) expressions[0]);
		glint = !parseResult.hasTag("out");
		return true;
	}

	@Override
	protected ItemType[] get(Event event, ItemType[] source) {
		return get(source.clone(), itemType -> {
			ItemMeta meta = itemType.getItemMeta();
			meta.setEnchantmentGlintOverride(glint);
			itemType.setItemMeta(meta);
			return itemType;
        });
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + (glint ? " with" : " without") + " enchantment glint";
	}

}
