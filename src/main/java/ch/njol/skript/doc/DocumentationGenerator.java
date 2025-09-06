package ch.njol.skript.doc;

import java.io.File;
import java.nio.file.Path;

/**
 * @deprecated Use {@link JSONGenerator} instead.
 */
@Deprecated(forRemoval = true, since = "INSERT VERSION")
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
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public abstract void generate();

}
