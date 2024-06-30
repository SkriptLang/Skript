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
package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Item Has Enchantment Glint Override")
@Description("Checks whether an item has the enchantment glint overridden, or is forced to glint or not.")
@Examples({
	"if the player's tool has the enchantment glint override",
		"\tsend \"Your tool has the enchantment glint override.\" to player",
	"",
	"if {_item} is forced to glint:",
		"\tsend \"This item is forced to glint.\" to player",
	"else if {_item} is forced to not glint:",
		"\tsend \"This item is forced to not glint.\" to player",
	"else:",
		"\tsend \"This item does not have any glint override.\" to player"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("INSERT VERSION")
public class CondItemEnchantmentGlint extends Condition {

	static {
		if (Skript.isRunningMinecraft(1, 20, 5))
			Skript.registerCondition(CondItemEnchantmentGlint.class,
				"%itemtypes% (has|have) [the] enchantment glint overrid(den|e)",
				"%itemtypes% (doesn't|does not|do not|don't) have [the] enchantment glint overrid(den|e)",
				"%itemtypes% (is|are) forced to [:not] glint");
	}

	private Expression<ItemType> itemtypes;
	private int pattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		itemtypes = (Expression<ItemType>) expressions[0];
		pattern = matchedPattern;
		// Pattern 'is forced to glint'
		if (matchedPattern == 2) {
			setNegated(parseResult.hasTag("not"));
		// Pattern 'has enchantment glint override'
		} else {
			setNegated(matchedPattern == 1);
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		return itemtypes.check(event, itemType -> {
			ItemMeta meta = itemType.getItemMeta();
			// Pattern 'is forced to glint'
			if (pattern == 2) {
				return meta.getEnchantmentGlintOverride();
			// Pattern 'has enchantment glint override'
			} else {
				return meta.hasEnchantmentGlintOverride();
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
