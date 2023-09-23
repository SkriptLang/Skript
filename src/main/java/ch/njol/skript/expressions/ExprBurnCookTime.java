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

import java.util.Arrays;
import java.util.function.Function;

import ch.njol.skript.classes.Changer.ChangeMode;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;

@Name("Burn/Cook Time")
@Description({
	"The time a furnace takes to burn an item in a <a href='events.html#fuel_burn'>fuel burn</a> event.",
	"Can also be used to change the burn/cook time of a placed furnace."
})
@Examples({
	"on fuel burn:",
		"\tif fuel slot is coal:",
			"\t\tset burning time to 1 tick"
})
@Since("2.3")
public class ExprBurnCookTime extends PropertyExpression<Block, Timespan> {

	static {
		Skript.registerExpression(ExprBurnCookTime.class, Timespan.class, ExpressionType.PROPERTY,
				"[the] burn[ing] time",
				"[the] (burn|1:cook)[ing] time of %blocks%",
				"%blocks%'[s] (burn|1:cook)[ing] time");
	}
	
	static final ItemType anyFurnace = Aliases.javaItemType("any furnace");

	private boolean cookTime;
	private boolean isEvent;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		cookTime = parseResult.mark == 1;
		isEvent = matchedPattern == 0;
		if (isEvent && !getParser().isCurrentEvent(FurnaceBurnEvent.class)) {
			Skript.error("Cannot use 'burning time' outside a fuel burn event.");
			return false;
		}
		if (!isEvent)
			setExpr((Expression<? extends Block>) exprs[0]);
		return true;
	}

	@Override
	protected Timespan[] get(Event event, Block[] source) {
		if (isEvent) {
			if (!(event instanceof FurnaceBurnEvent))
				return new Timespan[0];

			return CollectionUtils.array(Timespan.fromTicks_i(((FurnaceBurnEvent) event).getBurnTime()));
		} else {
			return Arrays.stream(source)
					.filter(anyFurnace::isOfType)
					.map(furnace -> {
						Furnace state = (Furnace) furnace.getState();
						return Timespan.fromTicks_i(cookTime ? state.getCookTime() : state.getBurnTime());
					})
					.toArray(Timespan[]::new);
		}
	}
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET)
			return CollectionUtils.array(Timespan.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;

		Function<Timespan, Timespan> value = null;
		Timespan changed = (Timespan) delta[0];

		switch (mode) {
			case ADD:
				value = (original) -> Arithmetics.calculate(Operator.ADDITION, original, changed, Timespan.class);
				break;
			case REMOVE:
				value = (original) -> Arithmetics.difference(original, changed, Timespan.class);
				break;
			case SET:
				value = (original) -> changed;
				break;
			//$CASES-OMITTED$
			default:
				assert false;
				break;
		}

		assert value != null; // It isn't going to be null but the compiler complains so

		if (isEvent) {
			if (!(event instanceof FurnaceBurnEvent))
				return;

			FurnaceBurnEvent burnEvent = (FurnaceBurnEvent) event;
			burnEvent.setBurnTime(value.apply(Timespan.fromTicks_i(burnEvent.getBurnTime())).getTicks());
			return;
		}

		for (Block block : getExpr().getArray(event)) {
			if (!anyFurnace.isOfType(block))
				continue;
			Furnace furnace = (Furnace) block.getState();
			long time = value.apply(Timespan.fromTicks_i(cookTime ? furnace.getCookTime() : furnace.getBurnTime())).getTicks_i();

			if (cookTime)
				furnace.setCookTime((short) time);
			else
				furnace.setBurnTime((short) time);

			furnace.update();
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return isEvent ? "the burning time" : String.format("the %sing time of %s", cookTime ? "cook" : "burn", getExpr().toString(event, debug));
	}

}
