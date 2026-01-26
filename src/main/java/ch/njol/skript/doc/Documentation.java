package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;

/**
 * Utilities for documentation generation.
 */
@ApiStatus.Internal
public final class Documentation {

	private static final File DOCS_OUTPUT_DIRECTORY = new File(Skript.getInstance().getDataFolder(), "docs");

	private static final boolean FORCE_HOOKS_SYSTEM_PROPERTY = "true".equals(System.getProperty("skript.forceregisterhooks"));

	/**
	 * Checks whether the system property 'skript.forceregisterhooks' property is {@code "true"}.
	 * If true, elements requiring missing dependencies will be forced to register in order to generate documentation.
	 * @return Whether it is safe to register elements dependent on missing dependencies.
	 */
	public static boolean canGenerateUnsafeDocs() {
		return FORCE_HOOKS_SYSTEM_PROPERTY;
	}

	/**
	 * @return The location to generate documentation files within.
	 */
	public static File getDocsOutputDirectory() {
		String environmentOutputDir = System.getenv("SKRIPT_DOCS_OUTPUT_DIR");
		return environmentOutputDir == null ? DOCS_OUTPUT_DIRECTORY : new File(environmentOutputDir);
	}

}
