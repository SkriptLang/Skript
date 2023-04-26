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
package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Task;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains utility methods related to players
 */
public abstract class PlayerUtils {

	private static final Set<Player> inventoryUpdateList = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Updates the clients inventory within a tick, using {@link Player#updateInventory()}.
	 * Recommended over directly calling the update method,
	 * as multiple calls to this method within a short timespan will not send multiple updates to the client.
	 */
	public static void updateInventory(@Nullable Player player) {
		if (player != null)
			inventoryUpdateList.add(player);
	}

	static {
		new Task(Skript.getInstance(), 1, 1) {
			@Override
			public void run() {
				for (Player p : inventoryUpdateList)
					p.updateInventory();

				inventoryUpdateList.clear();
			}
		};
	}

	/**
	 * @deprecated use {@link Bukkit#getOnlinePlayers()} instead
	 */
	@Deprecated
	public static Collection<? extends Player> getOnlinePlayers() {
		return ImmutableList.copyOf(Bukkit.getOnlinePlayers());
	}

	public static boolean canEat(Player p, Material food) {
		GameMode gm = p.getGameMode();
		if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR)
			return false; // Can't eat anything in those gamemodes
		
		boolean edible = food.isEdible();
		if (!edible)
			return false;
		boolean special;
		switch (food) {
			case GOLDEN_APPLE:
			case CHORUS_FRUIT:
				special = true;
				break;
				//$CASES-OMITTED$
			default:
				special = false;
		}
		if (p.getFoodLevel() < 20 || special)
			return true;

		return false;
	}

	/**
	 * Gets the experience points needed to reach the next level, starting at the given level.
	 * E.g. getLevelXP(30) returns the experience points needed to reach level 31 from level 30.
	 *
	 * @param level The starting level
	 * @return The experience points needed to reach the next level
	 */
	public static int getLevelXP(int level) {
		if (level <= 15)
			return (2 * level) + 7;
		if (level <= 30)
			return (5 * level) - 38;
		return (9 * level) - 158;
	}

	/**
	 * Gets the cumulative experience needed to reach the given level, but no further.
	 * E.g. getCumulativeXP(30) returns the experience points needed to reach level 30 from level 0.
	 *
	 * @param level The level to get the cumulative XP for
	 * @return The experience points needed to reach the given level
	 */
	public static int getCumulativeXP(int level) {
		if (level <= 15)
			return (level * level) + (6 * level);
		if (level <= 30)
			return (int) (2.5 * (level * level) - (40.5 * level) + 360);
		return (int) (4.5 * (level * level) - (162.5 * level) + 2220);
	}

	/**
	 * Gets the total experience points needed to reach the given level, including the given progress.
	 * E.g. getTotalXP(30, 0.5) returns the experience points needed to reach level 30 from level 0, and have a half-full xp bar.
	 *
	 * @param level The level to get the total XP of
	 * @param progress The progress towards the next level, between 0 and 1
	 * @return The total experience points needed to reach the given level and progress
	 */
	public static int getTotalXP(int level, double progress) {
		return (int) (getCumulativeXP(level) + getLevelXP(level) * progress);
	}

	/**
	 * Gets the total experience points of the given player.
	 *
	 * @param player The player to get the total XP of
	 * @return The total experience points of the given player
	 */
	public static int getTotalXP(Player player) {
		return getTotalXP(player.getLevel(), player.getExp());
	}

	/**
	 * Sets the total experience points of the given player.
	 *
	 * @param player The player to set the total XP of
	 * @param experience The total experience points to set
	 */
	public static void setTotalXP(Player player, int experience) {
		int level = 0;
		if (experience < 0)
			experience = 0;
		while (experience >= getLevelXP(level)) {
			experience -= getLevelXP(level);
			level++;
		}
		player.setLevel(level);
		player.setExp((float) experience / getLevelXP(level));
	}
}
