package org.skriptlang.skript.docs;

import ch.njol.skript.util.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Documentation generator for a JSON output.
 */
class JSONGenerator implements DocumentationGenerator {

	private static final Version JSON_VERSION = new Version(3, 0);

	private static final Gson GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.serializeNulls()
		.create();

	private final SkriptAddon addon;
	private final DocumentationAdapter adapter;

	JSONGenerator(SkriptAddon addon, DocumentationAdapter adapter) {
		this.addon = addon;
		this.adapter = adapter;
	}

	@Override
	public void generate(Path path) {
		JsonObject docs = new JsonObject();

		// Version
		JsonObject version = new JsonObject();
		version.addProperty("major", JSON_VERSION.getMajor());
		version.addProperty("minor", JSON_VERSION.getMinor());
		docs.add("version", version);

		// Source
		JsonObject source = new JsonObject();
		source.addProperty("name", addon.name());
		// TODO way to determine version
		source.addProperty("version", "unknown");
		docs.add("source", source);

		// Add in adapter properties
		// We do it this way so that the properties added above appear first
		GSON.toJsonTree(adapter.dataMap()).getAsJsonObject().asMap()
			.forEach(docs::add);

		// write to disk
		try {
			Files.writeString(path, GSON.toJson(docs));
		} catch (IOException ex) {
			// TODO better exception
			throw ch.njol.skript.Skript.exception(ex, "An error occurred while trying to generate JSON documentation");
		}
	}

}
