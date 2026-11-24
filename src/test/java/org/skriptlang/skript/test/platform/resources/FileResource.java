package org.skriptlang.skript.test.platform.resources;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.test.platform.Resource;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Resource representing a file within the data root.
 *
 * @param source The path pointing to this file.
 */
public record FileResource(String source, String target) implements Resource {

	@Override
	public void fetch(Path dataRoot, Path envRoot) throws IOException {
		Path source = dataRoot.resolve(source());
		Path target = envRoot.resolve(target());

		if (Files.isDirectory(source)) {
			Files.walkFileTree(source, new SimpleFileVisitor<>() {
				@Override
				public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs)
					throws IOException {
					Path relative = source.relativize(dir);
					Path dest = target.resolve(relative);
					Files.createDirectories(dest);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs)
					throws IOException {
					Path relative = source.relativize(file);
					Path dest = target.resolve(relative);
					Files.createDirectories(dest.getParent());
					Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

}
