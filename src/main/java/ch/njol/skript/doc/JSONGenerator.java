package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.registrations.EventValues.EventValueInfo;
import ch.njol.skript.util.Version;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.StructureInfo;

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
	public static final Version JSON_VERSION = new Version(1, 0);

	private static final Gson GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.serializeNulls()
		.create();

	public JSONGenerator(File templateDir, File outputDir) {
		super(templateDir, outputDir);
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
	private static JsonObject generatedAnnotatedElement(SyntaxElementInfo<?> syntaxInfo) {
		Class<?> syntaxClass = syntaxInfo.getElementClass();
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

		syntaxJsonObject.add("patterns", cleanPatterns(syntaxInfo.getPatterns()));

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

		Events events = syntaxClass.getAnnotation(Events.class);
		syntaxJsonObject.add("events", events == null ? null : convertToJsonArray(events.value()));

		RequiredPlugins requirements = syntaxClass.getAnnotation(RequiredPlugins.class);
		syntaxJsonObject.add("requirements", requirements == null ? null : convertToJsonArray(requirements.value()));

		Keywords keywords = syntaxClass.getAnnotation(Keywords.class);
		syntaxJsonObject.add("keywords", keywords == null ? null : convertToJsonArray(keywords.value()));

		return syntaxJsonObject;
	}

	/**
	 * Generates the documentation JsonObject for an event
	 *
	 * @param info the event to generate the documentation object for
	 * @return a documentation JsonObject for the event
	 */
	private static JsonObject generateEventElement(SkriptEventInfo<?> info) {
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(info));
		syntaxJsonObject.addProperty("name", info.getName());
		syntaxJsonObject.addProperty("since", info.getSince());
		syntaxJsonObject.addProperty("cancellable", isCancellable(info));

		syntaxJsonObject.add("patterns", cleanPatterns(info.getPatterns()));
		syntaxJsonObject.add("description", convertToJsonArray(info.getDescription()));
		syntaxJsonObject.add("requirements", convertToJsonArray(info.getRequiredPlugins()));
		syntaxJsonObject.add("examples", convertToJsonArray(info.getExamples()));
		syntaxJsonObject.add("eventValues", getEventValues(info));
		syntaxJsonObject.add("keywords", convertToJsonArray(info.getKeywords()));

		return syntaxJsonObject;
	}

	/**
	 * Generates the documentation for the event values of an event
	 *
	 * @param info the event to generate the event values of
	 * @return a JsonArray containing the documentation JsonObjects for each event value
	 */
	private static JsonArray getEventValues(SkriptEventInfo<?> info) {
		Set<JsonObject> eventValues = new HashSet<>();

		Multimap<Class<? extends Event>, EventValueInfo<?, ?>> allEventValues = EventValues.getPerEventEventValues();
		for (Class<? extends Event> supportedEvent : info.events) {
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

					ClassInfo<?> exactClassInfo = Classes.getExactClassInfo(eventValueInfo.c());
					if (exactClassInfo == null) {
						continue;
					}

					String name = getClassInfoName(exactClassInfo).toLowerCase(Locale.ENGLISH);
					if (name.isBlank()) {
						continue;
					}

					if (eventValueInfo.time() == EventValues.TIME_PAST) {
						name = "past " + name;
					} else if (eventValueInfo.time() == EventValues.TIME_FUTURE) {
						name = "future " + name;
					}

					JsonObject object = new JsonObject();
					object.addProperty("id", DocumentationIdProvider.getId(exactClassInfo));
					object.addProperty("name", name);
					eventValues.add(object);
				}
			}
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
	private static boolean isCancellable(SkriptEventInfo<?> info) {
		boolean cancellable = false;
		for (Class<? extends Event> event : info.events) {
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
	private static <T extends StructureInfo<? extends Structure>> JsonArray generateStructureElementArray(Iterator<T> infos) {
		JsonArray syntaxArray = new JsonArray();
		infos.forEachRemaining(info -> {
			if (info instanceof SkriptEventInfo<?> eventInfo) {
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
	private static <T extends SyntaxElementInfo<? extends SyntaxElement>> JsonArray generateSyntaxElementArray(Iterator<T> infos) {
		JsonArray syntaxArray = new JsonArray();
		infos.forEachRemaining(info -> {
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
		syntaxJsonObject.addProperty("name", getClassInfoName(classInfo));
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
	 * Gets either the explicitly declared documentation name or code name of a ClassInfo
	 *
	 * @param classInfo the ClassInfo to get the effective name of
	 * @return the effective name of the ClassInfo
	 */
	private static String getClassInfoName(ClassInfo<?> classInfo) {
		return Objects.requireNonNullElse(classInfo.getDocName(), classInfo.getCodeName());
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
		functionJsonObject.add("returnType", getReturnType(function));

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
	private static JsonObject getReturnType(JavaFunction<?> function) {
		JsonObject returnType = new JsonObject();
		returnType.addProperty("name", getClassInfoName(function.getReturnType()));
		returnType.addProperty("id", DocumentationIdProvider.getId(function.getReturnType()));
		return returnType;
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
	 * Writes the documentation JsonObject to an output path
	 *
	 * @param outputPath the path to write the documentation to
	 * @param jsonDocs   the documentation JsonObject
	 */
	private void saveDocs(Path outputPath, JsonObject jsonDocs) {
		try {
			Files.writeString(outputPath, GSON.toJson(jsonDocs));
		} catch (IOException exception) {
			//noinspection ThrowableNotThrown
			Skript.exception(exception, "An error occurred while trying to generate JSON documentation");
		}
	}

	@Override
	public void generate() {
		JsonObject jsonDocs = new JsonObject();

		jsonDocs.addProperty("skriptVersion", Skript.getVersion().toString());
		jsonDocs.add("version", getVersion());
		jsonDocs.add("conditions", generateSyntaxElementArray(Skript.getConditions().iterator()));
		jsonDocs.add("effects", generateSyntaxElementArray(Skript.getEffects().iterator()));
		jsonDocs.add("expressions", generateSyntaxElementArray(Skript.getExpressions()));
		jsonDocs.add("events", generateStructureElementArray(Skript.getEvents().iterator()));
		jsonDocs.add("classes", generateClassInfoArray(Classes.getClassInfos().iterator()));

		Stream<StructureInfo<? extends Structure>> structuresExcludingEvents = Skript.getStructures().stream()
			.filter(structureInfo -> !(structureInfo instanceof SkriptEventInfo));
		jsonDocs.add("structures", generateStructureElementArray(structuresExcludingEvents.iterator()));
		jsonDocs.add("sections", generateSyntaxElementArray(Skript.getSections().iterator()));

		jsonDocs.add("functions", generateFunctionArray(Functions.getJavaFunctions().iterator()));

		saveDocs(outputDir.toPath().resolve("docs.json"), jsonDocs);
	}

}
