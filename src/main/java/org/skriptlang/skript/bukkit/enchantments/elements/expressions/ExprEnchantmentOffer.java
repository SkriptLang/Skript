package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ch.njol.skript.lang.EventRestrictedSyntax;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Enchantment Offer")
@Description("The enchantment offer in enchant prepare events.")
@Example("""
	on enchant prepare:
		send "Your enchantment offers are: %the enchantment offers%" to player
	""")
@Since("2.5")
@Events("enchant prepare")
public class ExprEnchantmentOffer extends SimpleExpression<EnchantmentOffer> implements EventRestrictedSyntax {

	/*
	* This should probably be an event value, but ExprElement doesn't support the %number%(st|nd|rd|th) %classinfo% syntax,
	* and we have to keep it for backward compatibility, so for now it's best to just keep it as an expression
	* */
	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprEnchantmentOffer.class, EnchantmentOffer.class)
			.addPatterns(
				"[all [of]] [the] enchant[ment] offers",
				"enchant[ment] offer[s] %numbers%",
				"[the] %number%(st|nd|rd|th) enchant[ment] offer"
			).priority(SyntaxInfo.SIMPLE).build());
	}

	@SuppressWarnings("null")
	private Expression<Number> exprOfferNumber;

	private boolean all;

	// Used for getCost()
	private final Random rand = new Random();

	@Override
	@SuppressWarnings({"null", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			all = true;
		} else {
			exprOfferNumber = (Expression<Number>) exprs[0];
			all = false;
		}
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PrepareItemEnchantEvent.class);
	}

	@SuppressWarnings({"null", "unused"})
	@Override
	@Nullable
	protected EnchantmentOffer[] get(Event event) {
		if (!(event instanceof PrepareItemEnchantEvent))
			return null;

		if (all)
			return ((PrepareItemEnchantEvent) event).getOffers();
		if (exprOfferNumber == null)
			return new EnchantmentOffer[0];
		if (exprOfferNumber.isSingle()) {
			Number offerNumber = exprOfferNumber.getSingle(event);
			if (offerNumber == null)
				return new EnchantmentOffer[0];
			int offer = offerNumber.intValue();
			if (offer < 1 || offer > ((PrepareItemEnchantEvent) event).getOffers().length)
				return new EnchantmentOffer[0];
			return new EnchantmentOffer[]{((PrepareItemEnchantEvent) event).getOffers()[offer - 1]};
		}
		List<EnchantmentOffer> offers = new ArrayList<>();
		int intIndex;
		for (Number index : exprOfferNumber.getArray(event)) {
			intIndex = index.intValue();
			if (intIndex >= 1 && intIndex <= ((PrepareItemEnchantEvent) event).getOffers().length)
				offers.add(((PrepareItemEnchantEvent) event).getOffers()[intIndex - 1]);
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

	@Override
	@SuppressWarnings("null")
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.DELETE)
			return;
		EnchantmentType type = mode != ChangeMode.DELETE ? (EnchantmentType) delta[0] : null;
		if (event instanceof PrepareItemEnchantEvent prepareEvent) {
			switch (mode) {
				case SET:
					assert type != null;
					final Number[] indices = all ? new Number[]{0, 1, 2} : exprOfferNumber.getArray(prepareEvent);
					for (Number index : indices) {
						int slot = index.intValue() - 1;
						EnchantmentOffer offer = prepareEvent.getOffers()[slot];
						if (offer == null) {
							offer = new EnchantmentOffer(type.getType(), type.getLevel(), getCost(slot + 1, prepareEvent.getEnchantmentBonus()));
							prepareEvent.getOffers()[slot] = offer;
						} else {
							offer.setEnchantment(type.getType());
							offer.setEnchantmentLevel(type.getLevel());
						}
					}
					break;
				case DELETE:
					if (all) {
						Arrays.fill(prepareEvent.getOffers(), null);
					} else {
						for (Number index : exprOfferNumber.getArray(prepareEvent))
							prepareEvent.getOffers()[index.intValue() - 1] = null;
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
	public String toString(@Nullable Event event, boolean debug) {
		return all ? "the enchantment offers" : "enchantment offer(s) " + exprOfferNumber.toString(event, debug);
	}

	/**
	 * Returns an enchantment cost from an enchantment button and number of bookshelves.
	 * @param slot The enchantment button slot (1, 2, or 3).
	 * @param bookshelves The number of bookshelves around the enchantment table.
	 * @return A cost for that enchantment button with the number of bookshelves, or 1 if 'slot' is not an integer from 1 to 3.
	 */
	public int getCost(int slot, int bookshelves) {
		// (from 1 to 8) + floor(bookshelves / 2) + (from 0 to bookshelves)
		int base = (rand.nextInt(7) + 1) + (bookshelves / 2) + (rand.nextInt(bookshelves + 1));
		return switch (slot) {
			case 1 -> Math.max(base / 3, 1);
			case 2 -> (base * 2) / 3 + 1;
			case 3 -> Math.max(base, bookshelves * 2);
			default -> 1;
		};
	}

}
