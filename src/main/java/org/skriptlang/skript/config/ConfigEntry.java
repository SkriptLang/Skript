package org.skriptlang.skript.config;

import ch.njol.skript.lang.util.common.AnyValued;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

/**
 * Represents a line with a key and value in a {@link Config},
 * along with the comments that belong to it.
 *
 * @param <T> The type of the entry's value.
 */
public record ConfigEntry<T>(
	String key, T value, String inlineComment,
	String[] comments
) implements ConfigNode, AnyValued<T> {

	public ConfigEntry(@NotNull String key, T value,
					   @NotNull String inlineComment, @NotNull String @NotNull [] comments) {
		this.key = key;
		this.value = value;
		this.inlineComment = inlineComment;
		this.comments = comments;
	}

	@Override
	public @NotNull String key() {
		return key;
	}


	@Override
	public Class<T> valueType() {
		return (Class<T>) value.getClass();
	}

	@Override
	public @NotNull String inlineComment() {
		return inlineComment;
	}

	@Override
	public @NotNull String @NotNull [] comments() {
		return comments;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner("\n");

		if (!inlineComment.isBlank()) {
			joiner.add("%s: %s # %s".formatted(key, value, inlineComment));
		} else {
			joiner.add("%s: %s".formatted(key, value));
		}

		for (String comment : comments) {
			if (comment.isBlank()) {
				joiner.add("");
			} else {
				joiner.add("# %s".formatted(comment));
			}
		}

		return joiner.toString();
	}
}
