package ch.njol.skript.variables;

import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Variable;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.IndexTrackingTreeMap;
import org.skriptlang.skript.variables.VariableNameComparator;

import java.util.*;
import java.util.Map.Entry;


/**
 * A map for storing variables in a sorted and efficient manner.
 */
final class VariablesMap {



	/**
	 * The map that stores all non-list variables.
	 */
	final HashMap<String, Object> hashMap = new HashMap<>();
	/**
	 * The tree of variables, branched by the list structure of the variables.
	 */
	final TreeMap<String, Object> treeMap = new TreeMap<>();

	/**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 *
	 * @param name the name of the variable, possibly a list variable.
	 * @return an {@link Object} for a normal variable or a
	 * {@code Map<String, Object>} for a list variable,
	 * or {@code null} if the variable is not set.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	Object getVariable(String name) {
		if (!name.endsWith("*")) {
			// Not a list variable, quick access from the hash map
			return hashMap.get(name);
		} else {
			// List variable, search the tree branches
			String[] split = Variables.splitVariableName(name);
			Map<String, Object> parent = treeMap;

			// Iterate over the parts of the variable name
			for (int i = 0; i < split.length; i++) {
				String n = split[i];
				if (n.equals("*")) {
					// End of variable name, return map
					assert i == split.length - 1;
					return parent;
				}

				// Check if the current (sub-)tree has the expected child node
				Object childNode = parent.get(n);
				if (childNode == null)
					return null;

				// Continue the iteration if the child node is a tree itself
				if (childNode instanceof Map) {
					// Continue iterating with the subtree
					parent = (Map<String, Object>) childNode;
					assert i != split.length - 1;
				} else {
					// ..., otherwise the list variable doesn't exist here
					return null;
				}
			}
			return null;
		}
	}

	/**
	 * Sets the given variable to the given value.
	 * <p>
	 * This method accepts list variables,
	 * but these may only be set to {@code null}.
	 * <p>
	 * If you are setting a list all at once, consider {@link #setList}
	 *
	 * @param name the variable name.
	 * @param value the variable value, {@code null} to delete the variable.
	 */
	@SuppressWarnings("unchecked")
	void setVariable(String name, @Nullable Object value) {
		// First update the hash map easily
		if (!name.endsWith("*")) {
			if (value == null)
				hashMap.remove(name);
			else
				hashMap.put(name, value);
		}

		// Then update the tree map by going down the branches
		String[] split = Variables.splitVariableName(name);
		TreeMap<String, Object> parent = treeMap;

		// Iterate over the parts of the variable name
		for (int i = 0; i < split.length; i++) {
			String childNodeName = split[i];

			if (i == split.length - 1) {
				// End of the variable name reached: write first, as that is one search instead of a get and a put.
				Object previous = value == null ? parent.remove(childNodeName) : parent.put(childNodeName, value);
				if (previous instanceof TreeMap) {
					// The variable is also a list, e.g. {mylist} while {mylist::3} is set,
					//  so restore the list and store the value under its null key instead.
					// This is a performance hit, but a -25% to common case is worth a +30% to rare case.
					TreeMap<String, Object> childNodeMap = (TreeMap<String, Object>) previous;
					parent.put(childNodeName, childNodeMap);
					if (value == null)
						childNodeMap.remove(null);
					else
						childNodeMap.put(null, value);
				}
				break;
			}

			Object childNode = parent.get(childNodeName);

			if (childNode == null) {
				// Expected child node not found
				if (value != null) {
					// Create child node, add it to parent and continue iteration
					childNode = new IndexTrackingTreeMap<>(VariableNameComparator.INSTANCE);

					parent.put(childNodeName, childNode);
					parent = (TreeMap<String, Object>) childNode;
				} else {
					// Want to set variable to null, bu variable is already null
					break;
				}
			} else if (childNode instanceof TreeMap) {
				// Child node found
				TreeMap<String, Object> childNodeMap = ((TreeMap<String, Object>) childNode);

				if (i == split.length - 2 && split[i + 1].equals("*")) {
					// Second to last part of variable name
					assert value == null;

					// Delete all indices of the list variable from hashMap
					deleteFromHashMap(StringUtils.join(split, Variable.SEPARATOR, 0, i + 1), childNodeMap);

					// If the list variable itself has a value ,
					//  e.g. list `{mylist::3}` while variable `{mylist}` also has a value,
					//  then adjust the parent for that
					Object currentChildValue = childNodeMap.get(null);
					if (currentChildValue == null)
						parent.remove(childNodeName);
					else
						parent.put(childNodeName, currentChildValue);

					break;
				} else {
					// Continue iteration
					parent = childNodeMap;
				}
			} else {
				// Ran into leaf node
				if (value != null) {
					// Need to continue iteration, create new child node and put old value in it
					TreeMap<String, Object> newChildNodeMap = new IndexTrackingTreeMap<>(VariableNameComparator.INSTANCE);
					newChildNodeMap.put(null, childNode);

					// Add new child node to parent
					parent.put(childNodeName, newChildNodeMap);
					parent = newChildNodeMap;
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Sets a list to the following keys and values.
	 * Does not lock or save anything: for global variables, the caller must hold the write lock and save the changes.
	 *
	 * @param listName The full name of the list, without ::* on the end.
	 * @param keys     The keys to use for the list's values
	 * @param values   The values to use in the list.
	 */
	@SuppressWarnings("unchecked")
	void setList(String listName, String[] keys, Object[] values) {
		// clear old list
		if (keys.length == 0) {
			setVariable(listName + Variable.SEPARATOR + "*", null); // nothing to write, just delete the list
			return;
		}
		// find location of new list, creating sublists on the way as setVariable does
		String[] split = Variables.splitVariableName(listName);
		TreeMap<String, Object> parent = treeMap;
		for (int i = 0; i < split.length - 1; i++) {
			Object child = parent.get(split[i]);
			if (child instanceof TreeMap) {
				// enter the child map
				parent = (TreeMap<String, Object>) child;
				continue;
			}
			// Nothing here yet, or a plain value in the way: make a sublist, keeping that value under its null key
			TreeMap<String, Object> sublist = new IndexTrackingTreeMap<>(VariableNameComparator.INSTANCE);
			if (child != null)
				sublist.put(null, child);
			parent.put(split[i], sublist);
			parent = sublist;
		}
		// verify sorting order
		if (!isStrictlyIncreasing(keys)) {
			KeyedValue<?>[] entries = sortedWithoutRepeats(keys, values);
			keys = new String[entries.length];
			values = new Object[entries.length];
			for (int i = 0; i < entries.length; i++) {
				keys[i] = entries[i].key();
				values[i] = entries[i].value();
			}
		}

		// Swap in the new list, then clean up whatever it replaced
		IndexTrackingTreeMap<Object> list = IndexTrackingTreeMap.ofSorted(VariableNameComparator.INSTANCE, keys, values);
		Object previous = parent.put(split[split.length - 1], list);
		Object listValue = previous; // {listName}'s existing value, if it has one
		if (previous instanceof TreeMap) {
			// if previously a list, clear it out but save the null key for our new map.
			TreeMap<String, Object> oldList = (TreeMap<String, Object>) previous;
			deleteFromHashMap(listName, oldList); // the old elements, including nested sublists
			listValue = oldList.get(null);
		}
		if (listValue != null)
			list.put(null, listValue);

		// ensure everything's in the hashmap too.
		String prefix = listName + Variable.SEPARATOR;
		for (int i = 0; i < keys.length; i++) {
			hashMap.put(prefix + keys[i], values[i]);
		}
	}

	/**
	 * @param keys the keys to check
	 * @return Whether the keys are in {@link VariableNameComparator#INSTANCE} order with no repeats, as
	 * {@link IndexTrackingTreeMap#ofSorted} requires.
	 */
	private static boolean isStrictlyIncreasing(String[] keys) {
		for (int i = 1; i < keys.length; i++) {
			if (VariableNameComparator.INSTANCE.compare(keys[i - 1], keys[i]) >= 0)
				return false;
		}
		return true;
	}

	/**
	 * Sorts the entries by key. A key that appears more than once keeps its last value,
	 * as writing each element in turn would.
	 *
	 * @param keys   the keys to sort
	 * @param values the values corresponding to each key
	 * @return an array of sorted key-value pairs.
	 */
	private static KeyedValue<?>[] sortedWithoutRepeats(String[] keys, Object[] values) {
		KeyedValue<?>[] entries = new KeyedValue<?>[keys.length];
		for (int i = 0; i < keys.length; i++)
			entries[i] = new KeyedValue<>(keys[i], values[i]);
		// stable, so entries with the same key stay in the order they were given
		Arrays.sort(entries, Comparator.comparing(KeyedValue<?>::key, VariableNameComparator.INSTANCE));

		// avoid duplicate keys.
		int count = 0;
		for (int i = 0; i < entries.length; i++) {
			boolean writtenAgainLater = i + 1 < entries.length
				&& VariableNameComparator.INSTANCE.compare(entries[i].key(), entries[i + 1].key()) == 0;
			if (!writtenAgainLater)
				entries[count++] = entries[i];
		}
		return count == entries.length ? entries : Arrays.copyOf(entries, count);
	}

	/**
	 * Deletes all indices of a list variable from the {@link #hashMap}.
	 *
	 * @param parent the list variable prefix,
	 *                  e.g. {@code list} for {@code list::*}.
	 * @param current the map of the list variable.
	 */
	@SuppressWarnings("unchecked")
	void deleteFromHashMap(String parent, TreeMap<String, Object> current) {
		for (Entry<String, Object> e : current.entrySet()) {
			if (e.getKey() == null)
				continue;
			String childName = parent + Variable.SEPARATOR + e.getKey();

			// Remove from hashMap
			hashMap.remove(childName);

			// Recurse if needed
			Object val = e.getValue();
			if (val instanceof TreeMap) {
				deleteFromHashMap(childName, (TreeMap<String, Object>) val);
			}
		}
	}

	/**
	 * Creates a copy of this map.
	 *
	 * @return the copy.
	 */
	public VariablesMap copy() {
		VariablesMap copy = new VariablesMap();

		copy.hashMap.putAll(hashMap);

		TreeMap<String, Object> treeMapCopy = copyTreeMap(treeMap);
		copy.treeMap.putAll(treeMapCopy);

		return copy;
	}

	/**
	 * Makes a deep copy of the given {@link TreeMap}.
	 * <p>
	 * The 'deep copy' means that each subtree of the given tree is copied
	 * as well.
	 *
	 * @param original the original tree map.
	 * @return the copy.
	 */
	@SuppressWarnings("unchecked")
	private static TreeMap<String, Object> copyTreeMap(TreeMap<String, Object> original) {
		TreeMap<String, Object> copy = new IndexTrackingTreeMap<>(VariableNameComparator.INSTANCE);

		for (Entry<String, Object> child : original.entrySet()) {
			String key = child.getKey();
			Object value = child.getValue();

			// Copy by recursion if the child is a TreeMap
			if (value instanceof TreeMap) {
				value = copyTreeMap((TreeMap<String, Object>) value);
			}

			copy.put(key, value);
		}

		return copy;
	}

}
