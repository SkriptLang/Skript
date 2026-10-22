package org.skriptlang.skript.test.platform.resources;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;

/**
 * A helper resource for downloading Paper jars.
 */
public class PaperJarResource extends URLResource {

	private static final Gson GSON = new Gson();

	private final String version;

	public PaperJarResource(String version, String target) {
		super(null, target);
		this.version = version;
	}

	@Override
	public String url() {
		if (url != null) {
			return url;
		}

		String downloadURL;
		try (HttpClient client = HttpClient.newHttpClient()) {
			HttpRequest buildRequest = HttpRequest.newBuilder()
				.uri(URI.create("https://fill.papermc.io/v3/projects/paper/versions/" + version + "/builds/latest"))
				.header("User-Agent", "SkriptLang/Skript/{@version} (admin@skriptlang.org)")
				.GET()
				.build();

			HttpResponse<InputStream> buildResponse = client.send(buildRequest, BodyHandlers.ofInputStream());
			JsonObject buildObject;
			try (InputStreamReader reader = new InputStreamReader(buildResponse.body(), StandardCharsets.UTF_8)) {
				buildObject = GSON.fromJson(reader, JsonObject.class);
			}

			downloadURL = buildObject.getAsJsonObject("downloads")
				.getAsJsonObject("server:default")
				.get("url").getAsString();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Failed to fetch Paper version: " + version, e);
		}
		assert downloadURL != null && !downloadURL.isEmpty();
		url = downloadURL;

		return url;
	}
}
