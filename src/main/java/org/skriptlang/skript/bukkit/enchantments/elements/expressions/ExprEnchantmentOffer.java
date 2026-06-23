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
	* This should probably be an event value, but ExprElement doesn't support the %integer%(st|nd|rd|th) %classinfo% syntax,
	* and we have to keep it for backward compatibility, so for now it's best to just keep it as an expression
	* */
	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprEnchantmentOffer.class, EnchantmentOffer.class)
			.addPatterns(
				"[all [[of] the]|the] enchant[ment] offers",
				"enchant[ment] offer[s] %integers%",
				"[the] %integer%(st|nd|rd|th) enchant[ment] offer")
			.supplier(ExprEnchantmentOffer::new)
			.priority(SyntaxInfo.SIMPLE)
			.build());
	}

	private Expression<Integer> offerNumber;

	private boolean all;

	// Used for getCost()
	private final Random rand = new Random();

	@Override
	@SuppressWarnings({"null", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			all = true;
		} else {
			offerNumber = (Expression<Integer>) exprs[0];
			all = false;
		}
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PrepareItemEnchantEvent.class);
	}

	@Override
	protected EnchantmentOffer @Nullable [] get(Event event) {
		if (!(event instanceof PrepareItemEnchantEvent enchant))
			return null;

		if (all)
			return enchant.getOffers();
		if (offerNumber == null)
			return new EnchantmentOffer[0];
		if (offerNumber.isSingle()) {
			Integer offer = offerNumber.getSingle(event);
			if (offer == null)
				return new EnchantmentOffer[0];
			if (offer < 1 || offer > enchant.getOffers().length)
				return new EnchantmentOffer[0];
			return new EnchantmentOffer[]{enchant.getOffers()[offer - 1]};
		}
		List<EnchantmentOffer> offers = new ArrayList<>();
		for (Integer index : offerNumber.getArray(event)) {
			if (index >= 1 && index <= enchant.getOffers().length)
				offers.add(enchant.getOffers()[index - 1]);
		}
		return offers.toArray(new EnchantmentOffer[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(EnchantmentType.class);
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null || mode == ChangeMode.DELETE;
		EnchantmentType type = mode != ChangeMode.DELETE ? (EnchantmentType) delta[0] : null;
		if (!(event instanceof PrepareItemEnchantEvent prepareEvent))
			return;

		switch (mode) {
			case SET -> {
				assert type != null;
				Integer[] indices = all ? new Integer[]{1, 2, 3} : offerNumber.getArray(prepareEvent);
				for (Integer index : indices) {
					int slot = index - 1;
					if (slot < 0 || slot >= prepareEvent.getOffers().length)
						continue;

					EnchantmentOffer offer = prepareEvent.getOffers()[slot];
					if (offer == null) {
						offer = new EnchantmentOffer(type.getType(), type.getLevel(),
							getCost(slot + 1, prepareEvent.getEnchantmentBonus()));
						prepareEvent.getOffers()[slot] = offer;
					} else {
						offer.setEnchantment(type.getType());
						offer.setEnchantmentLevel(type.getLevel());
					}
				}
			}
			case DELETE -> {
				if (all) {
					Arrays.fill(prepareEvent.getOffers(), null);
				} else {
					for (Integer index : offerNumber.getArray(prepareEvent)) {
						int slot = index - 1;
						if (slot < 0 || slot >= prepareEvent.getOffers().length)
							continue;

						prepareEvent.getOffers()[slot] = null;
					}
				}
			}
			default -> {
				assert false;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return !all && offerNumber.isSingle();
	}

	@Override
	public Class<? extends EnchantmentOffer> getReturnType() {
		return EnchantmentOffer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return all ? "the enchantment offers" : "enchantment offer " + offerNumber.toString(event, debug);
	}

	/**
	 * Returns an enchantment cost from an enchantment button and number of bookshelves.
	 * @param slot The enchantment button slot (1, 2, or 3).
	 * @param bookshelves The number of bookshelves around the enchantment table.
	 * @return A cost for that enchantment button with the number of bookshelves, or 1 if 'slot' is not an integer from 1 to 3.
	 */
	public int getCost(int slot, int bookshelves) {
		// Based on https://minecraft.wiki/w/Enchanting_table_mechanics#Basic_mechanics
		// (from 1 to 8) + floor(bookshelves / 2) + (from 0 to bookshelves)
		int base = (rand.nextInt(8) + 1) + (bookshelves / 2) + (rand.nextInt(bookshelves + 1));
		return switch (slot) {
			case 1 -> Math.max(base / 3, 1);
			case 2 -> (base * 2) / 3 + 1;
			case 3 -> Math.max(base, bookshelves * 2);
			default -> 1;
		};
	}

}
