package org.skriptlang.skript.log.runtime;

import ch.njol.skript.log.SkriptLogger;
import org.skriptlang.skript.log.runtime.Frame.FrameOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

public class RuntimeErrorCatcher implements RuntimeErrorConsumer {

	private static final RuntimeErrorManager ERROR_MANAGER = RuntimeErrorManager.getInstance();

	private List<RuntimeErrorConsumer> storedConsumers = new ArrayList<>();

	private final List<RuntimeError> cachedErrors = new ArrayList<>();

	private final List<Entry<FrameOutput, Level>> cachedFrames = new ArrayList<>();

	public RuntimeErrorCatcher() {}

	/**
	 * Starts this {@link RuntimeErrorCatcher}, removing all {@link RuntimeErrorConsumer}s from {@link RuntimeErrorManager}
	 * and storing them in {@link #storedConsumers}.
	 * Makes this {@link RuntimeErrorCatcher} the only {@link RuntimeErrorConsumer} in {@link RuntimeErrorManager}
	 * to catch {@link RuntimeError}s.
	 * @return This {@link RuntimeErrorCatcher}
	 */
	public RuntimeErrorCatcher start() {
		storedConsumers = ERROR_MANAGER.removeAllConsumers();
		ERROR_MANAGER.addConsumer(this);
		return this;
	}

	/**
	 * Stops this {@link RuntimeErrorCatcher}, removing from {@link RuntimeErrorManager} and restoring the previous
	 * {@link RuntimeErrorConsumer}s from {@link #storedConsumers}.
	 * Prints all cached {@link RuntimeError}s, {@link #cachedErrors}, and cached {@link FrameOutput}s, {@link #cachedFrames}.
	 */
	public void stop() {
		if (!ERROR_MANAGER.removeConsumer(this))
			SkriptLogger.LOGGER.severe("[Skript] A 'RuntimeErrorCatcher' was stopped incorrectly.");
		ERROR_MANAGER.addConsumers(storedConsumers.toArray(RuntimeErrorConsumer[]::new));
		for (RuntimeError runtimeError : cachedErrors)
			storedConsumers.forEach(consumer -> consumer.printError(runtimeError));
		for (Entry<FrameOutput, Level> entry : cachedFrames)
			storedConsumers.forEach(consumer -> consumer.printFrameOutput(entry.getKey(), entry.getValue()));
	}

	/**
	 * Gets all the cached {@link RuntimeError}s.
	 */
	public List<RuntimeError> getCachedErrors() {
		return cachedErrors;
	}

	/**
	 * Clear all cached {@link RuntimeError}s.
	 */
	public RuntimeErrorCatcher clearCachedErrors() {
		cachedErrors.clear();
		return this;
	}

	/**
	 * Clears all cached {@link FrameOutput}s.
	 */
	public RuntimeErrorCatcher clearCachedFrames() {
		cachedFrames.clear();
		return this;
	}

	@Override
	public void printError(RuntimeError error) {
		cachedErrors.add(error);
	}

	@Override
	public void printFrameOutput(FrameOutput output, Level level) {
		cachedFrames.add(new Entry<FrameOutput, Level>() {
			@Override
			public FrameOutput getKey() {
				return output;
			}

			@Override
			public Level getValue() {
				return level;
			}

			@Override
			public Level setValue(Level value) {
				return null;
			}
		});
	}

}
