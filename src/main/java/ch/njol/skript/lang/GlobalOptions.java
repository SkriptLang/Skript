package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Represents the global-options.sk file
 */
public class GlobalOptions {

	private static final Map<String, String> options = new HashMap<>();

	/**
	 * Loads the global-options.sk file
	 */
	public static void load() {
		options.clear();
		File file = new File(Skript.getInstance().getDataFolder(), "global-options.sk");
		if (!file.exists()) {
			createFile(file);
		}
		try {
			Config config = new Config(file, true, false, ":");
			config.getMainNode().convertToEntries(-1);
			loadOptions(config.getMainNode(), "", options);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * @return An unmodifiable map of global options
	 */
	public static Map<String, String> getOptions() {
		return Collections.unmodifiableMap(options);
	}

	/**
	 * Replaces global options in a string
	 */
	public static String replaceOptions(String string) {
		if (options.isEmpty()) { // don't bother
			return string;
		}
		return StringUtils.replaceAll(string, "\\{@(.+?)\\}", m -> {
			String option = GlobalOptions.getOptions().get(m.group(1));
			if (option == null) {
				Skript.error("undefined option " + m.group());
				return m.group();
			}
			return Matcher.quoteReplacement(option);
		});
	}

	/**
	 * Loads the global-options.sk file from the jar
	 */
	private static void createFile(File targetFile) {
		try (InputStream stream = Skript.getInstance().getResource("global-options.sk")) {
			if (stream == null) {
				Skript.error("The global-options.sk file doesn't exist and couldn't be read from the jar file.");
				return;
			}
			Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO - i don't know what to do with the exception
			throw new RuntimeException(e);
		}
	}

	private static void loadOptions(SectionNode sectionNode, String prefix, Map<String, String> options) {
		for (Node node : sectionNode) {
			if (node instanceof EntryNode) {
				options.put(prefix + node.getKey(), ((EntryNode) node).getValue());
			} else if (node instanceof SectionNode) {
				loadOptions((SectionNode) node, prefix + node.getKey() + ".", options);
			} else {
				Skript.error("Invalid line in options");
			}
		}
	}

}
