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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;

final class VariablesMap {

	final static Comparator<String> variableNameComparator = new Comparator<String>() {
		@Override
		public int compare(@Nullable String s1, @Nullable String s2) {
			if (s1 == null)
				return s2 == null ? 0 : -1;

			if (s2 == null)
				return 1;

			int i = 0;
			int j = 0;

			boolean lastNumberNegative = false;
			while (i < s1.length() && j < s2.length()) {
				char c1 = s1.charAt(i);
				char c2 = s2.charAt(j);

				if ('0' <= c1 && c1 <= '9' && '0' <= c2 && c2 <= '9') {
					// Numbers/digits are treated differently from other characters.
					int i2 = StringUtils.findLastDigit(s1, i);
					int j2 = StringUtils.findLastDigit(s2, j);

					long n1 = Utils.parseLong("" + s1.substring(i, i2));
					long n2 = Utils.parseLong("" + s2.substring(j, j2));

					// If the number is prefixed by a '-', it should be treated as negative, thus inverting the order.
					// If the previous number was negative, and the only thing separating them was a '.',
					//  then this number should also be in inverted order.
					boolean previousNegative = lastNumberNegative;

					lastNumberNegative = i > 0 && s1.charAt(i - 1) == '-';
					int isPositive = (lastNumberNegative | previousNegative) ? -1 : 1;

					if (n1 > n2)
						return isPositive;

					if (n1 < n2)
						return -1 * isPositive;

					// Represent same number, but different length, indicating leading zeros
					if (i2 - i > j2 - j)
						return -1;
					if (i2 - i < j2 - j)
						return 1;

					i = i2;
					j = j2;
				} else {
					// Normal characters
					if (c1 > c2)
						return 1;
					if (c1 < c2)
						return -1;
					// Reset the last number flag if we're exiting a number.
					if (c1 != '.')
						lastNumberNegative = false;
					i++;
					j++;
				}
			}
			if (i < s1.length())
				return lastNumberNegative ? -1 : 1;
			if (j < s2.length())
				return lastNumberNegative ? 1 : -1;
			return 0;
		}
	};
	
	final HashMap<String, Object> hashMap = new HashMap<>();
	final TreeMap<String, Object> treeMap = new TreeMap<>();
	
	/**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 * 
	 * @param name
	 * @return an Object for a normal Variable or a Map<String, Object> for a list variable, or null if the variable is not set.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	final Object getVariable(String name) {
		if (!name.endsWith("*")) {
			return hashMap.get(name);
		} else {
			String[] split = Variables.splitVariableName(name);
			Map<String, Object> current = treeMap;
			for (int i = 0; i < split.length; i++) {
				String n = split[i];
				if (n.equals("*")) {
					assert i == split.length - 1;
					return current;
				}
				Object o = current.get(n);
				if (o == null)
					return null;
				if (o instanceof Map) {
					current = (Map<String, Object>) o;
					assert i != split.length - 1;
					continue;
				} else {
					return null;
				}
			}
			return null;
		}
	}
	
	/**
	 * Sets a variable.
	 * 
	 * @param name The variable's name. Can be a "list variable::*" (<tt>value</tt> must be <tt>null</tt> in this case)
	 * @param value The variable's value. Use <tt>null</tt> to delete the variable.
	 */
	@SuppressWarnings("unchecked")
	final void setVariable(String name, @Nullable Object value) {
		if (!name.endsWith("*")) {
			if (value == null)
				hashMap.remove(name);
			else
				hashMap.put(name, value);
		}
		String[] split = Variables.splitVariableName(name);
		TreeMap<String, Object> parent = treeMap;
		for (int i = 0; i < split.length; i++) {
			String n = split[i];
			Object current = parent.get(n);
			if (current == null) {
				if (i == split.length - 1) {
					if (value != null)
						parent.put(n, value);
					break;
				} else if (value != null) {
					parent.put(n, current = new TreeMap<>(variableNameComparator));
					parent = (TreeMap<String, Object>) current;
					continue;
				} else {
					break;
				}
			} else if (current instanceof TreeMap) {
				if (i == split.length - 1) {
					if (value == null)
						((TreeMap<String, Object>) current).remove(null);
					else
						((TreeMap<String, Object>) current).put(null, value);
					break;
				} else if (i == split.length - 2 && split[i + 1].equals("*")) {
					assert value == null;
					deleteFromHashMap(StringUtils.join(split, Variable.SEPARATOR, 0, i + 1), (TreeMap<String, Object>) current);
					Object v = ((TreeMap<String, Object>) current).get(null);
					if (v == null)
						parent.remove(n);
					else
						parent.put(n, v);
					break;
				} else {
					parent = (TreeMap<String, Object>) current;
					continue;
				}
			} else {
				if (i == split.length - 1) {
					if (value == null)
						parent.remove(n);
					else
						parent.put(n, value);
					break;
				} else if (value != null) {
					TreeMap<String, Object> c = new TreeMap<>(variableNameComparator);
					c.put(null, current);
					parent.put(n, c);
					parent = c;
					continue;
				} else {
					break;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	void deleteFromHashMap(String parent, TreeMap<String, Object> current) {
		for (Entry<String, Object> e : current.entrySet()) {
			if (e.getKey() == null)
				continue;
			hashMap.remove(parent + Variable.SEPARATOR + e.getKey());
			Object val = e.getValue();
			if (val instanceof TreeMap) {
				deleteFromHashMap(parent + Variable.SEPARATOR + e.getKey(), (TreeMap<String, Object>) val);
			}
		}
	}
	
}
