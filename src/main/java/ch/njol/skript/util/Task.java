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
package ch.njol.skript.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.util.Closeable;

/**
 * @author Peter Güttinger
 */
public abstract class Task implements Runnable, Closeable {
	
	private final Plugin plugin;
	private final boolean async;
	private long period = -1;
	
	private int taskID = -1;
	
	public Task(final Plugin plugin, final long delay, final long period) {
		this(plugin, delay, period, false);
	}
	
	public Task(final Plugin plugin, final long delay, final long period, final boolean async) {
		this.plugin = plugin;
		this.period = period;
		this.async = async;
		schedule(delay);
	}
	
	public Task(final Plugin plugin, final long delay) {
		this(plugin, delay, false);
	}
	
	public Task(final Plugin plugin, final long delay, final boolean async) {
		this.plugin = plugin;
		this.async = async;
		schedule(delay);
	}
	
	/**
	 * Only call this if the task is not alive.
	 * 
	 * @param delay
	 */
	@SuppressWarnings("deprecation")
	private void schedule(final long delay) {
		assert !isAlive();
		if (!Skript.getInstance().isEnabled())
			return;
		
		if (period == -1) {
			if (async) {
				taskID = Skript.isRunningMinecraft(1, 4, 6) ?
						Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, delay).getTaskId() :
						Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, this, delay);
			} else {
				taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
			}
		} else {
			if (async) {
				taskID = Skript.isRunningMinecraft(1, 4, 6) ?
						Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, delay, period).getTaskId() :
						Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, this, delay, period);
			} else {
				taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, delay, period);
			}
		}
		assert taskID != -1;
	}
	
	/**
	 * @return Whether this task is still running, i.e. whether it will run later or is currently running.
	 */
	public final boolean isAlive() {
		if (taskID == -1)
			return false;
		return Bukkit.getScheduler().isQueued(taskID) || Bukkit.getScheduler().isCurrentlyRunning(taskID);
	}
	
	/**
	 * Cancels this task.
	 */
	public final void cancel() {
		if (taskID != -1) {
			Bukkit.getScheduler().cancelTask(taskID);
			taskID = -1;
		}
	}
	
	@Override
	public void close() {
		cancel();
	}
	
	/**
	 * Re-schedules the task to run next after the given delay. If this task was repeating it will continue so using the same period as before.
	 * 
	 * @param delay
	 */
	public void setNextExecution(final long delay) {
		assert delay >= 0;
		cancel();
		schedule(delay);
	}
	
	/**
	 * Sets the period of this task. This will re-schedule the task to be run next after the given period if the task is still running.
	 * 
	 * @param period Period in ticks or -1 to cancel the task and make it non-repeating
	 */
	public void setPeriod(final long period) {
		assert period == -1 || period > 0;
		if (period == this.period)
			return;
		this.period = period;
		if (isAlive()) {
			cancel();
			if (period != -1)
				schedule(period);
		}
	}
	
	/**
	 * Equivalent to <tt>{@link #callSync(Callable, Plugin) callSync}(c, {@link Skript#getInstance()})</tt>
	 */
	@Nullable
	public static <T> T callSync(final Callable<T> c) {
		return callSync(c, Skript.getInstance());
	}
	
	/**
	 * Calls a method on Bukkit's main thread.
	 * <p>
	 * Hint: Use a Callable&lt;Void&gt; to make a task which blocks your current thread until it is completed.
	 * 
	 * @param c The method
	 * @param p The plugin that owns the task. Must be enabled.
	 * @return What the method returned or null if it threw an error or was stopped (usually due to the server shutting down)
	 */
	@Nullable
	public static <T> T callSync(final Callable<T> c, final Plugin p) {
		if (Bukkit.isPrimaryThread()) {
			try {
				return c.call();
			} catch (final Exception e) {
				Skript.exception(e);
			}
		}
		final Future<T> f = Bukkit.getScheduler().callSyncMethod(p, c);
		try {
			while (true) {
				try {
					return f.get();
				} catch (final InterruptedException e) {}
			}
		} catch (final ExecutionException e) {
			Skript.exception(e);
		} catch (final CancellationException e) {} catch (final ThreadDeath e) {}// server shutting down
		return null;
	}
	
}
