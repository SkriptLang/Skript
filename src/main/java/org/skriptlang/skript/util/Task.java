package org.skriptlang.skript.util;

import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

// todo doc
public final class Task implements Executable<Event, Void>, Completable, Cancellable { // todo change to context with context api

	private transient final Object variables;
	private transient final Consumer<Event> runner;
	private final boolean autoComplete;

	private transient final CountDownLatch latch = new CountDownLatch(1);
	private volatile boolean started, ready, cancelled;
	/**
	 * A collection of tasks to be run when the task is shut down prematurely.
	 * This is used for cancelling parts of the task that are still in progress.
	 */
	private transient final List<Runnable> cancellationSteps;

	public Task(boolean autoComplete, Object variables, Consumer<Event> runner) {
		this.variables = variables;
		this.runner = runner;
		this.autoComplete = autoComplete;
		this.cancellationSteps = new ArrayList<>(8);
	}

	@Override
	public Void execute(Event event, Object... arguments) {
		if (this.markStarted())
			return null; // Don't restart old tasks
		TaskEvent here = new TaskEvent(this);
		Variables.setLocalVariables(here, variables);
		try {
			this.runner.accept(here);
		} finally {
			if (autoComplete)
				this.complete();
			Variables.removeLocals(here);
		}
		return null;
	}

	/**
	 * Marks this task as having been started.
	 * If the task has previously been started, it should be aborted here.
	 *
	 * @return Whether to abort the task
	 */
	private synchronized boolean markStarted() {
		if (started)
			return true;
		this.started = true;
		return false;
	}

	public Object variables() {
		return variables;
	}

	public Consumer<Event> runner() {
		return runner;
	}

	public void scheduleCancellationStep(Runnable runnable) {
		synchronized (cancellationSteps) {
			this.cancellationSteps.add(runnable);
		}
	}

	@Contract(pure = false)
	public boolean await(long timeout, TimeUnit unit) {
		try {
			if (ready || cancelled) return true;
			return latch.await(timeout, unit);
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Contract(pure = false)
	public boolean await() {
		try {
			if (ready || cancelled) return true;
			this.latch.await();
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Override
	public void complete() {
		this.ready = true;
		this.latch.countDown();
	}

	@Override
	public boolean isComplete() {
		return ready;
	}

	@Override
	public void cancel() {
		if (cancelled)
			return;
		this.cancelled = true;
		this.latch.countDown();
		this.cancellationSteps.forEach(Runnable::run);
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	public static class TaskEvent extends Event {

		private final Task task;

		public TaskEvent(Task task) {
			this.task = task;
		}

		public Task task() {
			return task;
		}

		@Override
		@NotNull
		public HandlerList getHandlers() {
			throw new IllegalStateException();
		}

	}

}
