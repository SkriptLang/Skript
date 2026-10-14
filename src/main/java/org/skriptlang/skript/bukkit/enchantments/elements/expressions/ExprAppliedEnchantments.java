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
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Applied Enchantments")
@Description("""
	The applied enchantments in an enchant event.
	Deleting or removing the applied enchantments will prevent the item's enchantment.
	""")
@Example("""
    on enchant:
    	set the applied enchantments to sharpness 10 and fire aspect 5
    """)
@Events("enchant")
@Since("2.5")
public class ExprAppliedEnchantments extends SimpleExpression<EnchantmentType> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			builder(ExprAppliedEnchantments.class, EnchantmentType.class)
				.addPattern("[the] applied enchant[ment]s")
				.supplier(ExprAppliedEnchantments::new)
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
	protected EnchantmentType @Nullable [] get(Event event) {
		if (!(event instanceof EnchantItemEvent enchantEvent))
			return new EnchantmentType[0];

		return enchantEvent.getEnchantsToAdd().entrySet().stream()
			.map(entry -> new EnchantmentType(entry.getKey(), entry.getValue()))
			.toArray(EnchantmentType[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, DELETE, REMOVE -> CollectionUtils.array(EnchantmentType[].class);
			default -> null;
		};
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof EnchantItemEvent enchantEvent))
			return;

		EnchantmentType[] enchants = new EnchantmentType[delta != null ? delta.length : 0];
		if (delta != null && delta.length != 0) {
			for (int i = 0; i < delta.length; i++) {
				enchants[i] = (EnchantmentType) delta[i];
			}
		}

		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			enchantEvent.getEnchantsToAdd().clear();

		switch (mode) {
			case DELETE -> {}
			case SET, ADD -> {
				for (EnchantmentType enchant : enchants)
					enchantEvent.getEnchantsToAdd().put(enchant.getType(), enchant.getLevel());
			}
			case REMOVE -> {
				for (EnchantmentType enchant : enchants)
					enchantEvent.getEnchantsToAdd().remove(enchant.getType(), enchant.getLevel());
			}
			default -> {
				assert false;
			}
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
	public String toString(@Nullable Event event, boolean debug) {
		return "the applied enchantments";
	}

}
