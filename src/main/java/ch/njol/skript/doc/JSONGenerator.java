package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.registrations.EventValues.EventValueInfo;
import ch.njol.skript.util.Version;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Generates JSON docs
 */
public class JSONGenerator extends DocumentationGenerator {

	/**
	 * The current version of the JSON generator
	 */
	public static final Version JSON_VERSION = new Version(2, 0);

	private static final Gson GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.serializeNulls()
		.create();

	/**
	 * Creates a {@link JSONGenerator} for the specified addon.
	 *
	 * @param addon The addon.
	 * @return The created {@link JSONGenerator}.
	 */
	public static JSONGenerator of(@NotNull SkriptAddon addon) {
		return new JSONGenerator(addon);
	}

	private final @NotNull SkriptAddon addon;

	private JSONGenerator(@NotNull SkriptAddon addon) {
		super(new File(""), new File(""));

		Preconditions.checkNotNull(addon, "addon cannot be null");

		this.addon = addon;
	}

	/**
	 * @deprecated Use {@link #of(SkriptAddon)} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public JSONGenerator(File templateDir, File outputDir) {
		super(templateDir, outputDir);
		addon = Skript.getAddonInstance();
	}

	/**
	 * @return The version of the JSON generator
	 */
	private static JsonObject getVersion() {
		JsonObject version = new JsonObject();
		version.addProperty("major", JSON_VERSION.getMajor());
		version.addProperty("minor", JSON_VERSION.getMinor());
		return version;
	}

	/**
	 * Coverts a String array to a JsonArray
	 *
	 * @param strings the String array to convert
	 * @return the JsonArray containing the Strings
	 */
	private static JsonArray convertToJsonArray(String @Nullable ... strings) {
		if (strings == null || strings.length == 0)
			return null;
		JsonArray jsonArray = new JsonArray();
		for (String string : strings)
			jsonArray.add(new JsonPrimitive(string));
		return jsonArray;
	}

	/**
	 * Generates the documentation JsonObject for an element that is annotated with documentation
	 * annotations (e.g. effects, conditions, etc.)
	 *
	 * @param syntaxInfo the syntax info element to generate the documentation object of
	 * @return the JsonObject representing the documentation of the provided syntax element
	 */
	private static JsonObject generatedAnnotatedElement(SyntaxInfo<?> syntaxInfo) {
		Class<?> syntaxClass = syntaxInfo.type();
		Name name = syntaxClass.getAnnotation(Name.class);
		if (name == null || syntaxClass.getAnnotation(NoDoc.class) != null)
			return null;

		JsonObject syntaxJsonObject = new JsonObject();

		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(syntaxInfo));
		syntaxJsonObject.addProperty("name", name.value());
		Since since = syntaxClass.getAnnotation(Since.class);
		syntaxJsonObject.add("since", since == null ? null : convertToJsonArray(since.value()));

		Deprecated deprecated = syntaxClass.getAnnotation(Deprecated.class);
		syntaxJsonObject.addProperty("deprecated", deprecated != null);

		Description description = syntaxClass.getAnnotation(Description.class);
		syntaxJsonObject.add("description", description == null ? null : convertToJsonArray(description.value()));

		syntaxJsonObject.add("patterns", cleanPatterns(syntaxInfo.patterns().toArray(new String[0])));

		if (syntaxClass.isAnnotationPresent(Examples.class)) {
			@NotNull Examples examplesAnnotation = syntaxClass.getAnnotation(Examples.class);
			syntaxJsonObject.add("examples", convertToJsonArray(examplesAnnotation.value()));
		} else if (syntaxClass.isAnnotationPresent(Example.Examples.class)) {
			// If there are multiple examples, they get containerised
			@NotNull Example.Examples examplesAnnotation = syntaxClass.getAnnotation(Example.Examples.class);
			syntaxJsonObject.add("examples", convertToJsonArray(Arrays.stream(examplesAnnotation.value())
				.map(Example::value).toArray(String[]::new)));
		} else if (syntaxClass.isAnnotationPresent(Example.class)) {
			// If the user adds just one example, it isn't containerised
			@NotNull Example example = syntaxClass.getAnnotation(Example.class);
			syntaxJsonObject.add("examples", convertToJsonArray(example.value()));
		} else {
			syntaxJsonObject.add("examples", null);
		}

		syntaxJsonObject.add("events", getAnnotatedEvents(syntaxClass.getAnnotation(Events.class)));

		RequiredPlugins requirements = syntaxClass.getAnnotation(RequiredPlugins.class);
		syntaxJsonObject.add("requirements", requirements == null ? null : convertToJsonArray(requirements.value()));

		Keywords keywords = syntaxClass.getAnnotation(Keywords.class);
		syntaxJsonObject.add("keywords", keywords == null ? null : convertToJsonArray(keywords.value()));

		if (Expression.class.isAssignableFrom(syntaxInfo.type())) {
			syntaxJsonObject.add("returns", getExpressionReturnTypes((Expression<?>) syntaxInfo.instance()));
		}

		return syntaxJsonObject;
	}

	/**
	 * Returns the formatted events based on the {@link Events} annotation.
	 *
	 * @param events The events annotation.
	 * @return A json array with the formatted events value, or null if there is no annotation.
	 */
	private static @Nullable JsonArray getAnnotatedEvents(Events events) {
		if (events == null || events.value() == null) {
			return null;
		}

		JsonArray array = new JsonArray();

		for (String event : events.value()) {
			JsonObject object = new JsonObject();

			object.addProperty("id", event);
			object.addProperty("name", event);

			array.add(object);
		}

		return array;
	}

	/**
	 * Attempts to get an {@link Expression}'s return types by creating a new instance
	 * and calling {@link Expression#possibleReturnTypes()}.
	 * If the type cannot be determined statically, will return {@code null}.
	 *
	 * @param expression The expression class.
	 * @return The return types, or null if they are not static.
	 */
	private static @Nullable JsonArray getExpressionReturnTypes(Expression<?> expression) {
		try {
			JsonArray types = new JsonArray();

			for (Class<?> type : expression.possibleReturnTypes()) {
				ClassInfo<?> exact = Classes.getExactClassInfo(type);

				if (exact == null) {
					continue;
				}

				JsonObject object = new JsonObject();
				object.addProperty("id", exact.getCodeName());
				object.addProperty("name", exact.getName().getSingular());
				types.add(object);
			}

			return types;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Generates the documentation JsonObject for an event
	 *
	 * @param info the event to generate the documentation object for
	 * @return a documentation JsonObject for the event
	 */
	private static JsonObject generateEventElement(BukkitSyntaxInfos.Event<?> info) {
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(info));
		syntaxJsonObject.addProperty("name", info.name());
		syntaxJsonObject.addProperty("cancellable", isCancellable(info));

		syntaxJsonObject.add("since", convertToJsonArray(info.since().toArray(new String[0])));
		syntaxJsonObject.add("patterns", cleanPatterns(info.patterns().toArray(new String[0])));
		syntaxJsonObject.add("description", convertToJsonArray(info.description().toArray(new String[0])));
		syntaxJsonObject.add("requirements", convertToJsonArray(info.requiredPlugins().toArray(new String[0])));
		syntaxJsonObject.add("examples", convertToJsonArray(info.examples().toArray(new String[0])));
		syntaxJsonObject.add("eventValues", getEventValues(info));
		syntaxJsonObject.add("keywords", convertToJsonArray(info.keywords().toArray(new String[0])));

		return syntaxJsonObject;
	}

	/**
	 * Generates the documentation for the event values of an event
	 *
	 * @param info the event to generate the event values of
	 * @return a JsonArray containing the documentation JsonObjects for each event value
	 */
	private static JsonArray getEventValues(BukkitSyntaxInfos.Event<?> info) {
		Set<JsonObject> eventValues = new HashSet<>();

		Multimap<Class<? extends Event>, EventValueInfo<?, ?>> allEventValues = EventValues.getPerEventEventValues();
		for (Class<? extends Event> supportedEvent : info.events()) {
			for (Class<? extends Event> event : allEventValues.keySet()) {
				if (!event.isAssignableFrom(supportedEvent)) {
					continue;
				}

				Collection<EventValueInfo<?, ?>> eventValueInfos = allEventValues.get(event);

				for (EventValueInfo<?, ?> eventValueInfo : eventValueInfos) {
					Class<?>[] excludes = eventValueInfo.excludes();
					if (excludes != null && Set.of(excludes).contains(event)) {
						continue;
					}

					Class<?> valueClass = eventValueInfo.valueClass();
					ClassInfo<?> classInfo;
					if (valueClass.isArray()) {
						classInfo = Classes.getExactClassInfo(valueClass.componentType());
					} else {
						classInfo = Classes.getExactClassInfo(valueClass);
					}

					if (classInfo == null) {
						continue;
					}

					String name = classInfo.getName().getSingular();
					if (valueClass.isArray()) {
						name = classInfo.getName().getPlural();
					}
					if (name.isBlank()) {
						continue;
					}

					if (eventValueInfo.time() == EventValues.TIME_PAST) {
						name = "past " + name;
					} else if (eventValueInfo.time() == EventValues.TIME_FUTURE) {
						name = "future " + name;
					}

					JsonObject object = new JsonObject();
					object.addProperty("id", DocumentationIdProvider.getId(classInfo));
					object.addProperty("name", name.toLowerCase(Locale.ENGLISH));
					eventValues.add(object);
				}
			}
		}

		if (eventValues.isEmpty()) {
			return null;
		}

		JsonArray array = new JsonArray();
		for (JsonObject eventValue : eventValues) {
			array.add(eventValue);
		}
		return array;
	}

	/**
	 * Determines whether an event is cancellable.
	 *
	 * @param info the event to check
	 * @return true if the event is cancellable, false otherwise
	 */
	private static boolean isCancellable(BukkitSyntaxInfos.Event<?> info) {
		boolean cancellable = false;
		for (Class<? extends Event> event : info.events()) {
			if (Cancellable.class.isAssignableFrom(event) || BlockCanBuildEvent.class.isAssignableFrom(event)) {
				cancellable = true;
				break;
			}
		}
		return cancellable;
	}


	/**
	 * Generates a JsonArray containing the documentation JsonObjects for each structure in the iterator
	 *
	 * @param infos the structures to generate documentation for
	 * @return a JsonArray containing the documentation JsonObjects for each structure
	 */
	private static <T extends SyntaxInfo<? extends Structure>> JsonArray generateStructureElementArray(Collection<T> infos) {
		JsonArray syntaxArray = new JsonArray();
		infos.forEach(info -> {
			if (info instanceof BukkitSyntaxInfos.Event<?> eventInfo) {
				syntaxArray.add(generateEventElement(eventInfo));
			} else {
				JsonObject structureElementJsonObject = generatedAnnotatedElement(info);
				if (structureElementJsonObject != null)
					syntaxArray.add(structureElementJsonObject);
			}
		});
		return syntaxArray;
	}

	/**
	 * Generates a JsonArray containing the documentation JsonObjects for each syntax element in the iterator
	 *
	 * @param infos the syntax elements to generate documentation for
	 * @return a JsonArray containing the documentation JsonObjects for each syntax element
	 */
	private static <T extends SyntaxInfo<? extends SyntaxElement>> JsonArray generateSyntaxElementArray(Collection<T> infos) {
		JsonArray syntaxArray = new JsonArray();
		infos.forEach(info -> {
			JsonObject syntaxJsonObject = generatedAnnotatedElement(info);
			if (syntaxJsonObject != null)
				syntaxArray.add(syntaxJsonObject);
		});
		return syntaxArray;
	}

	/**
	 * Generates the documentation JsonObject for a classinfo
	 *
	 * @param classInfo the ClassInfo to generate the documentation of
	 * @return the documentation Jsonobject of the ClassInfo
	 */
	private static JsonObject generateClassInfoElement(ClassInfo<?> classInfo) {
		if (!classInfo.hasDocs())
			return null;

		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(classInfo));
		syntaxJsonObject.addProperty("name", Objects.requireNonNullElse(classInfo.getDocName(), classInfo.getCodeName()));
		syntaxJsonObject.addProperty("since", classInfo.getSince());

		syntaxJsonObject.add("patterns", cleanPatterns(classInfo.getUsage()));
		syntaxJsonObject.add("description", convertToJsonArray(classInfo.getDescription()));
		syntaxJsonObject.add("requirements", convertToJsonArray(classInfo.getRequiredPlugins()));
		syntaxJsonObject.add("examples", convertToJsonArray(classInfo.getExamples()));

		return syntaxJsonObject;
	}

	/**
	 * Generates a JsonArray containing the documentation JsonObjects for each classinfo in the iterator
	 *
	 * @param classInfos the classinfos to generate documentation for
	 * @return a JsonArray containing the documentation JsonObjects for each classinfo
	 */
	private static JsonArray generateClassInfoArray(Iterator<ClassInfo<?>> classInfos) {
		JsonArray syntaxArray = new JsonArray();
		classInfos.forEachRemaining(classInfo -> {
			JsonObject classInfoElement = generateClassInfoElement(classInfo);
			if (classInfoElement != null)
				syntaxArray.add(classInfoElement);
		});
		return syntaxArray;
	}

	/**
	 * Generates the documentation JsonObject for a JavaFunction
	 *
	 * @param function the JavaFunction to generate the JsonObject of
	 * @return the JsonObject of the JavaFunction
	 */
	private static JsonObject generateFunctionElement(JavaFunction<?> function) {
		JsonObject functionJsonObject = new JsonObject();
		functionJsonObject.addProperty("id", DocumentationIdProvider.getId(function));
		functionJsonObject.addProperty("name", function.getName());
		functionJsonObject.addProperty("since", function.getSince());
		functionJsonObject.add("returns", getFunctionReturnType(function));

		functionJsonObject.add("description", convertToJsonArray(function.getDescription()));
		functionJsonObject.add("examples", convertToJsonArray(function.getExamples()));

		String functionSignature = function.getSignature().toString(false, false);
		functionJsonObject.add("patterns", convertToJsonArray(functionSignature));
		return functionJsonObject;
	}

	/**
	 * Gets the return type of JavaFunction, with the name and id
	 *
	 * @param function the JavaFunction to get the return type of
	 * @return the JsonObject representing the return type of the JavaFunction
	 */
	private static JsonObject getFunctionReturnType(JavaFunction<?> function) {
		JsonObject object = new JsonObject();

		ClassInfo<?> returnType = function.getReturnType();
		if (returnType == null) {
			return null;
		}

		object.addProperty("id", DocumentationIdProvider.getId(returnType));
		object.addProperty("name", Objects.requireNonNullElse(returnType.getDocName(), returnType.getCodeName()));
		return object;
	}

	/**
	 * Generates a JsonArray containing the documentation JsonObjects for each function in the iterator
	 *
	 * @param functions the functions to generate documentation for
	 * @return a JsonArray containing the documentation JsonObjects for each function
	 */
	private static JsonArray generateFunctionArray(Iterator<JavaFunction<?>> functions) {
		JsonArray syntaxArray = new JsonArray();
		functions.forEachRemaining(function -> syntaxArray.add(generateFunctionElement(function)));
		return syntaxArray;
	}

	/**
	 * Cleans the provided patterns
	 *
	 * @param strings the patterns to clean
	 * @return the cleaned patterns
	 */
	private static JsonArray cleanPatterns(String... strings) {
		if (strings == null || strings.length == 0 || (strings.length == 1 && strings[0].isBlank()))
			return null;

		for (int i = 0; i < strings.length; i++) {
			strings[i] = Documentation.cleanPatterns(strings[i], false, false);
		}
		return convertToJsonArray(strings);
	}

	/**
	 * Generates the json documentation for this addon at the specified path.
	 *
	 * @param output The output path.
	 */
	public void generate(Path output) throws IOException {
		JsonObject jsonDocs = new JsonObject();

		jsonDocs.add("version", getVersion());
		jsonDocs.add("addon", getAddon());
		jsonDocs.add("conditions", generateSyntaxElementArray(addon.syntaxRegistry().syntaxes(SyntaxRegistry.CONDITION)));
		jsonDocs.add("effects", generateSyntaxElementArray(addon.syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT)));
		jsonDocs.add("expressions", generateSyntaxElementArray(addon.syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)));
		jsonDocs.add("events", generateStructureElementArray(addon.syntaxRegistry().syntaxes(BukkitRegistryKeys.EVENT)));
		jsonDocs.add("types", generateClassInfoArray(Classes.getClassInfos().iterator()));

		Stream<DefaultSyntaxInfos.Structure<?>> structuresExcludingEvents = addon.syntaxRegistry()
			.syntaxes(SyntaxRegistry.STRUCTURE)
			.stream()
			.filter(structureInfo -> !(structureInfo instanceof BukkitSyntaxInfos.Event<?>));

		jsonDocs.add("structures", generateStructureElementArray(structuresExcludingEvents.toList()));
		jsonDocs.add("sections", generateSyntaxElementArray(addon.syntaxRegistry().syntaxes(SyntaxRegistry.SECTION)));

		jsonDocs.add("functions", generateFunctionArray(Functions.getJavaFunctions().iterator()));

		Files.writeString(output, GSON.toJson(jsonDocs));
	}

	/**
	 * Gets the json object representing the addon.
	 *
	 * @return The json object representing the addon.
	 */
	private JsonObject getAddon() {
		JsonObject object = new JsonObject();

		object.addProperty("name", this.addon.name());
		object.addProperty("version", this.addon.version.toString());

		return object;
	}

	/**
	 * @deprecated Use {@link #generate(Path)} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	@Override
	public void generate() {
		try {
			generate(outputDir.toPath());
		} catch (IOException ex) {
			Skript.exception(ex, "An error occurred while trying to generate JSON documentation");
		}
	}

}
