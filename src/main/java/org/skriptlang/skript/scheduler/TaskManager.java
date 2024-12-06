package org.skriptlang.skript.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

	/**
	 * @return the registered {@link PlatformScheduler} currently running for Skript.
	 */
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

	/**
	 * Cancel all tasks.
	 */
	public static void cancelAll() {
		scheduler.cancelAll();
	}

	/**
	 * Submits a callable to the main server thread.
	 * This is depending on the scheduler, if Spigot, will run sync to the main server thread.
	 * 
	 * @param <T>
	 * @param callable The callable to execute and return result with.
	 * @return Future to handle execution or any thread errors.
	 * @throws Exception if the callable computation is unable to do so.
	 */
	public static <T> Future<T> submitSafely(Callable<T> callable) throws Exception {
		return scheduler.submitSafely(callable);
	}

}
