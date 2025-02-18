package org.skriptlang.skript.bukkit.tags.sources;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.tags.TagModule;

import java.util.Collection;

/**
 * The origin of a tag, eg. from Bukkit, from Paper, from a custom Skript tag, or from anywhere.
 * Used for classification and filtering tags.
 */
public enum TagOrigin {
	/**
	 * Bukkit supplies both native minecraft tags and datapack tags.
	 */
	BUKKIT,

	/**
	 * Paper supplies a set of custom tags they curate.
	 */
	PAPER,

	/**
	 * Custom tags registered via Skript.
	 */
	SKRIPT,

	/**
	 * Used when asking for tags, matches all origins.
	 */
	ANY;

	/**
	 * Returns an optional choice of all the origins (minecraft, datapack, paper, and custom).
	 * Will not include paper on non-paper servers.
	 * Contains parse tags.
	 * @see #fromParseTags(Collection)
	 */
	@Contract(pure = true)
	public static @NotNull String getFullPattern() {
		if (TagModule.PAPER_TAGS_EXIST)
			return "[:minecraft|:datapack|:paper|:custom]";
		return "[:minecraft|:datapack|:custom]";
	}

	/**
	 * Determines the origin of tags based on the parse tags provided.
	 *
	 * @param tags the list of tags to parse for determining the origin.
	 * @return the determined {@code TagOrigin}.
	 *         Returns {@code TagOrigin.ANY} if no specific origin is found.
	 * @see #getFullPattern()
	 */
	@Contract(value = "_ -> new", pure = true)
	public static TagOrigin fromParseTags(@NotNull Collection<String> tags) {
		TagOrigin origin = TagOrigin.ANY;
		if (tags.contains("minecraft") || tags.contains("datapack")) {
			origin = TagOrigin.BUKKIT;
		} else if (tags.contains("paper")) {
			origin = TagOrigin.PAPER;
		} else if (tags.contains("custom")) {
			origin = TagOrigin.SKRIPT;
		}
		return origin;
	}

	/**
	 * Checks if the current TagOrigin matches another TagOrigin, considering ANY as a wildcard.
	 *
	 * @param other The other TagOrigin to be matched against.
	 * @return {@code true} if the TagOrigins match (i.e., they are the same, or either is {@link #ANY}).
	 */
	public boolean matches(TagOrigin other) {
		return this == other || this == ANY || other == ANY;
	}

	/**
	 * Returns a string for use in {@link ch.njol.skript.lang.Debuggable#toString(Event, boolean)} methods.
	 * @param datapackOnly Whether to output "datapack " or "minecraft " for {@link #BUKKIT}.
	 * @return a string representing the origin, with a trailing space.
	 */
	@Contract(pure = true)
	public @NotNull String toString(boolean datapackOnly) {
		return switch (this) {
			case BUKKIT -> datapackOnly ? "datapack" : "minecraft";
			case PAPER -> "paper";
			case SKRIPT -> "custom";
			case ANY -> "";
		};
	}

}
