/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.registrations.Classes;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.StructureInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class JSONGenerator extends DocumentationGenerator {

	public JSONGenerator(File templateDir, File outputDir, DocumentationIdProvider idProvider) {
		super(templateDir, outputDir, idProvider);
	}

	private static JsonArray convertToJsonArray(String @Nullable[] strings) {
		if (strings == null)
			return null;
		JsonArray jsonArray = new JsonArray();
		for (String string : strings)
			jsonArray.add(new JsonPrimitive(string));
		return jsonArray;
	}

	private static JsonArray convertArrayToJsonArray(String[] strings) {
		JsonArray jsonArray = new JsonArray();
		for (String string : strings) {
			jsonArray.add(string);
		}
		return jsonArray;
	}

	private @Nullable JsonObject generatedAnnotatedElement(Class<?> syntaxClass, String[] patterns) {
		Name nameAnnotation = syntaxClass.getAnnotation(Name.class);
		if (nameAnnotation == null || syntaxClass.getAnnotation(NoDoc.class) != null)
			return null;
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", idProvider.getId(syntaxClass));
		syntaxJsonObject.addProperty("name", nameAnnotation.value());

		Since sinceAnnotation = syntaxClass.getAnnotation(Since.class);
		syntaxJsonObject.addProperty("since", sinceAnnotation.value());

		Description descriptionAnnotation = syntaxClass.getAnnotation(Description.class);
		if (descriptionAnnotation != null)
			syntaxJsonObject.add("description", convertArrayToJsonArray(descriptionAnnotation.value()));
		else
			syntaxJsonObject.add("description", new JsonArray());

		Examples examplesAnnotation = syntaxClass.getAnnotation(Examples.class);
		if (examplesAnnotation != null)
			syntaxJsonObject.add("examples", convertToJsonArray(examplesAnnotation.value()));
		else
			syntaxJsonObject.add("examples", new JsonArray());


		syntaxJsonObject.add("patterns", convertToJsonArray(patterns));
		return syntaxJsonObject;
	}

	private JsonObject generateEventElement(SkriptEventInfo<?> eventInfo) {
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", idProvider.getId(eventInfo));
		syntaxJsonObject.addProperty("name", eventInfo.name);
		syntaxJsonObject.addProperty("since", eventInfo.getSince());
		syntaxJsonObject.add("description", convertArrayToJsonArray(eventInfo.getDescription()));
		syntaxJsonObject.add("examples", convertArrayToJsonArray(eventInfo.getExamples()));
		syntaxJsonObject.add("patterns", convertToJsonArray(eventInfo.patterns));
		return syntaxJsonObject;
	}


	private <T extends StructureInfo<? extends Structure>> JsonArray generateStructureElementArray(Iterator<T> infos) {
		JsonArray syntaxArray = new JsonArray();
		infos.forEachRemaining(info -> {
			if (info instanceof SkriptEventInfo) {
				syntaxArray.add(generateEventElement((SkriptEventInfo<?>) info));
			} else {
				JsonObject structureElementJsonObject = generatedAnnotatedElement(info.getElementClass(), info.patterns);
				if (structureElementJsonObject != null)
					syntaxArray.add(structureElementJsonObject);
			}
		});
		return syntaxArray;
	}

	private <T extends SyntaxElementInfo<? extends SyntaxElement>> JsonArray generateSyntaxElementArray(Iterator<T> infos) {
		JsonArray syntaxArray = new JsonArray();
		infos.forEachRemaining(info -> {
			JsonObject syntaxJsonObject = generatedAnnotatedElement(info.getElementClass(), info.patterns);
			if (syntaxJsonObject != null)
				syntaxArray.add(syntaxJsonObject);
		});
		return syntaxArray;
	}

	private @Nullable JsonObject generateClassInfoElement(ClassInfo<?> classInfo) {
		if (!classInfo.hasDocs())
			return null;
		JsonObject syntaxJsonObject = new JsonObject();
		syntaxJsonObject.addProperty("id", idProvider.getId(classInfo));
		syntaxJsonObject.addProperty("name", classInfo.getDocName());
		syntaxJsonObject.addProperty("since", classInfo.getSince());
		syntaxJsonObject.add("description", convertToJsonArray(classInfo.getDescription()));
		syntaxJsonObject.add("examples", convertToJsonArray(classInfo.getExamples()));
		syntaxJsonObject.add("patterns", convertToJsonArray(classInfo.getUsage()));
		return syntaxJsonObject;
	}

	private JsonArray generateClassInfoArray(Iterator<ClassInfo<?>> classInfos) {
		JsonArray syntaxArray = new JsonArray();
		classInfos.forEachRemaining(classInfo -> {
			JsonObject classInfoElement = generateClassInfoElement(classInfo);
			if (classInfoElement != null)
				syntaxArray.add(classInfoElement);
		});
		return syntaxArray;
	}

	private JsonObject generateFunctionElement(JavaFunction<?> function) {
		JsonObject functionJsonObject = new JsonObject();
		functionJsonObject.addProperty("id", idProvider.getId(function));
		functionJsonObject.addProperty("name", function.getName());
		functionJsonObject.addProperty("since", function.getSince());
		functionJsonObject.add("description", convertToJsonArray(function.getDescription()));
		functionJsonObject.add("examples", convertToJsonArray(function.getExamples()));

		ClassInfo<?> returnType = function.getReturnType();
		if (returnType != null) {
			functionJsonObject.addProperty("return-type", returnType.getDocName());
		}

		String functionSignature = function.getSignature().toString(true, false);
		functionJsonObject.add("patterns", convertToJsonArray(new String[] { functionSignature }));
		return functionJsonObject;
	}

	private JsonArray generateFunctionArray(Iterator<JavaFunction<?>> functions) {
		JsonArray syntaxArray = new JsonArray();
		functions.forEachRemaining(function -> syntaxArray.add(generateFunctionElement(function)));
		return syntaxArray;
	}

	private void saveDocs(Path outputPath, JsonObject jsonDocs) {
		try {
			Files.writeString(outputPath, jsonDocs.toString());
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
