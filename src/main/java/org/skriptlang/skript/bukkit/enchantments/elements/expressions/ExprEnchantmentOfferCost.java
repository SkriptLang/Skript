package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.util.Math2;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Enchantment Offer Cost")
@Description({
	"The cost of an enchantment offer. This is displayed to the right of an enchantment offer.",
	"If the cost is changed, it will always be at least 1.",
	"This changes how many levels are required to enchant, but does not change the number of levels removed.",
	"To change the number of levels removed, use the enchant event."
})
@Example("set cost of enchantment offer 1 to 50")
@Since("2.5")
public class ExprEnchantmentOfferCost extends SimplePropertyExpression<EnchantmentOffer, Long> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEnchantmentOfferCost.class, Long.class, "[enchant[ment]] cost", "enchantmentoffers", false)
				.supplier(ExprEnchantmentOfferCost::new)
				.build());
	}

	@Override
	public Long convert(final EnchantmentOffer offer) {
		return (long) offer.getCost();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		EnchantmentOffer[] offers = getExpr().getArray(event);
		if (offers.length == 0 || delta == null)
			return;
		long cost = ((Number) delta[0]).longValue();
		if (cost < 1) 
			return;
		long change;
		for (EnchantmentOffer offer : offers) {
			switch (mode) {
				case SET -> offer.setCost(Math2.clampToInt(cost));
				case ADD -> {
					change = Math2.addClamped(offer.getCost(), cost);
					if (change < 1)
						return;
					offer.setCost(Math2.clampToInt(change));
				}
				case REMOVE -> {
					change = Math2.addClamped(offer.getCost(), -cost);
					if (change < 1)
						return;
					offer.setCost(Math2.clampToInt(change));
				}
				default -> {
					assert false;
				}
			}
		}
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "enchantment cost";
	}

}
