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
package org.skriptlang.skript.scheduler;

import java.util.concurrent.TimeUnit;

import org.bukkit.plugin.Plugin;
import org.skriptlang.skript.scheduler.platforms.FoliaScheduler;
import org.skriptlang.skript.scheduler.platforms.SpigotScheduler;

import ch.njol.skript.Skript;

/**
 * Class to handle the abstract versioning and to be loaded from the Skript main class.
 */
public class TaskManager {

	private static PlatformScheduler scheduler;
	private static Skript instance;

	/**
	 * For the main Skript class to call once.
	 * @throws IllegalAccessException if the TaskManager has already been initalized.
	 */
	public TaskManager(Skript instance) throws IllegalAccessException {
		if (TaskManager.instance != null)
			throw new IllegalAccessException("The TaskManager has already been initalized!");
		TaskManager.instance = instance;
		if (Skript.classExists("io.papermc.paper.threadedregions.RegionizedServer")) {
			TaskManager.scheduler = new FoliaScheduler();
		} else {
			TaskManager.scheduler = new SpigotScheduler();
		}
	}

	public PlatformScheduler getScheduler() {
		return scheduler;
	}

	public static void run(Task task, long delayInTicks) {
		scheduler.run(task, delayInTicks);
	}

	public static boolean cancel(Task task) {
		return scheduler.cancel(task);
	}

	public static boolean isAlive(Task task) {
		return scheduler.isAlive(task);
	}

	public static void run(AsyncTask task, long delay, TimeUnit unit) {
		scheduler.run(task, delay, unit);
	}

	public static boolean cancel(AsyncTask task) {
		return scheduler.cancel(task);
	}

	public static boolean isAlive(AsyncTask task) {
		return scheduler.isAlive(task);
	}

	public static void cancelAll(Plugin plugin) {
		scheduler.cancelAll(plugin);
	}

}
