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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Item Enchantments")
@Description("All the enchantments an <a href='classes.html#itemtype'>item type</a> has.")
@Examples("clear enchantments of event-item")
@Since("2.2-dev36")
public class ExprEnchantments extends SimpleExpression<EnchantmentType> {

	static {
		PropertyExpression.register(ExprEnchantments.class, EnchantmentType.class, "enchantments", "itemtypes");
	}

	@SuppressWarnings("null")
	private Expression<ItemType> items;

	@SuppressWarnings({"null","unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		return true;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	@Nullable
	protected EnchantmentType[] get(Event e) {
		List<EnchantmentType> enchantments = new ArrayList<>();
		
		for (ItemType item : items.getArray(e)) {
			EnchantmentType[] enchants = item.getEnchantmentTypes();
			
			if (enchants == null)
				continue;
			
			Collections.addAll(enchantments, enchants);
		}
		return enchantments.toArray(new EnchantmentType[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		// Enchantment doesn't get automatically converted to EnchantmentType if you give it more than a one.
		// Meaning you can transform an Enchantment array to an EnchantmentType array automatically,
		// So, we gotta do it manually.
		return CollectionUtils.array(Enchantment[].class, EnchantmentType[].class);
	}

	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		ItemType[] source = items.getArray(e);
		
		EnchantmentType[] enchants = new EnchantmentType[delta != null ? delta.length : 0];
		
		if (delta != null && delta.length != 0) {
			for (int i = 0; i<delta.length; i++) {
				if (delta[i] instanceof EnchantmentType)
					enchants[i] = (EnchantmentType) delta[i];
				else
					enchants[i] = new EnchantmentType((Enchantment) delta[i]);
			}
		}
		
		switch (mode) {
			case ADD:
				for (ItemType item : source)
					item.addEnchantments(enchants);
				break;
			case REMOVE:
			case REMOVE_ALL:
				for (ItemType item : source) {
					ItemMeta meta = item.getItemMeta();
					assert meta != null;
					for (EnchantmentType enchant : enchants) {
						Enchantment ench = enchant.getType();
						assert ench != null;
						if (enchant.getInternalLevel() == -1
								|| meta.getEnchantLevel(ench) == enchant.getLevel()) {
							// Remove directly from meta since it's more efficient on this case
							meta.removeEnchant(ench);
						}
					item.setItemMeta(meta);
					}
				}
				break;
			case SET:
				for (ItemType item : source) {
					item.clearEnchantments();
					item.addEnchantments(enchants);
				}
				break;
			case DELETE:
			case RESET:
				for (ItemType item : source)
					item.clearEnchantments();
				break;
		}
	}

	@Override
	public Class<? extends EnchantmentType> getReturnType() {
		return EnchantmentType.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the enchantments of " + items.toString(e, debug);
	}
	
}
