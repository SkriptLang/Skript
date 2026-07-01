package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Minimum/Maximum Enchantment Level")
@Description("""
	The minimum or maximum allowed level in Minecraft of a particular <a href='#enchantment'>enchantment</a>.
	
	The minimum starting level is 1 for all existing enchantments as of 26.1.2.
	""")
@Example("""
	set {_maximum} to the maximum enchantment level of sharpness
	if the level of sharpness of the player's tool is greater than {_maximum}:
		send "<gold>Your tool's sharpness level was capped out at the maximum allowed level.</gold>"
		set the level of sharpness of the player's tool to {_maximum}
	""")
@Example("""
	set {_min} to the minimum enchantment level of sharpness
	set {_max} to the maximum enchantment level of sharpness
	loop integers between {_min} and {_max}:
		set slot loop-counter of {_gui} to enchanted book named "Sharpness %loop-value%" with lore "<reset>Click to enchant!"
	""")
@Since("INSERT VERSION")
public class ExprMinMaxEnchantmentLevel extends SimplePropertyExpression<Enchantment, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprMinMaxEnchantmentLevel.class, Integer.class,
			"((:max|min)[imum]|starting) enchant[ment] level", "enchantments", true)
			.supplier(ExprMinMaxEnchantmentLevel::new)
			.build());
	}

	private boolean maximum = false;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		maximum = parseResult.hasTag("max");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(Enchantment from) {
		return maximum ? from.getMaxLevel() : from.getStartLevel();
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (maximum ? "maximum" : "minimum") + " enchantment level";
	}

}
