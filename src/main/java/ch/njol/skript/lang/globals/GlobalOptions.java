package ch.njol.skript.lang.globals;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.structures.StructOptions;
import ch.njol.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Represents the 'globals/options.sk' file
 */
public class GlobalOptions extends GlobalFile {

	private static final Map<String, String> options = new HashMap<>();

	public GlobalOptions() {
		super("options");
	}

	/**
	 * Loads the 'globals/options.sk' file
	 */
	public void load() {
		options.clear();
		try {
			Config config = new Config(file, true, false, ":");
			config.getMainNode().convertToEntries(-1);
			StructOptions.loadOptions(config.getMainNode(), "", options);

			// for unit tests
			if (Skript.testing()) {
				options.put("GlobalOptionTest", "works!!!");
				options.put("GlobalOptionOverrideTest", "shouldn't work!!!");
			}
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
				return m.group();
			}
			return Matcher.quoteReplacement(option);
		});
	}

}
