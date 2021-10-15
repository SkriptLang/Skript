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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;

@Name("Is Enchanted")
@Description("Checks whether an item is enchanted.")
@Examples({"tool of the player is enchanted with efficiency 2",
		"helm, chestplate, leggings or boots are enchanted",
		"",
		"# For enchanted books",
		"player's tool is stored enchanted",
		"event-item is enchanted with stored power 2"})
@Since("1.4.6, INSERT VERSION (stored enchantments)")
public class CondIsEnchanted extends Condition {
	
	static {
		PropertyCondition.register(CondIsEnchanted.class,
			"[(1¦stored[ly])] enchanted [with %-enchantmenttype%]", "itemtypes");
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@Nullable
	private Expression<EnchantmentType> enchs;
	private boolean isStored;
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		enchs = (Expression<EnchantmentType>) exprs[1];
		isStored = parseResult.mark > 0;
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		if (enchs != null)
			return items.check(e, item ->
					(isStored && item.getItemMeta() instanceof EnchantmentStorageMeta) ? enchs.check(e, item::hasStoredEnchantments) : enchs.check(e, item::hasEnchantments), isNegated());
		else
			return items.check(e, item ->
					(isStored && item.getItemMeta() instanceof EnchantmentStorageMeta) ? item.hasStoredEnchantments() : item.hasEnchantments(), isNegated());

	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, e, debug, items,
			(isStored ? "stored " : "") + "enchanted" + (enchs == null ? "" : " with " + enchs.toString(e, debug)));
	}
	
}
