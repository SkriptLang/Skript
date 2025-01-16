package org.skriptlang.skript.util;

import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

// todo doc
public final class Task implements Executable<Event, Void>, Completable { // todo change to context with context api

	private final Object variables;
	private final Consumer<Event> runner;
	private final boolean autoComplete;

	private transient final CountDownLatch latch = new CountDownLatch(1);
	private transient volatile boolean ready;

	public Task(boolean autoComplete, Object variables, Consumer<Event> runner) {
		this.variables = variables;
		this.runner = runner;
		this.autoComplete = autoComplete;
	}

	@Override
	public Void execute(Event event, Object... arguments) {
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

	public Object variables() {
		return variables;
	}

	public Consumer<Event> runner() {
		return runner;
	}

	@Contract(pure = false)
	public boolean await(long timeout, TimeUnit unit) {
		try {
			if (this.ready) return true;
			return latch.await(timeout, unit);
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Contract(pure = false)
	public boolean await() {
		try {
			if (this.ready) return true;
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

	private static class TaskEvent extends Event {

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
