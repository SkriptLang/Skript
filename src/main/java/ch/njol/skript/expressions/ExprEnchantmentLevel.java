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

import java.util.stream.Stream;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
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
import ch.njol.util.coll.CollectionUtils;

@Name("Enchantment Level")
@Description("The level of a particular <a href='classes.html#enchantment'>enchantment</a> on an item.")
@Examples({"player's tool is a sword of sharpness:",
		"	message \"You have a sword of sharpness %level of sharpness of the player's tool% equipped\""})
@Since("2.0")
public class ExprEnchantmentLevel extends SimpleExpression<Long> {
	
	static {
		Skript.registerExpression(ExprEnchantmentLevel.class, Long.class, ExpressionType.PROPERTY,
				"[the] [enchant[ment]] level[s] of %enchantments% (on|of) %itemtypes%",
				"[the] %enchantments% [enchant[ment]] level[s] (on|of) %itemtypes%",
				"%itemtypes%'[s] %enchantments% [enchant[ment]] level[s]",
				"%itemtypes%'[s] [enchant[ment]] level[s] of %enchantments%");
	}
	
	@SuppressWarnings("null")
	private Expression<ItemType> items;
	@SuppressWarnings("null")
	private Expression<Enchantment> enchants;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		int i = matchedPattern < 2 ? 1 : 0;
		items = (Expression<ItemType>) exprs[i];
		enchants = (Expression<Enchantment>) exprs[i ^ 1];
		return true;
	}
	
	@Override
	protected Long[] get(final Event e) {
		Enchantment[] enchantments = enchants.getArray(e);
		return Stream.of(items.getArray(e))
			.map(ItemType::getEnchantmentTypes)
			.flatMap(Stream::of)
			.filter(enchantment -> CollectionUtils.contains(enchantments, enchantment.getType()))
			.map(EnchantmentType::getLevel)
			.map(i -> (long) i)
			.toArray(Long[]::new);
	}
	
	@Override
	public boolean isSingle() {
		return items.isSingle() && enchants.isSingle();
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case REMOVE:
			case ADD:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;
		
		ItemType[] source = items.getArray(e);
		Enchantment[] enchantments = enchants.getArray(e);
		int newLevel = ((Number) delta[0]).intValue();
		
		for (ItemType item : source) {
			if (!item.hasAnyEnchantments(enchantments))
				continue;
			
			EnchantmentType[] enchants = item.getEnchantmentTypes();
			assert enchants != null; // Can't be null at this point due to the above check
			for (EnchantmentType enchant : enchants) {
				item.removeEnchantments(enchant);
				Enchantment type = enchant.getType();
				int changed = newLevel;
				assert type != null;
				
				if (mode == ChangeMode.ADD)
					changed = Math.max(0, enchant.getLevel() + changed);
				else if (mode == ChangeMode.REMOVE)
					changed = Math.max(0, enchant.getLevel() - changed);
				
				if (changed > 0)
					item.addEnchantments(new EnchantmentType(type, changed));
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the level of " + enchants.toString(e, debug) + " of " + items.toString(e, debug);
	}
	
}
