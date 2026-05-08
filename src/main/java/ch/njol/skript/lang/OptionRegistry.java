package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Registry;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Stores script specific and global options
 */
public class OptionRegistry implements Registry<OptionRegistry.ScriptOptions> {

	/**
	 * Stores all options
	 * If 'script' is null, it's stored as a global option
	 * Otherwise, it is local to the script
	 */
	private final Map<Script, ScriptOptions> scriptOptions = new HashMap<>();

	public static class ScriptOptions {

		private final Map<String, String> options = new HashMap<>();
		private final @Nullable Script script;

		private ScriptOptions(@Nullable Script script) {
			this.script = script;
		}

		public @Nullable Script script() {
			return script;
		}

		/**
		 * Gets an option
		 * @param option The option's name, must not be null.
		 * @return The option's value
		 */
		public @Nullable String get(String option) {
			if (option == null) {
				throw new SkriptAPIException("option cannot be null");
			}

			return options.get(option);
		}

		/**
		 * Sets an option
		 * @param option The option's name, must not be null
		 * @param value The option's value, must not be null
		 */
		public void set(String option, String value) {
			if (option == null) {
				throw new SkriptAPIException("option cannot be null");
			}

			if (value == null) {
				throw new SkriptAPIException("value cannot be null");
			}

			options.put(option, value);
		}

		/**
		 * Deletes an option.
		 * @param option The option's name, must not be null.
		 * @return Whether the option was deleted
		 */
		public boolean delete(String option) {
			if (option == null) {
				throw new SkriptAPIException("option cannot be null");
			}

			return options.remove(option) != null;
		}

		/**
		 * Checks if an option with this name exists.
		 * @param option The option's name, must not be null.
		 * @return Whether the option exists.
		 */
		public boolean exists(String option) {
			if (option == null) {
				throw new SkriptAPIException("option cannot be null");
			}

			return options.containsKey(option);
		}

		/**
		 * Deletes all options
		 */
		public void clear() {
			options.clear();
		}

		@ApiStatus.Internal
		@Deprecated(forRemoval = true) // this method will go private when OptionsData is completely removed
		public Map<String, String> optionsMap() {
			return options;
		}

	}

	/**
	 * Gets a script specific option. If the script specific option doesn't exist, defaults to global option
	 * @param script The script
	 * @param option The option's name
	 * @return The script specific option, or global option, or null
	 */
	public String getOption(@NotNull Script script, String option) {
		ScriptOptions scriptOptions = this.scriptOptions.get(script);
		if (scriptOptions != null && scriptOptions.exists(option)) {
			return scriptOptions.get(option);
		}

		ScriptOptions globalOptions = this.scriptOptions.get(null);
		return globalOptions.get(option);
	}

	/**
	 * Gets all global options
	 */
	public ScriptOptions getGlobalOptions() {
		return scriptOptions.get(null);
	}


	/**
	 * Returns all local options of a script
	 * @param script The script, must not be null
	 * @return The script's options, or null if this script doesn't have local options.
	 */
	public @Nullable ScriptOptions getLocalOptions(Script script) {
		if (script == null) {
			throw new SkriptAPIException("script cannot be null");
		}

		return scriptOptions.get(script);
	}

	/**
	 * Loads global options
	 * @param sectionNode The initial node to load options from
	 */
	@ApiStatus.Internal
	public void loadGlobalOptions(SectionNode sectionNode) {
		scriptOptions.computeIfAbsent(null, ScriptOptions::new);
		loadLocalOptions(null, sectionNode);
	}

	/**
	 * Loads script specific options
	 * @param script The script
	 * @param sectionNode The initial node to load options from
	 */
	@ApiStatus.Internal
	public void loadLocalOptions(Script script, SectionNode sectionNode) {
		scriptOptions.computeIfAbsent(script, ScriptOptions::new);
		ScriptOptions scriptOptions = this.scriptOptions.get(script);
		loadOptions(sectionNode, "", scriptOptions.optionsMap());
	}

	@Override
	public Collection<ScriptOptions> elements() {
		return scriptOptions.values();
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
