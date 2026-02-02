package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Registry;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Stores script specific and global options
 */
public class OptionRegistry implements Registry<Map<Script, Map<String, String>>> {

	/**
	 * Stores all options
	 * If 'script' is null, it's stored as a global option
	 * Otherwise, it is local to the script
	 */
	private final Map<Script, Map<String, String>> options = new HashMap<>();

	/**
	 * Gets a script specific option. If the script specific option doesn't exist, defaults to global option
	 * @param script The script
	 * @param option The option's name
	 * @return The script specific option, or global option, or null
	 * @see #getLocalOption(Script, String)
	 * @see #getGlobalOption(String) 
	 */
	public String getOption(@NotNull Script script, String option) {
		Map<String, String> scriptOptions = options.get(script);
		if (scriptOptions != null && scriptOptions.containsKey(option)) {
			return scriptOptions.get(option);
		}

		Map<String, String> globalOptions = options.get(null);
		return globalOptions.get(option);
	}

	/**
	 * Get a map of all options<br>
	 * use <code>.get(null)</code> for a map of global options<br>
	 * use <code>.get(script)</code> for a map of script specific options
	 * @see #getLocalOptions(Script)
	 * @see #getGlobalOptions()
	 */
	public Map<Script, Map<String, String>> getOptions() {
		return options;
	}

	/**
	 * Gets a global option
	 * @param option The option's name
	 * @return The option's value, or null if it doesn't exist
	 * @see #getOption(Script, String)
	 * @see #getLocalOption(Script, String) 
	 */
	public String getGlobalOption(String option) {
		if (options.containsKey(null)) {
			return options.get(null).get(option);
		}
		return null;
	}

	/**
	 * Sets a global option
	 * @param option The option's name
	 * @param value The option's new value, must not be null
	 * @see #setLocalOption(Script, String, String)    
	 */
	public void setGlobalOption(String option, String value) {
		if (value == null && !options.containsKey(null)) {
			return;
		}
		options.get(null).put(option, value);
	}

	/**
	 * Loads global options
	 * @param sectionNode The initial node to load options from
	 */
	@ApiStatus.Internal
	public void loadGlobalOptions(SectionNode sectionNode) {
		loadLocalOptions(null, sectionNode);
	}

	/**
	 * Get all global options
	 * @return A map of option names and their values, or null if global options weren't loaded yet
	 * @see #getOptions()
	 * @see #getLocalOptions(Script) 
	 */
	public Map<String, String> getGlobalOptions() {
		return options.get(null);
	}

	/**
	 * Gets a script specific option
	 * @param script The script, must not be null
	 * @param option The option's name
	 * @return The option's value, or null if it doesn't exist
	 * @see #getOption(Script, String)
	 * @see #getGlobalOption(String)
	 */
	public String getLocalOption(Script script, String option) {
		if (script == null || !options.containsKey(script)) {
			return null;
		}
		return options.get(script).get(option);
	}

	/**
	 * Sets a script specific option
	 * @param script The script, must not be null
	 * @param option The option's name
	 * @param value The option's new value, must not be null
	 * @see #setGlobalOption(String, String)
	 */
	public void setLocalOption(Script script, String option, String value) {
		if (script == null || value == null) {
			return;
		}
		options.computeIfAbsent(script, k -> new HashMap<>());
		options.get(script).put(option, value);
	}

	/**
	 * Loads script specific options
	 * @param script The script
	 * @param sectionNode The initial node to load options from
	 */
	@ApiStatus.Internal
	public void loadLocalOptions(Script script, SectionNode sectionNode) {
		options.computeIfAbsent(script, k -> new HashMap<>());
		Map<String, String> localOptions = options.get(script);
		loadOptions(sectionNode, "", localOptions);
	}

	/**
	 * Deletes all script specific options of a script
	 * @param script The script
	 */
	public void deleteLocalOptions(Script script) {
		options.remove(script);
	}

	/**
	 * Get local options of a script
	 * @param script The script
	 * @return A map of option names and their values
	 * @see #getGlobalOptions()
	 */
	public Map<String, String> getLocalOptions(Script script) {
		if (script == null) {
			return null;
		}
		return options.get(script);
	}

	@Override
	public Collection<Map<Script, Map<String, String>>> elements() {
		return Collections.singleton(options);
	}

	/**
	 * Replaces all options in a string with their values, prioritizes local options
	 * @param script The script to get the options from
	 * @param string The string to replace the options of
	 * @return A string with all options replaced by their values
	 */
	public String replaceOptions(Script script, String string) {
		return StringUtils.replaceAll(string, "\\{@(.+?)\\}", m -> {
			String optionName = m.group(1);
			String option = getOption(script, optionName);
			if (option == null) {
				Skript.error("Undefined option '" + optionName + "'. Options must be declared before they are used.");
				return m.group();
			}
			return Matcher.quoteReplacement(option);
		});
	}

	private static void loadOptions(SectionNode sectionNode, String prefix, Map<String, String> options) {
		for (Node node : sectionNode) {
			if (node instanceof EntryNode entryNode) {
				options.put(prefix + node.getKey(), entryNode.getValue());
			} else if (node instanceof SectionNode section) {
				loadOptions(section, prefix + node.getKey() + ".", options);
			} else {
				Skript.error("Invalid line " + node.getKey() + " in options");
			}
		}
	}

}
