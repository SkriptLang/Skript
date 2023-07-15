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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Player Time")
@Description({
	"Gets the player's time. This will set the player's own personal time.",
	"When setting relative time for a player, their time will tick inline with the world, without it will be static."
})
@Examples({
	"set player time of player to dusk, 10:00 or midnight # random out of the three options",
	"add 10 hours to player's player time",
	"remove 10:00 from player's player time",
	"reset player time of players"
})
@Since("INSERT VERSION")
public class ExprPlayerTime extends SimplePropertyExpression<Player, Time> {

	static {
		registerDefault(ExprPlayerTime.class, Time.class, "[:relative] player time", "players");
	}

	private boolean relative;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		relative = parseResult.hasTag("relative");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Time convert(Player player) {
		return new Time((int) player.getPlayerTime());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case RESET:
				return CollectionUtils.array();
			case ADD:
			case REMOVE:
				return CollectionUtils.array(Timespan.class);
			case SET:
				return CollectionUtils.array(Time.class, Timeperiod.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		long value = 0;
		if (delta != null) {
			switch (mode) {
				case SET:
					value = delta[0] instanceof Time ? ((Time) delta[0]).getTicks() : ((Timeperiod) delta[0]).start;
					break;
				case ADD:
				case REMOVE:
					value = ((Timespan) delta[0]).getTicks_i();
					break;
				default:
					assert false;
			}
		}
		switch (mode) {
			case RESET:
				for (Player player : getExpr().getArray(event)) {
					player.resetPlayerTime();
				}
				break;
			case SET:
				for (Player player : getExpr().getArray(event)) {
					player.setPlayerTime(value, relative);
				}
				break;
			case REMOVE:
				value = -value;
			case ADD:
				for (Player player : getExpr().getArray(event)) {
					long newTime = player.getPlayerTime() + value;
					player.setPlayerTime(Math.max(0, newTime), relative);
				}
				break;
			default:
				assert false;
		}
	}

	@Override
	public Class<? extends Time> getReturnType() {
		return Time.class;
	}

	@Override
	protected String getPropertyName() {
		return (relative ? "relative " : "") + "player time";
	}

}
