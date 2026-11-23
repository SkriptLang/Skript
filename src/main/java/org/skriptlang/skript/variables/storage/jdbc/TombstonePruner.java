package org.skriptlang.skript.variables.storage.jdbc;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

/**
 * Decides when a shared table's tombstones are old enough to prune. Tombstones are kept for {@link #RETENTION_NANOS},
 * so a server that stops polling for less than that still sees every deletion when it catches up.
 * <p>
 * Versions don't say when they were saved, so it samples the version every minute, and prunes below the newest sample
 * that's over an hour old. It only decides; the storage deletes. Times are {@link System#nanoTime()} values, passed in
 * so tests can choose them. Not thread-safe: the loading thread uses it first, then only the saving thread.
 */
final class TombstonePruner {

	/**
	 * How long tombstones are kept.
	 */
	static final long RETENTION_NANOS = TimeUnit.HOURS.toNanos(1);

	/**
	 * Returned by {@link #dueCutoff} when nothing should be pruned now.
	 */
	static final long NOT_DUE = -1;

	private static final long PRUNE_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos(5);
	private static final long SAMPLE_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos(1);

	private record Sample(long nanos, long version) {}

	private final ArrayDeque<Sample> samples = new ArrayDeque<>(); // oldest first
	private long lastPruneNanos;

	/**
	 * @param now when the storage started, which counts as its last prune
	 */
	TombstonePruner(long now) {
		lastPruneNanos = now;
	}

	/**
	 * Notes a version the table had at a time, if a minute has passed since the last sample.
	 */
	void sample(long now, long version) {
		Sample last = samples.peekLast();
		if (last == null || now - last.nanos() >= SAMPLE_INTERVAL_NANOS)
			samples.addLast(new Sample(now, version));
	}

	/**
	 * Decides whether to prune now, at most every 5 minutes.
	 *
	 * @return The version below which tombstones are over an hour old, or {@link #NOT_DUE}
	 */
	long dueCutoff(long now) {
		if (now - lastPruneNanos < PRUNE_INTERVAL_NANOS)
			return NOT_DUE;
		lastPruneNanos = now;
		// the newest sample over an hour old; older ones are no use any more
		Sample cutoff = null;
		while (!samples.isEmpty() && now - samples.peekFirst().nanos() >= RETENTION_NANOS)
			cutoff = samples.pollFirst();
		if (cutoff == null)
			return NOT_DUE;
		samples.addFirst(cutoff); // kept, so the next prune still has a cutoff
		return cutoff.version();
	}

}
