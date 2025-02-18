package ch.njol.skript.doc;

import java.io.File;

/**
 * Represents a class which generates a documentation format (like HTML or JSON)
 */
public abstract class DocumentationGenerator {

	protected File templateDir;
	protected File outputDir;

	public DocumentationGenerator(File templateDir, File outputDir) {
		this.templateDir = templateDir;
		this.outputDir = outputDir;
	}

	/**
	 * Generates the documentation file
	 */
	public abstract void generate();

}
