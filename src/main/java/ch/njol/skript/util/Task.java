package ch.njol.skript.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.util.Closeable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("removal")
public abstract class Task implements Runnable, Closeable {

	private final boolean async;
	private final Plugin plugin;
	
	private boolean useScriptLoaderExecutor;
	private long period = -1;
	private int taskID = -1;

	/**
	 * Creates a new task that will run after the given delay and then repeat every period ticks.
	 * <p>
	 * @param plugin The plugin that owns this task.
	 * @param delay Delay in ticks before the task is run for the first time.
	 * @param period Period in ticks between subsequent executions of the task.
	 */
	public Task(Plugin plugin, long delay, long period) {
		this(plugin, delay, period, false);
	}

	/**
	 * Creates a new task that will run after the given delay and then repeat every period ticks optionally asyncronously.
	 * <p>
	 * @param plugin The plugin that owns this task.
	 * @param delay Delay in ticks before the task is run for the first time.
	 * @param period Period in ticks between subsequent executions of the task.
	 * @param async Whether to run the task asynchronously
	 */
	public Task(Plugin plugin, long delay, long period, boolean async) {
		this.plugin = plugin;
		this.period = period;
		this.async = async;
		schedule(delay);
	}

	/**
	 * Creates a new task that will run after the given delay.
	 * <p>
	 * @param plugin The plugin that owns this task.
	 * @param delay Delay in ticks before the task is run for the first time.
	 */
	public Task(Plugin plugin, long delay) {
		this(plugin, false, delay, false);
	}

	/**
	 * Creates a new task that will run optionally on the script loader executor.
	 * <p>
	 * @param plugin The plugin that owns this task.
	 * @param useScriptLoaderExecutor Whether to use the script loader executor. Setting is based on the config.sk user setting.
	 */
	public Task(Plugin plugin, boolean useScriptLoaderExecutor) {
		this(plugin, useScriptLoaderExecutor, 0, false);
	}

	/**
	 * Creates a new task that will run after the given delay and optionally asynchronously.
	 * <p>
	 * @param plugin The plugin that owns this task.
	 * @param delay Delay in ticks before the task is run for the first time.
	 * @param async Whether to run the task asynchronously
	 */
	public Task(Plugin plugin, long delay, boolean async) {
		this(plugin, delay, -1, async);
	}

	/**
	 * Creates a new task that will run optionally on the script loader executor and after a delay.
	 * <p>
	 * @param plugin The plugin that owns this task.
	 * @param useScriptLoaderExecutor Whether to use the script loader executor. Setting is based on the config.sk user setting.
	 * @param delay Delay in ticks before the task is run for the first time.
	 */
	public Task(Plugin plugin, boolean useScriptLoaderExecutor, long delay) {
		this(plugin, useScriptLoaderExecutor, delay, false);
	}

	// Private because async and useScriptLoaderExecutor contradict each other, as the script loader executor may be asynchronous.
	private Task(Plugin plugin, boolean useScriptLoaderExecutor, long delay, boolean async) {
		this.useScriptLoaderExecutor = useScriptLoaderExecutor;
		this.plugin = plugin;
		this.async = async;
		schedule(delay);
	}

	/**
	 * Only call this if the task is not alive.
	 *
	 * @param delay
	 */
	private void schedule(final long delay) {
		assert !isAlive();
		if (!Skript.getInstance().isEnabled())
			return;
		if (useScriptLoaderExecutor) {
			Executor executor = ScriptLoader.getExecutor();
			if (delay > 0) {
				taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> executor.execute(this), delay);
			} else {
				executor.execute(this);
			}
		} else {
			if (period == -1) {
				if (async) {
					taskID = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, delay).getTaskId();
				} else {
					taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
				}
			} else {
				if (async) {
					taskID = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, delay, period).getTaskId();
				} else {
					taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, delay, period);
				}
			}
			assert taskID != -1;
		}
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
		} catch (final CancellationException e) {}
		return null;
	}

}
