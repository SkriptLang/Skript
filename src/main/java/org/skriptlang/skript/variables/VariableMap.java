package org.skriptlang.skript.variables;

import ch.njol.skript.lang.Variable;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A map specifically for tracking and storing variables, used for both local and global variables.
 * <p>
 * The data is split across two maps:
 * <ul>
 *     <li>{@link #values} maps the full name of every variable to its value. This is the only place values are stored,
 *     so reading or overwriting a single variable is one hash operation, no matter how deep it is.</li>
 *     <li>{@link #branches} maps every name that has children to its {@link VariableBranch}, together forming the tree.
 *     A branch stores no values. It only keeps its sorted child keys, each pointing to either the child's full name,
 *     which can then be looked up in {@link #values}, or the child's own branch if it has children too.</li>
 * </ul>
 * For example, setting {@code {coins}}, {@code {a::b::c}} and {@code {a::d}} gives us:
 * <pre>{@code
 * values:   "coins"   -> v1
 *           "a::b::c" -> v2
 *           "a::d"    -> v3
 *
 * branches: "a"    -> Branch(children: "b" -> Branch("a::b"), "d" -> "a::d")
 *           "a::b" -> Branch(children: "c" -> "a::b::c")
 * }</pre>
 * Plain root-level names like {@code coins} never get put into the tree, since nothing can view the root.
 * <p>
 * The tree never keeps anything empty. Deleting {@code {x::a}} removes it from the children of {@code x},
 * unless it still has children of its own, and a branch left with no children is removed as well.
 * Overwriting a value that's already there adds or removes nothing, so overwrites never touch the tree.
 * Only creating or deleting a name does, which is the expensive path.
 * <p>
 * Maps from {@link #concurrent()} are thread-safe: reads and overwrites are lock-free,
 * and anything that changes the tree takes one lock that covers the whole map.
 * Maps from {@link #local()} run the same algorithm on plain collections with no lock,
 * and are confined to one thread at a time.
 */
@ApiStatus.Internal
public final class VariableMap {

	/**
	 * What the name of a list variable ends with.
	 */
	private static final String LIST_SUFFIX = Variable.SEPARATOR + "*";

	/**
	 * This controls whether to use threadsafe concurrent data structures, which are needed for global variables,
	 * or to use plain hashmaps and treemaps, which are faster for local variables.
	 */
	private final boolean concurrent;

	/**
	 * Guards every change to the tree. Null for local maps, which are confined to one thread and have no need for it.
	 */
	private final @Nullable ReentrantLock lock;

	/**
	 * The main value store, mapping the full name of every variable to its value.
	 * A {@link ConcurrentHashMap} if {@link #concurrent} is true, otherwise a {@link HashMap}.
	 * Only access this through the helpers below, so that each callsite sees one concrete type.
	 */
	private final Map<String, Object> values;

	/**
	 * Stores which indices correspond to values and which are lists.
	 * Allocated up front for concurrent maps, as readers access it without the lock.
	 * Lazily allocated for local maps, as most events never use a list.
	 * A {@link ConcurrentHashMap} if {@link #concurrent} is true, otherwise a {@link HashMap}.
	 * Only access this through the helpers below, so that each callsite sees one concrete type.
	 */
	private @Nullable Map<String, VariableBranch> branches;

	/**
	 * Creates a thread-safe map, for global variables and storages.
	 * Reads and overwrites are lock-free, and anything that changes the tree takes one lock.
	 *
	 * @return A new concurrent variable map.
	 */
	@Contract(" -> new")
	public static @NotNull VariableMap concurrent() {
		return new VariableMap(true);
	}

	/**
	 * Creates a map for local variables, with no thread-safety overhead.
	 * It can only be used by one thread at a time, but can be handed over to another thread through the scheduler.
	 *
	 * @return A new local variable map.
	 */
	@Contract(" -> new")
	public static @NotNull VariableMap local() {
		return new VariableMap(false);
	}

	/**
	 * Creates a new variable map with the specified type.
	 *
	 * @param concurrent Whether to enable concurrent data structures for thread safety.
	 */
	private VariableMap(boolean concurrent) {
		this.concurrent = concurrent;
		if (this.concurrent) {
			lock = new ReentrantLock();
			values = new ConcurrentHashMap<>();
			branches = new ConcurrentHashMap<>();
		} else {
			lock = null;
			values = new HashMap<>();
		}
	}


	// ========== Values ============

	/**
	 * Returns values as a {@link ConcurrentHashMap} in order to avoid the values callsites from being megamorphic,
	 * and instead so the JIT can optimize the concrete type calls.
	 * Should only be called if {@link #concurrent} is true.
	 *
	 * @return The values map with a concrete type of {@link ConcurrentHashMap}.
	 */
	private ConcurrentHashMap<String, Object> sharedValues() {
		assert concurrent;
		return (ConcurrentHashMap<String, Object>) values;
	}

	/**
	 * Returns values as a {@link HashMap} in order to avoid the values callsites from being megamorphic,
	 * and instead so the JIT can optimize the concrete type calls.
	 * Should only be called if {@link #concurrent} is false.
	 *
	 * @return The values map with a concrete type of {@link HashMap}.
	 */
	private HashMap<String, Object> localValues() {
		assert !concurrent;
		return (HashMap<String, Object>) values;
	}

	/**
	 * Simple helper to read from {@link #values}.
	 *
	 * @param key The full name of the variable
	 * @return The value, or null if there is none. Never a list.
	 */
	private @Nullable Object valueGet(String key) {
		return concurrent ? sharedValues().get(key) : localValues().get(key);
	}

	/**
	 * Simple helper to write to {@link #values}. {@link ConcurrentHashMap} throws on null values,
	 * so deletions have to go through {@link #valueRemove} instead.
	 *
	 * @param key   The full name of the variable
	 * @param value The value to set
	 * @return The previous value, or null if there was none
	 */
	private @Nullable Object valuePut(String key, @NotNull Object value) {
		return concurrent ? sharedValues().put(key, value) : localValues().put(key, value);
	}

	/**
	 * Simple helper to remove from {@link #values}.
	 *
	 * @param key The full name of the variable
	 * @return The previous value, or null if there was none
	 */
	private @Nullable Object valueRemove(String key) {
		return concurrent ? sharedValues().remove(key) : localValues().remove(key);
	}

	/**
	 * Simple helper to check if a variable is set right now. {@link #values} is the source of truth,
	 * unlike {@link VariableBranch#hasValue}, which can be briefly out of date.
	 *
	 * @param key The full name of the variable
	 * @return Whether the variable has a value
	 */
	private boolean valueIsSet(String key) {
		return concurrent ? sharedValues().containsKey(key) : localValues().containsKey(key);
	}

	// ============= Branches ===============

	/**
	 * Returns branches as a {@link ConcurrentHashMap} in order to avoid the branches callsites from being megamorphic,
	 * and instead so the JIT can optimize the concrete type calls.
	 * Should only be called if {@link #concurrent} is true.
	 *
	 * @return The branches map with a concrete type of {@link ConcurrentHashMap}.
	 */
	private ConcurrentHashMap<String, VariableBranch> sharedBranches() {
		assert concurrent;
		return (ConcurrentHashMap<String, VariableBranch>) branches;
	}

	/**
	 * Returns branches as a {@link HashMap} in order to avoid the branches callsites from being megamorphic,
	 * and instead so the JIT can optimize the concrete type calls.
	 * Should only be called if {@link #concurrent} is false.
	 *
	 * @return The branches map with a concrete type of {@link HashMap}. Will be null if {@link #branches} is null.
	 */
	private HashMap<String, VariableBranch> localBranches() {
		assert !concurrent;
		return (HashMap<String, VariableBranch>) branches;
	}

	/**
	 * Simple helper to read from {@link #branches}.
	 *
	 * @param name The full name of the branch, without the list suffix
	 * @return The branch, or null if this name has no children
	 */
	private @Nullable VariableBranch branchGet(String name) {
		if (branches == null)
			return null;
		return concurrent ? sharedBranches().get(name) : localBranches().get(name);
	}

	/**
	 * Simple helper to add to {@link #branches}, allocating it for local maps if needed.
	 * Must be called under the lock.
	 *
	 * @param branch The branch to add, keyed by its full name
	 */
	private void branchPut(VariableBranch branch) {
		if (concurrent) {
			sharedBranches().put(branch.fullName, branch);
			return;
		}
		if (branches == null)
			branches = new HashMap<>();
		localBranches().put(branch.fullName, branch);
	}

	/**
	 * Simple helper to remove from {@link #branches}. This only removes the entry if it's still {@code branch},
	 * so removing a stale branch can't orphan a newer one with the same name.
	 * Must be called under the lock.
	 *
	 * @param branch The branch to remove
	 */
	private void branchRemove(VariableBranch branch) {
		if (branches == null)
			return;
		if (concurrent) {
			sharedBranches().remove(branch.fullName, branch);
		} else {
			localBranches().remove(branch.fullName, branch);
		}
	}

	/**
	 * Gets the number of branches in the tree. There's no reason to use this outside of tests,
	 * where it's a simple way to check that nothing is left behind.
	 *
	 * @return The number of names that currently have children.
	 */
	int branchCount() {
		return branches == null ? 0 : branches.size();
	}

	/**
	 * Updates the tree to match {@link #values} for one name, after that name was created or deleted.
	 * This is the only way a single variable changes the tree, so overwrites never get here.
	 * Must be called under the lock.
	 * <p>
	 * Writers change {@link #values} first and only then take the lock, so calls can run in a different order
	 * than the writes they belong to. For concurrent maps this means {@code present} can't be trusted,
	 * so it's read again from {@link #values}. The last call to run always sees the final state,
	 * and calls that find there's nothing to do just do nothing, so racing writers end up correct in any order.
	 *
	 * @param name    The full name of the variable, which can't be a list name
	 * @param present Whether the caller just set this name, rather than deleting it
	 */
	@SuppressWarnings("NonAtomicOperationOnVolatileField")
	private void reconcile(String name, boolean present) {
		assert lock == null || lock.isHeldByCurrentThread();
		if (concurrent)
			present = valueIsSet(name); // double check since caller may be out of date
		// is it a list?
		int sep = lastSeparator(name);
		if (sep == -1) {
			// not a list, so just update hasValue.
			VariableBranch branch = branchGet(name);
			if (branch != null)
				branch.hasValue = present;
			return;
		}

		// get the parent branch
		String parentName = name.substring(0, sep);
		VariableBranch parent = present ? ensureBranch(parentName) : branchGet(parentName);
		if (parent == null)
			return;

		// check existing value
		String key = name.substring(sep + Variable.SEPARATOR.length());
		Object oldEntry = present ? childPutIfAbsent(parent, key, name) : childGet(parent, key);
		if (oldEntry instanceof VariableBranch oldBranch && oldBranch.hasValue != present) {
			// adding/removing a value when the list exists (set/delete {a::b} when {a::b::*} exists)
			oldBranch.hasValue = present;
			parent.valueChildren += present ? 1 : -1;
		} else if (present && oldEntry == null) {
			// new leaf (set {a::b} when {a::b::*} doesn't exist)
			parent.childCount++;
			parent.valueChildren++;
			parent.added(key);
		} else if (!present && oldEntry instanceof String) {
			// deleting leaf (delete {a::b} when {a::b::*} doesn't exist)
			childRemove(parent, key, oldEntry);
			parent.childCount--;
			parent.valueChildren--;
			parent.freed(key);
			pruneIfEmpty(parent);
		}
		// anything else means another call already handled this, so there's nothing to do
	}

	/**
	 * Gets the branch for a name, creating it and any missing ancestors, and linking each new branch into its parent.
	 * If the name was a leaf, it gets swapped for the new branch in its parent.
	 * Must be called under the lock.
	 *
	 * @param name The full name of the branch, without the list suffix
	 * @return The existing or newly created branch
	 */
	@SuppressWarnings("NonAtomicOperationOnVolatileField")
	private VariableBranch ensureBranch(String name) {
		assert lock == null || lock.isHeldByCurrentThread();
		// check for quick exit
		VariableBranch existing = branchGet(name);
		if (existing != null)
			return existing;

		// branch doesn't exist, create.
		int sep = lastSeparator(name);
		VariableBranch parent = sep == -1 ? null : ensureBranch(name.substring(0, sep));
		VariableBranch branch = new VariableBranch(name, parent, newChildren());
		branch.hasValue = valueIsSet(name);
		branchPut(branch);

		// update parent
		if (parent != null) {
			String key = name.substring(sep + Variable.SEPARATOR.length());
			Object previous = childPut(parent, key, branch);
			if (previous != null) {
				// leaf -> branch. It's already counted in the parent, so the counters stay as they are.
				branch.hasValue = true;
			} else {
				// new branch
				parent.childCount++;
				if (branch.hasValue)
					parent.valueChildren++;
				parent.added(key);
			}
		}
		return branch;
	}

	/**
	 * Removes a branch if it has no children left, so that the tree never keeps anything empty.
	 * If the name still has a value, it goes back to being a leaf in its parent.
	 * Otherwise, it's removed from its parent as well, which may leave the parent empty, so this repeats up the tree.
	 * Must be called under the lock.
	 *
	 * @param branch The branch that just lost a child
	 */
	@SuppressWarnings("NonAtomicOperationOnVolatileField")
	private void pruneIfEmpty(VariableBranch branch) {
		assert lock == null || lock.isHeldByCurrentThread();
		while (branch != null && branch.childCount == 0) {
			branchRemove(branch);
			branch.removed = true;
			VariableBranch parent = branch.parent;
			if (parent == null)
				return;
			String key = branch.fullName.substring(parent.fullName.length() + Variable.SEPARATOR.length());
			if (valueIsSet(branch.fullName)) {
				// become leaf since value still exists. It's still one child, so childCount doesn't change.
				childPut(parent, key, branch.fullName);
				if (!branch.hasValue) // if it wasn't already marked as having value (it should have been, but races!)
					parent.valueChildren++; // leaves always count, and the pending reconcile won't count an existing leaf
				return;
			}
			// nothing left so remove and continue
			childRemove(parent, key, branch);
			parent.childCount--;
			if (branch.hasValue)
				parent.valueChildren--;
			parent.freed(key);
			branch = parent;
		}
	}

	// ============= Branch Children =============

	/**
	 * Returns a branch's children as a {@link ConcurrentSkipListMap} in order to avoid the children callsites
	 * from being megamorphic, and instead so the JIT can optimize the concrete type calls.
	 * Should only be called if {@link #concurrent} is true.
	 *
	 * @param branch The branch to get the children of
	 * @return The children with a concrete type of {@link ConcurrentSkipListMap}.
	 */
	private ConcurrentSkipListMap<String, Object> sharedChildren(VariableBranch branch) {
		assert concurrent;
		return (ConcurrentSkipListMap<String, Object>) branch.children;
	}

	/**
	 * Returns a branch's children as a {@link TreeMap} in order to avoid the children callsites
	 * from being megamorphic, and instead so the JIT can optimize the concrete type calls.
	 * Should only be called if {@link #concurrent} is false.
	 *
	 * @param branch The branch to get the children of
	 * @return The children with a concrete type of {@link TreeMap}.
	 */
	private TreeMap<String, Object> localChildren(VariableBranch branch) {
		assert !concurrent;
		return (TreeMap<String, Object>) branch.children;
	}

	/**
	 * Creates an empty children map of the right type for this map,
	 * sorted by {@link VariableNameComparator#INSTANCE}.
	 *
	 * @return A new, empty children map.
	 */
	private NavigableMap<String, Object> newChildren() {
		return concurrent
			? new ConcurrentSkipListMap<>(VariableNameComparator.INSTANCE)
			: new TreeMap<>(VariableNameComparator.INSTANCE);
	}

	/**
	 * Simple helper to read a child entry of a branch.
	 *
	 * @param branch The branch to read from
	 * @param key    The child's key, which is the last part of its name. Must not be null.
	 * @return The child's full name if it's a leaf, its {@link VariableBranch} if it has children, or null if there's no child
	 */
	private @Nullable Object childGet(VariableBranch branch, String key) {
		return concurrent ? sharedChildren(branch).get(key) : localChildren(branch).get(key);
	}

	/**
	 * Simple helper to set a child entry of a branch, replacing any existing entry.
	 * Used for swapping a leaf for a branch and back. Must be called under the lock.
	 *
	 * @param branch The branch to write to
	 * @param key    The child's key
	 * @param entry  The child's full name if it's a leaf, or its {@link VariableBranch} if it has children
	 * @return The previous entry, or null if there was none
	 */
	private @Nullable Object childPut(VariableBranch branch, String key, Object entry) {
		return concurrent ? sharedChildren(branch).put(key, entry) : localChildren(branch).put(key, entry);
	}

	/**
	 * Simple helper to link a new child into a branch without overwriting an existing entry.
	 * Must be called under the lock.
	 *
	 * @param branch The branch to write to
	 * @param key    The child's key
	 * @param entry  The child's full name if it's a leaf, or its {@link VariableBranch} if it has children
	 * @return The existing entry, or null if the new one was linked
	 */
	private @Nullable Object childPutIfAbsent(VariableBranch branch, String key, Object entry) {
		return concurrent ? sharedChildren(branch).putIfAbsent(key, entry) : localChildren(branch).putIfAbsent(key, entry);
	}

	/**
	 * Simple helper to unlink a child from a branch. This only removes the entry if it's still {@code entry}.
	 * Must be called under the lock.
	 *
	 * @param branch The branch to remove from
	 * @param key    The child's key
	 * @param entry  The entry we expect to remove
	 */
	private void childRemove(VariableBranch branch, String key, Object entry) {
		if (concurrent) {
			sharedChildren(branch).remove(key, entry);
		} else {
			localChildren(branch).remove(key, entry);
		}
	}

	/**
	 * Simple helper to iterate the child entries of a branch, in sorted order.
	 * For concurrent maps this is weakly consistent. For local maps, changing the branch
	 * while iterating will throw a {@link ConcurrentModificationException}, just like any {@link TreeMap}.
	 *
	 * @param branch The branch to iterate
	 * @return An iterator over the branch's keys and child entries
	 */
	private Iterator<Map.Entry<String, Object>> childIterator(VariableBranch branch) {
		return concurrent ? sharedChildren(branch).entrySet().iterator() : localChildren(branch).entrySet().iterator();
	}

	// ================ Locks ================

	/**
	 * Takes the lock that guards the tree. Does nothing for local maps.
	 */
	private void lock() {
		if (lock != null) {
			lock.lock();
		}
	}

	/**
	 * Releases the lock that guards the tree. Does nothing for local maps.
	 */
	private void unlock() {
		if (lock != null) {
			lock.unlock();
		}
	}

	// ============= Public API ==============

	/**
	 * Gets the value of a variable.
	 *
	 * @param name The full name of the variable, or a list name ending in {@code ::*}
	 * @return The value, a {@link VariableList} view for a list name, or null if the variable isn't set
	 */
	public @Nullable Object getVariable(String name) {
		if (name.endsWith(LIST_SUFFIX)) {
			VariableBranch branch = branchGet(name.substring(0, name.length() - LIST_SUFFIX.length()));
			return branch == null || branch.childCount == 0 ? null : new View(branch, true);
		}
		return valueGet(name);
	}

	/**
	 * Sets or deletes a variable. Overwriting a value that's already there is lock-free and never touches the tree.
	 * Lists can't be set to a value, they can only be cleared by setting them to null.
	 *
	 * @param name  The full name of the variable, or a list name ending in {@code ::*}
	 * @param value The value to set, or null to delete the variable or clear the list.
	 *              (If {@code name} ends in {@code ::*}, {@code value} must be null.)
	 * @return The previous value, or null if there was none. Always null for lists.
	 * @throws IllegalArgumentException If {@code name} is a list name and {@code value} isn't null
	 */
	public @Nullable Object setVariable(String name, @Nullable Object value) {
		if (name.endsWith(LIST_SUFFIX)) {
			if (value != null)
				throw new IllegalArgumentException("Cannot set list variable '" + name + "' to a single value (" + value + ").");
			// TODO:
			return null;
		}
		Object previous = value == null ? valueRemove(name) : valuePut(name, value);
		// if we are adding/removing a value here, then we need to make sure the branch tree is up to date.
		if ((previous == null) != (value == null)) {
			lock();
			try {
				reconcile(name, value != null);
			} finally {
				unlock();
			}
		}
		return previous;
	}

	/**
	 * Gets the number of variables in this map. Lists aren't counted, only the values inside them.
	 * This is only an estimate for concurrent maps if writes are happening at the same time.
	 *
	 * @return The number of variables with a value.
	 */
	public long size() {
		return concurrent ? sharedValues().mappingCount() : localValues().size();
	}

	/**
	 * @return Whether this map has no variables.
	 */
	public boolean isEmpty() {
		return concurrent ? sharedValues().isEmpty() : localValues().isEmpty();
	}

	/**
	 * A live view of one branch, which is what reading a list variable returns.
	 * It holds no data of its own and reads everything from the branch and {@link #values} as it's needed,
	 * so creating one is cheap, reading a list copies nothing, and any later change to the list shows up here.
	 * <p>
	 * {@link AbstractMap} provides the rest of the map, including throwing on anything that tries to modify it.
	 * Its {@code get}, {@code containsKey} and {@code size} walk every entry though, so those are overridden.
	 * <p>
	 * For concurrent maps this is weakly consistent. A value can show up in {@link #values} slightly before it
	 * shows up here, and values can disappear while iterating, so anything that reads as null is treated as not there.
	 */
	private final class View extends AbstractMap<String, Object> implements VariableList {
		private final VariableBranch branch; // source of the list
		private final boolean includeOwnValue; // whether to include the value of {a::b} as the null key value in the view, assuming list is {a::b::*}

		/**
		 * Creates a new view of a branch.
		 *
		 * @param branch          the branch to view
		 * @param includeOwnValue whether the {@link Map} methods include the list's own value under the null key
		 */
		View(VariableBranch branch, boolean includeOwnValue) {
			this.branch = branch;
			this.includeOwnValue = includeOwnValue;
		}

		/**
		 * Translates a child entry into what callers should see, so they never get a full name or a branch.
		 * A branch can only be empty if it's in the middle of being pruned, so all that's left to show is its value.
		 *
		 * @param entry The child entry, which is the child's full name or its {@link VariableBranch}
		 * @return The child's value, a nested view if it has children, or null if it was just deleted
		 */
		private @Nullable Object exposed(Object entry) {
			if (entry instanceof String leaf)
				return valueGet(leaf);
			VariableBranch child = (VariableBranch) entry;
			return child.childCount > 0 ? new View(child, true) : valueGet(child.fullName);
		}

		/**
		 * Gets a direct child of this list.
		 *
		 * @param key The child's key, or null for the list's own value if this view includes it
		 * @return The child's value, a nested view if it has children, or null if there's no such child
		 */
		@Override
		public @Nullable Object get(Object key) {
			// {a::b}
			if (key == null)
				return includeOwnValue ? ownValue() : null;
			// bad key
			if (!(key instanceof String name))
				return null;
			// {a::b::<key>}
			Object entry = childGet(branch, name);
			return entry == null ? null : exposed(entry);
		}

		/**
		 * Gets the entries of this list, in variable name order.
		 * If this view includes the list's own value, it comes first under the null key.
		 * The entries can't be changed, and neither can the set.
		 *
		 * @return A view of the entries, which walks the live children on each iteration.
		 */
		@Override
		public @NotNull Set<Entry<String, Object>> entrySet() {
			return new AbstractSet<>() {
				@Override
				public Iterator<Entry<String, Object>> iterator() {
					Object own = includeOwnValue ? ownValue() : null;
					Iterator<Entry<String, Object>> children = childIterator(branch);
					return new Iterator<>() {
						// start with own value as the null entry
						private @Nullable Entry<String, Object> next = own != null ? new SimpleImmutableEntry<>(null, own) : null;

						@Override
						public boolean hasNext() {
							// map each child to its value and provide in an entry
							while (next == null && children.hasNext()) {
								Entry<String, Object> child = children.next();
								Object value = exposed(child.getValue());
								if (value != null)
									next = new SimpleImmutableEntry<>(child.getKey(), value);
							}
							return next != null;
						}

						@Override
						public Entry<String, Object> next() {
							if (!hasNext())
								throw new NoSuchElementException();
							Entry<String, Object> entry = next;
							next = null;
							return entry;
						}
					};
				}

				@Override
				public int size() {
					return View.this.size();
				}
			};
		}

		@Override
		public @Nullable Object ownValue() {
			return valueGet(branch.fullName);
		}

		@Override
		public @UnmodifiableView Map<String, Object> children() {
			if (!includeOwnValue)
				return this;
			return new View(branch, false);
		}

		@Override
		public int valueCount() {
			return branch.valueChildren;
		}

		@Override
		public int nextOpenIndex() {
			return branch.nextIndex;
		}

		/**
		 * Gets the number of entries in this view, which includes sublists with no value of their own.
		 * Use {@link #valueCount()} for what a script considers the size of a list.
		 *
		 * @return The number of direct children, plus one if this view includes a set own value.
		 */
		@Override
		public int size() {
			return branch.childCount + ((includeOwnValue && branch.hasValue) ? 1 : 0);
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public boolean containsKey(Object key) {
			return get(key) != null;
		}

	}

	/**
	 * Finds the separator that splits a name into its parent and its key.
	 * This has to scan left to right, not use {@link String#lastIndexOf(String)}, so that it matches
	 * {@link ch.njol.skript.variables.Variables#splitVariableName}. The two disagree on names like {@code a:::b}.
	 *
	 * @param string The full variable name
	 * @return The index of the last separator, or -1 if there are none
	 */
	static int lastSeparator(String string) {
		int index = -1;
		int nextIndex = 0;
		while ((nextIndex = string.indexOf(Variable.SEPARATOR, nextIndex)) != -1) {
			index = nextIndex;
			nextIndex += Variable.SEPARATOR.length();
		}
		return index;
	}

}
