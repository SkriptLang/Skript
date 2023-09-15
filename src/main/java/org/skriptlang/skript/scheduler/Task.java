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

import org.bukkit.plugin.Plugin;

import ch.njol.skript.Skript;
import ch.njol.util.Closeable;

/**
 * Represents a Task that only calls it's runnable synchronous.
 * Unless otherwise defined by the scheduler like Folia.
 */
public abstract class Task implements Runnable, Closeable {

	private final Plugin plugin;
	private long periodInTicks = -1;

	public Task(long delayInTicks, long periodInTicks) {
		this(Skript.getInstance(), delayInTicks, periodInTicks);
	}

	public Task(Plugin plugin, long delayInTicks, long periodInTicks) {
		this.plugin = plugin;
		this.periodInTicks = periodInTicks;
		TaskManager.run(this, delayInTicks);
	}

	public Task(long delayInTicks) {
		this(Skript.getInstance(), delayInTicks);
	}

	public Task(Plugin plugin, long delayInTicks) {
		this.plugin = plugin;
		TaskManager.run(this, delayInTicks);
	}

	/**
	 * @return The plugin that is calling this task.
	 */
	public Plugin getPlugin() {
		return plugin;
	}

	/**
	 * @return The period of this task if it's repeating in ticks, otherwise -1
	 */
	public long getPeriod() {
		return periodInTicks;
	}

	/**
	 * @return Whether this task is still running, i.e. whether it will run later or is currently running.
	 */
	public final boolean isAlive() {
		return TaskManager.isAlive(this);
	}

	/**
	 * Cancels this task.
	 * @return true if the task was successfully cancelled.
	 */
	public final boolean cancel() {
		return TaskManager.cancel(this);
	}

	@Override
	public void close() {
		cancel();
	}

	/**
	 * Re-schedules the task to run next after the given delay. If this task was repeating it will continue so using the same period as before.
	 * 
	 * @param delay The delay using the existing TimeUnit.
	 */
	public void setNextExecution(long delay) {
		assert delay >= 0;
		cancel();
		TaskManager.run(this, delay);
	}

	/**
	 * Sets the period of this task. This will re-schedule the task to be run next after the given period if the task is still running.
	 * 
	 * @param period Period in ticks or -1 to cancel the task and make it non-repeating.
	 */
	public void setPeriod(long period) {
		assert period == -1 || period > 0;
		if (period == this.periodInTicks)
			return;
		this.periodInTicks = period;
		if (isAlive()) {
			cancel();
			if (period != -1)
				TaskManager.run(this, period);
		}
	}

	/**
	 * Calls a one off task that executes once with no delay.
	 */
	public static void run(Runnable runnable) {
		new Task(0) {
			@Override
			public void run() {
				runnable.run();
			}
		};
	}

	/**
	 * Utility method for converting the provided milliseconds to ticks to work with the Spigot API.
	 * 
	 * @param milliseconds The amount of milliseconds to convert to ticks.
	 * @return The amount of ticks the provided milliseconds argument contained.
	 */
	public static long millisecondsToTicks(long milliseconds) {
		return (milliseconds / 1000) * 20;
	}

}
