package org.skriptlang.skript.docs;

/**
 * An exception to be used if something goes wrong during the documentation generation process.
 */
public class DocumentationException extends RuntimeException {

	public DocumentationException(String message) {
		super(message);
	}

	public DocumentationException(String message, Throwable cause) {
		super(message, cause);
	}

}
