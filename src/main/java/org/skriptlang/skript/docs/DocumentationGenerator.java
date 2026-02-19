package org.skriptlang.skript.docs;

import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.addon.SkriptAddon;

import java.nio.file.Path;

/**
 * Describes a generator for the elements registered by a {@link SkriptAddon}, such as:
 * <ul>
 *     <li>types</li>
 *     <li>syntax</li>
 *     <li>functions</li>
 * </ul>
 */
public interface DocumentationGenerator {

	/**
	 * Creates
	 * @param addon The addon to generate documentation for.
	 * @return A generator capable of generating a JSON representation of documentation.
	 */
	@Contract("_, _ -> new")
	static DocumentationGenerator json(SkriptAddon addon, DocumentationAdapter adapter) {
		return new JSONGenerator(addon, adapter);
	}

	/**
	 * Generates documentation at the provided path.
	 * @param path The path to generate documentation at.
	 */
	void generate(Path path);

}
