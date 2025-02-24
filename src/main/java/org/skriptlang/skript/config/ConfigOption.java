package org.skriptlang.skript.config;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a configuration option.
 *
 * @param <T> The type of the option.
 */
public class ConfigOption<T> {

	private final String key;
	private final T defaultValue;

	ConfigOption(
		@NotNull String key,
		@NotNull T defaultValue
	) {
		this.key = key;
		this.defaultValue = defaultValue;

		SkriptConfig.eventRegistry().register(SkriptConfig.ReloadEvent.class, this::onLoad);
	}

	/**
	 * Called when the full configuration has loaded.
	 * <p>
	 * Usually used to apply changes to other variables.
	 * </p>
	 */
	void onLoad() {

	}

	/**
	 * An optional parser to parse the value from a string.
	 * <p>
	 * Usually used when the type of the option is not a primitive.
	 * </p>
	 *
	 * @param input The string input
	 * @return The parsed value, or null if parsing failed.
	 */
	T parse(String input) {
		return null;
	}

	/**
	 * Gets the key of the option.
	 *
	 * @return The key of the option.
	 */
	public @NotNull String key() {
		return key;
	}

	/**
	 * Gets the default value of the option.
	 *
	 * @return The default value of the option.
	 */
	public @NotNull T defaultValue() {
		return defaultValue;
	}

	/**
	 * Gets the value of the option.
	 *
	 * @return The value of the option.
	 */
	public T value() {
		T parsed = parse(String.valueOf(ch.njol.skript.SkriptConfig.getConfig().getValue(key)));
		if (parsed != null) {
			return parsed;
		}
		return ch.njol.skript.SkriptConfig.getConfig().getValue(key);
	}

}
