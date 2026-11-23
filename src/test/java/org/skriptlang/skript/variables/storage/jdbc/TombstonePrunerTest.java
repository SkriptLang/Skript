package org.skriptlang.skript.variables.storage.jdbc;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TombstonePrunerTest {

	private static final long MINUTE = TimeUnit.MINUTES.toNanos(1);
	private static final long START = 1_000_000_000L;

	private final TombstonePruner pruner = new TombstonePruner(START);

	private static long at(long minutes) {
		return START + minutes * MINUTE;
	}

	@Test
	public void nothingIsPrunedInTheFirstHour() {
		pruner.sample(at(0), 1);
		assertEquals(TombstonePruner.NOT_DUE, pruner.dueCutoff(at(10)));
		assertEquals(TombstonePruner.NOT_DUE, pruner.dueCutoff(at(59)));
	}

	@Test
	public void prunesBelowTheNewestSampleOverAnHourOld() {
		pruner.sample(at(0), 1);
		pruner.sample(at(5), 2);
		pruner.sample(at(20), 3);
		assertEquals(2, pruner.dueCutoff(at(70)));
	}

	@Test
	public void prunesAtMostEveryFiveMinutes() {
		pruner.sample(at(0), 1);
		assertEquals(1, pruner.dueCutoff(at(60)));
		assertEquals(TombstonePruner.NOT_DUE, pruner.dueCutoff(at(64)));
		assertEquals(1, pruner.dueCutoff(at(65))); // the cutoff is kept for the next prune
	}

	@Test
	public void theNextPruneStartsWhereAFinishedOneStopped() {
		assertEquals(0, pruner.prunedBelow());
		pruner.prunedBelow(5);
		pruner.prunedBelow(3); // never goes back
		assertEquals(5, pruner.prunedBelow());
	}

	@Test
	public void samplesAtMostEveryMinute() {
		pruner.sample(at(0), 1);
		pruner.sample(at(0) + MINUTE / 2, 2); // too soon, so not kept
		assertEquals(1, pruner.dueCutoff(at(61)));
	}

}
