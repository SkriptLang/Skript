package org.skriptlang.skript.docs;

import ch.njol.skript.classes.ClassInfo;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.Map;

/**
 * A documentation adapter is used for extracting information out of {@link Documentable} objects.
 * It collects this information into a tree-like format that can then be output into other formats.
 * @see DocumentationGenerator
 */
public interface DocumentationAdapter {

	/**
	 * Creates and populates an adapter with documentation for elements provided by {@code addon}.
	 * This default implementation covers:
	 * <ul>
	 *     <li>{@link SyntaxInfo}s</li>
	 *     <li>{@link ClassInfo}s</li>
	 *     <li>{@link Experiment}s</li>
	 * </ul>
	 * @param addon The addon to extract documentation from.
	 * @return A populated adapter.
	 */
	static DocumentationAdapter of(SkriptAddon addon) {
		return new DocumentationAdapterImpl(addon);
	}

	/**
	 * Writes a documentable to this adapter.
	 * This is equivalent to {@code documentable.write(thisAdapter)}.
	 * @param documentable The documentable to write.
	 * @see Documentable#write(DocumentationAdapter)
	 */
	default void write(Documentable documentable) {
		documentable.write(this);
	}

	void write(String key, Object value);

	/**
	 * Enters a new scope, which represents a new level of information.
	 * For example, all {@link Expression}s may be represented within a scope,
	 *  and each individual Expression within a scope.
	 * @param key The name of the scope to enter.
	 */
	void enterScope(String key);

	/**
	 * Exits the last entered scope.
	 * Must follow a {@link #enterScope(String)} call.
	 */
	void exitScope();

	/**
	 * @return The name of the scope most recently entered through {@link #enterScope(String)}.
	 */
	String currentScope();

	/**
	 * @return The current data map containing all written data.
	 */
	Map<String, Object> dataMap();

}
