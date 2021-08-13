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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.registrations.Classes;

/**
 * Template engine, primarily used for generating Skript documentation
 * pages by combining data from annotations and templates.
 * 
 */
public class HTMLGenerator {
	
	private File template;
	private File output;
	
	private String skeleton;
	
	public HTMLGenerator(File templateDir, File outputDir) {
		this.template = templateDir;
		this.output = outputDir;
		
		this.skeleton = readFile(new File(template + "/template.html")); // Skeleton which contains every other page
	}
	
	@SuppressWarnings("null")
	private static <T> Iterator<T> sortedIterator(Iterator<T> it, Comparator<? super T> comparator) {
		List<T> list = new ArrayList<>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		
		Collections.sort(list, comparator);
		return list.iterator();
	}
	
	/**
	 * Sorts annotated documentation entries alphabetically.
	 */
	private static class AnnotatedComparator implements Comparator<SyntaxElementInfo<?>> {

		public AnnotatedComparator() {}

		@Override
		public int compare(@Nullable SyntaxElementInfo<?> o1, @Nullable SyntaxElementInfo<?> o2) {
			// Nullness check
			if (o1 == null || o2 == null) {
				assert false;
				throw new NullPointerException();
			}


			if (o1.c.getAnnotation(NoDoc.class) != null) {
				if (o2.c.getAnnotation(NoDoc.class) != null)
					return 0;
				return 1;
			} else if (o2.c.getAnnotation(NoDoc.class) != null)
				return -1;
			
			Name name1 = o1.c.getAnnotation(Name.class);
			Name name2 = o2.c.getAnnotation(Name.class);
			if (name1 == null)
				throw new SkriptAPIException("Name annotation expected: " + o1.c);
			if (name2 == null)
				throw new SkriptAPIException("Name annotation expected: " + o2.c);
			
			return name1.value().compareTo(name2.value());
		}
	}
	
	private static final AnnotatedComparator annotatedComparator = new AnnotatedComparator();
	
	/**
	 * Sorts events alphabetically.
	 */
	private static class EventComparator implements Comparator<SkriptEventInfo<?>> {

		public EventComparator() {}

		@Override
		public int compare(@Nullable SkriptEventInfo<?> o1, @Nullable SkriptEventInfo<?> o2) {
			// Nullness check
			if (o1 == null || o2 == null) {
				assert false;
				throw new NullPointerException();
			}
			
			if (o1.c.getAnnotation(NoDoc.class) != null)
				return 1;
			else if (o2.c.getAnnotation(NoDoc.class) != null)
				return -1;
			
			return o1.name.compareTo(o2.name);
		}
		
	}
	
	private static final EventComparator eventComparator = new EventComparator();
	
	/**
	 * Sorts class infos alphabetically.
	 */
	private static class ClassInfoComparator implements Comparator<ClassInfo<?>> {

		public ClassInfoComparator() {}

		@Override
		public int compare(@Nullable ClassInfo<?> o1, @Nullable ClassInfo<?> o2) {
			// Nullness check
			if (o1 == null || o2 == null) {
				assert false;
				throw new NullPointerException();
			}
			
			String name1 = o1.getDocName();
			if (name1 == null)
				name1 = o1.getCodeName();
			String name2 = o2.getDocName();
			if (name2 == null)
				name2 = o2.getCodeName();
			
			return name1.compareTo(name2);
		}
		
	}
	
	private static final ClassInfoComparator classInfoComparator = new ClassInfoComparator();
	
	/**
	 * Sorts functions by their names, alphabetically.
	 */
	private static class FunctionComparator implements Comparator<JavaFunction<?>> {

		public FunctionComparator() {}

		@Override
		public int compare(@Nullable JavaFunction<?> o1, @Nullable JavaFunction<?> o2) {
			// Nullness check
			if (o1 == null || o2 == null) {
				assert false;
				throw new NullPointerException();
			}
			
			return o1.getName().compareTo(o2.getName());
		}
		
	}
	
	private static final FunctionComparator functionComparator = new FunctionComparator();

	/**
	 * Generates documentation using template and output directories
	 * given in the constructor.
	 */
	public void generate() {
		for (File f : template.listFiles()) {
			if (f.getName().matches("css|js|assets")) { // Copy CSS/JS/Assets folders
				String slashName = "/" + f.getName();
				File fileTo = new File(output + slashName);
				fileTo.mkdirs();
				for (File filesInside : new File(template + slashName).listFiles()) {
					if (filesInside.isDirectory()) 
						continue;
						
					if (!filesInside.getName().toLowerCase().endsWith(".png")) { // Copy images
						writeFile(new File(fileTo + "/" + filesInside.getName()), readFile(filesInside));
					}
					
					else if (!filesInside.getName().matches("(?i)(.*)\\.(html?|js|css|json)")) {
						try {
							Files.copy(filesInside, new File(fileTo + "/" + filesInside.getName()));
						} catch (IOException e) {
							e.printStackTrace();
						}
							
					}
				}
				continue;
			} else if (f.isDirectory()) // Ignore other directories
				continue;
			if (f.getName().endsWith("template.html") || f.getName().endsWith(".md"))
				continue; // Ignore skeleton and README

			Skript.info("Creating documentation for " + f.getName());

			String content = readFile(f);
			String page;
			if (f.getName().endsWith(".html"))
				page = skeleton.replace("${content}", content); // Content to inside skeleton
			else // Not HTML, so don't even try to use template.html
				page = content;

			page = page.replace("${skript.version}", Skript.getVersion().toString()); // Skript version
			page = page.replace("${skript.build.date}", new SimpleDateFormat("dd/MM/yyyy").format(new Date())); // Build date
			page = page.replace("${pagename}", f.getName().replace(".html", ""));

			List<String> replace = Lists.newArrayList();
			int include = page.indexOf("${include"); // Single file includes
			while (include != -1) {
				int endIncl = page.indexOf("}", include);
				String name = page.substring(include + 10, endIncl);
				replace.add(name);

				include = page.indexOf("${include", endIncl);
			}

			for (String name : replace) {
				String temp = readFile(new File(template + "/templates/" + name));
				temp = temp.replace("${skript.version}", Skript.getVersion().toString());
				page = page.replace("${include " + name + "}", temp);
			}

			int generate = page.indexOf("${generate"); // Generate expressions etc.
			while (generate != -1) {
				int nextBracket = page.indexOf("}", generate);
				String[] genParams = page.substring(generate + 11, nextBracket).split(" ");
				StringBuilder generated = new StringBuilder();

				String descTemp = readFile(new File(template + "/templates/" + genParams[1]));
				String genType = genParams[0];
				if (genType.equals("expressions")) {
					Iterator<ExpressionInfo<?,?>> it = sortedIterator(Skript.getExpressions(), annotatedComparator);
					while (it.hasNext()) {
						ExpressionInfo<?,?> info = it.next();
						assert info != null;
						if (info.c.getAnnotation(NoDoc.class) != null)
							continue;
						String desc = generateAnnotated(descTemp, info, generated.toString());
						generated.append(desc);
					}
				} else if (genType.equals("effects")) {
					List<SyntaxElementInfo<? extends Effect>> effects = new ArrayList<>(Skript.getEffects());
					Collections.sort(effects, annotatedComparator);
					for (SyntaxElementInfo<? extends Effect> info : effects) {
						assert info != null;
						if (info.c.getAnnotation(NoDoc.class) != null)
							continue;
						generated.append(generateAnnotated(descTemp, info, generated.toString()));
					}
				} else if (genType.equals("conditions")) {
					List<SyntaxElementInfo<? extends Condition>> conditions = new ArrayList<>(Skript.getConditions());
					Collections.sort(conditions, annotatedComparator);
					for (SyntaxElementInfo<? extends Condition> info : conditions) {
						assert info != null;
						if (info.c.getAnnotation(NoDoc.class) != null)
							continue;
						generated.append(generateAnnotated(descTemp, info, generated.toString()));
					}
				} else if (genType.equals("events")) {
					List<SkriptEventInfo<?>> events = new ArrayList<>(Skript.getEvents());
					Collections.sort(events, eventComparator);
					for (SkriptEventInfo<?> info : events) {
						assert info != null;
						if (info.c.getAnnotation(NoDoc.class) != null)
							continue;
						generated.append(generateEvent(descTemp, info, generated.toString()));
					}
				} else if (genType.equals("classes")) {
					List<ClassInfo<?>> classes = new ArrayList<>(Classes.getClassInfos());
					Collections.sort(classes, classInfoComparator);
					for (ClassInfo<?> info : classes) {
						if (ClassInfo.NO_DOC.equals(info.getDocName()))
							continue;
						assert info != null;
						generated.append(generateClass(descTemp, info, generated.toString()));
					}
				} else if (genType.equals("functions")) {
					List<JavaFunction<?>> functions = new ArrayList<>(Functions.getJavaFunctions());
					Collections.sort(functions, functionComparator);
					for (JavaFunction<?> info : functions) {
						assert info != null;
						generated.append(generateFunction(descTemp, info));
					}
				}
				
				page = page.replace(page.substring(generate, nextBracket + 1), generated.toString());
				
				generate = page.indexOf("${generate", nextBracket);
			}
			
			String name = f.getName();
			if (name.endsWith(".html")) { // Fix some stuff specially for HTML
				page = page.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"); // Tab to 4 non-collapsible spaces
				assert page != null;
				page = minifyHtml(page);
			}
			assert page != null;
			writeFile(new File(output + File.separator + name), page);
		}
	}
	
	private static String minifyHtml(String page) {
		StringBuilder sb = new StringBuilder(page.length());
		boolean space = false;
		for (int i = 0; i < page.length();) {
			int c = page.codePointAt(i);
			if ((c == '\n' || c == ' ')) {
				if (!space) {
					sb.append(' ');
					space = true;
				}
			} else {
				space = false;
				sb.appendCodePoint(c);
			}
			
			i += Character.charCount(c);
		}
		return replaceBR(sb.toString());
	}

	private static String replaceBR(String page) { // Replaces specifically `<br/>` with `\n` - This is useful in code blocks where you can't use newlines due to the minifyHtml method (Execute after minifyHtml)
		return page.replaceAll("<br/>", "\n");
	}
	
	private static String handleIf(String desc, String start, boolean value) {
		int ifStart = desc.indexOf(start);
		while (ifStart != -1) {
			int ifEnd = desc.indexOf("${end}", ifStart);
			String data = desc.substring(ifStart + start.length() + 1, ifEnd);
			
			String before = desc.substring(0, ifStart);
			String after = desc.substring(ifEnd + 6);
			if (value)
				desc = before + data + after;
			else
				desc = before + after;
			
			ifStart = desc.indexOf(start, ifEnd);
		}
		
		return desc;
	}
	
	/**
	 * Generates documentation entry for a type which is documented using
	 * annotations. This means expressions, effects and conditions.
	 * @param descTemp Template for description.
	 * @param info Syntax element info.
	 * @param page The page's code to check for ID duplications, can be left empty.
	 * @return Generated HTML entry.
	 */
	private String generateAnnotated(String descTemp, SyntaxElementInfo<?> info, @Nullable String page) {
		Class<?> c = info.c;
		String desc = "";

		Name name = c.getAnnotation(Name.class);
		desc = descTemp.replace("${element.name}", getNullOrEmptyDefault(name.value(), "Unknown Name"));

		Since since = c.getAnnotation(Since.class);
		desc = desc.replace("${element.since}", getNullOrEmptyDefault(since.value(), "Unknown"));

		Description description = c.getAnnotation(Description.class);
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(getNullOrEmptyDefault(description.value(), "Unknown description.")).replace("\n\n", "<p>"));
		desc = desc.replace("${element.desc-safe}", Joiner.on("\n").join(getNullOrEmptyDefault(description.value(), "Unknown description."))
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		Examples examples = c.getAnnotation(Examples.class);
		desc = desc.replace("${element.examples}", Joiner.on("<br>").join(getNullOrEmptyDefault(examples.value(), "Missing examples.")));
		desc = desc.replace("${element.examples-safe}", Joiner.on("\\n").join(getNullOrEmptyDefault(examples.value(), "Missing examples."))
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		DocumentationId DocID = c.getAnnotation(DocumentationId.class);
		String ID = DocID != null ? DocID.value() : info.c.getSimpleName();
		// Fix duplicated IDs
		if (page != null) {
			if (page.contains("#" + ID + "\"")) {
				ID = ID + "-" + (StringUtils.countMatches(page, "#" + ID + "\"") + 1);
			}
		}
		desc = desc.replace("${element.id}", ID);

		Events events = c.getAnnotation(Events.class);
		assert desc != null;
		desc = handleIf(desc, "${if events}", events != null);
		if (events != null) {
			String[] eventNames = events.value();
			String[] eventLinks = new String[eventNames.length];
			for (int i = 0; i < eventNames.length; i++) {
				String eventName = eventNames[i];
				eventLinks[i] = "<a href=\"events.html#" + eventName.replace(" ", "_") + "\">" + eventName + "</a>";
			}
			desc = desc.replace("${element.events}", Joiner.on(", ").join(eventLinks));
		}
		desc = desc.replace("${element.events-safe}", events == null ? "" : Joiner.on(", ").join(events.value()));
		
		RequiredPlugins plugins = c.getAnnotation(RequiredPlugins.class);
		assert desc != null;
		desc = handleIf(desc, "${if required-plugins}", plugins != null);
		desc = desc.replace("${element.required-plugins}", plugins == null ? "" : Joiner.on(", ").join(plugins.value()));
		
		List<String> toGen = Lists.newArrayList();
		int generate = desc.indexOf("${generate");
		while (generate != -1) {
			//Skript.info("Found generate!");
			int nextBracket = desc.indexOf("}", generate);
			String data = desc.substring(generate + 11, nextBracket);
			toGen.add(data);
			//Skript.info("Added " + data);
			
			generate = desc.indexOf("${generate", nextBracket);
		}
		
		// Assume element.pattern generate
		for (String data : toGen) {
			String[] split = data.split(" ");
			String pattern = readFile(new File(template + "/templates/" + split[1]));
			//Skript.info("Pattern is " + pattern);
			StringBuilder patterns = new StringBuilder();
			for (String line : getNullOrEmptyDefault(info.patterns, "Missing patterns.")) {
				assert line != null;
				line = cleanPatterns(line);
				String parsed = pattern.replace("${element.pattern}", line);
				//Skript.info("parsed is " + parsed);
				patterns.append(parsed);
			}
			
			String toReplace = "${generate element.patterns " + split[1] + "}";
			//Skript.info("toReplace " + toReplace);
			desc = desc.replace(toReplace, patterns.toString());
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.toString().replace("\\", "\\\\"));
		}

		assert desc != null;
		return desc;
	}
	
	private String generateEvent(String descTemp, SkriptEventInfo<?> info, @Nullable String page) {
		String desc = "";
		
		String docName = getNullOrEmptyDefault(info.getName(), "Unknown Name");
		desc = descTemp.replace("${element.name}", docName);
		
		String since = getNullOrEmptyDefault(info.getSince(), "Unknown");
		desc = desc.replace("${element.since}", since);
		
		String[] description = getNullOrEmptyDefault(info.getDescription(), "Missing description.");
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(description).replace("\n\n", "<p>"));
		desc = desc
				.replace("${element.desc-safe}", Joiner.on("\\n").join(description)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		
		String[] examples = getNullOrEmptyDefault(info.getExamples(), "Missing examples.");
		desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(examples));
		desc = desc
				.replace("${element.examples-safe}", Joiner.on("\\n").join(examples)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		String ID = info.getDocumentationID() != null ? info.getDocumentationID() : info.getId();
		// Fix duplicated IDs
		if (page != null) {
			if (page.contains("#" + ID + "\"")) {
				ID = ID + "-" + (StringUtils.countMatches(page, "#" + ID + "\"") + 1);
			}
		}
		desc = desc.replace("${element.id}", ID);
		
		assert desc != null;
		desc = handleIf(desc, "${if events}", false);
		desc = handleIf(desc, "${if required-plugins}", false);
		
		List<String> toGen = Lists.newArrayList();
		int generate = desc.indexOf("${generate");
		while (generate != -1) {
			int nextBracket = desc.indexOf("}", generate);
			String data = desc.substring(generate + 11, nextBracket);
			toGen.add(data);
			
			generate = desc.indexOf("${generate", nextBracket);
		}
		
		// Assume element.pattern generate
		for (String data : toGen) {
			String[] split = data.split(" ");
			String pattern = readFile(new File(template + "/templates/" + split[1]));
			StringBuilder patterns = new StringBuilder();
			for (String line : getNullOrEmptyDefault(info.patterns, "Missing patterns.")) {
				assert line != null;
				line = cleanPatterns(info.getName().startsWith("On ") ? "[on] " + line : line);
				String parsed = pattern.replace("${element.pattern}", line);
				patterns.append(parsed);
			}
			
			desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns.toString());
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.toString().replace("\\", "\\\\"));
		}

		assert desc != null;
		return desc;
	}
	
	private String generateClass(String descTemp, ClassInfo<?> info, @Nullable String page) {
		String desc = "";
		
		String docName = getNullOrEmptyDefault(info.getDocName(), "Unknown Name");
		desc = descTemp.replace("${element.name}", docName);
		
		String since = getNullOrEmptyDefault(info.getSince(), "Unknown");
		desc = desc.replace("${element.since}", since);
		
		String[] description = getNullOrEmptyDefault(info.getDescription(), "Missing description.");
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(description).replace("\n\n", "<p>"));
		desc = desc
				.replace("${element.desc-safe}", Joiner.on("\\n").join(description)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		
		String[] examples = getNullOrEmptyDefault(info.getExamples(), "Missing examples.");
		desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(examples));
		desc = desc.replace("${element.examples-safe}", Joiner.on("\\n").join(examples)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		String ID = info.getDocumentationID() != null ? info.getDocumentationID() : info.getCodeName();
		// Fix duplicated IDs
		if (page != null) {
			if (page.contains("#" + ID + "\"")) {
				ID = ID + "-" + (StringUtils.countMatches(page, "#" + ID + "\"") + 1);
			}
		}
		desc = desc.replace("${element.id}", ID);

		assert desc != null;
		desc = handleIf(desc, "${if events}", false);
		desc = handleIf(desc, "${if required-plugins}", false);
		
		List<String> toGen = Lists.newArrayList();
		int generate = desc.indexOf("${generate");
		while (generate != -1) {
			int nextBracket = desc.indexOf("}", generate);
			String data = desc.substring(generate + 11, nextBracket);
			toGen.add(data);
			
			generate = desc.indexOf("${generate", nextBracket);
		}
		
		// Assume element.pattern generate
		for (String data : toGen) {
			String[] split = data.split(" ");
			String pattern = readFile(new File(template + "/templates/" + split[1]));
			StringBuilder patterns = new StringBuilder();
			String[] lines = getNullOrEmptyDefault(info.getUsage(), "Missing patterns.");
			if (lines == null)
				continue;
			for (String line : lines) {
				assert line != null;
				line = cleanPatterns(line, false);
				String parsed = pattern.replace("${element.pattern}", line);
				patterns.append(parsed);
			}
			
			desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns.toString());
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.toString().replace("\\", "\\\\"));
		}
		
		assert desc != null;
		return desc;
	}
	
	private String generateFunction(String descTemp, JavaFunction<?> info) {
		String desc = "";
		
		String docName = getNullOrEmptyDefault(info.getName(), "Unknown Name");
		desc = descTemp.replace("${element.name}", docName);
		
		String since = getNullOrEmptyDefault(info.getSince(), "Unknown");
		desc = desc.replace("${element.since}", since);
		
		String[] description = getNullOrEmptyDefault(info.getDescription(), "Missing description.");
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(description));
		desc = desc
				.replace("${element.desc-safe}", Joiner.on("\\n").join(description)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		
		String[] examples = getNullOrEmptyDefault(info.getExamples(), "Missing examples.");
		desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(examples));
		desc = desc
				.replace("${element.examples-safe}", Joiner.on("\\n").join(examples)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		desc = desc.replace("${element.id}", info.getName());
		
		assert desc != null;
		desc = handleIf(desc, "${if events}", false);
		desc = handleIf(desc, "${if required-plugins}", false);
		
		List<String> toGen = Lists.newArrayList();
		int generate = desc.indexOf("${generate");
		while (generate != -1) {
			int nextBracket = desc.indexOf("}", generate);
			String data = desc.substring(generate + 11, nextBracket);
			toGen.add(data);
			
			generate = desc.indexOf("${generate", nextBracket);
		}
		
		// Assume element.pattern generate
		for (String data : toGen) {
			String[] split = data.split(" ");
			String pattern = readFile(new File(template + "/templates/" + split[1]));
			String patterns = "";
			Parameter<?>[] params = info.getParameters();
			String[] types = new String[params.length];
			for (int i = 0; i < types.length; i++) {
				types[i] = params[i].toString();
			}
			String line = docName + "(" + Joiner.on(", ").join(types) + ")"; // Better not have nulls
			patterns += pattern.replace("${element.pattern}", line);
			
			desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns);
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.replace("\\", "\\\\"));
		}
		
		assert desc != null;
		return desc;
	}
	
	@SuppressWarnings("null")
	private static String readFile(File f) {
		try {
			return Files.toString(f, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private static void writeFile(File f, String data) {
		try {
			Files.write(data, f, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String cleanPatterns(final String patterns) {
		return Documentation.cleanPatterns(patterns);
	}

	private static String cleanPatterns(final String patterns, boolean escapeHTML) {
		if (escapeHTML)
			return Documentation.cleanPatterns(patterns);
		else
			return Documentation.cleanPatterns(patterns, false);
	}

	/**
	 * Checks if a string is empty or null then it will return the message provided
	 * 
	 * @param string the String to check
	 * @param message the String to return if either condition is true
	 */
	public String getNullOrEmptyDefault(@Nullable String string, String message) {
		return (string == null || string.isEmpty()) ? message : string; // Null check first otherwise NullPointerException is thrown
	}
	
	public String[] getNullOrEmptyDefault(@Nullable String[] string, String message) {
		return (string == null || string.length == 0 || string[0].equals("")) ? new String[]{ message } : string; // Null check first otherwise NullPointerException is thrown
	}
			
	
}
