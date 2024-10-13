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
package ch.njol.skript.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * Map for fast access of entry nodes and section nodes within section nodes.
 * 
 * @author Peter Güttinger
 */
public class NodeMap {
	
	private final Map<String, Node> map = new HashMap<>();
	
	public static boolean inMap(final Node n) {
		return n instanceof EntryNode || n instanceof SectionNode;
	}
	
	private static String getKey(final Node n) {
		final String key = n.getKey();
		if (key == null) {
			assert false : n;
			return "";
		}
		return "" + key.toLowerCase(Locale.ENGLISH);
	}
	
	private static String getKey(final String key) {
		return "" + key.toLowerCase(Locale.ENGLISH);
	}
	
	public void put(final Node n) {
		if (!inMap(n))
			return;
		map.put(getKey(n), n);
	}
	
	@Nullable
	public Node remove(final Node n) {
		return remove(getKey(n));
	}
	
	@Nullable
	public Node remove(final @Nullable String key) {
		if (key == null)
			return null;
		return map.remove(getKey(key));
	}
	
	@Nullable
	public Node get(final @Nullable String key) {
		if (key == null)
			return null;
		return map.get(getKey(key));
	}
	
}
