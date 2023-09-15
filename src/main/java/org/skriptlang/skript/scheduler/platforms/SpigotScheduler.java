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
package org.skriptlang.skript.scheduler.platforms;

import java.util.WeakHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.scheduler.AsyncTask;
import org.skriptlang.skript.scheduler.PlatformScheduler;
import org.skriptlang.skript.scheduler.Task;

import ch.njol.skript.Skript;

public class SpigotScheduler implements PlatformScheduler {

	private static final Map<Integer, AsyncTask> asyncTasks = new WeakHashMap<>();
	private static final Map<Integer, Task> tasks = new WeakHashMap<>();

	@Override
	public void run(Task task, long delayInTicks) {
		if (task.getPeriod() == -1) {
			tasks.put(Bukkit.getScheduler().runTaskLater(task.getPlugin(), task, delayInTicks).getTaskId(), task);
		} else {
			tasks.put(Bukkit.getScheduler().scheduleSyncRepeatingTask(task.getPlugin(), task, delayInTicks, task.getPeriod()), task);
		}
	}

	@Override
	public boolean cancel(Task task) {
		Optional<Integer> optional = tasks.entrySet().stream()
				.filter(entry -> entry.getValue().equals(task))
				.map(entry -> entry.getKey())
				.findFirst();
		if (!optional.isPresent())
			return false;
		int taskID = optional.get();
		Bukkit.getScheduler().cancelTask(taskID);
		tasks.remove(taskID);
		return true;
	}

	@Override
	public boolean isAlive(Task task) {
		return tasks.entrySet().stream()
				.filter(entry -> entry.getValue().equals(task))
				.map(entry -> entry.getKey())
				.map(taskID -> Bukkit.getScheduler().isQueued(taskID) || Bukkit.getScheduler().isCurrentlyRunning(taskID))
				.findFirst()
				.orElse(false);
	}

	@Override
	public void run(AsyncTask task, long delay, TimeUnit unit) {
		if (task.getPeriod() == -1) {
			asyncTasks.put(Bukkit.getScheduler().runTaskLaterAsynchronously(task.getPlugin(), task, delay).getTaskId(), task);
		} else {
			// We have to convert everything to ticks when using the Spigot API.
			long delayTicks = (task.getTimeUnit().toMillis(delay) / 1000) * 20;
			long periodTicks = (task.getTimeUnit().toMillis(task.getPeriod()) / 1000) * 20;
			asyncTasks.put(Bukkit.getScheduler().runTaskTimerAsynchronously(task.getPlugin(), task, delayTicks, periodTicks).getTaskId(), task);
		}
	}

	@Override
	public boolean cancel(AsyncTask task) {
		Optional<Integer> optional = asyncTasks.entrySet().stream()
				.filter(entry -> entry.getValue().equals(task))
				.map(entry -> entry.getKey())
				.findFirst();
		if (!optional.isPresent())
			return false;
		int taskID = optional.get();
		Bukkit.getScheduler().cancelTask(taskID);
		tasks.remove(taskID);
		return true;
	}

	@Override
	public boolean isAlive(AsyncTask task) {
		return asyncTasks.entrySet().stream()
				.filter(entry -> entry.getValue().equals(task))
				.map(entry -> entry.getKey())
				.map(taskID -> Bukkit.getScheduler().isQueued(taskID) || Bukkit.getScheduler().isCurrentlyRunning(taskID))
				.findFirst()
				.orElse(false);
	}

	@Override
	public void cancelAll(Plugin plugin) {
		asyncTasks.values().stream().filter(task -> task.getPlugin().equals(plugin)).forEach(task -> this.cancel(task));
		tasks.values().stream().filter(task -> task.getPlugin().equals(plugin)).forEach(task -> this.cancel(task));
	}

	/**
	 * Equivalent to <tt>{@link #callSync(Callable, Plugin) callSync}(c, {@link Skript#getInstance()})</tt>
	 */
	@Nullable
	public static <T> T callSync(Callable<T> callable) {
		return callSync(callable, Skript.getInstance());
	}

	/**
	 * Calls a method on Bukkit's main thread.
	 * <p>
	 * Hint: Use a Callable&lt;Void&gt; to make a task which blocks your current thread until it is completed.
	 * 
	 * @param callable The method
	 * @param plugin The plugin that owns the task. Must be enabled.
	 * @return What the method returned or null if it threw an error or was stopped (usually due to the server shutting down)
	 */
	@Nullable
	public static <T> T callSync(Callable<T> callable, Plugin plugin) {
		if (Bukkit.isPrimaryThread()) {
			try {
				return callable.call();
			} catch (Exception e) {
				Skript.exception(e);
			}
		}
		Future<T> future = Bukkit.getScheduler().callSyncMethod(plugin, callable);
		try {
			while (true) {
				try {
					return future.get();
				} catch (InterruptedException e) {}
			}
		} catch (ExecutionException e) {
			Skript.exception(e);
		} catch (CancellationException | ThreadDeath e) {} // server shutting down
		return null;
	}

}
