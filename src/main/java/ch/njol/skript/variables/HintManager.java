package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.SkriptLogger;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Used for managing local variable type hints during the parsing process.
 * <h4>Hint Tracking</h4>
 * <p>
 * Type hints are tracked in scopes which are essentially equivalent to sections.
 * Hints are only shared between scopes when they are entered or exited.
 * That is, when entering a scope ({@link #enterScope()}, it is initialized with the hints of the previous top-level scope.
 * When exiting a scope {@link #enterScope()}, remaining hints from that scope are added to the existing hints of the new top-level scope.
 * Thus, it is only necessary to obtain hints for the current scope.
 * {@link #get(Variable)} is provided for obtaining the hints of a variable in the current scope.
 * </p>
 * <h4>Hint Modification</h4>
 * <p>
 * The standard syntax where hints are modified is the Change Effect ({@link ch.njol.skript.effects.EffChange}).
 * Consider the following basic SET example:
 * <pre>
 * {@code
 * set {_x} to 5
 * # hint for {_x} is Integer.class
 * }
 * </pre>
 * </p>
 * A SET operation overrides all existing type hints for a variable <b>in the current scope</b> (see {@link #set(Variable, Class[])}).
 * In a more advanced example, we can see how hints are shared between scopes:
 * <pre>
 * {@code
 * set {_x} to 5
 * # hint for {_x} is Integer.class
 * if <some condition>:
 *   set {_x} to true
 *   # hint for {_x} is Boolean.class
 * # here, it is not known if the section above would have executed
 * # we consider all possible values
 * # thus, hints for {_x} are Integer.class and Boolean.class
 * }
 * </pre>
 * </p>
 * ADD is another considered operation (see {@link #add(Variable, Class[])}).
 * Consider the following example:
 * <pre>
 * {@code
 * add 5 to {_x::*}
 * # hint for {_x::*} is Integer.class
 * }
 * </pre>
 * Essentially, an ADD operation is handled similarly to a SET operation, but hints are combined rather than overridden,
 *  as the list may contain other types.
 * Note that REMOVE is <b>not</b> a handled operation, as a list variable might contain multiple values of some type.
 * Finally, a DELETE operation (see {@link #delete(Variable)}) allows us to trim down context where applicable.
 * Consider the following examples:
 * <pre>
 * {@code
 * set {_x} to 5
 * # hint for {_x} is Integer.class
 * delete {_x}
 * # now, there are no hints for {_x}
 * }
 * </pre>
 * <pre>
 * {@code
 * set {_x} to 5
 * # hint for {_x} is Integer.class
 * if <some condition>:
 *   delete {_x}
 *   # now, there are no hints for {_x}
 * # the previous section no longer had hints for {_x}, so there is nothing to copy over
 * # thus, hint for {_x} is Integer.class
 * }
 * </pre>
 * @see ParserInstance#getHintManager()
 */
public class HintManager {

	private final LinkedList<Map<String, Set<Class<?>>>> typeHints = new LinkedList<>();

	/**
	 * Enters a new scope for storing hints.
	 * Hints from the previous (current top-level) scope are copied over.
	 * @see #exitScope()
	 */
	public void enterScope() {
		typeHints.push(new HashMap<>());
		if (typeHints.size() > 1) { // copy existing values into new scope
			mergeScope(1, 0);
		}
	}

	/**
	 * Exits the current (top-level) scope.
	 * Hints from the exited scope will be copied over to the new top-level scope.
	 * @see #enterScope()
	 */
	public void exitScope() {
		if (typeHints.size() > 1) { // copy over updated hints
			mergeScope(0, 1);
		}
		typeHints.pop();
	}

	/**
	 * Resets (clears) all type hints for the current (top-level) scope.
	 * Scopes are represented as integers, where <code>0</code> represents the most recently entered scope
	 * (i.e. the scope pushed by the most recent {@link #enterScope()} call).
	 */
	public void clearScope(int level) {
		typeHints.get(level).clear();
	}

	/**
	 * Copies hints from one scope to another.
	 * Scopes are represented as integers, where <code>0</code> represents the most recently entered scope
	 * (i.e. the scope pushed by the most recent {@link #enterScope()} call).
	 * <p>
	 * <b>Note: This does not overwrite the existing hints of <code>to</code>. Instead, the hints are merged together.</b>
	 * @param from The scope to copy hints from.
 	 * @param to The scope to copy hints to.
	 */
	public void mergeScope(int from, int to) {
		var fromMap = typeHints.get(from);
		var toMap = typeHints.get(to);
		mergeHints(fromMap, toMap);
	}

	private static void mergeHints(Map<String, Set<Class<?>>> from, Map<String, Set<Class<?>>> to) {
		for (var entry : from.entrySet()) {
			to.computeIfAbsent(entry.getKey(), key -> new HashSet<>()).addAll(entry.getValue());
		}
	}

	/**
	 * Overrides hints for {@code variable} in the current scope.
	 * @param variable The variable to set {@code hints} for.
	 * @param hints The hint(s) to set for {@code variable}.
	 * @see #set(String, Class[])    
	 */
	public void set(Variable<?> variable, Class<?>... hints) {
		checkCanUseHints(variable);
		set(variable.getName().toString(null), hints);
	}

	/**
	 * Overrides hints for {@code variableName} in the current scope.
	 * @param variableName The name of the variable to set {@code hints} for.
	 * @param hints The hint(s) to set for {@code variableName}.
	 * @see #set(Variable, Class[])    
	 */
	public void set(String variableName, Class<?>... hints) {
		if (areHintsUnavailable()) {
			return;
		}

		Set<Class<?>> hintSet = new HashSet<>();
		for (Class<?> hint : hints) {
			if (hint != Object.class) { // ignore some useless types
				hintSet.add(hint);
			}
		}

		delete_i(variableName);
		if (!hintSet.isEmpty()) {
			add_i(variableName, hintSet);
		}
	}

	/**
	 * Deletes hints for {@code variable} in the current scope.
	 * @param variable The variable to clear hints for.
	 * @see #delete(String)     
	 */
	public void delete(Variable<?> variable) {
		checkCanUseHints(variable);
		delete(variable.getName().toString(null));
	}

	/**
	 * Deletes hints for {@code variableName} in the current scope.
	 * @param variableName The name of the variable to clear hints for.
	 * @see #delete(Variable)    
	 */
	public void delete(String variableName) {
		if (areHintsUnavailable()) {
			return;
		}
		delete_i(variableName);
	}

	private void delete_i(String variableName) {
		//noinspection DataFlowIssue
		typeHints.peek().remove(variableName);
	}

	/**
	 * Adds hints for {@code variable} in the current scope.
	 * @param variable The variable to add {@code hints} to.
	 * @param hints The hint(s) to add for {@code variable}.
	 * @see #add(String, Class[])    
	 */
	public void add(Variable<?> variable, Class<?>... hints) {
		checkCanUseHints(variable);
		add(variable.getName().toString(null), hints);
	}

	/**
	 * Adds hints for {@code variableName} in the current scope.
	 * @param variableName The name of the variable to add {@code hints} to.
	 * @param hints The hint(s) to add for {@code variableName}.
	 * @see #add(Variable, Class[])    
	 */
	public void add(String variableName, Class<?>... hints) {
		if (areHintsUnavailable()) {
			return;
		}

		Set<Class<?>> hintSet = new HashSet<>();
		for (Class<?> hint : hints) {
			if (hint != Object.class) { // ignore some useless types
				hintSet.add(hint);
			}
		}

		add_i(variableName, hintSet);
	}

	private void add_i(String variableName, Set<Class<?>> hintSet) {
		//noinspection DataFlowIssue
		typeHints.peek().computeIfAbsent(variableName, key -> new HashSet<>()).addAll(hintSet);
	}

	/**
	 * Removes hints for {@code variable} in the current scope.
	 * @param variable The variable to remove {@code hints} from.
	 * @param hints The hint(s) to remove for {@code variable}.
	 * @see #remove(String, Class[])
	 */
	public void remove(Variable<?> variable, Class<?>... hints) {
		checkCanUseHints(variable);
		remove(variable.getName().toString(null), hints);
	}

	/**
	 * Removes hints for {@code variableName} in the current scope.
	 * @param variableName The name of the variable to add {@code hints} to.
	 * @param hints The hint(s) to remove for {@code variableName}.
	 * @see #remove(Variable, Class[])
	 */
	public void remove(String variableName, Class<?>... hints) {
		if (areHintsUnavailable()) {
			return;
		}
		//noinspection DataFlowIssue
		Set<Class<?>> hintSet = typeHints.peek().get(variableName);
		if (hintSet != null) {
			for (Class<?> hint : hints) {
				hintSet.remove(hint);
			}
			if (hintSet.isEmpty()) {
				delete_i(variableName);
			}
		}
	}

	/**
	 * Obtains the type hints for {@code variable} in the current scope.
	 * @param variable The variable to get hints from.
	 * @return An unmodifiable set of hints.
	 * @see #get(String) 
	 */
	public @Unmodifiable Set<Class<?>> get(Variable<?> variable) {
		checkCanUseHints(variable);
		return get(variable.getName().toString(null));
	}

	/**
	 * Obtains the type hints for {@code variableName} in the current scope.
	 * @param variableName The name of the variable to get hints from.
	 * @return An unmodifiable set of hints.
	 * @see #add(Variable, Class[]) 
	 */
	public @Unmodifiable Set<Class<?>> get(String variableName) {
		if (areHintsUnavailable()) {
			return ImmutableSet.of();
		}
		//noinspection DataFlowIssue
		Set<Class<?>> hintSet = typeHints.peek().get(variableName);
		if (hintSet != null) {
			return ImmutableSet.copyOf(hintSet);
		}
		return ImmutableSet.of();
	}

	/**
	 * @return A backup of this manager's current scope.
	 */
	public Backup backup() {
		return new Backup(this);
	}

	/**
	 * Overwrites the current scope with the scope represented in {@code backup}.
	 * @param backup The backup to apply.
	 */
	public void restore(Backup backup) {
		typeHints.set(0, backup.hints);
	}

	/**
	 * Represents a snapshot of a scope.
	 */
	public static final class Backup {

		private final Map<String, Set<Class<?>>> hints;

		private Backup(HintManager source) {
			hints = new HashMap<>();
			//noinspection DataFlowIssue
			mergeHints(source.typeHints.peek(), hints);
		}

	}

	private boolean areHintsUnavailable() {
		if (typeHints.isEmpty()) {
			if (SkriptLogger.debug()) { // not ideal, print a warning on debug level
				SkriptLogger.LOGGER.warning("Attempted to use type hints outside of any scope");
			}
			return true;
		}
		return false;
	}

	/**
	 * @param variable The variable to check.
	 * @return Whether hints can be used for {@code variable}.
	 */
	public static boolean canUseHints(Variable<?> variable) {
		return variable.isLocal() && variable.getName().isSimple();
	}

	private static void checkCanUseHints(Variable<?> variable) {
		if (!canUseHints(variable)) {
			throw new IllegalArgumentException("Variables must be local and have a simple name to have hints");
		}
	}

}
