package org.skriptlang.skript.scheduler.platforms;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ch.njol.skript.Skript;

import com.google.common.collect.MapMaker;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.skriptlang.skript.scheduler.AsyncTask;
import org.skriptlang.skript.scheduler.PlatformScheduler;
import org.skriptlang.skript.scheduler.Task;

public class SpigotScheduler implements PlatformScheduler {

	private static final ConcurrentMap<Integer, AsyncTask> asyncTasks = new MapMaker().weakKeys().weakValues().makeMap();
	private static final ConcurrentMap<Integer, Task> tasks = new MapMaker().weakKeys().weakValues().makeMap();

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
	public void cancelAll() {
		Plugin plugin = Skript.getInstance();
		asyncTasks.values().stream().filter(task -> task.getPlugin().equals(plugin)).forEach(task -> this.cancel(task));
		tasks.values().stream().filter(task -> task.getPlugin().equals(plugin)).forEach(task -> this.cancel(task));
	}

	@Override
	public <T> Future<T> submitSafely(Callable<T> callable) throws Exception {
		if (Bukkit.isPrimaryThread()) {
			return CompletableFuture.completedFuture(callable.call());
		}
		return Bukkit.getScheduler().callSyncMethod(Skript.getInstance(), callable);
	}

}
