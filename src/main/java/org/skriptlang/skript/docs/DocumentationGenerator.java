package org.skriptlang.skript.docs;

import org.jetbrains.annotations.Contract;

import java.nio.file.Path;

/**
 * Describes a generator capable of transforming the output of a {@link DocumentationAdapter}.
 */
@FunctionalInterface
public interface DocumentationGenerator {

	/**
	 * @return A generator capable of generating a JSON representation of documentation.
	 */
	@Contract("_, -> new")
	static DocumentationGenerator json(DocumentationAdapter adapter) {
		return new JsonGenerator(adapter);
	}

	/**
	 * Generates documentation at the provided path.
	 * @param path The path to generate documentation at.
	 */
	void generate(Path path);

}
