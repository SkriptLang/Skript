package org.skriptlang.skript.docs;

/**
 * Describes something that can be written to a {@link DocumentationAdapter}.
 */
public interface Documentable {

	/**
	 * @param adapter The adapter to be written to.
	 * @return Whether information about this object is able to be written to {@code adapter}.
	 */
	default boolean canWrite(DocumentationAdapter adapter) {
		return true;
	}

	/**
	 * Called immediately before the {@link #write(DocumentationAdapter)} process.
	 * @param adapter The adapter to be written to.
	 */
	default void preWrite(DocumentationAdapter adapter) { }

	/**
	 * Writes information about this object to an adapter.
	 * @param adapter The adapter to write to.
	 */
	void write(DocumentationAdapter adapter);

	/**
	 * Called immediately after the {@link #write(DocumentationAdapter)} process.
	 * @param adapter The adapter that was written to.
	 */
	default void postWrite(DocumentationAdapter adapter) { }

}
