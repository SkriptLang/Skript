package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.lang.EventRestrictedSyntax;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
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
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Enchanting Experience Cost")
@Description("""
	The cost of enchanting in an enchant event.
	This is number that was displayed in the enchantment table, not the actual number of levels removed.
	""")
@Example("""
	on enchant:
		send "Cost: %the displayed enchanting cost%" to player
	""")
@Events("enchant")
@Since("2.5")
public class ExprEnchantingExpCost extends SimpleExpression<Integer> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprEnchantingExpCost.class, Integer.class)
			.addPattern("[the] [displayed] ([e]xp[erience]|enchanting) cost")
			.supplier(ExprEnchantingExpCost::new)
			.build());
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EnchantItemEvent.class);
	}

	@Override
	protected Integer @Nullable [] get(Event event) {
		if (!(event instanceof EnchantItemEvent enchantEvent))
			return null;
		
		return new Integer[]{enchantEvent.getExpLevelCost()};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null || delta.length == 0 || !(event instanceof EnchantItemEvent enchantEvent))
			return;
		int cost = ((Number) delta[0]).intValue();
		switch (mode) {
			case SET -> enchantEvent.setExpLevelCost(cost);
			case ADD -> {
				int add = enchantEvent.getExpLevelCost() + cost;
				enchantEvent.setExpLevelCost(add);
			}
			case REMOVE -> {
				int subtract = enchantEvent.getExpLevelCost() - cost;
				enchantEvent.setExpLevelCost(subtract);
			}
			case RESET, DELETE, REMOVE_ALL -> {
				assert false;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the displayed cost of enchanting";
	}

}
