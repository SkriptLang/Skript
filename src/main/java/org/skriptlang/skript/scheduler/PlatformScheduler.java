package org.skriptlang.skript.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Task for a dedicated platform like Sponge, Paper, Bukkit or Folia.
 * See {@link TaskManager} for detailed Javadoc mirrored methods.
 */
public interface PlatformScheduler {

	void run(Task task, long delayInTicks);

	boolean cancel(Task task);

	boolean isAlive(Task task);

	void run(AsyncTask task, long delay, TimeUnit unit);

	boolean cancel(AsyncTask task);

	boolean isAlive(AsyncTask task);

	void cancelAll();

	<T> Future<T> submitSafely(Callable<T> callable) throws Exception;

}
