package org.skriptlang.skript.config;

import org.jetbrains.annotations.NotNull;

public interface ConfigNode {

	@NotNull String key();

	void key(@NotNull String key);

	@NotNull String inlineComment();

	void inlineComment(@NotNull String inlineComment);

	@NotNull String @NotNull [] comments();

	void comments(@NotNull String @NotNull [] comments);

}
