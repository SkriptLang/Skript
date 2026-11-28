package org.skriptlang.skript.variables.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.skriptlang.skript.variables.storage.TickBudget.*;

/**
 * Tests {@link TickBudget}. Times are in milliseconds here, and converted with {@link #ms(double)}.
 */
public class TickBudgetTest {

	private static final long TICK = ms(50);

	private static long ms(double millis) {
		return (long) (millis * 1_000_000);
	}

	@Test
	public void aQuietServerGetsTheMostTime() {
		assertEquals(MAX_NANOS, budget(0, ms(25), TICK, ms(25), false));
	}

	@Test
	public void aBusyServerGetsHalfOfWhatsLeft() {
		assertEquals(ms(4), budget(0, ms(42), TICK, ms(8), false));
	}

	@Test
	public void aServerThatsBehindStillGetsTheLeastTime() {
		assertEquals(MIN_NANOS, budget(0, ms(60), TICK, ms(-10), false));
		assertEquals(MIN_NANOS, budget(0, ms(49.5), TICK, ms(0.5), false));
	}

	@Test
	public void timeUsedByEarlierListenersIsCounted() {
		// the event said 25 ms were left, but that was before the listeners before us ran
		assertEquals(ms(2.5), budget(0, ms(45), TICK, ms(25), false));
	}

	@Test
	public void aServerCatchingUpUsesItsOwnSchedule() {
		// the next tick is due earlier than one tick after this one started
		assertEquals(ms(1.5), budget(0, ms(25), TICK, ms(3), false));
	}

	@Test
	public void anUnknownTickStartOnlyUsesTheEvent() {
		assertEquals(MAX_NANOS, budget(UNKNOWN, ms(25), TICK, ms(30), false));
		assertEquals(ms(3), budget(UNKNOWN, ms(25), TICK, ms(6), false));
	}

	@Test
	public void theTickRateIsFollowed() {
		// at 40 ticks per second, a tick is 25 ms
		assertEquals(ms(5), budget(0, ms(15), ms(25), ms(10), false));
		assertEquals(ms(2), budget(0, ms(21), ms(25), ms(10), false));
	}

	@Test
	public void aSprintingServerGetsTheLeastTime() {
		assertEquals(MIN_NANOS, budget(0, ms(1), TICK, ms(49), true));
		assertEquals(MIN_NANOS, budget(UNKNOWN, 0, TICK, ms(49), true));
	}

	@Test
	public void reportsGetHalfOfWhatsLeftWithNoLimit() {
		assertEquals(ms(20), reportBudget(0, ms(10), TICK, ms(40), false));
		assertEquals(ms(12.5), reportBudget(0, ms(25), TICK, ms(25), false)); // more than the refresh's most
	}

	@Test
	public void reportsAlwaysGetTheirLeast() {
		assertEquals(REPORT_MIN_NANOS, reportBudget(0, ms(42), TICK, ms(8), false));
		assertEquals(REPORT_MIN_NANOS, reportBudget(0, ms(60), TICK, ms(-10), false));
		assertEquals(REPORT_MIN_NANOS, reportBudget(0, ms(1), TICK, ms(49), true)); // sprinting
	}

	@Test
	public void reportsCountTimeUsedByEarlierListeners() {
		assertEquals(ms(10), reportBudget(0, ms(30), TICK, ms(45), false));
		assertEquals(ms(15), reportBudget(UNKNOWN, ms(30), TICK, ms(30), false));
	}

	@Test
	public void nanoTimeOverflowingDoesntMatter() {
		long start = Long.MAX_VALUE - ms(10);
		long now = start + ms(20); // overflows
		assertEquals(MAX_NANOS, budget(start, now, TICK, ms(30), false));
		assertEquals(ms(3), budget(start, start + ms(44), TICK, ms(30), false));
	}

}
