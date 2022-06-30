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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.stream.Stream;

@Name("Enchantment Level")
@Description("The level of a particular <a href='classes.html#enchantment'>enchantment</a> on an item.")
@Examples({"player's tool is a sword of sharpness:",
	"\tmessage \"You have a sword of sharpness %level of sharpness of the player's tool% equipped\""})
@Since("2.0")
public class ExprEnchantmentLevel extends SimpleExpression<Long> {

	static {
		Skript.registerExpression(ExprEnchantmentLevel.class, Long.class, ExpressionType.PROPERTY,
			"[the] [enchant[ment]] level[s] of %enchantments% (on|of) %itemtypes%",
			"[the] %enchantments% [enchant[ment]] level[s] (on|of) %itemtypes%",
			"%itemtypes%'[s] %enchantments% [enchant[ment]] level[s]",
			"%itemtypes%'[s] [enchant[ment]] level[s] of %enchantments%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Enchantment> enchants;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		int i = matchedPattern < 2 ? 1 : 0;
		items = (Expression<ItemType>) exprs[i];
		enchants = (Expression<Enchantment>) exprs[i ^ 1];
		return true;
	}

	@Override
	protected Long[] get(Event e) {
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
	@Nullable
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
		ItemType[] itemTypes = items.getArray(e);
		Enchantment[] enchantments = enchants.getArray(e);
		int changeValue = ((Number) delta[0]).intValue();

		for (ItemType itemType : itemTypes) {
			for (Enchantment enchantment : enchantments) {
				EnchantmentType enchantmentType = itemType.getEnchantmentType(enchantment);
				int oldLevel = enchantmentType == null ? 0 : enchantmentType.getLevel();

				int newItemLevel;
				switch (mode) {
					case ADD:
						newItemLevel = oldLevel + changeValue;
						break;
					case REMOVE:
						newItemLevel = oldLevel - changeValue;
						break;
					case SET:
						newItemLevel = changeValue;
						break;
					default:
						assert false;
						return;
				}

				if (newItemLevel <= 0) {
					itemType.removeEnchantments(new EnchantmentType(enchantment));
				} else {
					itemType.addEnchantments(new EnchantmentType(enchantment, newItemLevel));
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return items.isSingle() && enchants.isSingle();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the level of " + enchants.toString(e, debug) + " of " + items.toString(e, debug);
	}

}
