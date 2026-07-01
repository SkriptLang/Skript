package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Enchantment Hint")
@Description("""
	The enchantment hint in an enchant event.
	This is the enchantment that was shown to the player when they hovered over the enchantment offer that they eventually selected.
	""")
@Example("""
	on enchant:
		send "You got at least %the enchantment hint%!"
	""")
@Events("enchant")
@Since("INSERT VERSION")
public class ExprEnchantmentHint extends SimpleExpression<Enchantment> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprEnchantmentHint.class, Enchantment.class)
			.addPatterns("[the] enchant[ment] hint")
			.supplier(ExprEnchantmentHint::new)
			.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EnchantItemEvent.class);
	}

	@Override
	protected Enchantment[] get(Event event) {
		if (!(event instanceof EnchantItemEvent enchant))
			return new Enchantment[0];

		return CollectionUtils.array(enchant.getEnchantmentHint());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Enchantment> getReturnType() {
		return Enchantment.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the enchantment hint";
	}

}
