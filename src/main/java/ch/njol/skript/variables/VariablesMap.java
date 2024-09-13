/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A map for storing variables in a sorted and efficient manner.
 */
final class VariablesMap {

	/**
	 * The comparator for comparing variable names.
	 */
	static final Comparator<String> VARIABLE_NAME_COMPARATOR = (s1, s2) -> {
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

			if ('0' <= c1 && c1 <= '9' && '0' <= c2 && c2 <= '9') {
				// Numbers/digits are treated differently from other characters.

				// The index after the last digit
				int i2 = StringUtils.findLastDigit(s1, i);
				int j2 = StringUtils.findLastDigit(s2, j);

				// Amount of leading zeroes
				int z1 = 0;
				int z2 = 0;

				// Skip leading zeroes (except for the last if all 0's)
				if (!afterDecimalPoint) {
					if (c1 == '0') {
						while (i < i2 - 1 && s1.charAt(i) == '0') {
							i++;
							z1++;
						}
					}
					if (c2 == '0') {
						while (j < j2 - 1 && s2.charAt(j) == '0') {
							j++;
							z2++;
						}
					}
				}
				// Keep in mind that c1 and c2 may not have the right value (e.g. s1.charAt(i)) for the rest of this block

				// If the number is prefixed by a '-', it should be treated as negative, thus inverting the order.
				// If the previous number was negative, and the only thing separating them was a '.',
				//  then this number should also be in inverted order.
				boolean previousNegative = lastNumberNegative;

				// i - z1 contains the first digit, so i - z1 - 1 may contain a `-` indicating this number is negative
				lastNumberNegative = i - z1 > 0 && s1.charAt(i - z1 - 1) == '-';
				int isPositive = (lastNumberNegative | previousNegative) ? -1 : 1;

				// Different length numbers (99 > 9)
				if (!afterDecimalPoint && i2 - i != j2 - j)
					return ((i2 - i) - (j2 - j)) * isPositive;

				// Iterate over the digits
				while (i < i2 && j < j2) {
					char d1 = s1.charAt(i);
					char d2 = s2.charAt(j);

					// If the digits differ, return a value dependent on the sign
					if (d1 != d2)
						return (d1 - d2) * isPositive;

					i++;
					j++;
				}

				// Different length numbers (1.99 > 1.9)
				if (afterDecimalPoint && i2 - i != j2 - j)
					return ((i2 - i) - (j2 - j)) * isPositive;

				// If the numbers are equal, but either has leading zeroes,
				//  more leading zeroes is a lesser number (01 < 1)
				if (z1 != z2)
					return (z1 - z2) * isPositive;

				afterDecimalPoint = true;
			} else {
				// Normal characters
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
		if (i < s1.length())
			return lastNumberNegative ? -1 : 1;
		if (j < s2.length())
			return lastNumberNegative ? 1 : -1;
		return 0;
	};

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
			Object childNode = parent.get(childNodeName);

			if (childNode == null) {
				// Expected child node not found
				if (i == split.length - 1) {
					// End of the variable name reached, set variable if needed
					if (value != null)
						parent.put(childNodeName, value);

					break;
				} else if (value != null) {
					// Create child node, add it to parent and continue iteration
					childNode = new TreeMap<>(VARIABLE_NAME_COMPARATOR);

					parent.put(childNodeName, childNode);
					parent = (TreeMap<String, Object>) childNode;
				} else {
					// Want to set variable to null, bu variable is already null
					break;
				}
			} else if (childNode instanceof TreeMap) {
				// Child node found
				TreeMap<String, Object> childNodeMap = ((TreeMap<String, Object>) childNode);

				if (i == split.length - 1) {
					// End of variable name reached, adjust child node accordingly
					if (value == null)
						childNodeMap.remove(null);
					else
						childNodeMap.put(null, value);

					break;
				} else if (i == split.length - 2 && split[i + 1].equals("*")) {
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
				if (i == split.length - 1) {
					// If we arrived at the end of the variable name, update parent
					if (value == null)
						parent.remove(childNodeName);
					else
						parent.put(childNodeName, value);

					break;
				} else if (value != null) {
					// Need to continue iteration, create new child node and put old value in it
					TreeMap<String, Object> newChildNodeMap = new TreeMap<>(VARIABLE_NAME_COMPARATOR);
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
		TreeMap<String, Object> copy = new TreeMap<>(VARIABLE_NAME_COMPARATOR);

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
