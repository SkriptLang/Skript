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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Player Statistics")
@Description({
	"Returns the value of one or more statistics for one or more players.",
	"Statistics are countable data for every player, which is tracked by the server.",
	"You can find more info on the <a href=\"https://minecraft.fandom.com/wiki/Statistics\">Minecraft Wiki</a> and a list of statistic names on the " +
		"<a href=\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Statistic.html\">Spigot Javadocs</a>.",
})
@Examples({
	"# Examples:",
	"# \t/statistics AwesomePlayerName MINE_BLOCK sand itemtype",
	"# \t/statistics AwesomePlayerName KILL_ENTITY zombie entity",
	"# \t/statistics AwesomePlayerName CHEST_OPENED",
	"command /statistics <offlineplayer> <string> [<text>] [<text>]:",
	"\tusage: Usage: /statistics <player> <statistic = string> [kind e.g. cow, stone] [type = entity/itemtype]",
	"\ttrigger:",
	"\t\tif arg-3 is set:",
	"\t\t\tif arg-4 = \"entity\":",
	"\t\t\t\tset {_kind} to arg-3 parsed as entitytype",
	"\t\t\telse if arg-4 = \"itemtype\":",
	"\t\t\t\tset {_kind} to arg-3 parsed as itemtype",
	"\t\t\telse:",
	"\t\t\t\tsend \"You need to specify type: entity/itemtype\"",
	"\t\t\t\tstop",
	"",
	"\t\t\tif {_kind} is set:",
	"\t\t\t\tsend statistic arg-2 for {_kind} of arg-1 to player",
	"\t\telse:",
	"\t\t\tsend statistic arg-2 of arg-1 to player",
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.15+ (offlineplayers)")
public class ExprStatistics extends SimpleExpression<Long> {

	private static final String PLAYER_PATTERN = Skript.isRunningMinecraft(1, 15) ? "%-offlineplayers%" : "%-players%";

	static {
		Skript.registerExpression(ExprStatistics.class, Long.class, ExpressionType.COMBINED,
			"statistic[s] %statistics% [for %-entitydata/itemtype%] of " + PLAYER_PATTERN,
			PLAYER_PATTERN + "'[s] statistic[s] %statistics% [for %-entitydata/itemtype%]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Statistic> statistics;
	@Nullable
	private Expression<?> ofType;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<OfflinePlayer> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		statistics = (Expression<Statistic>) exprs[matchedPattern];
		ofType = exprs[matchedPattern + 1];
		players = (Expression<OfflinePlayer>) exprs[matchedPattern == 0 ? 2 : 0];
		return true;
	}

	@Override
	@Nullable
	public Long[] get(Event event) {
		OfflinePlayer[] players = this.players.getArray(event);
		Statistic[] statistics = this.statistics.getArray(event);
		Object ofType = this.ofType != null ? this.ofType.getSingle(event) : null; // TODO support getArray()

		if (players == null || statistics == null)
			return new Long[0];

		List<Long> result = new ArrayList<>(players.length * statistics.length);

		for (OfflinePlayer player : players) {
			for (Statistic statistic : statistics) {
				try {
					if (ofType instanceof ItemType) {
						result.add((long) player.getStatistic(statistic, ((ItemType) ofType).getMaterial()));
					} else if (ofType instanceof EntityData<?>) {
						result.add((long) player.getStatistic(statistic, EntityUtils.toBukkitEntityType((EntityData<?>) ofType)));
					} else {
						result.add((long) player.getStatistic(statistic));
					}
				} catch (IllegalArgumentException ignored) {
					return new Long[0];
				}
			}
		}
		return result.toArray(new Long[0]);
	}

	@Override
	public boolean isSingle() {
		return statistics.isSingle() && players.isSingle();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case REMOVE:
			case ADD:
			case RESET:
			case DELETE:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		OfflinePlayer[] players = this.players.getArray(event);
		Statistic[] statistics = this.statistics.getArray(event);
		Object ofType = this.ofType != null ? this.ofType.getSingle(event) : null; // TODO support getArray()

		if (ofType instanceof ItemType) {
			ofType = ((ItemType) ofType).getMaterial();
		} else if (ofType instanceof EntityData) {
			ofType = EntityUtils.toBukkitEntityType((EntityData<?>) ofType);
		}

		int value = (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) ? 0 : ((Long) delta[0]).intValue();

		applyStatistic(players, statistics, ofType, value, mode);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "statistic " + statistics.toString(event, debug) + (ofType != null ? " of " +
			players.toString(event, debug) : "") + " of " + players.toString(event, debug);
	}

	private static void applyStatistic(OfflinePlayer[] players, Statistic[] statistics, @Nullable Object ofType, int value, ChangeMode mode) {
		for (OfflinePlayer player : players) {
			for (Statistic statistic : statistics) {
				try {
					applyStatistic(player, statistic, ofType, value, mode);
				} catch (IllegalArgumentException ignored) {}
			}
		}
	}

	private static void applyStatistic(OfflinePlayer player, Statistic statistic, @Nullable Object ofType, int value, ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			setStatistic(player, statistic, ofType, value);
		} else if (mode == ChangeMode.ADD) {
			incrementStatistic(player, statistic, ofType, value);
		} else {
			decrementStatistic(player, statistic, ofType, value);
		}
	}

	private static void incrementStatistic(OfflinePlayer player, Statistic stat, @Nullable Object ofType, int value) {
		if (ofType instanceof Material) {
			player.incrementStatistic(stat, (Material) ofType, value);
		} else if (ofType instanceof EntityType) {
			player.incrementStatistic(stat, (EntityType) ofType, value);
		} else {
			player.incrementStatistic(stat, value);
		}
	}

	private static void decrementStatistic(OfflinePlayer player, Statistic stat, @Nullable Object ofType, int value) {
		if (ofType instanceof Material) {
			player.decrementStatistic(stat, (Material) ofType, value);
		} else if (ofType instanceof EntityType) {
			player.decrementStatistic(stat, (EntityType) ofType, value);
		} else {
			player.decrementStatistic(stat, value);
		}
	}

	private static void setStatistic(OfflinePlayer player, Statistic stat, @Nullable Object ofType, int value) {
		if (ofType instanceof Material) {
			player.setStatistic(stat, (Material) ofType, value);
		} else if (ofType instanceof EntityType) {
			player.setStatistic(stat, (EntityType) ofType, value);
		} else {
			player.setStatistic(stat, value);
		}
	}

}
