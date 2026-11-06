package org.skriptlang.skript.docs;

import ch.njol.skript.classes.ClassInfo;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.Map;
import java.util.function.BiConsumer;

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
		return of(addon, (ignored1, ignored2) -> { });
	}

	/**
	 * Creates and populates an adapter with documentation for elements provided by {@code addon}.
	 * This default implementation covers:
	 * <ul>
	 *     <li>{@link SyntaxInfo}s</li>
	 *     <li>{@link ClassInfo}s</li>
	 *     <li>{@link Experiment}s</li>
	 * </ul>
	 * @param addon The addon to extract documentation from.
	 * @param writeHandler A consumer to be called for every written {@link Documentable}.
	 *  This is called after {@link Documentable#write(DocumentationAdapter)} but before {@link Documentable#postWrite(DocumentationAdapter)}.
	 *  Thus, it is useful for intercepting certain documentables to write additional data.
	 * @return A populated adapter.
	 */
	static DocumentationAdapter of(SkriptAddon addon, BiConsumer<DocumentationAdapter, Documentable> writeHandler) {
		return new DocumentationAdapterImpl(addon, writeHandler, true);
	}

	/**
	 * @return The addon to extract information from.
	 */
	SkriptAddon addon();

	/**
	 * Writes a documentable to this adapter.
	 * @param documentable The documentable to write.
	 */
	default void write(Documentable documentable) {
		if (documentable.canWrite(this)) {
			documentable.preWrite(this);
			documentable.write(this);
			documentable.postWrite(this);
		}
	}

	/**
	 * Writes a new entry to this adapter.
	 * @param key The name of the entry.
	 * @param value The value of the entry.
	 */
	void write(String key, Object value);

	/**
	 * Enters a new scope, which represents a new level of information.
	 * For example, all {@link Expression}s may be represented within a scope,
	 *  and each individual Expression within a scope.
	 * This method does not guarantee that key of the entered scope will match {@code key}.
	 * That is, if a scope under the key {@code key} already exists, the newly entered scope may have its key
	 * only derived from {@code key} to avoid conflicts.
	 * If it is necessary to enter an existing scope, use {@link #editScope(String)}.
	 * @param key The name of the scope to enter.
	 * @see #exitScope()
	 */
	void enterScope(String key);

	/**
	 * Enters an existing scope present at the current level.
	 * This method should be preferred for cases where a guarantee that {@code key} is equal to
	 *  the key of the entered scope is required.
	 * @param key The name of the scope to edit.
	 * @see #enterScope(String)
	 */
	void editScope(String key);

	/**
	 * Exits the last entered scope.
	 * Must follow a {@link #enterScope(String)} call.
	 */
	void exitScope();

	/**
	 * Deletes a scope present at the current level.
	 * @param key The name of the scope to delete.
	 */
	void deleteScope(String key);

	/**
	 * @return The name of the scope most recently entered through {@link #enterScope(String)}.
	 */
	String currentScope();

	/**
	 * @return The current data map containing all written data.
	 */
	Map<String, Object> dataMap();

	/**
	 * A reference is a way to refer to another {@link Documentable} that has been written to an adapter.
	 * Rather than duplicate the information of that documentable in the output,
	 *  a reference will only resolve to a subset of information about that documentable, such as its documentation ID.
	 */
	interface Reference {

		/**
		 * @return The documentable being referenced.
		 */
		Documentable referenced();

	}

	/**
	 * Creates a new reference to a documentable.
	 * It is expected that {@code documentable} will be written to this adapter at some point.
	 * @param documentable The documentable to reference.
	 * @return A reference to {@code documentable}.
	 */
	Reference reference(Documentable documentable);

}
