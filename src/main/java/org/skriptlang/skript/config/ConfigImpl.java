package org.skriptlang.skript.config;

import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.util.Timespan;
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

/**
 * The current implementation of the {@link Config} interface.
 */
final class ConfigImpl implements Config {

	private final Map<ConfigSection, List<ConfigNode>> nodes;
	private final Path path;

	ConfigImpl(@NotNull Path path) throws IOException {
		Preconditions.checkNotNull(path, "path is not set");

		this.path = path;
		this.nodes = new ConfigReader(Files.readAllLines(path)).nodes;
	}

	ConfigImpl(@NotNull InputStream stream) throws IOException {
		Preconditions.checkNotNull(stream, "stream is not set");

		this.path = Path.of("");

		try (InputStreamReader is = new InputStreamReader(stream);
			 BufferedReader br = new BufferedReader(is)) {
			this.nodes = new ConfigReader(br.lines().toList()).nodes;
		}
	}

	ConfigImpl(@NotNull Map<ConfigSection, List<ConfigNode>> nodes) {
		Preconditions.checkNotNull(nodes, "nodes is not set");

		this.path = Path.of("");
		this.nodes = nodes;
	}

	@Override
	public <T> T getValue(@NotNull String path) {
		Preconditions.checkNotNull(path, "path is not set");

		ConfigNode node = getNode(path);
		if (node instanceof ConfigEntry<?> configEntry) {
			//noinspection unchecked
			return (T) configEntry.value();
		}
		return null;
	}

	@Override
	public ConfigNode getNode(@NotNull String path) {
		Preconditions.checkNotNull(path, "path is not set");

		String[] keys = path.split("\\.");

		ConfigSection parent = null;
		for (int i = 0; i < keys.length - 1; i++) {
			int finalI = i;
			parent = (ConfigSection) nodes.get(parent).stream()
				.filter(node -> node instanceof ConfigSection && node.key().equals(keys[finalI]))
				.findFirst()
				.orElse(null);

			if (parent == null) {
				return null;
			}
		}

		return nodes.get(parent).stream()
			.filter(node -> node.key().equals(keys[keys.length - 1]))
			.findFirst()
			.orElse(null);
	}

	@Override
	public ConfigNode[] getNodeChildren(@NotNull String path) {
		Preconditions.checkNotNull(path, "path is not set");

		ConfigNode node = getNode(path);
		if (node instanceof ConfigSection section) {
			return nodes.get(section).toArray(new ConfigNode[0]);
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

	private String toStringRecursively(List<ConfigNode> map, int depth) {
		if (map == null) {
			return "";
		}

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

	private static class ConfigReader {

		private final List<String> comments = new ArrayList<>();
		private final List<String> lines;
		private final Map<ConfigSection, List<ConfigNode>> nodes = new HashMap<>();
		private int parseLine = 0;

		public ConfigReader(List<String> lines) {
			this.lines = lines;

			parse(null, 0);
		}

		/**
		 * Parses a config file into a map of nodes.
		 *
		 * @param parent The parent section, or null for the root section.
		 * @param indentation The current indentation of the section.
		 */
		private void parse(ConfigSection parent, int indentation) {
			List<ConfigNode> nodes = new ArrayList<>();

			while (parseLine < lines.size()) {
				String line = lines.get(parseLine);
				String trimmed = line.trim();

				// if the line is empty or a comment, we add it to the comments list and continue
				if (trimmed.isEmpty()) {
					comments.add(trimmed);
					parseLine++;
					continue;
				}
				if (trimmed.charAt(0) == '#') {
					if (trimmed.length() < 2) {
						error("Lines with only comment symbols are not allowed");
					}
					if (trimmed.charAt(1) != ' ') {
						error("Comments must start with a space after the comment symbol");
					}
					comments.add(trimmed.substring(2));
					parseLine++;
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

				// if the current line is less indented than the previous line, we are done with this section
				if (currentIndentation < indentation) {
					break;
				}

				if (value.isBlank()) {
					if (currentIndentation > indentation + 1) {
						error("Invalid indentation: expected %d, got %d".formatted(indentation, currentIndentation));
					}

					ConfigSection section = new ConfigSection(key, inlineComment, comments.toArray(new String[0]));
					comments.clear();
					parseLine++;
					nodes.add(section);

					parse(section, indentation + 1);
				} else {
					if (currentIndentation > indentation) {
						error("Invalid indentation: expected %d, got %d".formatted(indentation, currentIndentation));
					}

					nodes.add(new ConfigEntry<>(key, parseValue(value), inlineComment, comments.toArray(new String[0])));
					comments.clear();
					parseLine++;
				}
			}

			List<ConfigNode> existing = this.nodes.getOrDefault(parent, new ArrayList<>());
			existing.addAll(nodes);
			this.nodes.put(parent, existing);
		}

		/**
		 * Attempts to parse primitives.
		 *
		 * @param value The value.
		 * @return The parsed value, or the original value if parsing into a different type failed.
		 */
		private static Object parseValue(String value) {
			String lower = value.toLowerCase(Locale.ENGLISH);

			if (lower.equals("true") || lower.equals("false")) {
				return Boolean.parseBoolean(lower);
			}
			if (JavaClasses.INTEGER_PATTERN.matcher(lower).matches()) {
				return Integer.parseInt(lower);
			}
			if (JavaClasses.DECIMAL_PATTERN.matcher(lower).matches()) {
				return Double.parseDouble(lower);
			}
			Timespan timespan = Timespan.parse(lower);
			if (timespan != null) {
				return timespan;
			}

			return value;
		}

		private void error(String error) {
			throw new IllegalStateException("Error on line %d: %s".formatted(parseLine + 1, error));
		}
	}

}
