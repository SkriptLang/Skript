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
	 * Sets the key of the node.
	 *
	 * @param key The key of the node.
	 */
	void key(@NotNull String key);

	/**
	 * Gets the inline comment of the node.
	 *
	 * @return The inline comment of the node.
	 */
	@NotNull String inlineComment();

	/**
	 * Sets the inline comment of the node.
	 *
	 * @param inlineComment The inline comment of the node.
	 */
	void inlineComment(@NotNull String inlineComment);

	/**
	 * Gets the comments of the node.
	 *
	 * @return The comments of the node.
	 */
	@NotNull String @NotNull [] comments();

	/**
	 * Sets the comments of the node.
	 *
	 * @param comments The comments of the node.
	 */
	void comments(@NotNull String @NotNull [] comments);

}
