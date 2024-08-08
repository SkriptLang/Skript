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

import ch.njol.skript.Skript;
import ch.njol.util.Closeable;

/**
 * Represents a Task that only calls it's runnable asyncronous.
 */
public abstract class AsyncTask implements Runnable, Closeable {

	private final TimeUnit unit;
	private final Plugin plugin;
	private long period = -1;

	public AsyncTask(long delayInTicks, long periodInTicks) {
		this(Skript.getInstance(), delayInTicks, periodInTicks);
	}

	public AsyncTask(Plugin plugin, long delayInTicks, long periodInTicks) {
		this(plugin, (delayInTicks / 20) * 1000, (periodInTicks / 20) * 1000, TimeUnit.MILLISECONDS);
	}

	public AsyncTask(long delay, long period, TimeUnit unit) {
		this(Skript.getInstance(), period, unit);
	}

	public AsyncTask(Plugin plugin, long delay, long period, TimeUnit unit) {
		this.plugin = plugin;
		this.period = period;
		this.unit = unit;
		TaskManager.run(this, delay, unit);
	}

	public AsyncTask(long delayInTicks) {
		this(Skript.getInstance(), delayInTicks);
	}

	public AsyncTask(Plugin plugin, long delayInTicks) {
		this(plugin, (delayInTicks / 20) * 1000, TimeUnit.MILLISECONDS);
	}

	public AsyncTask(long delay, TimeUnit unit) {
		this(Skript.getInstance(), delay, unit);
	}

	public AsyncTask(Plugin plugin, long delay, TimeUnit unit) {
		this.plugin = plugin;
		this.unit = unit;
		TaskManager.run(this, delay, unit);
	}

	/**
	 * @return The plugin that is calling this task.
	 */
	public Plugin getPlugin() {
		return plugin;
	}

	/**
	 * @return The period of this task if it's repeating, otherwise -1
	 */
	public long getPeriod() {
		return period;
	}

	/**
	 * @return The amount the delay and period represent in TimeUnit.
	 */
	public TimeUnit getTimeUnit() {
		return unit;
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
		setNextExecution(delay, unit);
	}

	/**
	 * Re-schedules the task to run next after the given delay. If this task was repeating it will continue so using the same period as before.
	 * 
	 * @param delay The delay using the existing TimeUnit.
	 * @param unit The new TimeUnit you want to delay the new task by.
	 */
	public void setNextExecution(long delay, TimeUnit unit) {
		assert delay >= 0;
		cancel();
		TaskManager.run(this, delay, unit);
	}

	/**
	 * Sets the period of this task. This will re-schedule the task to be run next after the given period if the task is still running.
	 * 
	 * @param period Period in ticks or -1 to cancel the task and make it non-repeating.
	 */
	public void setPeriod(long period) {
		setPeriod(period, unit);
	}

	/**
	 * Sets the period of this task. This will re-schedule the task to be run next after the given period if the task is still running.
	 * 
	 * @param period Period in ticks or -1 to cancel the task and make it non-repeating.
	 * @param unit The new TimeUnit you want to delay the new task by.
	 */
	public void setPeriod(long period, TimeUnit unit) {
		assert period == -1 || period > 0;
		if (period == this.period)
			return;
		this.period = period;
		if (isAlive()) {
			cancel();
			if (period != -1)
				TaskManager.run(this, period, unit);
		}
	}

	/**
	 * Calls a one off task that executes once with no delay.
	 */
	public static void run(Runnable runnable) {
		new AsyncTask(0, TimeUnit.MILLISECONDS) {
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
