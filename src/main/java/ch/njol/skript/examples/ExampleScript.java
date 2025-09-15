package ch.njol.skript.examples;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents an example script bundled with Skript or an addon.
 */
public record ExampleScript(String name, String content) {

	/**
	 * Loads an example script from a resource contained within an addon JAR.
	 *
	 * @param plugin The plugin providing the resource
	 * @param resourcePath The path to the resource inside the plugin
	 * @param outputName The name of the file to install the example as
	 * @return A new {@link ExampleScript} containing the resource's content
	 * @throws IOException If the resource cannot be found or read
	 */
	public static ExampleScript fromResource(JavaPlugin plugin, String resourcePath, String outputName) throws IOException {
		try (InputStream in = plugin.getResource(resourcePath)) {
			if (in == null) {
				throw new IOException("Resource not found: " + resourcePath);
			}
			String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			return new ExampleScript(outputName, content);
		}
	}
}