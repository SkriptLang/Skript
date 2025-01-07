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