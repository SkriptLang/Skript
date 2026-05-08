package org.skriptlang.skript.docs;

import ch.njol.skript.SkriptAPIException;

/**
 * A {@link Documentable} that has a retrievable {@link Documentation}.
 */
public interface DocumentationDocumentable extends Documentable {

	/**
	 * @return Documentation describing this object.
	 */
	Documentation documentation();

	@Override
	default boolean canWrite(DocumentationAdapter adapter) {
		return !Documentation.isNoDocs(documentation());
	}

	@Override
	default void preWrite(DocumentationAdapter adapter) {
		String id = documentation().id();
		if (id == null) {
			id = documentation().autoId();
			if (id.isEmpty()) {
				throw new SkriptAPIException(
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
