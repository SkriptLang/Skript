package org.skriptlang.skript.docs;

/**
 * Describes something that can be written to a {@link DocumentationAdapter}.
 */
public interface Documentable {

	void write(DocumentationAdapter adapter);

}
