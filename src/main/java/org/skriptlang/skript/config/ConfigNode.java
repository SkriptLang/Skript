package org.skriptlang.skript.config;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a node in a {@link Config}.
 */
public interface ConfigNode {

	/**
	 * Gets the key of the node.
	 *
	 * @return The key of the node.
	 */
	@NotNull String key();

	/**
	 * Gets the inline comment of the node.
	 *
	 * @return The inline comment of the node.
	 */
	@NotNull String inlineComment();

	/**
	 * Gets the comments of the node.
	 *
	 * @return The comments of the node.
	 */
	@NotNull String @NotNull [] comments();

}
