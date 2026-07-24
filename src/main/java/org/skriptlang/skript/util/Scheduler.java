package org.skriptlang.skript.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class Scheduler {

	private final JavaPlugin plugin;

	public Scheduler(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@NotNull
	public ScheduledTask runGlobalTask(@NotNull Runnable runnable) {
		return Bukkit.getServer().getGlobalRegionScheduler().run(
			plugin,
			scheduledTask -> runnable.run()
		);
	}

	@NotNull
	public ScheduledTask runGlobalDelayedTask(@NotNull Runnable runnable, long delayTicks) {
		return Bukkit.getServer().getGlobalRegionScheduler().runDelayed(
			plugin,
			scheduledTask -> runnable.run(),
			delayTicks
		);
	}

	@NotNull
	public ScheduledTask runGlobalRepeatingTask(@NotNull Runnable runnable, long initialDelayTicks, long periodTicks) {
		return Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
			plugin,
			scheduledTask -> runnable.run(),
			initialDelayTicks,
			periodTicks
		);
	}

	@NotNull
	public ScheduledTask runRegionTask(@NotNull Location location, @NotNull Runnable runnable) {
		return Bukkit.getServer().getRegionScheduler().run(
			plugin,
			location,
			scheduledTask -> runnable.run()
		);
	}

	@NotNull
	public ScheduledTask runRegionDelayedTask(@NotNull Location location, @NotNull Runnable runnable, long delayTicks) {
		return Bukkit.getServer().getRegionScheduler().runDelayed(
			plugin,
			location,
			scheduledTask -> runnable.run(),
			delayTicks
		);
	}

	@NotNull
	public ScheduledTask runRegionRepeatingTask(@NotNull Location location, @NotNull Runnable runnable, long initialDelayTicks, long periodTicks) {
		return Bukkit.getServer().getRegionScheduler().runAtFixedRate(
			plugin,
			location,
			scheduledTask -> runnable.run(),
			initialDelayTicks,
			periodTicks
		);
	}

	@NotNull
	public ScheduledTask runAsyncTaskNow(@NotNull Runnable runnable) {
		return Bukkit.getServer().getAsyncScheduler().runNow(
			plugin,
			scheduledTask -> runnable.run()
		);
	}

	@NotNull
	public ScheduledTask runAsyncDelayedTask(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
		return Bukkit.getServer().getAsyncScheduler().runDelayed(
			plugin,
			scheduledTask -> runnable.run(),
			delay,
			unit
		);
	}

	@NotNull
	public ScheduledTask runAsyncRepeatingTask(@NotNull Runnable runnable, long initialDelay, long period, @NotNull TimeUnit unit) {
		return Bukkit.getServer().getAsyncScheduler().runAtFixedRate(
			plugin,
			scheduledTask -> runnable.run(),
			initialDelay,
			period,
			unit
		);
	}

	@Nullable
	public ScheduledTask runEntityTask(@NotNull Entity entity, @NotNull Runnable runnable, @Nullable Runnable retired) {
		return entity.getScheduler().run(
			plugin,
			scheduledTask -> runnable.run(),
			retired
		);
	}

	@Nullable
	public ScheduledTask runEntityDelayedTask(@NotNull Entity entity, @NotNull Runnable runnable, @Nullable Runnable retired, long delayTicks) {
		return entity.getScheduler().runDelayed(
			plugin,
			scheduledTask -> runnable.run(),
			retired,
			delayTicks
		);
	}

	@Nullable
	public ScheduledTask runEntityRepeatingTask(@NotNull Entity entity, @NotNull Runnable runnable, @Nullable Runnable retired, long initialDelayTicks, long periodTicks) {
		return entity.getScheduler().runAtFixedRate(
			plugin,
			scheduledTask -> runnable.run(),
			retired,
			initialDelayTicks,
			periodTicks
		);
	}

	public void cancelAllTasks() {
		cancelAllGlobalTasks();
		cancelAllAsyncTasks();
	}

	public void cancelAllGlobalTasks() {
		Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
	}

	public void cancelAllAsyncTasks() {
		Bukkit.getAsyncScheduler().cancelTasks(plugin);
	}
}
