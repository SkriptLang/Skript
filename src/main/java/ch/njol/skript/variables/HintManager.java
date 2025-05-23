package ch.njol.skript.variables;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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

	private final Deque<Map<String, Set<Class<?>>>> typeHints = new ArrayDeque<>();

	/**
	 * Enters a new scope for storing hints.
	 * Hints from the previous (current top-level) scope are copied over.
	 * @see #exitScope()
	 */
	public void enterScope() {
		if (typeHints.isEmpty()) {
			typeHints.push(new HashMap<>());
		} else { // copy over available valuescle
			typeHints.push(new HashMap<>(typeHints.peek()));
		}
	}

	/**
	 * Exits the current (top-level) scope.
	 * Hints from the exited scope will be copied over to the new top-level scope.
	 * @see #enterScope()
	 */
	public void exitScope() {
		var hintMap = typeHints.pop();
		// copy over available values if applicable
		if (!typeHints.isEmpty()) {
			var topMap = typeHints.peek();
			for (var entry : hintMap.entrySet()) {
				topMap.computeIfAbsent(entry.getKey(), key -> new HashSet<>()).addAll(entry.getValue());
			}
		}
	}

	/**
	 * Resets (clears) all type hints for the current (top-level) scope.
	 */
	public void resetScope() {
		checkState();
		//noinspection DataFlowIssue - verified by checkState
		typeHints.peek().clear();
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
		checkState();
		Set<Class<?>> hintSet = new HashSet<>();
		for (Class<?> hint : hints) {
			if (hint != Object.class) { // ignore some useless types
				hintSet.add(hint);
			}
		}
		//noinspection DataFlowIssue - verified by checkState
		typeHints.peek().put(variableName, hintSet);
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
		checkState();
		//noinspection DataFlowIssue - verified by checkState
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
		checkState();
		//noinspection DataFlowIssue - verified by checkState
		Set<Class<?>> hintSet = typeHints.peek().computeIfAbsent(variableName, key -> new HashSet<>());
		for (Class<?> hint : hints) {
			if (hint != Object.class) { // ignore some useless types
				hintSet.add(hint);
			}
		}
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
		checkState();
		//noinspection DataFlowIssue - verified by checkState
		Set<Class<?>> hintSet = typeHints.peek().get(variableName);
		if (hintSet != null) {
			for (Class<?> hint : hints) {
				hintSet.remove(hint);
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
		checkState();
		//noinspection DataFlowIssue - verified by checkState
		Set<Class<?>> hintSet = typeHints.peek().get(variableName);
		if (hintSet != null) {
			return ImmutableSet.copyOf(hintSet);
		}
		return ImmutableSet.of();
	}

	private void checkState() {
		if (typeHints.isEmpty()) {
			throw new SkriptAPIException("Attempted to use type hints outside of any scope");
		}
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
