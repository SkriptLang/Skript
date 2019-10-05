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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
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

@Name("Time Played")
@Description("The amount of time a player has played for on the server. This info is stored on the server as a player statistic. " +
	"The player can see the same information in the client's statistics menu.")
@Examples({"set {_t} to time played of player",
	"if player's time played is greater than 10 minutes:",
	"\tgive player a diamond sword",
	"set player's time played to 0 seconds"})
@Since("INSERT VERSION")
public class ExprTimePlayed extends SimplePropertyExpression<Player, Timespan> {
	
	private final static Statistic TIME_PLAYED;

	static {
		register(ExprTimePlayed.class, Timespan.class, "time played", "players");
		if (Skript.isRunningMinecraft(1, 13)) {
			TIME_PLAYED = Statistic.PLAY_ONE_MINUTE; // Statistic name is misleading, it's actually measured in ticks
		} else {
			TIME_PLAYED = Statistic.valueOf("PLAY_ONE_TICK");
		}
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Player>) exprs[0]);
		return true;
	}
	
	@Nullable
	@Override
	public Timespan convert(Player player) {
		return Timespan.fromTicks_i((long) player.getStatistic(TIME_PLAYED) * 50);
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE) {
			return CollectionUtils.array(Timespan.class);
		} else {
			return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null) {
			return;
		}
		long ticks = ((Timespan) delta[0]).getTicks_i();
		for (Player player : getExpr().getArray(e)) {
			long playerTicks = player.getStatistic(TIME_PLAYED);
			switch (mode) {
				case ADD:
					ticks = playerTicks + ticks;
					break;
				case REMOVE:
					ticks = playerTicks - ticks;
					break;
			}
			player.setStatistic(TIME_PLAYED, (int) ticks);
		}
	}
	
	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "time played";
	}
	
}
