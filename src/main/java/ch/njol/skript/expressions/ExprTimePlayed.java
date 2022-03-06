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
import org.bukkit.OfflinePlayer;
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
@Description("The amount of time a player has played for on the server. This info is stored in the player's statistics in " +
	"the main world's data folder. Changing this will also change the player's stats which can be views in the client's statistics menu.")
@Examples({"set {_t} to time played of player",
	"if player's time played is greater than 10 minutes:",
	"\tgive player a diamond sword",
	"set player's time played to 0 seconds"})
@Since("2.5")
public class ExprTimePlayed extends SimplePropertyExpression<OfflinePlayer, Timespan> {

	private static final Statistic TIME_PLAYED;

	static {
		register(ExprTimePlayed.class, Timespan.class, "time played", "offlineplayers");
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
	public Timespan convert(OfflinePlayer offlinePlayer) {
		return Timespan.fromTicks_i(offlinePlayer.getStatistic(TIME_PLAYED));
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			return CollectionUtils.array(Timespan.class);
		} else {
			return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		long ticks = ((Timespan) delta[0]).getTicks_i();
		for (OfflinePlayer offlinePlayer : getExpr().getArray(e)) {
			long playerTicks = offlinePlayer.getStatistic(TIME_PLAYED);
			switch (mode) {
				case ADD:
					ticks = playerTicks + ticks;
					break;
				case REMOVE:
					ticks = playerTicks - ticks;
					break;
			}
			offlinePlayer.setStatistic(TIME_PLAYED, (int) ticks);
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
