package org.skriptlang.skript.config;

import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

/**
 * Represents a line which starts a section in a {@link Config},
 * along with the comments that belong to it.
 */
public record ConfigSection(
	String key, String inlineComment, String[] comments
) implements ConfigNode {

	public ConfigSection(@NotNull String key, @NotNull String inlineComment,
						 @NotNull String @NotNull [] comments) {
		this.key = key;
		this.inlineComment = inlineComment;
		this.comments = comments;
	}

	@Override
	public @NotNull String key() {
		return key;
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

		for (String comment : comments) {
			if (comment.isBlank()) {
				joiner.add("");
			} else {
				joiner.add("# %s".formatted(comment));
			}
		}

		if (!inlineComment.isBlank()) {
			joiner.add("%s: # %s".formatted(key, inlineComment));
		} else {
			joiner.add("%s:".formatted(key));
		}

		return joiner.toString();
	}

}
