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
	boolean branchHasValue(VariableBranch branch) {
		return branch.hasValue; // there's only one thread, so this can't be out of date
	}

	@Override
	boolean newBranchHasValue(String name, @Nullable VariableBranch parent) {
		// a name with a value is already a leaf in its parent, which ensureBranch picks up when it replaces the leaf.
		return parent == null && values.containsKey(name);
	}

	@Override
	VariableMap copyValues() {
		LocalVariableMap copy = new LocalVariableMap();
		copy.values.putAll(values);
		return copy;
	}

	@Override
	Map<String, Object> allValues() {
		return values;
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

	@Override
	Collection<VariableBranch> allBranches() {
		return branches == null ? List.of() : branches.values();
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
		// only one thread can be here, so entry can't have changed. TreeMap's remove(key, entry) would walk the tree twice
		children(branch).remove(key);
	}

	/**
	 * Every list has a branch registered under its name, so a hash lookup can tell a leaf from a list.
	 * Lists have to stay, and this way they don't get touched at all, so anything iterating them isn't disturbed.
	 * Leaves still only walk the tree once, to remove themselves.
	 */
	@Override
	@Nullable Object childRemoveLeaf(VariableBranch branch, String key, String name) {
		VariableBranch list = branchGet(name);
		return list != null ? list : children(branch).remove(key);
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
