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
public class ConfigEntry<T> implements ConfigNode, AnyValued<T> {

	private String key;
	private T value;
	private String inlineComment;
	private String[] comments;

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
	public void key(@NotNull String key) {
		this.key = key;
	}

	@Override
	public T value() {
		return value;
	}

	public void value(T value) {
		this.value = value;
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
	public void inlineComment(@NotNull String inlineComment) {
		this.inlineComment = inlineComment;
	}

	@Override
	public @NotNull String @NotNull [] comments() {
		return comments;
	}

	@Override
	public void comments(@NotNull String @NotNull [] comments) {
		this.comments = comments;
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
