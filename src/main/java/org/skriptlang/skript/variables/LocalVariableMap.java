package org.skriptlang.skript.variables;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The {@link VariableMap} for local variables, which runs on plain collections with no lock.
 * It's confined to one thread at a time, so there's nothing to synchronize.
 */
final class LocalVariableMap extends VariableMap {

	private final HashMap<String, Object> values = new HashMap<>();
	private @Nullable HashMap<String, VariableBranch> branches; // lazily allocated, as most events never use a list

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
		return present; // there's only one thread, so the caller can't be out of date
	}

	@Override
	@Nullable VariableBranch branchGet(String name) {
		return branches == null ? null : branches.get(name);
	}

	@Override
	void branchPut(VariableBranch branch) {
		if (branches == null)
			branches = new HashMap<>();
		branches.put(branch.fullName, branch);
	}

	@Override
	void branchRemove(VariableBranch branch) {
		if (branches != null)
			branches.remove(branch.fullName, branch);
	}

	@Override
	int branchCount() {
		return branches == null ? 0 : branches.size();
	}

	/**
	 * Returns a branch's children as a {@link TreeMap} in order to avoid the children callsites
	 * from being megamorphic, and instead so the JIT can optimize the concrete type calls.
	 *
	 * @param branch The branch to get the children of
	 * @return The children with a concrete type of {@link TreeMap}.
	 */
	private static TreeMap<String, Object> children(VariableBranch branch) {
		return (TreeMap<String, Object>) branch.children;
	}

	@Override
	NavigableMap<String, Object> newChildren() {
		return new TreeMap<>(VariableNameComparator.INSTANCE);
	}

	@Override
	NavigableMap<String, Object> newChildren(SortedMap<String, Object> sorted) {
		return new TreeMap<>(sorted);
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
	}

	@Override
	void unlock() {
	}

	@Override
	boolean holdsLock() {
		return true;
	}

	@Override
	public long size() {
		return values.size();
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

}
