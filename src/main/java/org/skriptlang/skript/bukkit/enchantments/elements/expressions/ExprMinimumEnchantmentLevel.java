package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Minimum Enchantment Level")
@Description("""
	The minimum starting level in Minecraft of a particular <a href='#enchantment'>enchantment</a>.
	This is 1 for all existing enchantments as of 26.1.2.
	""")
@Example("""
	set {_min} to the minimum enchantment level of sharpness
	set {_max} to the maximum enchantment level of sharpness
	loop integers between {_min} and {_max}:
	  set slot loop-counter of {_gui} to enchanted book named "Sharpness %loop-value%" with lore "<reset>Click to enchant!"
	""")
@Since("INSERT VERSION")
public class ExprMinimumEnchantmentLevel extends SimplePropertyExpression<Enchantment, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SimplePropertyExpression.infoBuilder(
			ExprMinimumEnchantmentLevel.class, Integer.class,
			"(min[imum]|start[ing]) enchant[ment] level", "enchantments", true).build());
	}

	@Override
	public @Nullable Integer convert(Enchantment from) {
		return from.getStartLevel();
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "minimum enchantment level";
	}
}
