package org.skriptlang.skript.test.platform.resources;

import org.skriptlang.skript.test.platform.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Resource representing something that must be downloaded.
 */
public class URLResource implements Resource {

	protected String url;
	protected final String target;

	public URLResource(String url, String target) {
		this.url = url;
		this.target = target;
	}

	/**
	 * @return String representing the URL where this resource can be downloaded.
	 */
	public String url() {
		return url;
	}

	@Override
	public String target() {
		return target;
	}

	@Override
	public void fetch(Path dataRoot, Path envRoot) throws IOException {
		String source = url();
		URL url = URI.create(source).toURL();
		Path target = envRoot.resolve(target());
		Files.createDirectories(target.getParent());
		try (InputStream is = url.openStream()) {
			Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

}
