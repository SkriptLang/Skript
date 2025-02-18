package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.registrations.Classes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.StructureInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
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
	private static @Nullable JsonArray convertToJsonArray(String @Nullable [] strings) {
		if (strings == null)
			return null;
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
	private @Nullable JsonObject generatedAnnotatedElement(SyntaxElementInfo<?> syntaxInfo) {
		Class<?> syntaxClass = syntaxInfo.getElementClass();
		Name nameAnnotation = syntaxClass.getAnnotation(Name.class);
		if (nameAnnotation == null || syntaxClass.getAnnotation(NoDoc.class) != null)
			return null;
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(syntaxInfo));
		syntaxJsonObject.addProperty("name", nameAnnotation.value());

		Since sinceAnnotation = syntaxClass.getAnnotation(Since.class);
		syntaxJsonObject.add("since", sinceAnnotation == null ? null : convertToJsonArray(sinceAnnotation.value()));

		Description descriptionAnnotation = syntaxClass.getAnnotation(Description.class);
		if (descriptionAnnotation != null) {
			syntaxJsonObject.add("description", convertToJsonArray(descriptionAnnotation.value()));
		} else {
			syntaxJsonObject.add("description", new JsonArray());
		}

		Examples examplesAnnotation = syntaxClass.getAnnotation(Examples.class);
		if (examplesAnnotation != null) {
			syntaxJsonObject.add("examples", convertToJsonArray(examplesAnnotation.value()));
		} else {
			syntaxJsonObject.add("examples", new JsonArray());
		}


		syntaxJsonObject.add("patterns", convertToJsonArray(syntaxInfo.getPatterns()));
		return syntaxJsonObject;
	}

	/**
	 * Generates the documentation JsonObject for an event
	 * @param eventInfo the event to generate the documentation object for
	 * @return a documentation JsonObject for the event
	 */
	private JsonObject generateEventElement(SkriptEventInfo<?> eventInfo) {
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(eventInfo));
		syntaxJsonObject.addProperty("name", eventInfo.name);
		syntaxJsonObject.addProperty("since", eventInfo.getSince());
		syntaxJsonObject.add("description", convertToJsonArray(eventInfo.getDescription()));
		syntaxJsonObject.add("examples", convertToJsonArray(eventInfo.getExamples()));
		syntaxJsonObject.add("patterns", convertToJsonArray(eventInfo.patterns));
		return syntaxJsonObject;
	}


	/**
	 * Generates a JsonArray containing the documentation JsonObjects for each structure in the iterator
	 * @param infos the structures to generate documentation for
	 * @return a JsonArray containing the documentation JsonObjects for each structure
	 */
	private <T extends StructureInfo<? extends Structure>> JsonArray generateStructureElementArray(Iterator<T> infos) {
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
	private <T extends SyntaxElementInfo<? extends SyntaxElement>> JsonArray generateSyntaxElementArray(Iterator<T> infos) {
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
	private @Nullable JsonObject generateClassInfoElement(ClassInfo<?> classInfo) {
		if (!classInfo.hasDocs())
			return null;
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(classInfo));
		syntaxJsonObject.addProperty("name", getClassInfoName(classInfo));
		syntaxJsonObject.addProperty("since", classInfo.getSince());
		syntaxJsonObject.add("description", convertToJsonArray(classInfo.getDescription()));
		syntaxJsonObject.add("examples", convertToJsonArray(classInfo.getExamples()));
		syntaxJsonObject.add("patterns", convertToJsonArray(classInfo.getUsage()));
		return syntaxJsonObject;
	}

	/**
	 * Generates a JsonArray containing the documentation JsonObjects for each classinfo in the iterator
	 * @param classInfos the classinfos to generate documentation for
	 * @return a JsonArray containing the documentation JsonObjects for each classinfo
	 */
	private JsonArray generateClassInfoArray(Iterator<ClassInfo<?>> classInfos) {
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
	private String getClassInfoName(ClassInfo<?> classInfo) {
		return Objects.requireNonNullElse(classInfo.getDocName(), classInfo.getCodeName());
	}

	/**
	 * Generates the documentation JsonObject for a JavaFunction
	 * @param function the JavaFunction to generate the JsonObject of
	 * @return the JsonObject of the JavaFunction
	 */
	private JsonObject generateFunctionElement(JavaFunction<?> function) {
		JsonObject functionJsonObject = new JsonObject();
		functionJsonObject.addProperty("id", DocumentationIdProvider.getId(function));
		functionJsonObject.addProperty("name", function.getName());
		functionJsonObject.addProperty("since", function.getSince());
		functionJsonObject.add("description", convertToJsonArray(function.getDescription()));
		functionJsonObject.add("examples", convertToJsonArray(function.getExamples()));

		ClassInfo<?> returnType = function.getReturnType();
		if (returnType != null) {
			functionJsonObject.addProperty("return-type", getClassInfoName(returnType));
		}

		String functionSignature = function.getSignature().toString(false, false);
		functionJsonObject.add("patterns", convertToJsonArray(new String[] { functionSignature }));
		return functionJsonObject;
	}

	/**
	 * Generates a JsonArray containing the documentation JsonObjects for each function in the iterator
	 * @param functions the functions to generate documentation for
	 * @return a JsonArray containing the documentation JsonObjects for each function
	 */
	private JsonArray generateFunctionArray(Iterator<JavaFunction<?>> functions) {
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
