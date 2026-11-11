package org.skriptlang.skript.log.runtime;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.log.runtime.Frame.FrameLimit;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Handles passing runtime errors between producers and consumers via a frame collection system.
 * <br>
 * The manager should be treated as a singleton and accessed via {@link #getInstance()}
 * or {@link Skript#getRuntimeErrorManager()}. Changing the frame length or limits requires edits to the
 * {@link SkriptConfig} values and a call to {@link #refresh()}. Reloading the config will automatically
 * call {@link #refresh()}.
 *
 * @see RuntimeErrorConsumer
 * @see RuntimeErrorProducer
 * @see Frame
 */
public class RuntimeErrorManager implements Closeable {

	private static RuntimeErrorManager instance;

	/**
	 * Prefer using {@link Skript#getRuntimeErrorManager()} instead.
	 * @return The singleton instance of the runtime error manager.
	 */
	@ApiStatus.Internal
	public static RuntimeErrorManager getInstance() {
		return instance;
	}

	static RuntimeErrorFilter standardFilter;

	/**
	 * Refreshes the runtime error manager for Skript, pulling from the config values.
	 * Tracked consumers are maintained during refreshes.
	 */
	public static void refresh() {
		long frameLength = SkriptConfig.runtimeErrorFrameDuration.value().getAs(Timespan.TimePeriod.TICK);
		if (instance == null) {
			instance = new RuntimeErrorManager(frameLength);
		} else {
			var oldMap = instance.filterMap;
			instance = new RuntimeErrorManager(frameLength);
			instance.filterMap.putAll(oldMap);
		}

		int errorLimit = SkriptConfig.runtimeErrorLimitTotal.value();
		int errorLineLimit = SkriptConfig.runtimeErrorLimitLine.value();
		int errorLineTimeout = SkriptConfig.runtimeErrorLimitLineTimeout.value();
		int errorTimeoutLength = Math.max(SkriptConfig.runtimeErrorTimeoutDuration.value(), 1);
		FrameLimit errorLimits = new FrameLimit(errorLimit, errorLineLimit, errorLineTimeout, errorTimeoutLength);

		int warningLimit = SkriptConfig.runtimeWarningLimitTotal.value();
		int warningLineLimit = SkriptConfig.runtimeWarningLimitLine.value();
		int warningLineTimeout = SkriptConfig.runtimeWarningLimitLineTimeout.value();
		int warningTimeoutLength = Math.max(SkriptConfig.runtimeWarningTimeoutDuration.value(), 1);
		FrameLimit warningsLimits = new FrameLimit(warningLimit, warningLineLimit, warningLineTimeout, warningTimeoutLength);

		if (standardFilter == null) {
			standardFilter = new RuntimeErrorFilter(errorLimits, warningsLimits);
		} else {
			standardFilter.setErrorFrameLimits(errorLimits);
			standardFilter.setWarningFrameLimits(warningsLimits);
		}
	}

	private final Task task;

	private final Map<RuntimeErrorFilter, Set<RuntimeErrorConsumer>> filterMap = new ConcurrentHashMap<>();

	/**
	 * Creates a new error manager, which also creates its own frames.
	 * <br>
	 * Must be closed when no longer being used.
	 *
	 * @param frameLength The length of a frame in ticks.
	 */
	public RuntimeErrorManager(long frameLength) {
		task = new Task(Skript.getInstance(), frameLength, frameLength, true) {
			@Override
			public void run() {
				for (var entry : filterMap.entrySet()) {
					RuntimeErrorFilter filter = entry.getKey();
					if (filter == null)
						continue;
					Set<RuntimeErrorConsumer> consumers = entry.getValue();

					Frame errorFrame = filter.getErrorFrame();
					consumers.forEach(consumer -> consumer.printFrameOutput(errorFrame.getFrameOutput(), Level.SEVERE));
					errorFrame.nextFrame();

					Frame warningFrame = filter.getErrorFrame();
					consumers.forEach(consumer -> consumer.printFrameOutput(warningFrame.getFrameOutput(), Level.WARNING));
					warningFrame.nextFrame();
				}
			}
		};
	}

	/**
	 * Emits a warning or error depending on severity.
	 * @param error The error to emit.
	 */
	public void error(@NotNull RuntimeError error) {
		for (var entry : filterMap.entrySet()) {
			RuntimeErrorFilter filter = entry.getKey();
			Set<RuntimeErrorConsumer> consumers = entry.getValue();
			if (filter == null || filter.test(error)){
				consumers.forEach((consumer -> consumer.printError(error)));
			}
		}
	}

	/**
	 * @return The frame containing emitted errors.
	 * @deprecated {@link RuntimeErrorFilter#getErrorFrame()}
	 */
	@Deprecated(since="2.13", forRemoval = true)
	public Frame getErrorFrame() {
		return standardFilter.getErrorFrame();
	}

	/**
	 * @return The frame containing emitted warnings.
	 * @deprecated {@link RuntimeErrorFilter#getWarningFrame()}
	 */
	@Deprecated(since="2.13", forRemoval = true)
	public Frame getWarningFrame() {
		return standardFilter.getWarningFrame();
	}

	/**
	 * Adds a {@link RuntimeErrorConsumer} that will receive the emitted errors and frame output data.
	 * Consumers will be maintained when the manager is refreshed.
	 * @param consumer The consumer to add.
	 */
	public void addConsumer(RuntimeErrorConsumer consumer) {
		synchronized (filterMap) {
			filterMap.computeIfAbsent(consumer.getFilter(), key -> new HashSet<>()).add(consumer);
		}
	}

	/**
	 * Adds multiple {@link RuntimeErrorConsumer}s that will receive the emitted errors and frame output data.
	 * Consumers will be maintained when the manager is refreshed.
	 * @param newConsumers The {@link RuntimeErrorConsumer}s to add.
	 */
	public void addConsumers(RuntimeErrorConsumer... newConsumers) {
		synchronized (filterMap) {
			for (var consumer : newConsumers) {
				filterMap.computeIfAbsent(consumer.getFilter(), key -> new HashSet<>()).add(consumer);
			}
		}
	}

	/**
	 * Removes a {@link RuntimeErrorConsumer} from the tracked list.
	 * @param consumer The consumer to remove.
	 * @return {@code true} If the {@code consumer} was removed.
	 */
	public boolean removeConsumer(RuntimeErrorConsumer consumer) {
		synchronized (filterMap) {
			var set = filterMap.get(consumer.getFilter());
			if (set == null)
				 return false;
			boolean removed = set.remove(consumer);
			if (set.isEmpty())
				filterMap.remove(consumer.getFilter());
			return removed;
		}
	}

	/**
	 * Removes all {@link RuntimeErrorConsumer}s that receive emitted errors and frame output data.
	 * @return All {@link RuntimeErrorConsumer}s removed.
	 */
	public List<RuntimeErrorConsumer> removeAllConsumers() {
		synchronized (filterMap) {
			List<RuntimeErrorConsumer> currentConsumers = new ArrayList<>();
			for (var set : filterMap.values())
				currentConsumers.addAll(set);
			filterMap.clear();
			return currentConsumers;
		}
	}

	@Override
	public void close() {
		task.close();
	}

}
