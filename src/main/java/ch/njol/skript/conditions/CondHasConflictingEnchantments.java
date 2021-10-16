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
import ch.njol.util.Kleenean;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Conflicting Enchantments")
@Description("Checks whether an item has conflicting enchantments with the given enchantment.")
@Examples({"player's tool has conflicting enchantments with efficiency",
		"event-item has conflicting stored enchantments with power"})
@Since("INSERT VERSION")
public class CondHasConflictingEnchantments extends Condition {
	
	static {
		PropertyCondition.register(CondHasConflictingEnchantments.class, PropertyType.HAVE,
			"conflicting [(1¦stored)] enchant[ment]s with %enchantment%", "itemtypes");
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Enchantment> ench;
	private boolean isStored;
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		ench = (Expression<Enchantment>) exprs[1];
		isStored = parseResult.mark > 0;
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		Enchantment ench = this.ench.getSingle(e);
		if (ench == null)
			return false;

		return items.check(e, item ->
		{
			ItemMeta meta = item.getItemMeta();
			if ((isStored && meta instanceof EnchantmentStorageMeta)) {
				return ((EnchantmentStorageMeta) meta).hasConflictingStoredEnchant(ench);
			} else {
				return meta.hasConflictingEnchant(ench);
			}
		}, isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, e, debug, items,
			"conflicting " + (isStored ? "stored " : "") + "enchantments with " + ench.toString(e, debug));
	}
	
}
