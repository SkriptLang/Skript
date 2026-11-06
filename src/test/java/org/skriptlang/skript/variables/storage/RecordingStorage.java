package org.skriptlang.skript.variables.storage;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.SerializedVariable;

import java.util.HashMap;
import java.util.Map;

/**
 * A storage that saves nothing, for testing the change queue in {@link VariableStorage}.
 * Tests drain it with {@link #drain()} and can apply batches to a map standing in for the stored data.
 */
class RecordingStorage extends VariableStorage {

	RecordingStorage() {
		super(null, "recording");
	}

	/**
	 * Drains the queue, as a storage's save thread would.
	 */
	Batch drain() {
		return drainPending();
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

	@Override
	protected boolean configure(SectionNode node) {
		return true;
	}

	@Override
	protected void load(StorageListener listener) {
	}

	@Override
	protected boolean requiresFile() {
		return false;
	}

	@Override
	public void close() {
	}

}
