package ch.njol.skript.doc;

import java.io.File;
import java.nio.file.Path;

/**
 * @deprecated Use {@link JSONGenerator} instead.
 */
@Deprecated(forRemoval = true, since = "2.13")
public abstract class DocumentationGenerator {

	protected File templateDir;
	protected File outputDir;

	public DocumentationGenerator(File templateDir, File outputDir) {
		this.templateDir = templateDir;
		this.outputDir = outputDir;
	}

	/**
	 * Use {@link JSONGenerator#generate(Path)} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.13")
	public abstract void generate();

}
