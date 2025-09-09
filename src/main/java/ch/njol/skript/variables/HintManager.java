package ch.njol.skript.variables;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Feature;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Used for managing local variable type hints during the parsing process.
 * <h4>Hint Tracking</h4>
 * <p>
 * Type hints are tracked in scopes which are generally equivalent to sections.
 * However, there may be additional scopes that are not associated with sections.
 * These scopes may be used for a variety of reasons.
 * For example, a non-section scope could be used for capturing the hints of a section.
 * These hints could be filtered before being passed back up.
 * </p>
 * <p>
 * When entering a scope ({@link #enterScope(boolean)}), it is initialized with the hints of the previous top-level scope.
 * When exiting a scope ({@link #exitScope()}), remaining hints from that scope are added to the existing hints of the new top-level scope.
 * This merging of scopes is provided and described by {@link #mergeScope(int, int, boolean)}.
 * Thus, it is only necessary to obtain hints for the current scope.
 * {@link #get(Variable)} is provided for obtaining the hints of a variable in the current scope.
 * </p>
 * <p>
 * It is possible to disable the collection and usage of hints through the activity state of a manager.
 * See {@link #setActive(boolean)} and {@link #isActive} for detailed information.
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
 * However, for use cases where applicable, {@link #remove(Variable, Class[])} is provided.
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
@ApiStatus.Experimental
public class HintManager {

	private record Scope(Map<String, Set<Class<?>>> hintMap, boolean isSection) { }

	private final LinkedList<Scope> typeHints = new LinkedList<>();
	private boolean isActive;

	public HintManager(boolean active) {
		isActive = active;
	}

	/**
	 * Marks this hint manager as active or inactive.
	 * An inactive hint manager does not collect hints.
	 * That is, actions such as setting, adding, etc. have no effect on the currently stored hints.
	 * Additionally, any calls to obtain hints will always result in an empty collection.
	 * As a result, type hints are effectively not used.
	 * @param active Whether this hint manager should be active.
	 * @see #isActive
	 */
	public void setActive(boolean active) {
		isActive = active;
	}

	/**
	 * @return Whether this manager is active.
	 * @see #setActive(boolean)
	 */
	public boolean isActive() {
		ParserInstance parser = ParserInstance.get();
		return isActive && parser.isActive() && parser.hasExperiment(Feature.TYPE_HINTS);
	}

	/**
	 * Enters a new scope for storing hints.
	 * Hints from the previous (current top-level) scope are copied over.
	 * @param isSection Whether this scope represents a section in a trigger.
	 * @see #exitScope()
	 */
	public void enterScope(boolean isSection) {
		typeHints.push(new Scope(new HashMap<>(), isSection));
		if (typeHints.size() > 1) { // copy existing values into new scope
			mergeScope(1, 0, false);
		}
	}

	/**
	 * Exits the current (top-level) scope.
	 * Hints from the exited scope will be copied over to the new top-level scope.
	 * @see #enterScope(boolean)
	 */
	public void exitScope() {
		if (typeHints.size() > 1) { // copy over updated hints
			mergeScope(0, 1, false);
		}
		typeHints.pop();
	}

	/**
	 * Resets (clears) all type hints for the current (top-level) scope.
	 * Scopes are represented as integers, where {@code 0} represents the most recently entered scope.
	 * For example, after calling {@link #enterScope(boolean)}, {@code 0} would represent the scope just entered
	 *  by calling the method, and {@code 1} would represent the most recently entered scope <i>before</i> calling the method.
	 * @param sectionOnly Whether only scopes representing sections should be considered.
	 */
	public void clearScope(int level, boolean sectionOnly) {
		if (level < 0 || level > typeHints.size() - 1) {
			throw new IndexOutOfBoundsException(
					"Scope level " + level + " is out of bounds (expected 0-" + (typeHints.size() - 1) + ")");
		}

		if (!sectionOnly) {
			typeHints.get(level).hintMap().clear();
			return;
		}

		int currentLevel = 0;
		var iterator = typeHints.iterator();
		while (iterator.hasNext()) {
			Scope scope = iterator.next();
			if (!scope.isSection()) {
				continue;
			}
			if (currentLevel == level) {
				iterator.remove();
				return;
			}
			currentLevel++;
		}

		// Did not find a section scope to remove
		throw new IndexOutOfBoundsException(
				"Section scope level " + level + " is out of bounds (expected 0-" + currentLevel + ")");
	}

	/**
	 * Copies hints from one scope to another.
	 * Scopes are represented as integers, where {@code 0} represents the most recently entered scope.
	 * For example, after calling {@link #enterScope(boolean)}, {@code 0} would represent the scope just entered
	 *  by calling the method, and {@code 1} would represent the most recently entered scope <i>before</i> calling the method.
	 * <p>
	 * <b>Note: This does not overwrite the existing hints of {@code to}. Instead, the hints are merged together.</b>
	 * @param from The scope to copy hints from.
 	 * @param to The scope to copy hints to.
	 * @param sectionOnly Whether only scopes representing sections should be considered.
	 */
	public void mergeScope(int from, int to, boolean sectionOnly) {
		int expectedSize = typeHints.size() - 1;
		if (from < 0 || from > expectedSize) {
			throw new IndexOutOfBoundsException(
					"'from' scope level " + from + " is out of bounds (expected 0-" + expectedSize + ")");
		}
		if (to < 0 || to > expectedSize) {
			throw new IndexOutOfBoundsException(
					"'to' scope level " + to + " is out of bounds (expected 0-" + expectedSize + ")");
		}

		Scope fromScope = null;
		Scope toScope = null;
		if (sectionOnly) {
			int currentLevel = 0;
			for (Scope scope : typeHints) {
				if (!scope.isSection()) {
					continue;
				}
				if (currentLevel == from) {
					fromScope = scope;
				}
				if (currentLevel == to) {
					toScope = scope;
				}
				if (fromScope != null && toScope != null) {
					break;
				}
				currentLevel++;
			}
			if (fromScope == null) {
				throw new IndexOutOfBoundsException(
						"'from' section scope level " + from + " is out of bounds (expected 0-" + currentLevel + ")");
			}
			if (toScope == null) {
				throw new IndexOutOfBoundsException(
						"'to' section scope level " + to + " is out of bounds (expected 0-" + currentLevel + ")");
			}
		} else {
			fromScope = typeHints.get(from);
			toScope = typeHints.get(to);
		}
		mergeHints(fromScope.hintMap(), toScope.hintMap());
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

		delete_i(variableName);
		if (hints.length != 0) {
			add_i(variableName, Set.of(hints));
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
		if (SkriptConfig.caseInsensitiveVariables.value()) {
			variableName = variableName.toLowerCase(Locale.ENGLISH);
		}

		typeHints.getFirst().hintMap().remove(variableName);

		// Attempt to also clear hints for the list variable if applicable
		if (variableName.endsWith(Variable.SEPARATOR + "*")) {
			String prefix = variableName.substring(0, variableName.length() - 1);
			typeHints.getFirst().hintMap().keySet().removeIf(key -> key.startsWith(prefix));
		}
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

		add_i(variableName, Set.of(hints));
	}

	private void add_i(String variableName, Set<Class<?>> hintSet) {
		if (SkriptConfig.caseInsensitiveVariables.value()) {
			variableName = variableName.toLowerCase(Locale.ENGLISH);
		}

		// Edge case: see Expression#beforeChange
		if (hintSet.contains(ch.njol.skript.util.slot.Slot.class)) {
			hintSet = new HashSet<>(hintSet);
			hintSet.add(org.bukkit.inventory.ItemStack.class);
		}

		typeHints.getFirst().hintMap().computeIfAbsent(variableName, key -> new HashSet<>()).addAll(hintSet);

		// Attempt to also add hints for the list variable if applicable
		if (!variableName.isEmpty() && variableName.charAt(variableName.length() - 1) != '*') {
			int listEnd = variableName.lastIndexOf(Variable.SEPARATOR);
			if (listEnd != -1) {
				String listVariableName = variableName.substring(0, listEnd + Variable.SEPARATOR.length()) + "*";
				typeHints.getFirst().hintMap().computeIfAbsent(listVariableName, key -> new HashSet<>()).addAll(hintSet);
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
		if (areHintsUnavailable()) {
			return;
		}

		if (SkriptConfig.caseInsensitiveVariables.value()) {
			variableName = variableName.toLowerCase(Locale.ENGLISH);
		}
		Set<Class<?>> hintSet = typeHints.getFirst().hintMap().get(variableName);

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

		if (SkriptConfig.caseInsensitiveVariables.value()) {
			variableName = variableName.toLowerCase(Locale.ENGLISH);
		}
		Set<Class<?>> hintSet = typeHints.getFirst().hintMap().get(variableName);

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
		typeHints.set(0, backup.scope);
	}

	/**
	 * Represents a snapshot of a scope.
	 */
	public static final class Backup {

		private final Scope scope;

		private Backup(HintManager source) {
			scope = new Scope(new HashMap<>(), source.typeHints.getFirst().isSection);
			mergeHints(source.typeHints.getFirst().hintMap(), scope.hintMap());
		}

	}

	private boolean areHintsUnavailable() {
		if (!isActive()) {
			return true;
		}
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
