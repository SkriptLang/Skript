package org.skriptlang.skript.docs;

/**
 * A {@link Documentable} that has a retrievable {@link Documentation}.
 */
public interface DocumentationDocumentable extends Documentable {

	/**
	 * @return Documentation describing this object.
	 */
	Documentation documentation();

	@Override
	default void write(DocumentationAdapter adapter) {
		adapter.write(documentation());
	}

}
