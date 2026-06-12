package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Enchantment Bonus")
@Description("The enchantment bonus in an enchant prepare event. This represents the number of bookshelves affecting/surrounding the enchantment table.")
@Example("""
	on enchant:
		send "There are %enchantment bonus% bookshelves surrounding this enchantment table!" to player
	""")
@Events("enchant prepare")
@Since("2.5")
public class ExprEnchantmentBonus extends SimpleExpression<Integer> implements EventRestrictedSyntax {
	
	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprEnchantmentBonus.class, Integer.class)
			.addPattern("[the] enchant[ment] bonus")
			.supplier(ExprEnchantmentBonus::new)
			.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked") /* hard-coded event type */
	public Class<? extends Event>[] supportedEvents() {
		return new Class[]{PrepareItemEnchantEvent.class};
	}

	@Override
	protected Integer @Nullable [] get(Event event) {
		if (!(event instanceof PrepareItemEnchantEvent prepare))
			return null;

		return new Integer[]{prepare.getEnchantmentBonus()};
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
		return "enchantment bonus";
	}

}
