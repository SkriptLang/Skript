package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Maximum Enchantment Level")
@Description("The maximum allowed level in Minecraft of a particular <a href='#enchantment'>enchantment</a>")
@Example("""
	set {_maximum} to the maximum enchantment level of sharpness
	if the level of sharpness of the player's tool is greater than {_maximum}:
	  send "<gold>Your tool's sharpness level was capped out at the maximum allowed level.</gold>"
	  set the level of sharpness of the player's tool to {_maximum}
	""")
@Since("INSERT VERSION")
public class ExprMaximumEnchantmentLevel extends SimplePropertyExpression<Enchantment, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SimplePropertyExpression.infoBuilder(
			ExprMaximumEnchantmentLevel.class, Integer.class,
			"max[imum] enchant[ment] level", "enchantments", true).build());
	}

	@Override
	public @Nullable Integer convert(Enchantment from) {
		return from.getMaxLevel();
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "maximum enchantment level";
	}
}
