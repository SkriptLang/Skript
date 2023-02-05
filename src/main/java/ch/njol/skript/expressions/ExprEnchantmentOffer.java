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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Enchantment Offer")
@Description("The enchantment offer in enchant prepare events.")
@Examples({"on enchant prepare:",
			"\tsend \"Your enchantment offers are: %the enchantment offers%\" to player"})
@Since("2.5")
@Events("enchant prepare")
@RequiredPlugins("1.11 or newer")
public class ExprEnchantmentOffer extends SimpleExpression<EnchantmentOffer> {

	static {
		if (Skript.classExists("org.bukkit.enchantments.EnchantmentOffer")) {
			Skript.registerExpression(ExprEnchantmentOffer.class, EnchantmentOffer.class, ExpressionType.SIMPLE, 
					"[all [of]] [the] enchant[ment] offers",
					"enchant[ment] offer[s] %numbers%",
					"[the] %number%(st|nd|rd|th) enchant[ment] offer");
		}
	}

	@SuppressWarnings("null")
	private Expression<Number> exprOfferNumber;

	private boolean all;

	// Used for getCost()
	private final Random rand = new Random();

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PrepareItemEnchantEvent.class)) {
			Skript.error("Enchantment offers are only usable in enchant prepare events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (matchedPattern == 0) {
			all = true;
		} else {
			exprOfferNumber = (Expression<Number>) exprs[0];
			all = false;
		}
		return true;
	}

	@SuppressWarnings({"null", "unused"})
	@Override
	@Nullable
	protected EnchantmentOffer[] get(Event e) {
		if (!(e instanceof PrepareItemEnchantEvent))
			return null;

		if (all)
			return ((PrepareItemEnchantEvent) e).getOffers();
		if (exprOfferNumber == null)
			return new EnchantmentOffer[0];
		if (exprOfferNumber.isSingle()) {
			Number offerNumber = exprOfferNumber.getSingle(e);
			if (offerNumber == null)
				return new EnchantmentOffer[0];
			int offer = offerNumber.intValue();
			if (offer < 1 || offer > ((PrepareItemEnchantEvent) e).getOffers().length)
				return new EnchantmentOffer[0];
			return new EnchantmentOffer[]{((PrepareItemEnchantEvent) e).getOffers()[offer - 1]};
		}
		List<EnchantmentOffer> offers = new ArrayList<>();
		int i;
		for (Number n : exprOfferNumber.getArray(e)) {
			i = n.intValue();
			if (i >= 1 || i <= ((PrepareItemEnchantEvent) e).getOffers().length)
				offers.add(((PrepareItemEnchantEvent) e).getOffers()[i - 1]);
		}
		return offers.toArray(new EnchantmentOffer[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(EnchantmentType.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.DELETE)
			return;
		EnchantmentType et = mode != ChangeMode.DELETE ? (EnchantmentType) delta[0] : null;
		if (event instanceof PrepareItemEnchantEvent) {
			PrepareItemEnchantEvent e = (PrepareItemEnchantEvent) event;
			switch (mode) {
				case SET:
					if (all) {
						for (int i = 0; i <= 2; i++) {
							EnchantmentOffer eo = e.getOffers()[i];
							if (eo == null) {
								eo = new EnchantmentOffer(et.getType(), et.getLevel(), getCost(i + 1, e.getEnchantmentBonus()));
								e.getOffers()[i] = eo;
							} else {
								eo.setEnchantment(et.getType());
								eo.setEnchantmentLevel(et.getLevel());
							}
						}
					} else {
						for (Number n : exprOfferNumber.getArray(e)) {
							int slot = n.intValue() - 1;
							EnchantmentOffer eo = e.getOffers()[slot];
							if (eo == null) {
								eo = new EnchantmentOffer(et.getType(), et.getLevel(), getCost(slot + 1, e.getEnchantmentBonus()));
								e.getOffers()[slot] = eo;
							} else {
								eo.setEnchantment(et.getType());
								eo.setEnchantmentLevel(et.getLevel());
							}
						}
					}
					break;
				case DELETE:
					if (all) {
						Arrays.fill(e.getOffers(), null);
					} else {
						for (Number n : exprOfferNumber.getArray(e))
							e.getOffers()[n.intValue() - 1] = null;
					}
					break;
				case ADD:
				case REMOVE:
				case RESET:
				case REMOVE_ALL:
					assert false;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return !all && exprOfferNumber.isSingle();
	}

	@Override
	public Class<? extends EnchantmentOffer> getReturnType() {
		return EnchantmentOffer.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return all ? "the enchantment offers" : "enchantment offer(s) " + exprOfferNumber.toString(e, debug);
	}

	/**
	 * Returns an enchantment cost from an enchantment button and number of bookshelves.
	 * @param slot The enchantment button slot (1, 2, or 3).
	 * @param bookshelves The number of bookshelves around the enchantment table.
	 * @return A cost for that enchantment button with the number of bookshelves, or 1 if 'slot' is not an integer from 1 to 3.
	 */
	public int getCost(int slot, int bookshelves) {
		// (from 1 to 8) + floor(bookshelves / 2) + (from 0 to bookshelves)
		int base = (int) ((rand.nextInt(7) + 1) + Math.floor(bookshelves / 2) + (rand.nextInt(bookshelves + 1)));
		switch (slot) {
			case 1: return Math.max(base / 3, 1);
			case 2: return (base * 2) / 3 + 1;
			case 3: return Math.max(base, bookshelves * 2);
			default: return 1;
		}
	}

}
