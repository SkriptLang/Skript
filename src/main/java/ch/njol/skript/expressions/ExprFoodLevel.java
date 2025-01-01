package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Food Level")
@Description("The food level of a player from 0 to 10. Has several aliases: food/hunger level/meter/bar. ")
@Examples({"set the player's food level to 10"})
@Since("1.0")
public class ExprFoodLevel extends PropertyExpression<Player, Number> {
	
	static {
		Skript.registerExpression(ExprFoodLevel.class, Number.class, ExpressionType.PROPERTY, "[the] (food|hunger)[[ ](level|met(er|re)|bar)] [of %players%]", "%players%'[s] (food|hunger)[[ ](level|met(er|re)|bar)]");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<Player>) vars[0]);
		return true;
	}
	
	@Override
	protected Number[] get(Event event, Player[] source) {
		return get(source, player -> {
			if (getTime() >= 0 && event instanceof FoodLevelChangeEvent foodLevelChangeEvent
				&& player.equals(foodLevelChangeEvent.getEntity()) && !Delay.isDelayed(event)) {
				return 0.5f * foodLevelChangeEvent.getFoodLevel();
			}
			return 0.5f * player.getFoodLevel();
		});
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the food level of " + getExpr().toString(e, debug);
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert mode != ChangeMode.REMOVE_ALL;
		
		final int s = delta == null ? 0 : Math.round(((Number) delta[0]).floatValue() * 2);
		for (final Player player : getExpr().getArray(e)) {
			final boolean event = getTime() >= 0 && e instanceof FoodLevelChangeEvent && ((FoodLevelChangeEvent) e).getEntity() == player && !Delay.isDelayed(e);
			int food;
			if (event)
				food = ((FoodLevelChangeEvent) e).getFoodLevel();
			else
				food = player.getFoodLevel();
			switch (mode) {
				case SET:
				case DELETE:
					food = Math2.fit(0, s, 20);
					break;
				case ADD:
					food = Math2.fit(0, food + s, 20);
					break;
				case REMOVE:
					food = Math2.fit(0, food - s, 20);
					break;
				case RESET:
					food = 20;
					break;
				case REMOVE_ALL:
					assert false;
			}
			if (event)
				((FoodLevelChangeEvent) e).setFoodLevel(food);
			else
				player.setFoodLevel(food);
		}
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, FoodLevelChangeEvent.class, getExpr());
	}
}
