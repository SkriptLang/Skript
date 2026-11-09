package org.skriptlang.skript.variables.storage;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A storage for testing {@link VariableStorage}: it saves batches into {@link #saved}, a map standing in for the
 * stored data, and records each saved batch, each save attempt and each reported failure.
 * Queue tests drain it directly with {@link #drain()}; save loop tests let its saving thread do the work.
 */
class RecordingStorage extends VariableStorage {

	/**
	 * The stored data. Synchronize on it to read it while the saving thread runs.
	 */
	final Map<String, SerializedVariable.Value> saved = new HashMap<>();

	/**
	 * Every batch saved successfully, in order. Synchronize on {@link #saved} to read it.
	 */
	final List<Batch> savedBatches = new ArrayList<>();

	/**
	 * While above 0, {@link #save(Batch)} throws an {@link IOException} and decrements it.
	 */
	final AtomicInteger failSaves = new AtomicInteger();

	/**
	 * While above 0, {@link #save(Batch)} throws an {@link Error} and decrements it.
	 */
	final AtomicInteger errorSaves = new AtomicInteger();

	/**
	 * The {@link System#nanoTime()} of every call to {@link #save(Batch)}, including failed calls.
	 */
	final List<Long> saveAttemptTimes = new CopyOnWriteArrayList<>();

	/**
	 * The messages of every failure reported through {@link #reportSaveFailure(Throwable, String)}, in order.
	 */
	final List<String> reportedFailures = new CopyOnWriteArrayList<>();

	/**
	 * The failure counts passed to {@link #reportSaveRecovered(int)}, in order.
	 */
	final List<Integer> reportedRecoveries = new CopyOnWriteArrayList<>();

	/**
	 * The name of the thread that last called {@link #save(Batch)}.
	 */
	volatile String lastSaveThread;

	/**
	 * Returned by {@link #saveIntervalMillis()}. Short, so save loop tests run quickly.
	 */
	long intervalMillis = 10;

	RecordingStorage() {
		super(null, "recording");
	}

	/**
	 * Drains the queue, as the saving thread would.
	 */
	Batch drain() {
		return drainPending();
	}

	/**
	 * @return how often {@link #save(Batch)} was called, including failed calls
	 */
	int saveAttempts() {
		return saveAttemptTimes.size();
	}

	/**
	 * Applies a batch to a map standing in for the stored data: clears first, then changes.
	 */
	static void apply(Batch batch, Map<String, SerializedVariable.Value> stored) {
		for (String prefix : batch.clears())
			stored.keySet().removeIf(name -> name.startsWith(prefix));
		for (SerializedVariable change : batch.changes()) {
			if (change.value == null) {
				stored.remove(change.name);
			} else {
				stored.put(change.name, change.value);
			}
		}
	}

	/**
	 * @return the batch's changes by name, for asserting on
	 */
	static Map<String, SerializedVariable.Value> changes(Batch batch) {
		Map<String, SerializedVariable.Value> changes = new HashMap<>();
		for (SerializedVariable change : batch.changes()) {
			if (changes.containsKey(change.name))
				throw new AssertionError("Batch has two changes to " + change.name);
			changes.put(change.name, change.value);
		}
		return changes;
	}

	/**
	 * Sets the storage's name, which {@code loadConfig} would normally set from the config section.
	 */
	void setNameForTest(String name) {
		try {
			Field field = VariableStorage.class.getDeclaredField("name");
			field.setAccessible(true);
			field.set(this, name);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	protected boolean configure(SectionNode node) {
		return true;
	}

	@Override
	protected void load(StorageListener listener) {
	}

	@Override
	protected void save(Batch batch) throws IOException {
		saveAttemptTimes.add(System.nanoTime());
		lastSaveThread = Thread.currentThread().getName();
		if (errorSaves.getAndUpdate(n -> n > 0 ? n - 1 : 0) > 0)
			throw new Error("erroring on purpose");
		if (failSaves.getAndUpdate(n -> n > 0 ? n - 1 : 0) > 0)
			throw new IOException("failing on purpose");
		synchronized (saved) {
			apply(batch, saved);
			savedBatches.add(batch);
		}
	}

	@Override
	void reportSaveFailure(@Nullable Throwable t, String message) {
		reportedFailures.add(message);
	}

	@Override
	void reportSaveRecovered(int failures) {
		reportedRecoveries.add(failures);
	}

	@Override
	protected long saveIntervalMillis() {
		return intervalMillis;
	}

	@Override
	protected boolean requiresFile() {
		return false;
	}

	@Override
	public void close() {
	}

}
