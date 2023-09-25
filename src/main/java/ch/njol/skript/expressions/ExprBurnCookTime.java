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

import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Burn/Cook Time")
@Description({"The time a furnace takes to burn an item in a <a href='events.html#fuel_burn'>fuel burn</a> event.",
			"Can also be used to change the burn/cook time of a placed furnace."})
@Examples({"on fuel burn:",
		"	if fuel slot is coal:",
		"		set burning time to 1 tick"})
@Since("2.3")
public class ExprBurnCookTime extends PropertyExpression<Block, Timespan> {

	static {
		Skript.registerExpression(ExprBurnCookTime.class, Timespan.class, ExpressionType.PROPERTY,
				"[the] burn[ing] time",
				"[the] (burn|1¦cook)[ing] time of %blocks%",
				"%blocks%'[s] (burn|1¦cook)[ing] time");
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
	protected Timespan[] get(Event e, Block[] source) {
		if (isEvent) {
			if (!(e instanceof FurnaceBurnEvent))
				return null;

			return CollectionUtils.array(Timespan.fromTicks(((FurnaceBurnEvent) e).getBurnTime()));
		} else {
			Timespan[] result = Arrays.stream(source)
					.filter(block -> anyFurnace.isOfType(block))
					.map(furnace -> {
						Furnace state = (Furnace) furnace.getState();
						return Timespan.fromTicks(cookTime ? state.getCookTime() : state.getBurnTime());
					})
					.toArray(Timespan[]::new);
			assert result != null;
			return result;
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return isEvent ? "the burning time" : "" + String.format("the %sing time of %s", cookTime ? "cook" : "burn", getExpr().toString(e, debug));
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.ADD
			|| mode == Changer.ChangeMode.REMOVE
			|| mode == Changer.ChangeMode.SET)
			return CollectionUtils.array(Timespan.class);
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null)
			return;

		Function<Timespan, Timespan> value = null;
		ClassInfo<Timespan> ci = DefaultClasses.TIMESPAN;
		Arithmetic<Timespan, Timespan> arithmetic = ci.getRelativeMath();
		Timespan changed = (Timespan) delta[0];
		assert arithmetic != null;

		switch (mode) {
			case ADD:
				value = (original) -> arithmetic.add(original, changed);
				break;
			case REMOVE:
				value = (original) -> arithmetic.difference(original, changed);
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
			if (!(e instanceof FurnaceBurnEvent))
				return;

			FurnaceBurnEvent event = (FurnaceBurnEvent) e;
			event.setBurnTime((int) value.apply(Timespan.fromTicks(event.getBurnTime())).getTicks());
			return;
		}

		for (Block block : getExpr().getArray(e)) {
			if (!anyFurnace.isOfType(block))
				continue;
			Furnace furnace = (Furnace) block.getState();
			long time = value.apply(Timespan.fromTicks(cookTime ? furnace.getCookTime() : furnace.getBurnTime())).getTicks();

			if (cookTime)
				furnace.setCookTime((short) time);
			else
				furnace.setBurnTime((short) time);

			furnace.update();
		}
	}
}
