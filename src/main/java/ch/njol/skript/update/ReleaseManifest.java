package ch.njol.skript.update;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import org.jetbrains.annotations.Nullable;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Describes a Skript release.
 */
public class ReleaseManifest {
	
	/**
	 * Loads a release manifest from JSON.
	 * @param json Release manifest.
	 * @return A release manifest.
	 * @throws JsonParseException If the given JSON was not valid.
	 */
	@SuppressWarnings("null")
	public static ReleaseManifest load(String json) throws JsonParseException {
		return new GsonBuilder().registerTypeAdapter(Class.class, new ClassSerializer())
				.create().fromJson(json, ReleaseManifest.class);
	}
	
	/**
	 * Release id, for example "2.3".
	 */
	public final String id;
	
	/**
	 * When the release was published.
	 */
	public final String date;
	
	/**
	 * Flavor of the release. For example "github" or "custom".
	 */
	public final String flavor;
	
	/**
	 * Type of update checker to use for this release.
	 */
	public final Class<? extends UpdateChecker> updateCheckerType;
	
	/**
	 * Source where updates for this release can be found,
	 * if there are updates.
	 */
	public final String updateSource;
	
	/**
	 * Source for downloads. If null, update checker will figure out it.
	 */
	@Nullable
	public final String downloadSource;
	
	public ReleaseManifest(String id, String date, String flavor, Class<? extends UpdateChecker> updateCheckerType,
			String updateSource, @Nullable String downloadSource) {
		this.id = id;
		this.date = date;
		this.flavor = flavor;
		this.updateCheckerType = updateCheckerType;
		this.updateSource = updateSource;
		this.downloadSource = downloadSource;
	}
	
	/**
	 * Serializes class to JSON and back.
	 */
	static class ClassSerializer implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

		@Override
		public @Nullable Class<?> deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
				@Nullable JsonDeserializationContext context)
				throws JsonParseException {
			try {
				assert json != null;
				return Class.forName(json.getAsJsonPrimitive().getAsString());
			} catch (ClassNotFoundException e) {
				throw new JsonParseException("class not found");
			}
		}

		@Override
		public JsonElement serialize(@Nullable Class<?> src, @Nullable Type typeOfSrc,
				@Nullable JsonSerializationContext context) {
			assert src != null;
			return new JsonPrimitive(src.getName());
		}
		
	}

	/**
	 * Creates an instance of the updater used by this type.
	 * @return New updater instance.
	 */
	@SuppressWarnings("null")
	public UpdateChecker createUpdateChecker() {
		try {
			return updateCheckerType.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("updater class cannot be created", e);
		}
	}
}
