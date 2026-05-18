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
	 * Describes information about an {@link SkriptAddon}.
	 * @param version The version of the addon this info represents.
	 */
	record AddonInfo(String version) { }

	/**
	 * Creates
	 * @param addon The addon to generate documentation for.
	 * @param info Info about {@code addon}.
	 * @return A generator capable of generating a JSON representation of documentation.
	 */
	@Contract("_, _, _ -> new")
	static DocumentationGenerator json(SkriptAddon addon, AddonInfo info, DocumentationAdapter adapter) {
		return new JSONGenerator(addon, info, adapter);
	}

	/**
	 * Generates documentation at the provided path.
	 * @param path The path to generate documentation at.
	 */
	void generate(Path path);

}
