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
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


@Name("Boss Bar")
@Description("The boss bar of a player")
@Examples({"set bossbar of player to \"Hello!\""})
@Since("INSERT VERSION")
public class ExprBossBar extends SimpleExpression<BossBar> {

	static {
		PropertyExpression.register(ExprBossBar.class, BossBar.class, "boss[ ]bars", "players");
	}

	// insertion order is important so this MUST be a LinkedHashSet
	private static Map<Player, LinkedHashSet<BossBar>> playerBossBarMap = new WeakHashMap<>();
	@Nullable
	private static BossBar lastBossBar;

	/**
	 * Gets a player's bossbars. The set is in the same order as the bars are displayed to the player (e.g. index 0 is
	 * the top bar, index 1 is visually below that and so on)
	 *
	 * @param player the player whose bossbars to get
	 * @return the potentially empty set of the player's bossbars, or null if not yet set
	 */
	@Nullable
	public static LinkedHashSet<BossBar> getBossBarsForPlayer(Player player) {
		return playerBossBarMap.get(player);
	}

	@Nullable
	public static BossBar getLastBossBar() {
		return lastBossBar;
	}

	private Expression<Player> players;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	protected BossBar[] get(Event event) {
		List<BossBar> allBars = new ArrayList<>();
		for (Player player : players.getArray(event)) {
			LinkedHashSet<BossBar> playerBars = getBossBarsForPlayer(player);
			if (playerBars != null) {
				allBars.addAll(playerBars);
			}
		}
		return allBars.toArray(new BossBar[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case DELETE:
			case ADD:
			case SET:
				return new Class[]{String[].class, BossBar[].class};
			case REMOVE:
				return new Class[]{BossBar[].class};
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		// create any new bossbars first so that each player shares the same bossbar when changing multiple players
		// at once
		BossBar[] newBars = null;
		if (delta != null && delta.length > 0) {
			newBars = new BossBar[delta.length];
			for (int i = 0; i < delta.length; i++) {
				BossBar newBar;
				if (delta[i] instanceof BossBar)
					newBar = (BossBar) delta[i];
				else
					newBar = Bukkit.createBossBar((String) delta[i], BarColor.WHITE, BarStyle.SOLID);
				newBars[i] = newBar;
				lastBossBar = newBar;
			}
		}
		for (Player player : players.getArray(event)) {
			switch (mode) {
				case DELETE:
					LinkedHashSet<BossBar> bossBars = getBossBarsForPlayer(player);
					if (bossBars != null) {
						for (BossBar bar : bossBars) {
							bar.removePlayer(player);
						}
						bossBars.clear();
					}
					break;
				case ADD:
					bossBars = getBossBarsForPlayer(player);
					if (bossBars == null) {
						bossBars = new LinkedHashSet<>();
						playerBossBarMap.put(player, bossBars);
					}
					assert newBars != null;
					for (BossBar bar : newBars) {
						bar.addPlayer(player);
						bossBars.add(bar);
					}
					break;
				case SET:
					change(event, null, ChangeMode.DELETE);
					change(event, delta, ChangeMode.ADD);
					break;
				case REMOVE:
					bossBars = getBossBarsForPlayer(player);
					if (bossBars != null) {
						for (Object bossBarToRemove : delta) {
							((BossBar) bossBarToRemove).removePlayer(player);
							bossBars.remove(bossBarToRemove);
						}
					}
					break;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<BossBar> getReturnType() {
		return BossBar.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "bossbars of " + players.toString(event, debug);
	}

}
