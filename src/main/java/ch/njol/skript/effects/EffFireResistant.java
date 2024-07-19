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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Make Fire Resistant")
@Description("Makes items fire resistant.")
@Examples({
	"make player's tool fire resistant:",
	"make {_items::*} not resistant to fire"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public class EffFireResistant extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "setFireResistant", boolean.class))
			Skript.registerEffect(EffFireResistant.class, "make %itemtypes% [:not] (fire resistant|resistant to fire)");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	private boolean not;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		not = parseResult.hasTag("not");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (ItemType item : this.items.getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			meta.setFireResistant(!not);
			item.setItemMeta(meta);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + items.toString(event, debug) + (not ? " not" : "") + " fire resistant";
	}

}
