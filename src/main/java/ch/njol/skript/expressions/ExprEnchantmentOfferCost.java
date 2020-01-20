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
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.util.coll.CollectionUtils;

@Name("Enchantment Offer Enchantment Cost")
@Description({"The cost of an enchantment offer. This is displayed to the right of an enchantment offer.",
			"If the cost is changed, it will always be at least 1.",
			"This changes how many levels are required to enchant, but does not change the number of levels removed.", 
			"To change the number of levels removed, use the enchant event."})
@Examples("set cost of enchantment offer 1 to 50")
@Since("INSERT VERSION")
public class ExprEnchantmentOfferCost extends SimplePropertyExpression<EnchantmentOffer, Number>{

	static {
		register(ExprEnchantmentOfferCost.class, Number.class, "[enchant[ment]] cost", "enchantmentoffers");
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "[enchant[ment]] cost";
	}

	@Override
	public Number convert(final EnchantmentOffer offer) {
		return offer.getCost();
	}

	@Override
	public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		EnchantmentOffer[] offers = getExpr().getArray(event);
		if (offers.length == 0)
			return;
		int cost = delta != null ? ((Number) delta[0]).intValue() : 1;
		if (cost < 1) cost = 1;
		int change = 1;
		switch (mode) {
			case SET:
				for (EnchantmentOffer offer : offers)
					offer.setCost(cost);
				break;
			case ADD:
				for (EnchantmentOffer offer : offers) {
					change = cost + offer.getCost();
					if (change < 1) change = 1;
					offer.setCost(change);
				}
				break;
			case REMOVE:
				for (EnchantmentOffer offer : offers) {
					change = cost - offer.getCost();
					if (change < 1) change = 1;
					offer.setCost(change);
				}
				break;
			case RESET:
			case DELETE:
			case REMOVE_ALL:
				assert false;
		}
	}
}
