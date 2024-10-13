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
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Unbreakable")
@Description("Checks whether an item is unbreakable.")
@Examples({
	"if event-item is unbreakable:",
		"\tsend \"This item is unbreakable!\" to player",
	"if tool of {_p} is breakable:",
		"\tsend \"Your tool is breakable!\" to {_p}"
})
@Since("2.5.1, 2.9.0 (breakable)")
public class CondIsUnbreakable extends PropertyCondition<ItemType> {
	
	static {
		register(CondIsUnbreakable.class, "[:un]breakable", "itemtypes");
	}

	private boolean breakable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		breakable = !parseResult.hasTag("un");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(ItemType item) {
		return item.getItemMeta().isUnbreakable() ^ breakable;
	}
	
	@Override
	protected String getPropertyName() {
		return breakable ? "breakable" : "unbreakable";
	}
	
}
