package ch.njol.skript.config;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ConfigHelper {

	public static @NotNull Set<Node> discoverNodes(@NotNull SectionNode node) {
		return Config.discoverNodes(node);
	}

}
