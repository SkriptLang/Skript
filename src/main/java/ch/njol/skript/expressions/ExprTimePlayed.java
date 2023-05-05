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
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Time Played")
@Description({
	"The amount of time a player has played for on the server. This info is stored in the player's statistics in " +
	"the main world's data folder. Changing this will also change the player's stats which can be views in the client's statistics menu.",
	"Using this expression on offline players on Minecraft 1.14 and below will return nothing <code>&lt;none&gt;</code>."
})
@Examples({
	"set {_t} to time played of player",
	"if player's time played is greater than 10 minutes:",
	"\tgive player a diamond sword",
	"",
	"set player's time played to 0 seconds"
})
@RequiredPlugins("MC 1.15+ (offline players)")
@Since("2.5, 2.7 (offline players)")
public class ExprTimePlayed extends SimplePropertyExpression<OfflinePlayer, Timespan> {

	private static final boolean IS_OFFLINE_SUPPORTED = Skript.methodExists(OfflinePlayer.class, "getStatistic", Statistic.class);

	static {
		register(ExprTimePlayed.class, Timespan.class, "time played", "offlineplayers");
	}
	
	@Nullable
	@Override
	public Timespan convert(OfflinePlayer offlinePlayer) {
		return getTimePlayed(offlinePlayer);
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
			return CollectionUtils.array(Timespan.class);
		return null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;

		long ticks = ((Timespan) delta[0]).getTicks_i();
		for (OfflinePlayer offlinePlayer : getExpr().getArray(event)) {
			if (!IS_OFFLINE_SUPPORTED && !offlinePlayer.isOnline())
				continue;

			Timespan playerTimespan = getTimePlayed(offlinePlayer);
			if (playerTimespan == null)
				continue;

			long playerTicks = playerTimespan.getTicks_i();
			switch (mode) {
				case ADD:
					ticks = playerTicks + ticks;
					break;
				case REMOVE:
					ticks = playerTicks - ticks;
					break;
			}

			if (IS_OFFLINE_SUPPORTED) {
				offlinePlayer.setStatistic(Statistic.PLAY_ONE_MINUTE, (int) ticks);
			} else if (offlinePlayer.isOnline()) {
				offlinePlayer.getPlayer().setStatistic(Statistic.PLAY_ONE_MINUTE, (int) ticks); // No NPE due to isOnline check
			}
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

	@Nullable
	private Timespan getTimePlayed(OfflinePlayer offlinePlayer) {
		if (IS_OFFLINE_SUPPORTED) {
			return Timespan.fromTicks_i(offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE));
		} else if (offlinePlayer.isOnline()) {
			return Timespan.fromTicks_i(offlinePlayer.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE));
		}
		return null;
	}
	
}
