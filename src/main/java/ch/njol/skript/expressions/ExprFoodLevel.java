/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.eclipse.jdt.annotation.Nullable;

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
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;

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
	protected Number[] get(final Event e, final Player[] source) {
		return get(source, new Getter<Number, Player>() {
			@Override
			public Number get(final Player p) {
				if (getTime() >= 0 && e instanceof FoodLevelChangeEvent && p.equals(((FoodLevelChangeEvent) e).getEntity()) && !Delay.isDelayed(e)) {
					return 0.5f * ((FoodLevelChangeEvent) e).getFoodLevel();
				}
				return 0.5f * p.getFoodLevel();
			}
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
