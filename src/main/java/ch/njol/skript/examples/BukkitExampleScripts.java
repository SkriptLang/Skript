package ch.njol.skript.examples;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Example scripts that rely on Bukkit-specific behaviours or APIs.
 */
public final class BukkitExampleScripts {

	public static final List<ExampleScript> EXAMPLES = List.of(
		load("chest menus.sk"),
		load("commands.sk"),
		load("events.sk"),
		load("timings.sk")
	);

	private static final ExampleScriptProvider PROVIDER = () -> EXAMPLES;

	private BukkitExampleScripts() {}

	public static Collection<ExampleScript> all() {
		return EXAMPLES;
	}

	public static ExampleScriptProvider provider() {
		return PROVIDER;
	}

	private static ExampleScript load(String name) {
		String path = "scripts/-examples/" + name;
		try (InputStream in = BukkitExampleScripts.class.getClassLoader().getResourceAsStream(path)) {
			if (in == null)
				throw new IllegalStateException("Missing example script " + path);
			String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			return new ExampleScript(name, content);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load example script " + path, e);
		}
	}

}
