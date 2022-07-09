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

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.eclipse.jdt.annotation.Nullable;

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
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Explosion Block Yield")
@Description({"The percentage of exploded blocks dropped in an explosion event.",
				"When changing the yield, a value greater than 1 will function the same as using 1.",
				"Attempting to change the yield to a value less than 0 will have no effect."})
@Examples({"on explode:",
			"set the explosion's block yield to 10%"})
@Events("explosion")
@Since("2.5")
public class ExprExplosionBlockYield extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprExplosionBlockYield.class, Number.class, ExpressionType.PROPERTY,
				"[the] [explosion['s]] block (yield|amount)",
				"[the] percentage of blocks dropped"
		);
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BlockExplodeEvent.class, EntityExplodeEvent.class)) {
			Skript.error("Explosion block yield can only be retrieved from an explode event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event e) {
		return new Number[]{ getYield(e) };
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}

	@Override
	public void change(final Event event, final @Nullable Object[] delta, final ChangeMode mode) {
		float n = delta == null ? 0 : ((Number) delta[0]).floatValue();
		if (n < 0) // Yield can't be negative
			return;
				
		// Yield can be a value from 0 to 1
		switch (mode) {
			case SET:
				setYield(event, n);
				break;
			case ADD:
				float add = getYield(event) + n;
				if (add < 0)
					return;
				setYield(event, add);
				break;
			case REMOVE:
				float subtract = getYield(event) - n;
				if (subtract < 0)
					return;
				setYield(event, subtract);
				break;
			case DELETE:
				setYield(event, 0);
				break;
			default:
				assert false;
		}
	}

	private float getYield(Event e) {
		if (e instanceof EntityExplodeEvent) {
			return ((EntityExplodeEvent) e).getYield();
		} else {
			return ((BlockExplodeEvent) e).getYield();
		}
	}
	
	private void setYield(Event e, float yield) {
		if (e instanceof EntityExplodeEvent) {
			((EntityExplodeEvent) e).setYield(yield);
		} else {
			((BlockExplodeEvent) e).setYield(yield);
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the explosion's block yield";
	}

}
