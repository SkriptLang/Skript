package org.skriptlang.skript.variables;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The thread-safe {@link VariableMap}, for global variables and storages.
 * Reads and overwrites are lock-free, and anything that changes the tree takes {@link #lock}.
 */
final class ConcurrentVariableMap extends VariableMap {

	private final ReentrantLock lock = new ReentrantLock(); // guards every change to the tree
	private final ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, VariableBranch> branches = new ConcurrentHashMap<>();

	@Override
	@Nullable Object valueGet(String key) {
		return values.get(key);
	}

	@Override
	@Nullable Object valuePut(String key, @NotNull Object value) {
		return values.put(key, value);
	}

	@Override
	@Nullable Object valueRemove(String key) {
		return values.remove(key);
	}

	@Override
	boolean valueIsSet(String key) {
		return values.containsKey(key);
	}

	@Override
	boolean isPresent(String name, boolean present) {
		return values.containsKey(name);
	}

	@Override
	@Nullable VariableBranch branchGet(String name) {
		return branches.get(name);
	}

	@Override
	void branchPut(VariableBranch branch) {
		branches.put(branch.fullName, branch);
	}

	@Override
	void branchRemove(VariableBranch branch) {
		branches.remove(branch.fullName, branch);
	}

	@Override
	int branchCount() {
		return branches.size();
	}

	/**
	 * Returns a branch's children as a {@link ConcurrentSkipListMap} in order to avoid the children callsites
	 * from being megamorphic, and instead so the JIT can optimize the concrete type calls.
	 *
	 * @param branch The branch to get the children of
	 * @return The children with a concrete type of {@link ConcurrentSkipListMap}.
	 */
	private static ConcurrentSkipListMap<String, Object> children(VariableBranch branch) {
		return (ConcurrentSkipListMap<String, Object>) branch.children;
	}

	@Override
	NavigableMap<String, Object> newChildren() {
		return new ConcurrentSkipListMap<>(VariableNameComparator.INSTANCE);
	}

	@Override
	NavigableMap<String, Object> newChildren(SortedMap<String, Object> sorted) {
		return new ConcurrentSkipListMap<>(sorted);
	}

	@Override
	@Nullable Object childGet(VariableBranch branch, String key) {
		return children(branch).get(key);
	}

	@Override
	@Nullable Object childPut(VariableBranch branch, String key, Object entry) {
		return children(branch).put(key, entry);
	}

	@Override
	@Nullable Object childPutIfAbsent(VariableBranch branch, String key, Object entry) {
		return children(branch).putIfAbsent(key, entry);
	}

	@Override
	void childRemove(VariableBranch branch, String key, Object entry) {
		children(branch).remove(key, entry);
	}

	@Override
	Iterator<Map.Entry<String, Object>> childIterator(VariableBranch branch) {
		return children(branch).entrySet().iterator();
	}

	@Override
	void lock() {
		lock.lock();
	}

	@Override
	void unlock() {
		lock.unlock();
	}

	@Override
	boolean holdsLock() {
		return lock.isHeldByCurrentThread();
	}

	@Override
	public long size() {
		return values.mappingCount();
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

}
