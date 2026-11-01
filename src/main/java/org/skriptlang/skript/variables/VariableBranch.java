package org.skriptlang.skript.variables;

import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.NavigableMap;

/**
 * The structure of one variable name that has children.
 * <p>
 * Each child entry is the child's full name if it's a leaf, or the child's own branch if it has children.
 * Storing the full name means iterating a list doesn't have to build a string for every element.
 * <p>
 * Everything here is written under the lock, but list views read most of these fields without it,
 * so they're volatile. That also means the non-atomic increments are fine, since there's only ever one writer.
 * <p>
 * The next available index can never exceed {@code childCount + 1}, so only small indices matter for finding it.
 * This means we can use a {@link BitSet} and avoid over-allocating even if the list contains very large indices.
 */
final class VariableBranch {
	final String fullName; // the name of this branch, without the list suffix, e.g. a::b for {a::b::*}
	final @Nullable VariableBranch parent;
	volatile NavigableMap<String, Object> children; // key -> child's full name or VariableBranch. e.g. 'c' -> 'a::b::c'
	volatile int childCount;
	volatile int valueChildren; // children that hold a value (not sublists), which is what the size of a list counts
	volatile boolean hasValue; // whether this has a value e.g. branch is a::b, if {a::b} is set, it has value.
	boolean removed; // whether this branch has been pruned, so stale references can tell

	/**
	 * The initial and minimum number of indices tracked.
	 */
	static final int MIN_TRACKED = 64;

	/**
	 * Bit {@code i} is set when positive integer key {@code i} exists,
	 * for {@code 1 <= i <= trackedLimit}. Keys above the limit are not tracked.
	 * Created on the first tracked index, so lists keyed only by other strings or large numbers never need it.
	 * Only touched under the lock.
	 */
	@Nullable BitSet trackedIndices;
	int trackedLimit = MIN_TRACKED; // only touched under the lock

	/**
	 * The smallest positive integer that is not a key. Every index below it is set,
	 * so it never exceeds {@code childCount + 1}. Written under the lock, but list views read it without the lock.
	 */
	volatile int nextIndex = 1;

	/**
	 * Creates a new branch with no counted children.
	 *
	 * @param fullName the name of this branch, without the list suffix
	 * @param parent   the parent branch, or null for root-level names
	 * @param children the children map, which is passed in since only the owning map knows which type to use
	 */
	VariableBranch(String fullName, @Nullable VariableBranch parent, NavigableMap<String, Object> children) {
		this.fullName = fullName;
		this.parent = parent;
		this.children = children;
	}

	/**
	 * Handles bookkeeping when a child is added, allocating the BitSet if needed.
	 * If this was the next available index, {@link #nextIndex} moves up to the next free one.
	 * Must be called after {@link #childCount} is updated and the child is linked.
	 *
	 * @param key The key of the new child. Non-integer keys are ignored.
	 */
	void added(String key) {
		int index = positiveInt(key);
		if (index == -1)
			return;
		if (index <= trackedLimit) {
			if (trackedIndices == null)
				trackedIndices = new BitSet(MIN_TRACKED + 1);
			trackedIndices.set(index);
		}
		if (index == nextIndex)
			advanceNextIndex();
	}

	/**
	 * Handles bookkeeping when a child is removed, moving {@link #nextIndex} down if this index
	 * is now the lowest free one, and resizing the BitSet if needed.
	 * Must be called after {@link #childCount} is updated.
	 *
	 * @param key The key of the removed child. Non-integer keys are ignored.
	 */
	void freed(String key) {
		int index = positiveInt(key);
		if (index != -1) {
			if (index <= trackedLimit && trackedIndices != null)
				trackedIndices.clear(index);
			if (index < nextIndex)
				nextIndex = index;
		}
		shrinkIfSparse();
	}

	/**
	 * Rebuilds the index tracking from scratch, for when all the children were replaced at once.
	 * The tracked range is sized to always hold {@link #nextIndex}, so this never needs to grow.
	 *
	 * @param keys The keys of the new children. Non-integer keys are ignored.
	 */
	void trackAll(String[] keys) {
		int limit = MIN_TRACKED;
		while (limit <= keys.length)
			limit *= 2;

		BitSet tracked = null;
		for (String key : keys) {
			int index = positiveInt(key);
			if (index != -1 && index <= limit) {
				if (tracked == null)
					tracked = new BitSet(limit + 1);
				tracked.set(index);
			}
		}

		trackedLimit = limit;
		trackedIndices = tracked;
		nextIndex = tracked == null ? 1 : tracked.nextClearBit(1);
	}

	/**
	 * Moves {@link #nextIndex} to the first index after it that is not a key,
	 * growing the tracked range whenever every tracked index is in use.
	 */
	private void advanceNextIndex() {
		assert trackedIndices != null;
		int next = trackedIndices.nextClearBit(nextIndex);
		while (next > trackedLimit) {
			grow();
			next = trackedIndices.nextClearBit(next);
		}
		nextIndex = next;
	}

	/**
	 * Grows the tracked range to the first power of two multiple of {@link #MIN_TRACKED} above {@link #childCount},
	 * which always holds {@link #nextIndex}, so one call is enough. Keys inside the new range were not tracked
	 * until now, so they are picked up with one pass over the children, skipped when every child is already tracked.
	 * This only happens once every tracked index is in use, so the branch holds at least
	 * {@link #trackedLimit} children and the pass is paid for by the inserts that filled it.
	 */
	private void grow() {
		int oldLimit = trackedLimit;
		int newLimit = oldLimit * 2;
		while (newLimit <= childCount)
			newLimit *= 2;
		trackedLimit = newLimit;
		assert trackedIndices != null;
		if (trackedIndices.cardinality() == childCount)
			return; // every child is a tracked index, so there's nothing new to pick up
		for (String key : children.keySet()) {
			int index = positiveInt(key);
			if (index > oldLimit && index <= newLimit)
				trackedIndices.set(index);
		}
	}

	/**
	 * Shrinks the tracked range once the branch holds fewer than an eighth of it, so a list that
	 * grew large and was then mostly emptied does not keep its peak-sized tracking. Growing at
	 * full and shrinking at an eighth keeps a list hovering around one size from resizing back
	 * and forth. Indices dropped from tracking are picked up again by {@link #grow()} if needed.
	 */
	private void shrinkIfSparse() {
		if (trackedLimit <= MIN_TRACKED || childCount >= trackedLimit / 8)
			return;
		int newLimit = MIN_TRACKED;
		while (newLimit < childCount * 2)
			newLimit *= 2;
		// nextIndex <= childCount + 1 <= newLimit, so it stays within the tracked range
		if (trackedIndices != null)
			trackedIndices = trackedIndices.get(0, newLimit + 1);
		trackedLimit = newLimit;
	}

	/**
	 * Parses a canonical positive integer key. Leading zeroes aren't canonical, since {@code 01} and {@code 1}
	 * are different keys. Keys over 9 digits are treated as non-integers, which both avoids overflow
	 * and avoids paying for it with exceptions on number-heavy keys, like timestamps.
	 *
	 * @param string The key to parse
	 * @return The value of the key, or -1 if it isn't a canonical positive integer of at most 9 digits
	 */
	static int positiveInt(String string) {
		int length = string.length();
		if (length == 0 || length > 9 || string.charAt(0) == '0')
			return -1;

		int value = 0;
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (!VariableNameComparator.isDigit(c))
				return -1;
			value = value * 10 + (c - '0');
		}
		return value;
	}

}
