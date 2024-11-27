package org.skriptlang.skript.localization;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.util.ViewProvider;

/**
 * A Localizer is used for the localization of translatable strings.
 *
 * This API is highly experimental and will be subject to change due to pending localization reworks.
 * In its current state, it acts as a bridge between old and new API.
 *
 * @see ch.njol.skript.localization.Language
 */
@ApiStatus.Experimental
public interface Localizer extends ViewProvider<Localizer> {

	/**
	 * @param addon The addon this localizer is localizing for.
	 * @return A localizer with no default translations.
	 */
	@Contract("_ -> new")
	static Localizer of(SkriptAddon addon) {
		return new LocalizerImpl(addon);
	}

	/**
	 * Sets the language file directories for this localizer.
	 * This method will initiate a loading of any language files in the provided directories.
	 * @param languageFileDirectory The path to the directory on the jar containing language files.
	 * @param dataFileDirectory The path to the directory on the disk containing language files.
	 * For example, this may include language files that have been saved to enable user customization.
	 */
	void setSourceDirectories(String languageFileDirectory, @Nullable String dataFileDirectory);

	/**
	 * @return The path to the directory on the jar containing language files.
	 */
	@Nullable String languageFileDirectory();

	/**
	 * @return The path to the directory on the disk containing language files.
	 */
	@Nullable String dataFileDirectory();

	/**
	 * Used for obtaining the translation of a language key.
	 * @param key The key of the translation to obtain.
	 * @return The translation represented by the provided key, or null if no translation exists.
	 */
	@Nullable String translate(String key);

	/**
	 * Constructs an unmodifiable view of this localizer.
	 * That is, no new translations may be added.
	 * @return An unmodifiable view of this localizer.
	 */
	@Override
	@Contract("-> new")
	default Localizer unmodifiableView() {
		return new LocalizerImpl.UnmodifiableLocalizer(this);
	}

}
