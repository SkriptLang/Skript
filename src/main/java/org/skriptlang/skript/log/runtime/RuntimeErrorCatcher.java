package org.skriptlang.skript.log.runtime;

import ch.njol.skript.log.SkriptLogger;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.log.runtime.Frame.FrameOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * A {@link RuntimeErrorConsumer} to be used in {@link RuntimeErrorManager} to catch {@link RuntimeError}s.
 * This should always be used with {@link #start()} and {@link #stop()}.
 */
public class RuntimeErrorCatcher implements RuntimeErrorConsumer {

	private List<RuntimeErrorConsumer> storedConsumers = new ArrayList<>();

	private final List<RuntimeError> cachedErrors = new ArrayList<>();

	private final List<Entry<FrameOutput, Level>> cachedFrames = new ArrayList<>();

	public RuntimeErrorCatcher() {}

	/**
	 * Gets the {@link RuntimeErrorManager}.
	 */
	private RuntimeErrorManager getManager() {
		return RuntimeErrorManager.getInstance();
	}

	/**
	 * Starts this {@link RuntimeErrorCatcher}, removing all {@link RuntimeErrorConsumer}s from {@link RuntimeErrorManager}
	 * and storing them in {@link #storedConsumers}.
	 * Makes this {@link RuntimeErrorCatcher} the only {@link RuntimeErrorConsumer} in {@link RuntimeErrorManager}
	 * to catch {@link RuntimeError}s.
	 * @return This {@link RuntimeErrorCatcher}
	 */
	public RuntimeErrorCatcher start() {
		storedConsumers = getManager().removeAllConsumers();
		getManager().addConsumer(this);
		return this;
	}

	/**
	 * Stops this {@link RuntimeErrorCatcher}, removing from {@link RuntimeErrorManager} and restoring the previous
	 * {@link RuntimeErrorConsumer}s from {@link #storedConsumers}.
	 * Prints all cached {@link RuntimeError}s, {@link #cachedErrors}, and cached {@link FrameOutput}s, {@link #cachedFrames}.
	 */
	public void stop() {
		if (!getManager().removeConsumer(this)) {
			SkriptLogger.LOGGER.severe("[Skript] A 'RuntimeErrorCatcher' was stopped incorrectly.");
			return;
		}
		getManager().addConsumers(storedConsumers.toArray(RuntimeErrorConsumer[]::new));
		for (RuntimeError runtimeError : cachedErrors)
			storedConsumers.forEach(consumer -> consumer.printError(runtimeError));
		for (Entry<FrameOutput, Level> entry : cachedFrames)
			storedConsumers.forEach(consumer -> consumer.printFrameOutput(entry.getKey(), entry.getValue()));
	}

	/**
	 * Gets all the cached {@link RuntimeError}s.
	 */
	public @UnmodifiableView List<RuntimeError> getCachedErrors() {
		return Collections.unmodifiableList(cachedErrors);
	}

	/**
	 * Gets all cached {@link FrameOutput}s stored with its corresponding {@link Level} in an {@link Entry}
	 */
	public @UnmodifiableView List<Entry<FrameOutput, Level>> getCachedFrames() {
		return Collections.unmodifiableList(cachedFrames);
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
