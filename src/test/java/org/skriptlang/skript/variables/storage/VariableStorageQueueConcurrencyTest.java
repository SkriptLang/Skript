package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable.Value;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNull;

/**
 * Writes, clears and sets lists from several threads while another thread keeps draining and "saving",
 * and checks after every round that the saved data matches Skript's variables.
 * A small set of names keeps threads on the same keys, and checking every short round keeps later writes
 * from hiding a lost or misordered change.
 */
public class VariableStorageQueueConcurrencyTest {

	private static final int WRITERS = 4;
	private static final int ROUNDS = 5_000;
	private static final int OPERATIONS_PER_ROUND = 50;

	private static final String[] NAMES = {"a", "ab::x", "a::1", "a::2", "a::b::1", "a::b::2", "b::1", "b::2"};
	private static final String[] PREFIXES = {"a::", "a::b::", "b::"};

	@Test(timeout = 180_000)
	public void savedDataMatchesTheVariables() throws Exception {
		RecordingStorage storage = new RecordingStorage();
		Map<String, Value> variables = new ConcurrentHashMap<>();
		// drained and applied under its own lock, so batches are saved one at a time and in order
		Map<String, Value> stored = new HashMap<>();
		AtomicReference<String> mismatch = new AtomicReference<>();

		ExecutorService executor = Executors.newFixedThreadPool(WRITERS + 1);
		try {
			AtomicBoolean writing = new AtomicBoolean(true);
			Future<?> drainer = executor.submit(() -> {
				while (writing.get())
					drainAndSave(storage, stored);
				return null;
			});

			// writers line up at the start of each round, so they race on the same names;
			// at the end of each round, with every writer stopped, the saved data must match the variables.
			// Checking every round matters: later writes to the same names would repair a lost or misordered change.
			int[] round = {0};
			CyclicBarrier roundStart = new CyclicBarrier(WRITERS);
			CyclicBarrier roundEnd = new CyclicBarrier(WRITERS, () -> {
				drainAndSave(storage, stored);
				synchronized (stored) {
					Map<String, Value> expected = new HashMap<>(variables);
					if (!expected.equals(stored) && mismatch.get() == null)
						mismatch.set("round " + round[0] + ": saved data differs for " + differences(expected, stored));
				}
				round[0]++;
			});
			List<Future<?>> writers = new ArrayList<>();
			for (int w = 0; w < WRITERS; w++) {
				int seed = w;
				writers.add(executor.submit(() -> {
					Random random = new Random(seed);
					for (int r = 0; r < ROUNDS && mismatch.get() == null; r++) {
						roundStart.await(30, TimeUnit.SECONDS);
						for (int op = 0; op < OPERATIONS_PER_ROUND; op++)
							randomOperation(random, storage, variables);
						roundEnd.await(30, TimeUnit.SECONDS);
					}
					return null;
				}));
			}
			for (Future<?> writer : writers)
				writer.get();

			writing.set(false);
			drainer.get();
			assertNull(mismatch.get());
		} finally {
			executor.shutdownNow();
		}
	}

	private static List<String> differences(Map<String, Value> expected, Map<String, Value> stored) {
		List<String> names = new ArrayList<>();
		for (String name : expected.keySet()) {
			if (expected.get(name) != stored.get(name))
				names.add(name + (stored.containsKey(name) ? " (wrong value)" : " (not saved)"));
		}
		for (String name : stored.keySet()) {
			if (!expected.containsKey(name))
				names.add(name + " (saved but deleted)");
		}
		return names;
	}

	private static void drainAndSave(RecordingStorage storage, Map<String, Value> stored) {
		synchronized (stored) {
			RecordingStorage.apply(storage.drain(), stored);
		}
	}

	private static void randomOperation(Random random, RecordingStorage storage, Map<String, Value> variables) {
		int kind = random.nextInt(20);
		if (kind < 15) {
			String name = NAMES[random.nextInt(NAMES.length)];
			Value value = kind == 0 ? null : new Value("test", new byte[] {(byte) random.nextInt()});
			storage.write(name, value, () -> {
				if (value == null) {
					variables.remove(name);
				} else {
					variables.put(name, value);
				}
				// widen the window between changing the variables and queueing the change, so misordering shows up
				if (ThreadLocalRandom.current().nextInt(8) == 0)
					Thread.yield();
			});
		} else if (kind < 18) {
			clear(storage, variables, PREFIXES[random.nextInt(PREFIXES.length)]);
		} else {
			setList(storage, variables, PREFIXES[random.nextInt(PREFIXES.length)], random);
		}
	}

	private static void clear(RecordingStorage storage, Map<String, Value> variables, String prefix) {
		long stamp = storage.lockExclusive();
		try {
			variables.keySet().removeIf(name -> name.startsWith(prefix));
			storage.recordClear(prefix, true);
		} finally {
			storage.unlockExclusive(stamp);
		}
	}

	private static void setList(RecordingStorage storage, Map<String, Value> variables, String prefix, Random random) {
		long stamp = storage.lockExclusive();
		try {
			variables.keySet().removeIf(name -> name.startsWith(prefix));
			storage.recordClear(prefix, true);
			for (int i = 1; i <= 2; i++) {
				String name = prefix + i;
				Value value = new Value("test", new byte[] {(byte) random.nextInt()});
				variables.put(name, value);
				storage.putPending(name, value);
			}
		} finally {
			storage.unlockExclusive(stamp);
		}
	}

}
