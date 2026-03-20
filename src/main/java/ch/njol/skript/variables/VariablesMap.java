package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import ch.njol.util.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.errorprone.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A thread-safe Radix Tree for storing variables.
 */
@ThreadSafe
public final class VariablesMap {

	/**
	 * Comparator for variable names.
	 */
	public static final Comparator<String> VARIABLE_NAME_COMP = (s1, s2) -> {
		if (s1 == null)
			return s2 == null ? 0 : -1;
		if (s2 == null)
			return 1;

		int i = 0;
		int j = 0;

		boolean lastNumberNegative = false;
		boolean afterDecimalPoint = false;

		while (i < s1.length() && j < s2.length()) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(j);

			// Numbers/digits are treated differently from other characters.
			if (Character.isDigit(c1) && Character.isDigit(c2)) {

				// The index after the last digit
				int end1 = StringUtils.findLastDigit(s1, i);
				int end2 = StringUtils.findLastDigit(s2, j);

				// Amount of leading zeroes
				int leadingZeros1 = 0;
				int leadingZeros2 = 0;

				if (!afterDecimalPoint) {
					while (i < end1 - 1 && s1.charAt(i) == '0') {
						i++;
						leadingZeros1++;
					}
					while (j < end2 - 1 && s2.charAt(j) == '0') {
						j++;
						leadingZeros2++;
					}
				}

				// If the number is prefixed by a '-', it should be treated as negative, thus inverting the order.
				// If the previous number was negative, and the only thing separating them was a '.',
				//  then this number should also be in inverted order.
				int startOfNumber = i - leadingZeros1;
				boolean currentIsNegative = startOfNumber > 0 && s1.charAt(startOfNumber - 1) == '-';

				// if the previous number was negative and we just crossed a dot, we stay negative
				boolean effectiveNegative = currentIsNegative || lastNumberNegative;
				int sign = effectiveNegative ? -1 : 1;

				int length1 = end1 - i;
				int length2 = end2 - j;

				// Different length numbers (99 > 9)
				if (!afterDecimalPoint && length1 != length2)
					return (length1 - length2) * sign;

				// Iterate over the digits
				while (i < end1 && j < end2) {
					int diff = s1.charAt(i) - s2.charAt(j);
					if (diff != 0)
						return diff * sign;
					i++;
					j++;
				}

				// Different length numbers (1.99 > 1.9)
				if (afterDecimalPoint && length1 != length2)
					return (length1 - length2) * sign;

				// If the numbers are equal, but either has leading zeroes,
				//  more leading zeroes is a lesser number (01 < 1)
				if (leadingZeros1 != leadingZeros2)
					return (leadingZeros1 - leadingZeros2) * sign;

				// We finished processing a number, we are now "after" a number.
				// If the next char is a dot, we remain in decimal mode.
				afterDecimalPoint = true;
				// this is for backwards compatibility, else it should be effectiveNegative
				lastNumberNegative = currentIsNegative;
			}
			// Normal characters
			else {
				if (c1 != c2)
					return c1 - c2;

				// Reset the last number flags if we're exiting a number.
				if (c1 != '.') {
					lastNumberNegative = false;
					afterDecimalPoint = false;
				}

				i++;
				j++;
			}
		}

		// One is prefix of the other
		if (i < s1.length())
			return lastNumberNegative ? -1 : 1;
		if (j < s2.length())
			return lastNumberNegative ? 1 : -1;
		return 0;
	};

	/**
	 * A node in the radix tree.
	 * <p>
	 * This also serves as a thread safe unmodifiable live view of the tree branch branch in
	 * the format returned by {@link #getVariable(String)}.
	 * <p>
	 * It does not lock the tree and is weakly consistent, modifications to the underlying
	 * variables map by other threads may not be immediately visible, prioritizing performance
	 * over strict in time snapshots. There is no way to verify whether the node is still
	 * valid (part of the tree).
	 */
	private static class Node extends AbstractMap<String, Object> {

		/**
		 * Lock that is read locked when entering to the node
		 * and released when the operation that locked it, is complete.
		 * <p>
		 * Write lock is acquired only when clearing an entire subtree
		 * of a node to ensure all operations on that subtree are
		 * completed before it is cleared or when calling {@link #prune()}
		 * which needs to lock the entire tree.
		 */
		final StampedLock lock = new StampedLock();

		/**
		 * Current value assigned to this variable or {@code null} if
		 * no value is set.
		 */
		final AtomicReference<@Nullable Object> ref = new AtomicReference<>();

		/**
		 * Children of this node.
		 * <p>
		 * This uses {@link ConcurrentSkipListMap} because:
		 * <br>
		 * <li>The iterator used by the live-view of this node must be thread safe</li>
		 * <li>The map itself must be thread safe as it is modified under read lock of the node</li>
		 * <li>The children need to be sorted by the variables name compare</li>
		 * </br>
		 * This allows us to use the live view of this map as a view of this node
		 * (if transformed to match the map format of {@link #getVariable(String)}).
		 */
		final Map<String, Node> children = new ConcurrentSkipListMap<>(VARIABLE_NAME_COMP);

		/**
		 * @return whether the node has children
		 */
		boolean hasChildren() {
			return !children.isEmpty();
		}

		/**
		 * @return whether the node is empty (has no value and no children)
		 */
		@Override
		public boolean isEmpty() {
			return ref.get() == null && !hasChildren();
		}

		@Override
		public int size() {
			int size = children.size();
			return ref.get() != null ? ++size : size; // include the value if present as it is mapped to null key
		}

		@Override
		public boolean containsKey(Object key) {
			return get(key) != null;
		}

		@Override
		public Object get(Object key) {
			if (key == null)
				return ref.get();
			Node child = children.get(key);
			return child != null ? child.unwrap() : null;
		}

		@Override
		public @NotNull Set<Entry<String, Object>> entrySet() {
			return new AbstractSet<>() {
				@Override
				@SuppressWarnings({"rawtypes", "unchecked"})
				public @NotNull Iterator iterator() {
					Object value = Node.this.ref.get();

					Iterator<Entry<String, Node>> wrapped = children.entrySet().iterator();
					Iterator<Entry<String, Object>> iterator;

					if (value != null) {
						// concat iterator with the value of this node if present
						Iterator<Entry<String, Object>> itself =
							(Iterator) Collections.singleton(new SimpleEntry<>(null, value)).iterator();
						// source iterators are not polled until necessary, the null key is first
						iterator = Iterators.concat(itself, (Iterator) wrapped);
					} else {
						iterator = (Iterator) wrapped;
					}

					// this transformation is lazy
					return Iterators.transform(iterator, entry -> {
						if (entry.getKey() != null /* sub tree */) {
							Node node = (Node) entry.getValue();
							return new SimpleEntry<>(entry.getKey(), node.unwrap());
						} else {
							return entry; // null key with value of this node
						}
					});
				}

				@Override
				public int size() {
					return Node.this.size();
				}
			};
		}

		/**
		 * @return returns the representation of this node in the exposed map
		 */
		private Object unwrap() {
			return hasChildren() ? this : ref.get();
		}

	}

	/**
	 * Root node of the tree.
	 */
	private final Node root = new Node();

	/**
	 * Estimate of empty branches in the radix tree.
	 * <p>
	 * The real number may be different as some branches may be re-populated after clear.
	 */
	private final AtomicInteger leftEmpty = new AtomicInteger(0);

	/**
	 * At how many writes that leave empty branches {@link #prune()} should be executed.
	 */
	private final int pruneAt;

	/**
	 * Executor of automatic prune operation.
	 */
	private final Executor pruneExecutor;

	/**
	 * Constructs new variables map that automatically calls {@link #prune()}
	 * after certain number of {@link #setVariable(String, Object)} left
	 * empty branches in the radix tree.
	 *
	 * @param pruneAt after which number of such writes the variables map should call prune
	 * @param pruneExecutor executor which will execute the expensive prune operation
	 */
	public VariablesMap(int pruneAt, Executor pruneExecutor) {
		this.pruneAt = pruneAt;
		this.pruneExecutor = pruneExecutor;
	}

	/**
	 * Constructs new variables map that automatically calls {@link #prune()}
	 * after certain number of {@link #setVariable(String, Object)} left
	 * empty branches in the radix tree.
	 *
	 * @param pruneAt after which number of such writes the variables map should call prune
	 */
	public VariablesMap(int pruneAt) {
		this(pruneAt, Runnable::run);
	}

	/**
	 * Constructs new variables map.
	 */
	public VariablesMap() {
		this(Integer.MAX_VALUE, Runnable::run);
	}

	/**
	 * Returns the value of the requested variable.
	 * <p>
	 * In case of list variables, the returned map is thread safe unmodifiable live view of the variables map.
	 * <p>
	 * If map is returned, it is sorted using the variables name comparator.
	 * <p>
	 * If map is returned the structure is as following:
	 * <ul>
	 *     <li>
	 *         If value is present for the variable and
	 *         <ul>
	 *             <li>the variable has no children, its value is mapped directly to the key</li>
	 *             <li>the variable has children, it is mapped to a map, that maps {@code null} to its value and its
	 *             children are mapped using the same strategy</li>
	 *         </ul>
	 *     </li>
	 *     <li>If value is not present for the variable, it is mapped to a map with its children mapped using the same
	 *     strategy</li>
	 * </ul>
	 *
	 * @param name the name of the variable, possibly a list variable.
	 * @return an {@link Object} for a normal variable or a
	 * {@code Map<String, Object>} for a list variable,
	 * or {@code null} if the variable is not set.
	 */
	public @Nullable Object getVariable(String name) {
		boolean isList = name.endsWith(Variable.SEPARATOR + "*");
		if (isList)
			name = name.substring(0, name.length() - (Variable.SEPARATOR.length() + 1)); // strip the "::*" suffix

		String[] parts = Variables.splitVariableName(name);

		int limit = parts.length + 1; // +1 for the root node
		Node[] path = new Node[limit];
		long[] stamps = new long[limit];
		int depth = 0;

		Node current = root;
		path[0] = current;
		stamps[0] = current.lock.readLock();

		try {
			for (String part : parts) {
				if (!current.hasChildren())
					return null;
				Node next = current.children.get(part);
				if (next == null)
					return null;

				long nextStamp = next.lock.readLock();

				depth++;
				path[depth] = next;
				stamps[depth] = nextStamp;

				current = next;
			}

			if (isList) {
				return current;
			} else {
				return current.ref.get();
			}
		} finally {
			for (int i = depth; i >= 0; i--) {
				if (path[i] != null)
					path[i].lock.unlockRead(stamps[i]);
			}
		}
	}

	/**
	 * Sets the given variable to the given value.
	 * <p>
	 * This method accepts list variables,
	 * but these may only be set to {@code null}.
	 *
	 * @param name the variable name.
	 * @param value the variable value, {@code null} to delete the variable.
	 * @return previous value for changed variable, {@code null} if not set or
	 * the variable is a list that was cleared
	 */
	public @Nullable Object setVariable(String name, @Nullable Object value) {
		boolean isList = name.endsWith(Variable.SEPARATOR + "*");

		String actualName = isList
			? name.substring(0, name.length() - (Variable.SEPARATOR.length() + 1))
			: name;

		if (isList) {
			Preconditions.checkState(value == null, "List variables can only be set to null");
		}

		String[] parts = Variables.splitVariableName(actualName);

		if (isList) { // we are clearing a list
			clearListVariable(root, parts);
			return null;
		} else {
			return modifySingleVariable(root, parts, old -> value /* discard previous, set to new */);
		}
	}

	/**
	 * Returns the variable with given name and if there is none set, sets it to
	 * the next value provided by the mapping function.
	 * <p>
	 * This method only accepts single variables.
	 * <p>
	 * The {@code mappingFunction} is executed under a write lock on the variable's node.
	 * Do not perform expensive operations or access other variables inside this function to avoid
	 * deadlock and performance degradation.
	 *
	 * @param name the variable name.
	 * @param mappingFunction function providing the new value in case it is not set
	 * @return current value of the variable
	 */
	public Object computeIfAbsent(String name, Function<? super String, ? super Object> mappingFunction) {
		Preconditions.checkState(!name.endsWith(Variable.SEPARATOR + "*"));
		AtomicReference<Object> got = new AtomicReference<>();
		String[] parts = Variables.splitVariableName(name);
		modifySingleVariable(root, parts, prev -> {
			if (prev == null) {
				Object computed = mappingFunction.apply(name);
				got.set(computed);
				return computed;
			}
			got.set(prev);
			return prev;
		});
		return got.get();
	}

	/**
	 * Applies operation at node of given variable under its write lock.
	 *
	 * @param root root node
	 * @param parts parts of the variable
	 * @param operation operation to apply
	 * @return value associated with the node before the operation
	 */
	private @Nullable Object modifySingleVariable(Node root, String[] parts, UnaryOperator<Object> operation) {
		int limit = parts.length + 1; // +1 for the root node
		Node[] path = new Node[limit];
		long[] stamps = new long[limit];
		int depth = 0;

		Node current = root;
		path[0] = current;
		stamps[0] = current.lock.readLock();

		try {
			for (String part : parts) {
				// this can be done under read lock because the children map implementation
				// itself is thread safe
				Node next = current.children.computeIfAbsent(part, key -> new Node());

				long nextStamp = next.lock.readLock();

				depth++;
				path[depth] = next;
				stamps[depth] = nextStamp;

				current = next;
			}

			Object prev = current.ref.getAndUpdate(operation);
			checkForPrune(current);
			return prev;
		} finally {
			for (int i = depth; i >= 0; i--) {
				if (path[i] != null)
					path[i].lock.unlock(stamps[i]);
			}
		}
	}

	/**
	 * Clears a list variable.
	 * <p>
	 * Compare to other operations, this one functions a lot differently and
	 * does not need to read lock the entire path.
	 * <p>
	 * This is the only operation that uses the write lock of the node it is clearing.
	 * Reason for this is, all operations happening on this part of the sub-tree
	 * also hold a read lock for this particular node, meaning it:
	 * <ul>
	 *     <li>Stops any other future operations from happening until the list clear completes</li>
	 *     <li>Waits for all operations happening in the sub-tree to finish</li>
	 * </ul>
	 * This ensures no invalid values are returned (different thread could return already deleted
	 * values otherwise).
	 *
	 * @param root root node
	 * @param parts parts of the variable to clear
	 */
	private void clearListVariable(Node root, String[] parts) {
		Node current = root;
		for (String key : parts) {
			Node next = current.children.get(key);
			// if child does not exist there is nothing to clear
			if (next == null)
				return;
			current = next;
		}
		long stamp = current.lock.writeLock();
		try {
			current.children.clear();
		} finally {
			current.lock.unlockWrite(stamp);
			checkForPrune(current);
		}
	}

	/**
	 * Checks if the node if empty, if yes, it increases the
	 * empty nodes counter and possibly triggers automatic {@link #prune()} call
	 * if the number of empty nodes exceeds {@link #pruneAt}.
	 * <p>
	 * This does not have to be fully accurate and atomic with the clear operations
	 * themselves, as {@link #leftEmpty} is only an estimate.
	 *
	 * @param node node to check after appplying an operation
	 */
	private void checkForPrune(Node node) {
		if (!node.isEmpty())
			return;
		int count = leftEmpty.incrementAndGet();
		if (count >= pruneAt && leftEmpty.compareAndSet(count, 0)) {
			pruneExecutor.execute(this::prune);
		}
	}

	/**
	 * Prunes the entire tree, removing all empty nodes.
	 * <p>
	 * This operation is expensive and fully write locks the radix tree.
	 */
	public void prune() {
		prune(root);
	}

	private boolean prune(Node node) {
		long stamp = node.lock.writeLock();
		try {
			if (node.isEmpty())
				return true;

			var it = node.children.entrySet().iterator();
			while (it.hasNext()) {
				var entry = it.next();
				boolean isChildEmpty = prune(entry.getValue());
				if (isChildEmpty)
					it.remove();
			}

			return node.isEmpty();
		} finally {
			node.lock.unlockWrite(stamp);
		}
	}

	/**
	 * Creates a copy of this map.
	 *
	 * @return the copy.
	 */
	public VariablesMap copy() {
		VariablesMap copy = new VariablesMap();
		copy(this.root, copy.root);
		return copy;
	}

	private void copy(Node source, Node target) {
		long stamp = source.lock.readLock();
		try {
			target.ref.set(source.ref.get());
			if (source.hasChildren()) {
				source.children.forEach((key, sourceChild) -> {
					Node targetChild = new Node();
					copy(sourceChild, targetChild);
					target.children.put(key, targetChild);
				});
			}
		} finally {
			source.lock.unlockRead(stamp);
		}
	}

	/**
	 * @return whether the variables map is empty
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Returns all variables in this map.
	 * <p>
	 * The map is unmodifiable and ordered in the variables name order.
	 * <p>
	 * This map is not nested and contains variables in format {@code full key <-> value}
	 *
	 * @return all variables in this map
	 */
	public @Unmodifiable Map<String, Object> getAll() {
		Map<String, Object> all = new TreeMap<>(VARIABLE_NAME_COMP);
		getAll("", root, all::put);
		return Collections.unmodifiableMap(all);
	}

	private void getAll(String buffer, Node source, BiConsumer<String, Object> collector) {
		long stamp = source.lock.readLock();
		try {
			if (source.ref.get() != null)
				collector.accept(buffer, source.ref.get());
			if (source.hasChildren()) {
				source.children.forEach((key, child) -> {
					String nextName = buffer.isEmpty() ? key : buffer + Variable.SEPARATOR + key;
					getAll(nextName, child, collector);
				});
			}
		} finally {
			source.lock.unlockRead(stamp);
		}
	}

	/**
	 * Returns number of variables in this map.
	 *
	 * @return number of variables in this map
	 */
	public long size() {
		return size(root);
	}

	private long size(Node node) {
		long stamp = node.lock.readLock();
		long size = 0;
		try {
			if (node.ref.get() != null)
				size++;
			if (node.hasChildren()) {
				for (Node child : node.children.values())
					size += size(child);
			}
		} finally {
			node.lock.unlockRead(stamp);
		}
		return size;
	}

}
