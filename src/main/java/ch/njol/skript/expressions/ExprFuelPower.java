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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Fuel Power")
@Description("Gets the fuel power for this fuel. Each unit of power can fuel one brewing operation. can be modified")
@Examples({
	"on fuel brewing:",
	"\tset fuel power to 100"
})
@Events("fuel brewing")
@Since("INSERT VERSION")
public class ExprFuelPower extends SimpleExpression<Integer> {

	static {
		Skript.registerExpression(ExprFuelPower.class, Integer.class, ExpressionType.SIMPLE, "[the] fuel power");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BrewingStandFuelEvent.class)) {
			Skript.error("You can't use the 'fuel power' expression outside of a fuel brewing event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Integer[] get(Event event) {
		return new Integer[]{((BrewingStandFuelEvent) event).getFuelPower()};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if(mode != ChangeMode.DELETE && mode != ChangeMode.REMOVE_ALL)
			return CollectionUtils.array(Integer.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof BrewingStandFuelEvent)) return;
		int integer = delta[0] == null ? 0 : ((Number) delta[0]).intValue();
		int value = ((BrewingStandFuelEvent) event).getFuelPower();
		switch (mode) {
			case RESET:
			case SET:
				((BrewingStandFuelEvent) event).setFuelPower(integer);
				break;
			case REMOVE:
				integer = -integer;
				//$FALL-THROUGH$
			case ADD:
				((BrewingStandFuelEvent) event).setFuelPower(value + integer);
				break;
			case REMOVE_ALL:
			case DELETE:
				assert false;
		}
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
	public String toString(@Nullable Event e, boolean debug) {
		return "the fuel power";
	}
}
