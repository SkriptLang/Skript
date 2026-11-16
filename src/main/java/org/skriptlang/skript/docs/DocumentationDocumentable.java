package org.skriptlang.skript.docs;

/**
 * A {@link Documentable} that has a retrievable {@link Documentation}.
 */
@FunctionalInterface
public interface DocumentationDocumentable extends Documentable {

	/**
	 * @return Documentation describing this object.
	 */
	Documentation documentation();

	/**
	 * @return An automatically generated ID for this documentable.
	 * By default, this method simply wraps {@link Documentation#autoId()}.
	 * However, it can be overridden for additional functionality, such as enforcing an automatic prefix.
	 */
	default String documentationAutoId() {
		return documentation().autoId();
	}

	@Override
	default boolean canWrite(DocumentationAdapter adapter) {
		return !Documentation.isNoDocs(documentation());
	}

	@Override
	default void preWrite(DocumentationAdapter adapter) {
		String id = documentation().id();
		if (id == null) {
			id = documentationAutoId();
			if (id.isEmpty()) {
				throw new DocumentationException(
					"Method preWrite must be overridden for DocumentationDocumentable if documentation may not have an ID or autoID"
				);
			}
		}
		adapter.enterScope(id);
	}

	@Override
	default void write(DocumentationAdapter adapter) {
		adapter.write(documentation());
	}

	@Override
	default void postWrite(DocumentationAdapter adapter) {
		adapter.exitScope();
	}

}
