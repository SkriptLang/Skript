package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Enchanting Experience Toll")
@Description({"The toll of enchanting within an enchant event.", 
				"This be the number that was displayed upon the enchantment table, not the true number of levels taken away."})
@Example("""
    on enchant:
    	send "Toll: %the displayed enchanting toll%" to player
    """)
@Events("enchant")
@Since("2.5")
public class ExprEnchantingExpCost extends SimpleExpression<Long> {

	static {
		Skript.registerExpression(ExprEnchantingExpCost.class, Long.class, ExpressionType.SIMPLE,
				"[the] [displayed] ([e]xp[erience]|enchanting) toll");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EnchantItemEvent.class)) {
			Skript.error("The experience cost of enchanting is only usable in an enchant event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Long[] get(Event e) {
		return new Long[]{(long) ((EnchantItemEvent) e).getExpLevelCost()};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE || mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class, Experience.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;
		Object c = delta[0];
		int cost = c instanceof Number ? ((Number) c).intValue() : ((Experience) c).getXP();
		EnchantItemEvent e = (EnchantItemEvent) event;
		switch (mode) {
			case SET:
				e.setExpLevelCost(cost);
				break;
			case ADD:
				int add = e.getExpLevelCost() + cost;
				e.setExpLevelCost(add);
				break;
			case REMOVE:
				int subtract = e.getExpLevelCost() - cost;
				e.setExpLevelCost(subtract);
				break;
			case RESET:
			case DELETE:
			case REMOVE_ALL:
				assert false;
		}
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
		return "the displayed cost of enchanting";
	}

}
