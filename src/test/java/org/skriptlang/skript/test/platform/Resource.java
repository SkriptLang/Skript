package org.skriptlang.skript.test.platform;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.skriptlang.skript.test.platform.resources.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;

/**
 * Represents a resource required by the testing {@link Environment}.
 */
public interface Resource {

	/**
	 * GSON Deserializer for built-in {@link Resource} implementations.
	 */
	class ResourceDeserializer implements JsonDeserializer<Resource> {

		@Override
		public Resource deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			String type = jsonObject.get("type").getAsString();
			return switch (type) {
				case "file" -> context.deserialize(json, FileResource.class);
				case "url" -> context.deserialize(json, URLResource.class);
				case "paperJar" ->  context.deserialize(json, PaperJarResource.class);
				default -> throw new JsonParseException("Unknown resource type: " + type);
			};
		}

	}

	/**
	 * The path under platform root where it should be placed.
	 * Directories will be created as needed.
	 */
	String target();

	/**
	 * Fetches this resource and places it at the {@link #target()} path.
	 */
	void fetch(Path dataRoot, Path envRoot) throws IOException;

}
