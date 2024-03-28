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
package org.skriptlang.skript.elements.fireworks;

import org.bukkit.entity.Firework;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Firework Flown Time")
@Description({
	"How much time that the fireworks have been boosting the player in an <a href='events.html#elytra_boost'>elytra boost event</a>.",
	"Maximum is the same as time until detonation. Paper names them differently than Spigot."
})
@Examples({
	"if the time until detonation is less than 5 seconds:",
		"\tadd 2 seconds to time until detonation of event-firework"
})
@Since("INSERT VERSION")
@SuppressWarnings("removal")
public class ExprFireworkFlownTime extends SimplePropertyExpression<Firework, Timespan> {

	private final static boolean PAPER = Skript.methodExists(Firework.class, "getTicksFlown");

	/**
	 * Developer note:
	 * Spigot's methods are {@link Firework#getLife()} and {@link Firework#getMaxLife()} and were added in 1.19.
	 * Paper has had these methods since 1.18 but were named the following;
	 * {@link Firework#getTicksFlown()} and {@link Firework#getTicksToDetonate()} respectfully.
	 * Paper does not agree with Spigot's method naming, so they have Spigot's deprecated.
	 */
	static {
		registerDefault(ExprFireworkFlownTime.class, Timespan.class, "[firework] ([detonate:max[imum]] life[time]|flown time|detonate:time until detonat(e|ion))", "fireworks");
	}

	private boolean detonate;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		detonate = parseResult.hasTag("detonate");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Timespan convert(Firework firework) {
		if (PAPER)
			return Timespan.fromTicks_i(detonate ? firework.getTicksToDetonate() : firework.getTicksFlown());
		return Timespan.fromTicks_i(detonate ? firework.getMaxLife() : firework.getLife());
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case RESET:
			case SET:
				return CollectionUtils.array(Timespan.class);
			case REMOVE_ALL:
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		long ticks = (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) ? 0 : ((Timespan) delta[0]).getTicks_i();
		switch (mode) {
			case REMOVE:
				ticks = -ticks;
			case ADD:
				for (Firework firework : getExpr().getArray(event)) {
					long existing = convert(firework).getTicks_i();
					if (detonate) {
						if (PAPER) {
							firework.setTicksToDetonate((int) Math.min(0, existing + ticks));
						} else {
							firework.setMaxLife((int) Math.min(0, existing + ticks));
						}
					} else {
						if (PAPER) {
							firework.setTicksFlown((int) Math.min(0, existing + ticks));
						} else {
							firework.setLife((int) Math.min(0, existing + ticks));
						}
					}
				}
				break;
			case SET:
				for (Firework firework : getExpr().getArray(event)) {
					if (detonate) {
						if (PAPER) {
							firework.setTicksToDetonate((int) Math.min(0, ticks));
						} else {
							firework.setMaxLife((int) Math.min(0, ticks));
						}
					} else {
						if (PAPER) {
							firework.setTicksFlown((int) Math.min(0, ticks));
						} else {
							firework.setLife((int) Math.min(0, ticks));
						}
					}
				}
				break;
			case DELETE:
			case RESET:
				for (Firework firework : getExpr().getArray(event)) {
					if (detonate) {
						if (PAPER) {
							firework.setTicksToDetonate((int) 20 * firework.getFireworkMeta().getPower());
						} else {
							firework.setMaxLife((int) 20 * firework.getFireworkMeta().getPower());
						}
					} else {
						if (PAPER) {
							firework.setTicksFlown((int) 20 * firework.getFireworkMeta().getPower());
						} else {
							firework.setLife((int) 20 * firework.getFireworkMeta().getPower());
						}
					}
				}
				break;
			case REMOVE_ALL:
			default:
				break;
		}
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "firework " + (detonate ? "max " : " ") + "lifetime";
	}

}
