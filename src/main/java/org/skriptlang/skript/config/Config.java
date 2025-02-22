package org.skriptlang.skript.config;

import ch.njol.skript.lang.util.common.AnyNamed;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Represents a configuration file.
 * Configuration files are immutable; they can only be read.
 */
public interface Config extends AnyNamed {

	/**
	 * Loads a {@link Config} from a file.
	 *
	 * @param path The path to the file.
	 * @return The loaded {@link Config}.
	 * @throws IOException if reading the file fails.
	 */
	@Contract("_ -> new")
	static Config load(@NotNull Path path) throws IOException {
		return new ConfigImpl(path);
	}

	/**
	 * Loads a {@link Config} from an {@link InputStream}.
	 *
	 * @param stream The input stream.
	 * @return The loaded {@link Config}.
	 * @throws IOException if reading the stream fails.
	 */
	@Contract("_ -> new")
	static Config load(@NotNull InputStream stream) throws IOException {
		return new ConfigImpl(stream);
	}

	/**
	 * Gets the value at the specified path,
	 * or null if there is no value at the path.
	 *
	 * <p>
	 * A path is a string, where each node is seperated by a dot '{@code .}' character.
	 * </p>
	 *
	 * @param path The path to the value.
	 * @param <T>  The type of the value.
	 * @return The value at the specified path.
	 */
	<T> T getValue(@NotNull String path);

	/**
	 * Gets the node at the specified path,
	 * or null if there is no node at the path.
	 *
	 * <p>
	 * A path is a string, where each node is seperated by a dot '{@code .}' character.
	 * </p>
	 *
	 * @param path The path to the node.
	 * @return The node at the specified path.
	 */
	ConfigNode getNode(@NotNull String path);

	/**
	 * Gets the children of the node at the specified path.
	 *
	 * <p>
	 * A path is a string, where each node is seperated by a dot '{@code .}' character.
	 * </p>
	 *
	 * @param path The path to the node.
	 * @return The children of the node at the specified path.
	 */
	ConfigNode[] getNodeChildren(@NotNull String path);

	/**
	 * Saves the configuration to the specified path.
	 *
	 * @throws IOException if writing or creating the file fails.
	 */
	void save() throws IOException;

	/**
	 * Gets the path to the configuration file.
	 * If this configuration was loaded from an {@link InputStream},
	 * this will return an empty path.
	 *
	 * @return The path to the configuration file.
	 */
	@NotNull Path path();

}
