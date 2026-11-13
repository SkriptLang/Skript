package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import com.google.errorprone.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.storage.VariableStorage.Batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

/**
 * The changes waiting to be saved to one {@link VariableStorage}.
 * <p>
 * Writers queue changes here from any thread, in the same step as they change Skript's in-memory variables,
 * and the storage's saving thread {@link #drain() drains} them into a {@link Batch}. Only the last change to each
 * name is kept. A list clear is only recorded, in O(1), and the changes it covers are dropped when draining,
 * on the saving thread, so that a clear's cost doesn't depend on how many changes are queued.
 * <p>
 * Ordering: single writes take the queue's lock shared, and clears and list sets take it exclusively,
 * so a clear is ordered against every write. Changes to one name are also ordered against each other,
 * since {@link #write} changes Skript's variables inside the queue's per-name update.
 */
@ThreadSafe
final class ChangeQueue {

	private final StampedLock order = new StampedLock(); // stamped lock since re-entry is not needed.
	// the last unsaved change for each variable, with the epoch it was made in
	private ConcurrentHashMap<String, Pending> pending = new ConcurrentHashMap<>();
	// pending list clears in order
	private ArrayList<Clear> clears = new ArrayList<>();
	private long epoch; // used to drop changes made before a clear when draining. Increments every clear.
	private int sizeHint; // decaying peak hint for drain size, to avoid resizing the pending map too often.
	private boolean closed; // nothing is queued once set. guarded by order: set under the exclusive lock
	private static final Pending ALLOCATION_DUMMY = new Pending("", null, 0);

	/**
	 * An unsaved change to one variable.
	 *
	 * @param epoch the value of {@link #epoch} when the change was made; the change was made before every clear
	 *              whose epoch is equal or higher
	 */
	private record Pending(String name, SerializedVariable.@Nullable Value value, long epoch) {
	}

	/**
	 * An undrained list clear.
	 *
	 * @param prefix the list prefix, ending in {@code ::}
	 * @param epoch  the value of {@link #epoch} when the list was cleared
	 * @param save   whether the storage has to delete the list; false for clears that came from the storage itself
	 */
	private record Clear(String prefix, long epoch, boolean save) {
	}

	/**
	 * Queues a change to one variable, and runs {@code mapWrite} to change it in Skript's variables.
	 * <p>
	 * {@code mapWrite} runs while the name is locked, so changes to one name reach Skript's variables
	 * and this queue in the same order. It must not call into any storage.
	 *
	 * @param name     the variable's name
	 * @param value    the serialized new value, or null if the variable is deleted
	 * @param mapWrite changes the variable in Skript's variables
	 */
	void write(String name, SerializedVariable.@Nullable Value value, Runnable mapWrite) {
		long stamp = order.readLock();
		try {
			if (closed) { // the storage has stopped, so this only changes the map
				mapWrite.run();
				return;
			}
			long epoch = this.epoch; // can't change while the shared lock is held
			pending.compute(name, (key, old) -> {
				mapWrite.run();
				return new Pending(key, value, epoch);
			});
		} finally {
			order.unlockRead(stamp);
		}
	}

	/**
	 * Takes this queue's exclusive lock, which waits for every {@link #write} in progress and blocks new ones.
	 *
	 * @return the stamp to pass to {@link #unlockExclusive(long)}
	 */
	long lockExclusive() {
		return order.writeLock();
	}

	/**
	 * Releases the exclusive lock taken by {@link #lockExclusive()}.
	 *
	 * @param stamp the stamp returned by {@link #lockExclusive()}
	 */
	void unlockExclusive(long stamp) {
		order.unlockWrite(stamp);
	}

	/**
	 * Records that a list was cleared. Pending changes under the prefix made before now are dropped when draining.
	 * O(1), whatever the number of pending changes. The caller must hold {@link #lockExclusive()}.
	 *
	 * @param prefix the list prefix, ending in {@code ::}, such as {@code a::} for {@code {a::*}}
	 * @param save   true if the storage has to delete the list,
	 *               false if the clear came from the storage itself and only local changes should be dropped
	 */
	void recordClear(String prefix, boolean save) {
		assert order.isWriteLocked();
		assert prefix.endsWith("::") : prefix;
		if (closed)
			return;
		clears.add(new Clear(prefix, epoch++, save));
	}

	/**
	 * Queues a change while the exclusive lock is held, such as the new values of a list being set.
	 * Call it after {@link #recordClear(String, boolean)} for the same list,
	 * so that the change is not dropped by that clear. The caller must hold {@link #lockExclusive()}.
	 *
	 * @param name  the variable's name
	 * @param value the serialized new value, or null if the variable is deleted
	 */
	void putPending(String name, SerializedVariable.@Nullable Value value) {
		assert order.isWriteLocked();
		if (closed)
			return;
		pending.put(name, new Pending(name, value, epoch));
	}

	/**
	 * Applies a change that came from the storage itself, such as another server writing to the same database.
	 * Runs {@code mapWrite} while the name is locked, and drops any unsaved local change to it,
	 * so that the storage's newer value is not overwritten. Does not queue anything.
	 *
	 * @param name     the variable's name
	 * @param mapWrite changes the variable in Skript's variables; must not call into any storage
	 */
	void externalWrite(String name, Runnable mapWrite) {
		long stamp = order.readLock();
		try {
			if (closed) { // nothing is queued to drop
				mapWrite.run();
				return;
			}
			pending.compute(name, (key, old) -> {
				mapWrite.run();
				return null;
			});
		} finally {
			order.unlockRead(stamp);
		}
	}

	// stops queueing: changes from now on only reach the map. waits for the writes in progress, so everything
	// queued before this is still drained. called when the storage stops saving, before its final save
	void close() {
		long stamp = order.writeLock();
		try {
			closed = true;
		} finally {
			order.unlockWrite(stamp);
		}
	}

	/**
	 * Counts the changes and clears queued but not yet drained, for reporting what is lost at shutdown.
	 *
	 * @return the number of queued changes and clears that have to be saved
	 */
	int count() {
		long stamp = order.writeLock();
		try {
			int count = pending.size();
			for (Clear clear : clears) {
				if (clear.save())
					count++;
			}
			return count;
		} finally {
			order.unlockWrite(stamp);
		}
	}

	/**
	 * Takes every change queued since the last drain, for the saving thread to save.
	 * <p>
	 * The goal here is to minimize the time it takes for writers to queue changes, so that the main thread is blocked as little as possible.
	 * The expensive work of filtering out changes made before a clear is done after the lock is released, in this method on the calling thread,
	 * since the storage's thread doesn't have the pressing business of running a server tick.
	 *
	 * @return the changes to save, possibly empty
	 */
	Batch drain() {
		ConcurrentHashMap<String, Pending> drained;
		ArrayList<Clear> drainedClears;
		// get replacement map presized to the estimated right size
		ConcurrentHashMap<String, Pending> replacement;
		if (sizeHint > 64) {
			replacement = new ConcurrentHashMap<>((int) Math.min(Integer.MAX_VALUE, sizeHint * 4L / 3 + 1)); // 33% extra to avoid resizing
			// ConcurrentHashMap only allocates its table on the first insert.
			// do that here, on the saving thread, rather than on the first writer
			replacement.put("", ALLOCATION_DUMMY);
			replacement.remove("");
		} else {
			replacement = new ConcurrentHashMap<>();
		}
		// swap the queue for an empty one while holding the exclusive lock, so writers are blocked only briefly
		long stamp = lockExclusive();
		try {
			if (pending.isEmpty() && clears.isEmpty()) {
				sizeHint -= sizeHint / 4;
				return Batch.EMPTY;
			}
			drained = pending;
			drainedClears = clears;
			pending = replacement;
			clears = new ArrayList<>();
		} finally {
			unlockExclusive(stamp);
		}
		// nothing can change the drained queue any more, so the rest needs no lock

		// size hint update!
		sizeHint = Math.max(drained.size(), sizeHint - sizeHint / 4); // 25% decay per drain

		List<SerializedVariable> changes = new ArrayList<>(drained.size());
		// if there are no clears, every change is saved, so we can just unwrap.
		if (drainedClears.isEmpty()) {
			for (Pending change : drained.values())
				changes.add(new SerializedVariable(change.name(), change.value()));
			return new Batch(List.of(), changes);
		}

		// we have clears, so we have to drop changes made before a clear that covers them
		List<String> saved = new ArrayList<>(drainedClears.size());
		// group clears by first name segment, so each change only checks the clears that could cover it
		Map<String, List<Clear>> clearsByFirstSegment = new HashMap<>();
		long lastClearEpoch = -1;
		for (Clear clear : drainedClears) {
			if (clear.save())
				saved.add(clear.prefix());
			clearsByFirstSegment.computeIfAbsent(firstSegment(clear.prefix()), key -> new ArrayList<>(2)).add(clear);
			lastClearEpoch = Math.max(lastClearEpoch, clear.epoch());
		}

		for (Pending change : drained.values()) {
			// a change made after every clear can't be covered by one
			if (change.epoch() > lastClearEpoch || !isCleared(change, clearsByFirstSegment))
				changes.add(new SerializedVariable(change.name(), change.value()));
		}
		return new Batch(saved, changes);
	}

	/**
	 * @return whether a clear made after the change covers its name
	 */
	private static boolean isCleared(Pending change, Map<String, List<Clear>> clearsByFirstSegment) {
		// TODO: the substring in firstSegment is most of the remaining drain cost; hash the segment in place if needed
		List<Clear> candidates = clearsByFirstSegment.get(firstSegment(change.name()));
		if (candidates == null)
			return false;
		for (Clear clear : candidates) {
			if (clear.epoch() >= change.epoch() && change.name().startsWith(clear.prefix()))
				return true;
		}
		return false;
	}

	/**
	 * @return the part of the name before the first {@code ::}, or the whole name if there is none
	 */
	private static String firstSegment(String name) {
		int end = name.indexOf("::");
		return end < 0 ? name : name.substring(0, end);
	}

}
