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
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Generates JSON docs
 */
public class JSONGenerator extends DocumentationGenerator {

	public JSONGenerator(File templateDir, File outputDir) {
		super(templateDir, outputDir);
	}

	/**
	 * Coverts a String array to a JsonArray
	 * @param strings the String array to convert
	 * @return the JsonArray containing the Strings
	 */
	private static @NotNull JsonArray convertToJsonArray(String @Nullable [] strings) {
		if (strings == null)
			return new JsonArray();
		JsonArray jsonArray = new JsonArray();
		for (String string : strings)
			jsonArray.add(new JsonPrimitive(string));
		return jsonArray;
	}

	/**
	 * Generates the documentation JsonObject for an element that is annotated with documentation
	 * annotations (e.g. effects, conditions, etc.)
	 * @param syntaxInfo the syntax info element to generate the documentation object of
	 * @return the JsonObject representing the documentation of the provided syntax element
	 */
	private static @Nullable JsonObject generatedAnnotatedElement(SyntaxElementInfo<?> syntaxInfo) {
		Class<?> syntaxClass = syntaxInfo.getElementClass();
		Name nameAnnotation = syntaxClass.getAnnotation(Name.class);
		if (nameAnnotation == null || syntaxClass.getAnnotation(NoDoc.class) != null)
			return null;
		JsonObject syntaxJsonObject = new JsonObject();

		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(syntaxInfo));
		syntaxJsonObject.addProperty("name", nameAnnotation.value());
		syntaxJsonObject.add("patterns", convertToJsonArray(syntaxInfo.getPatterns()));

		Since sinceAnnotation = syntaxClass.getAnnotation(Since.class);
		syntaxJsonObject.add("since", convertToJsonArray(sinceAnnotation.value()));

		Description descriptionAnnotation = syntaxClass.getAnnotation(Description.class);
		syntaxJsonObject.add("description", convertToJsonArray(descriptionAnnotation.value()));

		Examples examplesAnnotation = syntaxClass.getAnnotation(Examples.class);
		syntaxJsonObject.add("examples", convertToJsonArray(examplesAnnotation.value()));

		Events events = syntaxClass.getAnnotation(Events.class);
		syntaxJsonObject.add("events", convertToJsonArray(events.value()));

		RequiredPlugins requirements = syntaxClass.getAnnotation(RequiredPlugins.class);
		syntaxJsonObject.add("requirements", convertToJsonArray(requirements.value()));

		return syntaxJsonObject;
	}

	/**
	 * Generates the documentation JsonObject for an event
	 * @param info the event to generate the documentation object for
	 * @return a documentation JsonObject for the event
	 */
	private static JsonObject generateEventElement(SkriptEventInfo<?> info) {
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(info));
		syntaxJsonObject.addProperty("name", info.getName());
		syntaxJsonObject.addProperty("since", info.getSince());
		syntaxJsonObject.addProperty("cancellable", isCancellable(info));

		syntaxJsonObject.add("patterns", convertToJsonArray(info.getPatterns()));
		syntaxJsonObject.add("description", convertToJsonArray(info.getDescription()));
		syntaxJsonObject.add("requirements", convertToJsonArray(info.getRequiredPlugins()));
		syntaxJsonObject.add("examples", convertToJsonArray(info.getExamples()));
		syntaxJsonObject.add("eventValues", getEventValues(info));

		return syntaxJsonObject;
	}

	/**
	 * Generates the documentation for the event values of an event
	 * @param info the event to generate the event values of
	 * @return a JsonArray containing the documentation JsonObjects for each event value
	 */
	private static JsonArray getEventValues(SkriptEventInfo<?> info) {
		JsonArray eventValues = new JsonArray();

		Multimap<Class<? extends Event>, EventValueInfo<?, ?>> allEventValues = EventValues.getPerEventEventValues();
		for (Class<? extends Event> supportedEvent : info.events) {
			for (Class<? extends Event> event : allEventValues.keySet()) {
				if (!event.isAssignableFrom(supportedEvent))
					continue;

				Collection<EventValueInfo<?, ?>> eventValueInfos = allEventValues.get(event);

				for (EventValueInfo<?, ?> eventValueInfo : eventValueInfos) {
					Class<?>[] excludes = eventValueInfo.excludes();
					if (excludes != null && Set.of(excludes).contains(event))
						continue;

					ClassInfo<?> exactClassInfo = Classes.getExactClassInfo(eventValueInfo.c());
					if (exactClassInfo == null)
						continue;

					JsonObject object = new JsonObject();
					object.addProperty("name", exactClassInfo.getName().toString());
					object.addProperty("id", DocumentationIdProvider.getId(exactClassInfo));
					eventValues.add(object);
				}
			}
		}
		return eventValues;
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
	 * @param classInfo the ClassInfo to generate the documentation of
	 * @return the documentation Jsonobject of the ClassInfo
	 */
	private static @Nullable JsonObject generateClassInfoElement(ClassInfo<?> classInfo) {
		if (!classInfo.hasDocs())
			return null;
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(classInfo));
		syntaxJsonObject.addProperty("name", getClassInfoName(classInfo));
		syntaxJsonObject.addProperty("since", classInfo.getSince());

		syntaxJsonObject.add("patterns", convertToJsonArray(classInfo.getUsage()));
		syntaxJsonObject.add("description", convertToJsonArray(classInfo.getDescription()));
		syntaxJsonObject.add("requirements", convertToJsonArray(classInfo.getRequiredPlugins()));
		syntaxJsonObject.add("examples", convertToJsonArray(classInfo.getExamples()));

		return syntaxJsonObject;
	}

	/**
	 * Generates a JsonArray containing the documentation JsonObjects for each classinfo in the iterator
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
	 * @param classInfo the ClassInfo to get the effective name of
	 * @return the effective name of the ClassInfo
	 */
	private static String getClassInfoName(ClassInfo<?> classInfo) {
		return Objects.requireNonNullElse(classInfo.getDocName(), classInfo.getCodeName());
	}

	/**
	 * Generates the documentation JsonObject for a JavaFunction
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
		functionJsonObject.add("patterns", convertToJsonArray(new String[] { functionSignature }));
		return functionJsonObject;
	}

	private static JsonObject getReturnType(JavaFunction<?> function) {
		JsonObject returnType = new JsonObject();
		returnType.addProperty("name", getClassInfoName(function.getReturnType()));
		returnType.addProperty("id", DocumentationIdProvider.getId(function.getReturnType()));
		return returnType;
	}

	/**
	 * Generates a JsonArray containing the documentation JsonObjects for each function in the iterator
	 * @param functions the functions to generate documentation for
	 * @return a JsonArray containing the documentation JsonObjects for each function
	 */
	private static JsonArray generateFunctionArray(Iterator<JavaFunction<?>> functions) {
		JsonArray syntaxArray = new JsonArray();
		functions.forEachRemaining(function -> syntaxArray.add(generateFunctionElement(function)));
		return syntaxArray;
	}

	/**
	 * Writes the documentation JsonObject to an output path
	 * @param outputPath the path to write the documentation to
	 * @param jsonDocs the documentation JsonObject
	 */
	private void saveDocs(Path outputPath, JsonObject jsonDocs) {
		try {
			Gson jsonGenerator = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			Files.writeString(outputPath, jsonGenerator.toJson(jsonDocs));
		} catch (IOException exception) {
			//noinspection ThrowableNotThrown
			Skript.exception(exception, "An error occurred while trying to generate JSON documentation");
		}
	}

	@Override
	public void generate() {
		JsonObject jsonDocs = new JsonObject();

		jsonDocs.add("skriptVersion", new JsonPrimitive(Skript.getVersion().toString()));
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
