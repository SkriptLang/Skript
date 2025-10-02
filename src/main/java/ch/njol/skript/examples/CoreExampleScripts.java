package ch.njol.skript.examples;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

public final class CoreExampleScripts {

	public static final List<ExampleScript> EXAMPLES = List.of(
		load("chest menus.sk"),
		load("commands.sk"),
		load("events.sk"),
		load("experimental features/for loops.sk"),
		load("experimental features/queues.sk"),
		load("experimental features/script reflection.sk"),
		load("functions.sk"),
		load("loops.sk"),
		load("options and meta.sk"),
		load("text formatting.sk"),
		load("timings.sk"),
		load("variables.sk")
	);

	private CoreExampleScripts() {}

	public static Collection<ExampleScript> all() {
		return EXAMPLES;
	}

	private static ExampleScript load(String name) {
		String path = "scripts/-examples/" + name;
		try (InputStream in = CoreExampleScripts.class.getClassLoader().getResourceAsStream(path)) {
			if (in == null)
				throw new IllegalStateException("Missing example script " + path);
			String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			return new ExampleScript(name, content);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load example script " + path, e);
		}
	}
}
