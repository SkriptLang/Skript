package org.skriptlang.skript.log.runtime;

import ch.njol.skript.Skript;
import ch.njol.skript.log.SkriptLogger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.log.runtime.Frame.FrameOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * A {@link RuntimeErrorConsumer} to be used in {@link RuntimeErrorManager} to catch {@link RuntimeError}s.
 * This should always be used with {@link #start()} and {@link #stop()}.
 */
public class RuntimeErrorCatcher implements RuntimeErrorConsumer, AutoCloseable {

	private List<RuntimeErrorConsumer> storedConsumers = new ArrayList<>();

	private final List<RuntimeError> cachedErrors = new ArrayList<>();

	// hard limit on stored errors to prevent a runaway loop from filling up memory, for example.
	private static final int ERROR_LIMIT = 1000;

	private boolean stopped = false;

	public RuntimeErrorCatcher() {}

	/**
	 * Gets the {@link RuntimeErrorManager}.
	 */
	private RuntimeErrorManager getManager() {
		return Skript.getRuntimeErrorManager();
	}

	@Override
	public @Nullable RuntimeErrorFilter getFilter() {
		return RuntimeErrorFilter.NO_FILTER; // no filter means everything gets printed.
	}

	/**
	 * Starts this {@link RuntimeErrorCatcher}, removing all {@link RuntimeErrorConsumer}s from {@link RuntimeErrorManager}
	 * and storing them in {@link #storedConsumers}.
	 * Makes this {@link RuntimeErrorCatcher} the only {@link RuntimeErrorConsumer} in {@link RuntimeErrorManager}
	 * to catch {@link RuntimeError}s.
	 * @return This {@link RuntimeErrorCatcher}
	 */
	public RuntimeErrorCatcher start() {
		stopped = false;
		storedConsumers = getManager().removeAllConsumers();
		getManager().addConsumer(this);
		return this;
	}

	/**
	 * Stops this {@link RuntimeErrorCatcher}, removing from {@link RuntimeErrorManager} and restoring the previous
	 * {@link RuntimeErrorConsumer}s from {@link #storedConsumers}. Does not clear cached errors. May be restarted.
	 */
	public void stop() {
		if (stopped)
			return;
		stopped = true;
		if (!getManager().removeConsumer(this)) {
			SkriptLogger.LOGGER.severe("[Skript] A 'RuntimeErrorCatcher' was stopped incorrectly.");
			return;
		}
		getManager().addConsumers(storedConsumers.toArray(RuntimeErrorConsumer[]::new));
	}

	/**
	 * Gets all the cached {@link RuntimeError}s.
	 */
	public @UnmodifiableView List<RuntimeError> getCachedErrors() {
		return Collections.unmodifiableList(cachedErrors);
	}

	/**
	 * Clear all cached {@link RuntimeError}s.
	 */
	public RuntimeErrorCatcher clearCachedErrors() {
		cachedErrors.clear();
		return this;
	}

	@Override
	public void printError(RuntimeError error) {
		if (cachedErrors.size() < ERROR_LIMIT)
			cachedErrors.add(error);
	}

	@Override
	public void printFrameOutput(FrameOutput output, Level level) {
		// do nothing, this won't be called since we have no filter.
	}

	/**
	 * Stops the catcher and clears the cached errors.
	 */
	@Override
	public void close() {
		this.clearCachedErrors().stop();
	}

}
