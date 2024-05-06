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

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;

@Name("Anvil Text Input")
@Description("An expression to get the name to be applied to an item in an anvil inventory.")
@Examples({
		"on inventory click:",
		"\ttype of event-inventory is anvil inventory",
		"\tif the anvil text input of the event-inventory is \"FREE OP\":",
		"\t\tban player"
})
@Since("2.7")
public class ExprAnvilText extends SimplePropertyExpression<Inventory, String> {

	static {
		register(ExprAnvilText.class, String.class, "anvil [inventory] (rename|text) input", "inventories");
	}

	@Override
	@Nullable
	public String convert(Inventory inv) {
		if (!(inv instanceof AnvilInventory))
			return null;
		return ((AnvilInventory) inv).getRenameText();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String getPropertyName() {
		return "anvil text input";
	}
	
}
