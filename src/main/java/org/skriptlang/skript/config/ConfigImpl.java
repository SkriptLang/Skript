package org.skriptlang.skript.config;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class ConfigImpl implements Config {

	private final Map<ConfigSection, ConfigNode[]> nodes;
	private final Path path;

	ConfigImpl(@NotNull Path path) throws IOException {
		Preconditions.checkNotNull(path, "path is not set");

		this.path = path;
		this.nodes = new HashMap<>();

		parse(Files.readAllLines(path).toArray(new String[0]));
	}

	ConfigImpl(@NotNull InputStream stream) throws IOException {
		this.path = Path.of("");
		this.nodes = new HashMap<>();

		try (InputStreamReader is = new InputStreamReader(stream);
			 BufferedReader br = new BufferedReader(is)) {
			parse(br.lines().toList().toArray(new String[0]));
		}
	}

	ConfigImpl(@NotNull Map<ConfigSection, ConfigNode[]> nodes) {
		this.path = Path.of("");
		this.nodes = nodes;
	}

	private void parse(String[] lines) {
		Deque<ConfigSection> stack = new ArrayDeque<>();
		List<ConfigNode> nodes = new ArrayList<>();
		int indentation = 0;

		List<String> comments = new ArrayList<>();

		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.isEmpty()) {
				comments.add(trimmed);
				continue;
			}
			if (trimmed.charAt(0) == '#') {
				comments.add(trimmed.substring(1).trim());
				continue;
			}

			String[] parts = line.split(":", 2);
			int currentIndentation = (int) parts[0].chars().filter(ch -> ch == '\t').count();
			String key = parts[0].trim();
			String fullValue = parts[1].trim();
			String value = fullValue.replaceFirst("#.*", "").trim();

			String inlineComment = "";
			if (fullValue.indexOf('#') != -1) {
				inlineComment = fullValue.substring(fullValue.indexOf('#') + 1).trim();
			}

			if (value.isBlank()) {
				if (currentIndentation > indentation + 1) {
					throw new IllegalStateException("Invalid indentation");
				}
				for (int i = 0; i < indentation - currentIndentation; i++) {
					stack.poll();
				}
				indentation++;

				ConfigSection section = new ConfigSection(key, inlineComment, comments.toArray(new String[0]));
				nodes.add(section);
				stack.add(section);

				this.nodes.merge(stack.getFirst(), nodes.toArray(new ConfigNode[0]), (existing, newNodes) -> {
					ConfigNode[] combined = Arrays.copyOf(existing, existing.length + newNodes.length);
					System.arraycopy(newNodes, 0, combined, existing.length, newNodes.length);
					return combined;
				});
				nodes.clear();
				comments.clear();
			} else {
				if (currentIndentation > indentation) {
					throw new IllegalStateException("Invalid indentation");
				}
				for (int i = 0; i < indentation - currentIndentation; i++) {
					stack.poll();
				}
				indentation = currentIndentation;

				nodes.add(new ConfigEntry<>(key, value, inlineComment, comments.toArray(new String[0])));
				comments.clear();
			}
		}

		this.nodes.put(stack.getFirst(), nodes.toArray(new ConfigNode[0]));
	}

	@Override
	public <T> T getValue(@NotNull String path) {
		ConfigNode node = getNode(path);
		if (node instanceof ConfigEntry<?> configEntry) {
			//noinspection unchecked
			return (T) configEntry.value();
		}
		return null;
	}

	@Override
	public <T> void setValue(@NotNull String path, T value) {
		ConfigNode node = getNode(path);
		if (node instanceof ConfigEntry<?> configEntry) {
			//noinspection unchecked
			((ConfigEntry<T>) configEntry).value(value);
		}
	}

	@Override
	public ConfigNode getNode(@NotNull String path) {
		String[] keys = path.split("\\.");

		ConfigSection parent = null;
		for (int i = 0; i < keys.length - 1; i++) {
			int finalI = i;
			parent = (ConfigSection) Arrays.stream(nodes.get(parent))
				.filter(node -> node instanceof ConfigSection && node.key().equals(keys[finalI]))
				.findFirst()
				.orElse(null);

			if (parent == null) {
				return null;
			}
		}

		return Arrays.stream(nodes.get(parent))
			.filter(node -> node.key().equals(keys[keys.length - 1]))
			.findFirst()
			.orElse(null);
	}

	@Override
	public ConfigNode[] getNodeChildren(@NotNull String path) {
		ConfigNode node = getNode(path);
		if (node instanceof ConfigSection section) {
			return nodes.get(section);
		}
		return null;
	}

	@Override
	public void save() throws IOException {
		Files.writeString(path, toString());
	}

	@Override
	public @UnknownNullability String name() {
		return path.getFileName().toString();
	}

	@Override
	public @NotNull Path path() {
		return path;
	}

	@Override
	public String toString() {
		return toStringRecursively(nodes.get(null), 0);
	}

	private String toStringRecursively(ConfigNode[] map, int depth) {
		StringJoiner joiner = new StringJoiner("\n");

		for (ConfigNode node : map) {
			joiner.add("\t".repeat(depth) + node.toString()
				.replace("\n", "\n" + "\t".repeat(depth)));

			if (node instanceof ConfigSection section) {
				joiner.add(toStringRecursively(nodes.get(section), depth + 1));
			}
		}

		return joiner.toString();
	}
}
