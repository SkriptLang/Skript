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

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Applied Enchantments")
@Description({"The applied enchantments in an enchant event.",
				" Deleting or removing the applied enchantments will prevent the item's enchantment."})
@Examples({"on enchant:",
			"\tset the applied enchantments to sharpness 10 and fire aspect 5"})
@Events("enchant")
@Since("2.5")
public class ExprAppliedEnchantments extends SimpleExpression<EnchantmentType> {

	static {
		Skript.registerExpression(ExprAppliedEnchantments.class, EnchantmentType.class, ExpressionType.SIMPLE, "[the] applied enchant[ment]s");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EnchantItemEvent.class)) {
			Skript.error("The applied enchantments are only usable in an enchant event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@SuppressWarnings("null")
	@Override
	@Nullable
	protected EnchantmentType[] get(Event e) {
		if (!(e instanceof EnchantItemEvent))
			return null;

		return ((EnchantItemEvent) e).getEnchantsToAdd().entrySet().stream()
				.map(entry -> new EnchantmentType(entry.getKey(), entry.getValue()))
				.toArray(EnchantmentType[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Enchantment[].class, EnchantmentType[].class);
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof EnchantItemEvent))
			return;

		EnchantmentType[] enchants = new EnchantmentType[delta != null ? delta.length : 0];
		if (delta != null && delta.length != 0) {
			for (int i = 0; i < delta.length; i++) {
				if (delta[i] instanceof EnchantmentType)
					enchants[i] = (EnchantmentType) delta[i];
				else
					enchants[i] = new EnchantmentType((Enchantment) delta[i]);
			}
		}
		EnchantItemEvent e = (EnchantItemEvent) event;
		switch (mode) {
			case SET:
				e.getEnchantsToAdd().clear();
			case ADD:
				for (EnchantmentType enchant : enchants)
					e.getEnchantsToAdd().put(enchant.getType(), enchant.getLevel());
				break;
			case REMOVE:
				for (EnchantmentType enchant : enchants)
					e.getEnchantsToAdd().remove(enchant.getType(), enchant.getLevel());
				break;
			case DELETE:
				e.getEnchantsToAdd().clear();
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends EnchantmentType> getReturnType() {
		return EnchantmentType.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "applied enchantments";
	}

}
