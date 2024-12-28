package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

@Name("Enchantment Bonus")
@Description("The enchantment bonus in an enchant prepare event. This represents the number of bookshelves affecting/surrounding the enchantment table.")
@Examples({"on enchant:", 
			"\tsend \"There are %enchantment bonus% bookshelves surrounding this enchantment table!\" to player"})
@Events("enchant prepare")
@Since("2.5")
public class ExprEnchantmentBonus extends SimpleExpression<Long> {

	static {
		Skript.registerExpression(ExprEnchantmentBonus.class, Long.class, ExpressionType.SIMPLE, "[the] enchantment bonus");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PrepareItemEnchantEvent.class)) {
			Skript.error("The enchantment bonus is only usable in an enchant prepare event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Long[] get(Event e) {
		return new Long[]{(long) ((PrepareItemEnchantEvent) e).getEnchantmentBonus()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}


	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "enchantment bonus";
	}

}
