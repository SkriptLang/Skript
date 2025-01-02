package ch.njol.skript.variables;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Variable;

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

	private static final Queue<Map<String, Class<?>>> TYPE_HINTS = new LinkedBlockingQueue<>();

	static {
		clear(); // Initialize type hints
	}

	public static void add(String variable, Class<?> hint) {
		if (hint.equals(Object.class)) // Ignore useless type hint
			return;
		Map<String, Class<?>> hints = TYPE_HINTS.peek();
		Class<?> existing = hints.get(variable);
		if (existing != null && existing.equals(hint))
			return;
		hints.put(variable, hint);
	}

	/**
	 * Return any known type hints of a local variable.
	 * 
	 * @param variable The local variable expression to check against.
	 * @return The return type that the local variable has been set to otherwise null if unset.
	 */
	@Nullable
	public static Class<?> get(Variable<?> variable) {
		if (!variable.isLocal())
			throw new SkriptAPIException("Must only get TypeHints of local variables.");
		return get(variable.getName().toString());
	}

	@Nullable
	public static Class<?> get(String variable) {
		// Go through stack of hints for different scopes
		for (Map<String, Class<?>> hints : TYPE_HINTS) {
			Class<?> hint = hints.get(variable);
			if (hint != null) // Found in this scope
				return hint;
		}
		
		return null; // No type hint available
	}

	public static void enterScope() {
		TYPE_HINTS.add(new HashMap<>());
	}

	public static void exitScope() {
		TYPE_HINTS.poll();
	}

	public static void clear() {
		TYPE_HINTS.clear();
		TYPE_HINTS.add(new HashMap<>());
	}

}
