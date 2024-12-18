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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * This is used to manage local variable type hints.
 * 
 * <ul>
 * <li>EffChange adds then when local variables are set
 * <li>Variable checks them when parser tries to create it
 * <li>ScriptLoader clears hints after each section has been parsed
 * <li>ScriptLoader enters and exists scopes as needed
 * </ul>
 */
public class TypeHints {
	
	private static final Deque<Map<String, Class<?>>> typeHints = new ArrayDeque<>();
	
	static {
		clear(); // Initialize type hints
	}
	
	public static void add(String variable, Class<?> hint) {
		if (hint.equals(Object.class)) // Ignore useless type hint
			return;
		
		// Take top of stack, without removing it
		Map<String, Class<?>> hints = typeHints.getFirst();
		hints.put(variable, hint);
	}
	
	@Nullable
	public static Class<?> get(String variable) {
		// Go through stack of hints for different scopes
		for (Map<String, Class<?>> hints : typeHints) {
			Class<?> hint = hints.get(variable);
			if (hint != null) // Found in this scope
				return hint;
		}
		
		return null; // No type hint available
	}
	
	public static void enterScope() {
		typeHints.push(new HashMap<>());
	}
	
	public static void exitScope() {
		typeHints.pop();
	}
	
	public static void clear() {
		typeHints.clear();
		typeHints.push(new HashMap<>());
	}
}