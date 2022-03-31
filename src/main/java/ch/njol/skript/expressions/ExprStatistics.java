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
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
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

@Name("Player Statistics")
@Description({
	"Return the value of a specific player statistic.",
	"Statistics are countable data for every player, which is tracked by the server.",
	"You can find the more info on <a href=\"https://minecraft.fandom.com/wiki/Statistics\">Minecraft Wiki</a> and for the list of statistic names on " +
		"<a href=\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Statistic.html\">Spigot Javadocs</a>",
})
@Examples({
	"# Examples:",
	"# \t/statistics Notch MINE_BLOCK sand itemtype",
	"# \t/statistics Notch KILL_ENTITY zombie entity",
	"# \t/statistics Notch CHEST_OPENED",
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
					"\t\t\t\tsend statistic value arg-2 of type {_kind} of arg-1 to player",
			"\t\telse:",
				"\t\t\tsend statistic value arg-2 of arg-1 to player",
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.15+ (for offlineplayers)")
public class ExprStatistics extends SimpleExpression<Long> {

	private static String PLAYER_PATTERN = Skript.isRunningMinecraft(1, 15) ? "%-offlineplayers%" : "%-players%";

	static {
		Skript.registerExpression(ExprStatistics.class, Long.class, ExpressionType.COMBINED,
			"statistic[s] [value[s]] [of] %strings% [of type %-entitydata/itemtype%] of " + PLAYER_PATTERN, // 'of type' to not conflict with `of %offlineplayers%`
			PLAYER_PATTERN + "'[s] statistic[s] [value[s]] [of] %strings% [of type %-entitydata/itemtype%]");
	}

	private Expression<String> statistics;
	private Expression<?> ofType;
	private Expression<OfflinePlayer> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		statistics = (Expression<String>) exprs[matchedPattern];
		ofType = exprs[matchedPattern + 1];
		players = (Expression<OfflinePlayer>) exprs[matchedPattern == 0 ? 2 : 0];

		String unknownStat = "";
		if (statistics instanceof Literal || statistics instanceof ExpressionList || (statistics instanceof VariableString && ((VariableString) statistics).isSimple())) {
			try {
				if (statistics instanceof ExpressionList) {
					for (Expression<?> exp : ((ExpressionList) statistics).getExpressions()) {
						if (!(exp instanceof Literal || (exp instanceof VariableString && ((VariableString) exp).isSimple())))
							continue;

						for (Object s : exp.getArray(null)) {
							if (!(s instanceof String))
								continue;

							unknownStat = (String) s;
							Statistic.valueOf((String) s);
						}
					}
				} else {
					unknownStat = statistics.getSingle(null);
					Statistic.valueOf(unknownStat);
				}
			} catch (Exception e) {
				Skript.error("Unknown statistic name: " + unknownStat);
				return false;
			}
		}
		return true;
	}

	@Override
	@Nullable
	public Long[] get(Event e) {
		OfflinePlayer[] players = this.players.getArray(e);
		String[] statistics = this.statistics.getArray(e);
		Object ofType = this.ofType != null ? this.ofType.getSingle(e) : null; // TODO support getArray()

		if (players == null || statistics == null)
			return null;

		ArrayList<Long> result = new ArrayList<>(players.length * statistics.length);

		for (OfflinePlayer p : players) {
			for (String s : statistics) {
				Statistic stat;
				try { // if it fails it won't stop others
					stat = Statistic.valueOf(s);
				} catch (IllegalArgumentException ex) {
					continue;
				}

				try {
					if (ofType instanceof ItemType)
						result.add((long) p.getStatistic(stat, ((ItemType) ofType).getMaterial()));
					else if (ofType instanceof EntityData<?>)
						result.add((long) p.getStatistic(stat, EntityUtils.toBukkitEntityType((EntityData) ofType)));
					else
						result.add((long) p.getStatistic(stat));
				} catch (IllegalArgumentException ex) {
					return null;
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
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;

		OfflinePlayer[] players = this.players.getArray(e);
		String[] statistics = this.statistics.getArray(e);
		Object ofType = this.ofType != null ? this.ofType.getSingle(e) : null; // TODO support getArray()

		if (ofType instanceof ItemType)
			ofType = ((ItemType) ofType).getMaterial();

		if (ofType instanceof EntityData<?>)
			ofType = EntityUtils.toBukkitEntityType((EntityData) ofType);

		int value = mode == ChangeMode.RESET ? 0 : ((Long) delta[0]).intValue();

		applyStatistic(players, statistics, ofType, value, mode);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "statistic " + statistics.toString(e, debug) + (ofType != null ? " of " +
			players.toString(e, debug) : "") + " of " + players.toString(e, debug);
	}

	private void applyStatistic(OfflinePlayer[] players, String[] statistics, Object ofType, int value, ChangeMode mode) {
		for (OfflinePlayer p : players) {
			for (String s : statistics) {
				try {
					Statistic stat = Statistic.valueOf(s);
					applyStatistic(p, stat, ofType, value, mode);
				} catch (IllegalArgumentException ex) {
					return;
				}
			}
		}
	}

	private void applyStatistic(OfflinePlayer p, Statistic stat, Object type, int value, ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			setStatistic(p ,stat, type, value);
		else if (mode == ChangeMode.ADD)
			incrementStatistic(p ,stat, type, value);
		else
			decrementStatistic(p ,stat, type, value);
	}

	private void incrementStatistic(OfflinePlayer p, Statistic stat, Object type, int value) {
		if (type instanceof Material)
			p.incrementStatistic(stat, (Material) type, value);
		else if (type instanceof EntityType)
			p.incrementStatistic(stat, (EntityType) type, value);
		else if (type == null)
			p.incrementStatistic(stat, value);

		return;
	}

	private void decrementStatistic(OfflinePlayer p, Statistic stat, Object type, int value) {
		if (type instanceof Material)
			p.decrementStatistic(stat, (Material) type, value);
		else if (type instanceof EntityType)
			p.decrementStatistic(stat, (EntityType) type, value);
		else if (type == null)
			p.decrementStatistic(stat, value);

		return;
	}

	private void setStatistic(OfflinePlayer p, Statistic stat, Object type, int value) {
		if (type instanceof Material)
			p.setStatistic(stat, (Material) type, value);
		else if (type instanceof EntityType)
			p.setStatistic(stat, (EntityType) type, value);
		else if (type == null)
			p.setStatistic(stat, value);

		return;
	}
	
}
