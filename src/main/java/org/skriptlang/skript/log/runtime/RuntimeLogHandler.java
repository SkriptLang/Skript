package org.skriptlang.skript.log.runtime;

import ch.njol.skript.Skript;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.OpenCloseable;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public class RuntimeLogHandler implements OpenCloseable {

	private static final RuntimeErrorManager ERROR_MANAGER = RuntimeErrorManager.getInstance();

	public RuntimeLogHandler() {}

	private final List<RuntimeError> CACHED_ERRORS = new ArrayList<>();
	private boolean printedErrors = false;

	/**
	 * Starts the current {@link RuntimeLogHandler} to catch runtime errors.
	 */
	public RuntimeLogHandler start() {
		ERROR_MANAGER.startLogHandler(this);
		return this;
	}

	/**
	 * Stops the current {@link RuntimeLogHandler}
	 */
	public RuntimeLogHandler stop() {
		if (!printedErrors && Skript.testing())
			SkriptLogger.LOGGER.warning("Runtime log handler was not instructed to print anything.");
		ERROR_MANAGER.stopLogHandler(this);
		return this;
	}

	@Override
	public void open() {
		start();
	}

	@Override
	public void close() {
		stop();
	}

	/**
	 * Logs a {@link RuntimeError}
	 * @param error The {@link RuntimeError} to be logged
	 */
	public void logError(RuntimeError error) {
		CACHED_ERRORS.add(0, error);
	}

	/**
	 * Prints all logged {@link RuntimeError}s
	 * This handler is stopped if not already done.
	 */
	public void printErrors() {
		assert !printedErrors;
		printedErrors = true;
		stop();
		CACHED_ERRORS.forEach(ERROR_MANAGER::error);
		CACHED_ERRORS.clear();
	}

	/**
	 * Check if this {@link RuntimeLogHandler} is stopped and not logging {@link RuntimeError}s
	 * @return {@code true} if stopped
	 */
	public boolean isStopped() {
		return ERROR_MANAGER.isStopped(this);
	}

	/**
	 * Gets the {@link List} of the logged {@link RuntimeError}s
	 */
	public List<RuntimeError> getErrors() {
		return CACHED_ERRORS;
	}

	/**
	 * Clears all logged {@link RuntimeError}s
	 */
	public void clear() {
		CACHED_ERRORS.clear();
	}

	/**
	 * Create a backup of this {@link RuntimeLogHandler}
	 */
	@Contract("-> new")
	public RuntimeLogHandler backup() {
		RuntimeLogHandler copy = new RuntimeLogHandler();
		copy.CACHED_ERRORS.addAll(this.CACHED_ERRORS);
		copy.printedErrors = this.printedErrors;
		return copy;
	}

	/**
	 * Restore this {@link RuntimeLogHandler} to a backup
	 */
	public void restore(RuntimeLogHandler copy) {
		this.printedErrors = copy.printedErrors;
		this.CACHED_ERRORS.clear();
		this.CACHED_ERRORS.addAll(copy.CACHED_ERRORS);
	}

}
