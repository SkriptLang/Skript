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

/**
 * Represents a Task for a dedicated platform like Sponge, Paper, Bukkit or Folia.
 */
public interface PlatformScheduler {

	void run(Task task, long delayInTicks);

	boolean cancel(Task task);

	boolean isAlive(Task task);

	void run(AsyncTask task, long delay, TimeUnit unit);

	boolean cancel(AsyncTask task);

	boolean isAlive(AsyncTask task);

	void cancelAll(Plugin plugin);

}
