package org.skriptlang.skript.variables.storage.jdbc;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class LogPrunerTest {

	private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
	private static final long START = 1_000_000_000L;

	private final LogPruner pruner = new LogPruner(START);

	private static long at(long seconds) {
		return START + seconds * SECOND;
	}

	@Test
	public void nothingIsPrunedInTheFirstFiveMinutes() {
		pruner.sample(at(0), 1);
		assertEquals(LogPruner.NOT_DUE, pruner.dueCutoff(at(60)));
		assertEquals(LogPruner.NOT_DUE, pruner.dueCutoff(at(299)));
	}

	@Test
	public void prunesBelowTheNewestSampleOverFiveMinutesOld() {
		pruner.sample(at(0), 1);
		pruner.sample(at(30), 2);
		pruner.sample(at(120), 3);
		assertEquals(2, pruner.dueCutoff(at(330)));
	}

	@Test
	public void prunesAtMostEveryMinute() {
		pruner.sample(at(0), 1);
		assertEquals(1, pruner.dueCutoff(at(300)));
		assertEquals(LogPruner.NOT_DUE, pruner.dueCutoff(at(359)));
		assertEquals(1, pruner.dueCutoff(at(360))); // the cutoff is kept for the next prune
	}

	@Test
	public void samplesAtMostEveryTenSeconds() {
		pruner.sample(at(0), 1);
		pruner.sample(at(5), 2); // too soon, so not kept
		assertEquals(1, pruner.dueCutoff(at(306)));
	}

}
