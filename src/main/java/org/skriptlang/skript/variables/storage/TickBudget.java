package org.skriptlang.skript.variables.storage;

import java.util.concurrent.TimeUnit;

/**
 * Decides how long the end of a tick may spend on variables: on reported changes left over from earlier in the tick,
 * then on the variable refresh. Each gets half the time left before the next tick. Only half, since other work still
 * may run after us, like other plugins' tick end listeners and the tasks the server runs between ticks.
 * The refresh's share is kept between {@link #MIN_NANOS} and {@link #MAX_NANOS}, and the reports' is at least
 * {@link #REPORT_MIN_NANOS}, with no limit, so a backlog of them clears as fast as the server has time for.
 * <p>
 * The time left is the smaller of two estimates, since each is only an upper bound:
 * <ul>
 *     <li>the server's own, from the tick end event, which follows its real schedule but was measured before
 *     the listeners before us ran;</li>
 *     <li>ours, from when the tick started and the tick rate, which includes those listeners but assumes the next
 *     tick is due one tick after this one started, which isn't the case while the server catches up.</li>
 * </ul>
 */
final class TickBudget {

	/**
	 * The least time the refresh gets each tick, even when the server is behind, so it always gets done.
	 */
	static final long MIN_NANOS = TimeUnit.MICROSECONDS.toNanos(500);

	/**
	 * The most time the refresh gets each tick, however much time is left.
	 */
	static final long MAX_NANOS = TimeUnit.MILLISECONDS.toNanos(5);

	/**
	 * The least time reported changes get each tick, even when the server is behind, so a backlog always shrinks.
	 * As much as they got before they could use the time left in a tick.
	 */
	static final long REPORT_MIN_NANOS = TimeUnit.MILLISECONDS.toNanos(5);

	/**
	 * Passed as the tick start when it isn't known, like in the first tick after the refresh starts.
	 */
	static final long UNKNOWN = Long.MIN_VALUE;

	private TickBudget() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Decides how long the refresh may run now, at the end of a tick.
	 *
	 * @param tickStart      {@link System#nanoTime()} when this tick started, or {@link #UNKNOWN}
	 * @param now            {@link System#nanoTime()} now
	 * @param tickNanos      how long a tick is at the current tick rate
	 * @param eventRemaining the time the tick end event says is left before the next tick. Negative when behind
	 * @param sprinting      whether the server is sprinting, so ticks run back to back
	 * @return how long the refresh may run now, in nanoseconds
	 */
	static long budget(long tickStart, long now, long tickNanos, long eventRemaining, boolean sprinting) {
		if (sprinting)
			return MIN_NANOS;
		return Math.clamp(remaining(tickStart, now, tickNanos, eventRemaining) / 2, MIN_NANOS, MAX_NANOS);
	}

	/**
	 * Decides how long reported changes left over from earlier in the tick may be applied now, at the end of a tick.
	 * Takes the same arguments as {@link #budget}.
	 *
	 * @return how long the reports may run now, in nanoseconds
	 */
	static long reportBudget(long tickStart, long now, long tickNanos, long eventRemaining, boolean sprinting) {
		if (sprinting)
			return REPORT_MIN_NANOS;
		return Math.max(remaining(tickStart, now, tickNanos, eventRemaining) / 2, REPORT_MIN_NANOS);
	}

	/**
	 * @return the time left before the next tick: the smaller of the two estimates. Negative when behind
	 */
	private static long remaining(long tickStart, long now, long tickNanos, long eventRemaining) {
		long remaining = eventRemaining;
		if (tickStart != UNKNOWN) // nanoTime values may overflow, so they're only ever compared by their difference
			remaining = Math.min(remaining, tickNanos - (now - tickStart));
		return remaining;
	}

}
