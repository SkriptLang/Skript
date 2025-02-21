package org.skriptlang.skript.config;

import ch.njol.skript.lang.util.common.AnyNamed;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface Config extends AnyNamed {

	static Config load(@NotNull Path path) throws IOException {
		return new ConfigImpl(path);
	}

	<T> T getValue(@NotNull String path);

	<T> void setValue(@NotNull String path, T value);

	ConfigNode getNode(@NotNull String path);

	ConfigNode[] getNodeChildren(@NotNull String path);

	void save() throws IOException;

	@NotNull Path path();

}
