package org.skriptlang.skript.variables.storage.jdbc;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

/**
 * Decides when a monitored table's change log entries are old enough to prune. Entries are kept for
 * {@link #RETENTION_NANOS}, so a server that stops polling for less than that still sees every change when it catches up.
 * <p>
 * Versions don't say when they were saved, so it samples the version every 10 seconds, and prunes below the newest
 * sample that's over 5 minutes old. It only decides; the storage deletes. Times are {@link System#nanoTime()} values, passed in
 * so tests can choose them. Not thread-safe: the loading thread uses it first, then only the saving thread.
 */
final class LogPruner {

	/**
	 * How long log entries are kept.
	 */
	static final long RETENTION_NANOS = TimeUnit.MINUTES.toNanos(5);

	/**
	 * Returned by {@link #dueCutoff} when nothing should be pruned now.
	 */
	static final long NOT_DUE = -1;

	private static final long PRUNE_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos(1);
	private static final long SAMPLE_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(10);

	private record Sample(long nanos, long version) {}

	private final ArrayDeque<Sample> samples = new ArrayDeque<>(); // oldest first
	private long lastPruneNanos;

	/**
	 * @param now when the storage started, which counts as its last prune
	 */
	LogPruner(long now) {
		lastPruneNanos = now;
	}

	/**
	 * Notes a version the table had at a time, if 10 seconds have passed since the last sample.
	 */
	void sample(long now, long version) {
		Sample last = samples.peekLast();
		if (last == null || now - last.nanos() >= SAMPLE_INTERVAL_NANOS)
			samples.addLast(new Sample(now, version));
	}

	/**
	 * Decides whether to prune now, at most every minute.
	 *
	 * @return The version below which log entries are over 5 minutes old, or {@link #NOT_DUE}
	 */
	long dueCutoff(long now) {
		if (now - lastPruneNanos < PRUNE_INTERVAL_NANOS)
			return NOT_DUE;
		lastPruneNanos = now;
		// the newest sample over 5 minutes old; older ones are no use any more
		Sample cutoff = null;
		while (!samples.isEmpty() && now - samples.peekFirst().nanos() >= RETENTION_NANOS)
			cutoff = samples.pollFirst();
		if (cutoff == null)
			return NOT_DUE;
		samples.addFirst(cutoff); // kept, so the next prune still has a cutoff
		return cutoff.version();
	}

}
